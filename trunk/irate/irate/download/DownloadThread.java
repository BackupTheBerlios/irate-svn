// Copyright 2003 Anthony Jones, Taras Glek

package irate.download;

import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.UpdateListener;

import java.io.*;
import java.net.*;
import java.util.*;

public class DownloadThread extends Thread {

  private Vector updateListeners = new Vector();
  private TrackDatabase trackDatabase;
  private Track currentTrack;
  private File downloadDir;
  private String state;
  private int percentComplete;
  private boolean ready;
  private boolean continuous;

  public DownloadThread(TrackDatabase trackDatabase) {
    this.trackDatabase = trackDatabase;
    downloadDir = trackDatabase.getDownloadDirectory();
    if (!downloadDir.exists())
      downloadDir.mkdir();
  }

  public void run() {
    while (true) {
      doCheckAutoDownload();

      try {
        boolean wasReady;
        synchronized (this) {
          if (!ready)
            wait();
          wasReady = ready;
        }
        if (wasReady) {
          do {
            process();
          }
          while (continuous);
          ready = false;
        }
      }
      catch (IOException ioe) {
        if (continuous)
          handleError("continuousfailed", "continuousfailed.html");
        ioe.printStackTrace();
      }
      catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
  }

  public void checkAutoDownload()
  {
    synchronized (this) {
      notifyAll();
    }
  }

  public void go() {
    synchronized (this) {
      ready = true;
      notifyAll();
    }
  }

  public void setContinuous(boolean continuous) {
    this.continuous = continuous;
  }

  private boolean downloadSinglePending() throws IOException {
    Track[] tracks = trackDatabase.getTracks();
    for (int i = 0; i < tracks.length; i++) {
      currentTrack = tracks[i];
      if (!currentTrack.isHidden()) {
        File file = currentTrack.getFile();
        if (file == null || !file.exists()) {
          try {
            if (file != null) {
              currentTrack.unSetFile();
              trackDatabase.save();
            }
            download(currentTrack);

              // We've successfully downloaded a track.
            return true;
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return false;
  }

  public void process() throws IOException {
    if (!downloadSinglePending()) {
      contactServer(trackDatabase);
      downloadSinglePending();
    }
  }

  public void handleError(String code, String urlString) {
    System.out.println("Server error: " + code);
  }

  URLConnection openConnection(URL u) throws IOException{
  	final URL url = u;

  	return null;
  }

  private abstract class TimeoutWorker implements Runnable{
	protected Object input;
  	private Object output;
	private Thread timeoutThread;
	private Exception exception;

	public TimeoutWorker(Object input) {
	  this.input = input;
//    workerThread = new Thread(this);
	}

	protected void setOutput(Object output) {
		this.output = output;
	//	timeoutThread.interrupt();
	}

	protected void setException(Exception exception) {
		this.exception = exception;
	//	timeoutThread.interrupt();
	}

	public abstract void run();

	public Object runOrTimeout(long timeout) throws Exception {
		//running thread
		timeoutThread = Thread.currentThread();
		//increment..the bigger the less cpu we use...but slower downloads
		int step = 100;
		//reset outputs
		exception = null;
		output = null;
		//start thread with a task that might timeout
		Thread th = new Thread(this);
		th.start();
		while(timeout > 0){
			Thread.sleep(step);
			timeout -= step;
			if(exception != null)
				throw exception;
			else if(output != null)
				return output;
		}
		//should probably mark th somehow so it doesn't try to set any values once it times out
		th = null;
		throw new IOException("Timeout exceeded");
	}
  }


  public void download(Track track) throws IOException {
    try {
      try {
        URL url = track.getURL();

        String urlString = url.toString();
        int index = urlString.lastIndexOf('/');
        if (index > 0)
          urlString = urlString.substring(index + 1);
        while (true) {
          index = urlString.indexOf("%20");
          if (index < 0)
            break;
          urlString = urlString.substring(0, index) + " " + urlString.substring(index + 3);
        }
        File file = new File(downloadDir, urlString);

        setState("Connecting " + track.getName());

        //0 second timeout for the impatient
        long timeout = 60000;
        TimeoutWorker worker = new TimeoutWorker((Object) url) {
        	public void run() {
        		try {
        			URLConnection conn = ((URL)input).openConnection();
        			conn.connect();
        			Vector v = new Vector();
        			v.add(conn);
        			v.add(new Integer(conn.getContentLength()));
					setOutput(v);
        		} catch(IOException e) {
        			setException(e);
        		}
        	}
        };
        URLConnection conn;
        Integer intContentLength;
        try{
          Vector v =  (Vector) worker.runOrTimeout(timeout);
          conn = (URLConnection) v.elementAt(0);
          intContentLength = (Integer) v.elementAt(1);
        }catch(Exception e) {
        	setState("Could not connect: "+ e);
          e.printStackTrace();
        	return;
        }
        //get rid of the problem where tracks are downloaded but in reality they are 404 messages or some other html crud
		String contentType = conn.getContentType();
		if(contentType.indexOf("text") != -1) {
			track.setBroken();
			track.setRating(0);
			return;
		}

        worker = null;
        int contentLength = intContentLength.intValue();
        setState("Downloading " + track.getName());
        final InputStream is = conn.getInputStream();
        OutputStream os = new FileOutputStream(file);
        final byte buf[] = new byte[128000];
        int totalBytes = 0;
        worker = new TimeoutWorker((Object) is){
          public void run() {
            int n;
            try {
              n = is.read(buf);
            } catch(Exception e) {
              setException(e);
              return;
            }
            setOutput(new Integer(n));
          }
        };

        while (true) {
          int nbytes;
          try {
            nbytes = ((Integer) worker.runOrTimeout(timeout)).intValue();
          } catch(Exception e) {
            e.printStackTrace();
            return;
          }

          if (nbytes < 0)
            break;
          os.write(buf, 0, nbytes);

          if (contentLength >= 0) {
            totalBytes += nbytes;
            int percent = totalBytes * 100 / contentLength;
            if (percent != percentComplete) {
              percentComplete = percent;
              notifyUpdateListeners();
            }
          }
        }
        os.close();
        is.close();
        track.setFile(file);
        trackDatabase.save();
      }
      finally {
        //if (is != null) try { is.close(); } catch (IOException e) { e.printStackTrace(); }
        //if (os != null) try { os.close(); } catch (IOException e) { e.printStackTrace(); }
        percentComplete = 0;
      }
    }
    catch (FileNotFoundException fnfe) {
      currentTrack.setBroken();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void contactServer(TrackDatabase trackDatabase) {
    try {
      setState("Connecting to server");
      Socket socket = new Socket(trackDatabase.getHost(), trackDatabase.getPort());

      InputStream is = socket.getInputStream();
      setState("Sending server request");
      OutputStream os = socket.getOutputStream();
      System.out.println("Request:");
      System.out.println(trackDatabase.toString());
      byte[] buf = trackDatabase.toString().getBytes();
      os.write(("Content-Length: " + Integer.toString(buf.length) + "\r\n\r\n").getBytes());
      os.write(buf);
      os.flush();
      setState("Receiving server reply");
      TrackDatabase reply = new TrackDatabase(is);
      is.close();
      os.close();
      System.out.println("reply: ");
      System.out.println(reply.toString());
      trackDatabase.add(reply);
      trackDatabase.save();

      String errorCode = reply.getErrorCode();
      if (errorCode.length() != 0)
        handleError(errorCode, reply.getErrorURLString());
    }
    catch (UnknownHostException uhe) {
      handleError("nohost", "hostnotfound.html");
    }
    catch (ConnectException ce) {
      if (ce.getMessage().equals("Connection timed out"))
        handleError("conntimeout", "connectiontimeout.html");
      else if (ce.getMessage().equals("Connection refused"))
        handleError("connrefused", "connectionrefused.html");
      else
        handleError("conntimeout", "connectionfailed.html");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getState() {
    return state;
  }

  public int getPercentComplete() {
    return percentComplete;
  }

  private void setState(String state) {
    this.state = state;
    notifyUpdateListeners();
  }

  public void addUpdateListener(UpdateListener updateListener) {
    updateListeners.add(updateListener);
  }

  private void notifyUpdateListeners() {
    for (int i = 0; i < updateListeners.size(); i++) {
      UpdateListener updateListener = (UpdateListener) updateListeners.elementAt(i);
      updateListener.actionPerformed();
    }
  }

  private void doCheckAutoDownload() {
    int autoDownload = trackDatabase.getAutoDownload();
    int noOfUnrated = trackDatabase.getNoOfUnrated();
    if (noOfUnrated >= autoDownload) 
      setState(noOfUnrated + " unrated track" + (noOfUnrated == 1 ? "" : "s"));
    else
      go();      
  }
}
