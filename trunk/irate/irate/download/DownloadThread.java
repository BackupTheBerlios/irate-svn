// Copyright 2003 Anthony Jones, Taras Glek

package irate.download;

import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.UpdateListener;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

public class DownloadThread extends Thread {

  private Vector updateListeners = new Vector();
  private TrackDatabase trackDatabase;
  private Track currentTrack;
  private File downloadDir;
  private String state;
  private int percentComplete;
  private boolean ready;
  private boolean continuous;
  private int contactCount = 0;
    
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
    boolean success = false;
    Track[] tracks = trackDatabase.getTracks();
    for (int i = 0; i < tracks.length; i++) {
      currentTrack = tracks[i];
      if (!currentTrack.isHidden()) {
        File file = currentTrack.getFile();
        if ((file == null || !file.exists()) && currentTrack.getDownloadAttempts() < 10) {
          try {
            if (file != null) {
              currentTrack.unSetFile();
              trackDatabase.save();
            }
            download(currentTrack);
            success = true;
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return success;
  }

  public void process() throws IOException {
    if (!downloadSinglePending()) {
      contactServer(trackDatabase);
      downloadSinglePending();
    }
  }

  /** Prints error to System out and pauses execution for 5seconds */
  public void handleError(String code, String urlString) {
    System.out.println("Server error: " + code);
    //prevent quick reconnects
    try {
      Thread.sleep(5000);
    }
    catch(InterruptedException ie) {}
  }

  URLConnection openConnection(URL u) throws IOException{
    final URL url = u;

    return null;
  }

  private abstract class TimeoutWorker {
    private Thread timeoutThread;
    private Exception exception;
    private boolean done;

    public TimeoutWorker() {
    }

    public abstract void run() throws Exception;

    public void runOrTimeout(long timeout) throws Exception {
      //running thread
      timeoutThread = Thread.currentThread();
      //increment..the bigger the less cpu we use...but slower downloads
      int step = 100;
      //reset outputs
      exception = null;
      done = false;
      //start thread with a task that might timeout
      new Thread(new Runnable() {
        public void run() {
          try {
            TimeoutWorker.this.run();
          }
          catch (Exception e) {
            e.printStackTrace();
            TimeoutWorker.this.exception = e;
          }
          done = true;
        }
      }).start();
      while (timeout > 0) {
        Thread.sleep(step);
        timeout -= step;
        if (exception != null)
          throw exception;
        if (done)
          return;
      }
      //should probably mark th somehow so it doesn't try to set any values once it times out
      throw new IOException("Timeout exceeded");
    }
  }
  
  /**
   * Get the file name associated with the given URL. 
   */
  private File getFileName(URL url) {
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
    return new File(trackDatabase.getDownloadDirectory(), urlString);
  }
  
  /** 
   * Create URL which contains any appropriate proxy information.
   */     
  private URL getProxyURL(URL url) throws MalformedURLException {
    String proxy = trackDatabase.getHTTPProxy(); //JDR
    if ((proxy!=null) && (!proxy.trim().equals("")) ) //JDR
    {//JDR HTTP proxy *is* defined in xml file.
      //System.out.println("\nUsing HTTPProxy:"+HTTPProxy+"\n"); //JDR
      return new URL("http", proxy, trackDatabase.getHTTPProxyPort(), url.toString());//JDR
      //JDR The URL constructor created this way will cause the URL to go through the proxy.
      //JDR port -1 = protocol port default (example http uses port 80 unless otherwise specified).
    }
    return url;
  }

  public void download(final Track track) throws IOException {
    final URL url = getProxyURL(track.getURL());
    System.out.println(url);

    final File file = getFileName(url);
    setState("Connecting to " + url.getHost());

    //120 second timeout for the impatient
    long timeout = 120000;
      
    try {
      /*
       * Open the connection and get the content length.
       */
      URLConnection conn;
      int contentLength;
      long continueOffset;
      final InputStream inputStream = null;
      try {
        // These values are returned by the Timeout Worker.
        final URLConnection[] urlConnectionArray = new URLConnection[1];
        final Integer[] contentLengthArray = new Integer[1];
        final Long[] continueOffsetArray = new Long[1];
        final InputStream[] inputStreamArray = new InputStream[1];
        new TimeoutWorker() {
          public void run() throws Exception {
            URLConnection conn = url.openConnection();
            conn.connect();
            contentLengthArray[0] = new Integer(conn.getContentLength());
            
            long continueOffset = file.exists() ? file.length() : 0;
            if (continueOffset != 0) {
              try {
                URLConnection resumeConn = url.openConnection();
                resumeConn.setRequestProperty("Range", "bytes=" + continueOffset + "-");
                resumeConn.connect();
                inputStreamArray[0] = resumeConn.getInputStream();
                urlConnectionArray[0] = resumeConn;
                continueOffsetArray[0] = new Long(continueOffset);
                return;
              }
              catch (IOException e) {
                e.printStackTrace();
              }
            }
              
            conn = url.openConnection();
            conn.connect();
            inputStreamArray[0] = conn.getInputStream();
            urlConnectionArray[0] = conn;
            continueOffsetArray[0] = new Long(0);
          }
        }.runOrTimeout(timeout);
        conn = urlConnectionArray[0];
        contentLength = contentLengthArray[0].intValue();
        continueOffset = continueOffsetArray[0].longValue();
        inputStream = inputStreamArray[0];
      }
      catch (Exception e) {
        throw new IOException(e.toString());
      }
      
      /*
       * If the file isn't already the right length, then we need to download.
       */
      try {
        /* 
         * Get rid of the problem where tracks are downloaded but in reality 
         * they are 404 messages or some other html crud.
         */
        String contentType = conn.getContentType();
        if (contentType.indexOf("text") != -1)
          throw new FileNotFoundException("Content type is Text");

        if (continueOffset != contentLength) {
          setState("Downloading: " + track.getName());
          // If the continue offset is non-zero then we open the file in
          // append mode. If the file on the server is shorter than the one 
          // we have on disk then we just start again.
          OutputStream os = new FileOutputStream(file.toString(), continueOffset != 0);
          final byte buf[] = new byte[128000];
          int totalBytes = (int)continueOffset;
          
          try {
            final Integer[] nbytesArray = new Integer[1];
            while (true) {
              try {
                new TimeoutWorker() {
                  public void run() throws Exception {
                    nbytesArray[0] = new Integer(inputStream.read(buf));
                  }
                }.runOrTimeout(timeout);
              }
              catch (Exception e) {
                throw new IOException(e.toString());
              }
    
              int nbytes = nbytesArray[0].intValue();
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
          }
          finally {
            os.close();
          }
        }
      }
      finally {
        inputStream.close();
      }
      track.setFile(file);
      trackDatabase.save();
    }
    catch (FileNotFoundException fnfe) {
      setState("Broken download: " + track.getName());
      currentTrack.setBroken();
    }
    catch (IOException e) {
      setState("Download failure: " + track.getName());
      currentTrack.increaseDownloadAttempts();
      e.printStackTrace();
    }
    finally {
      percentComplete = 0;
    }
  }

  public void contactServer(TrackDatabase trackDatabase) {
    try {
      setState("Connecting to server");
      Socket socket;
      try {
        socket = new Socket(trackDatabase.getHost(), trackDatabase.getPort());
      }
      catch (UnknownHostException uhe) {
        uhe.printStackTrace();

          // Retrying using the IP address
        socket = new Socket("202.72.160.235", trackDatabase.getPort());
      }

      InputStream is = socket.getInputStream();
      setState("Sending server request");
      OutputStream os = socket.getOutputStream();
      String str;
      
      if(contactCount++ > 0)
        str = trackDatabase.toSerialString();
      //send full db on first connect
      else
        str = trackDatabase.toString();
      
      //System.out.println("Request:");
      //System.out.println(str);
      byte[] buf = str.getBytes();
      os.write(("Content-Length: " + Integer.toString(buf.length) + "\r\nContent-Encoding: gzip\r\n\r\n").getBytes());
      GZIPOutputStream gos = new GZIPOutputStream(os);
      gos.write(buf);
      gos.finish();
      os.flush();
      setState("Receiving server reply");
      TrackDatabase reply = new TrackDatabase(new GZIPInputStream(is));
      is.close();
      os.close();
      //System.out.println("reply: ");
      //System.out.println(reply.toString());
      trackDatabase.add(reply);
      trackDatabase.save();

      String errorCode = reply.getErrorCode();
//if errorCode == "password" we can give a better prompt.
System.out.println("DownloadThread.java:326: " + errorCode);
      if (errorCode.length() != 0)
        handleError(errorCode, reply.getErrorURLString());
      else//if no error incrmement serial
        trackDatabase.incrementSerial();

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
  
  //Made public for UI Tweak by Allen Tipper 14.9.03
  //Can we have more reasons here?
  public void setState(String state) {
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

  //Made public for UI Tweak by Allen Tipper 14.9.03
  public void doCheckAutoDownload() {
    if (!trackDatabase.hasRatedEnoughTracks()) {
      setState("Not enough rated tracks");
    }
    else {
      int autoDownload = trackDatabase.getAutoDownload();
      int noOfUnrated = trackDatabase.getNoOfUnrated();
      if (noOfUnrated >= autoDownload)
        setState(noOfUnrated + " unrated track" + (noOfUnrated == 1 ? "" : "s"));
      else
        go();
    }
  }
}
