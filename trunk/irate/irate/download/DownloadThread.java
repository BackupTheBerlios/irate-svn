// Copyright 2003 Anthony Jones, Taras Glek

package irate.download;

import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.UpdateListener;
import irate.resources.BaseResources;

import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DownloadThread extends Thread {

  private final String LOCALE_RESOURCE_LOCATION = "irate.download.locale";
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
          handleError("continuousfailed", "continuousfailed.html"); //$NON-NLS-1$ //$NON-NLS-2$
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
    System.out.println("Server error: " + code); //$NON-NLS-1$
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
          throw new Exception(exception.toString());
        if (done)
          return;
      }
      //should probably mark th somehow so it doesn't try to set any values once it times out
      throw new IOException("Timeout exceeded"); //$NON-NLS-1$
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
      index = urlString.indexOf("%20"); //$NON-NLS-1$
      if (index < 0)
        break;
      urlString = urlString.substring(0, index) + " " + urlString.substring(index + 3); //$NON-NLS-1$
    }
    return new File(trackDatabase.getDownloadDirectory(), urlString);
  }
  
  /** 
   * Create URL which contains any appropriate proxy information.
   */     
  private URL getProxyURL(URL url) throws MalformedURLException {
    String proxy = trackDatabase.getHTTPProxy(); //JDR
    if ((proxy!=null) && (!proxy.trim().equals("")) ) //JDR //$NON-NLS-1$
    {//JDR HTTP proxy *is* defined in xml file.
      //System.out.println("\nUsing HTTPProxy:"+HTTPProxy+"\n"); //JDR
      return new URL("http", proxy, trackDatabase.getHTTPProxyPort(), url.toString());//JDR //$NON-NLS-1$
      //JDR The URL constructor created this way will cause the URL to go through the proxy.
      //JDR port -1 = protocol port default (example http uses port 80 unless otherwise specified).
    }
    return url;
  }

  public void download(final Track track) throws IOException {
    final URL url = getProxyURL(track.getURL());
    System.out.println(url);

    final File file = getFileName(url);
    setState(getResourceString("DownloadThread.Connecting_to") + url.getHost()); //$NON-NLS-1$

    //120 second timeout for the impatient
    long timeout = 120000;
      
    try {
      /*
       * Open the connection and get the content length.
       */
      URLConnection conn;
      int contentLength;
      long continueOffset;
      InputStream inputStream;
      boolean timedOut = false;
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
                resumeConn.setRequestProperty("Range", "bytes=" + continueOffset + "-"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
        if (contentType.indexOf("text") != -1) //$NON-NLS-1$
          throw new FileNotFoundException("Content type is Text"); //$NON-NLS-1$

        if (continueOffset != contentLength) {
          setState(getResourceString("DownloadThread.Downloading") + track.getName()); //$NON-NLS-1$
          // If the continue offset is non-zero then we open the file in
          // append mode. If the file on the server is shorter than the one 
          // we have on disk then we just start again.
          OutputStream os = new FileOutputStream(file.toString(), continueOffset != 0);
          final byte buf[] = new byte[128000];
          int totalBytes = (int)continueOffset;
          
          try {
            final Integer[] nbytesArray = new Integer[1];
            final InputStream finalInputStream = inputStream;
            while (true) {
              try {
                new TimeoutWorker() {
                  public void run() throws Exception {
                    nbytesArray[0] = new Integer(finalInputStream.read(buf));
                  }
                }.runOrTimeout(timeout);
              }
              catch (Exception e) {
                timedOut = true;
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
        // Seems to be an ugly Java bug in the URLConnection.
        // If a connection times out (connection lost) and then we attempt to 
        // close the stream, it blocks -- forever.  Or, until data is available, anyway.
        // So, in this case, lets not close the stream, as ugly as that is.  It shouldn't happen
        // all that often.
        if(!timedOut) {
          inputStream.close();
        }
        else {
          timedOut = false;
        }
      }
      track.setFile(file);
      trackDatabase.save();
    }
    catch (FileNotFoundException fnfe) {
      setState("Broken download: " + track.getName()); //$NON-NLS-1$
      currentTrack.setBroken();
    }
    catch (IOException e) {
      setState(getResourceString("DownloadThread.Download_failure") + track.getName()); //$NON-NLS-1$
      currentTrack.increaseDownloadAttempts();
      e.printStackTrace();
    }
    finally {
      percentComplete = 0;
    }
  }

  public void contactServer(TrackDatabase trackDatabase) {
    try {
      setState(getResourceString("DownloadThread.Connecting_to_server")); //$NON-NLS-1$
      Socket socket;
      try {
        socket = new Socket(trackDatabase.getHost(), trackDatabase.getPort());
      }
      catch (UnknownHostException uhe) {
        uhe.printStackTrace();

          // Retrying using the IP address
        socket = new Socket("202.72.160.235", trackDatabase.getPort()); //$NON-NLS-1$
      }

      InputStream is = socket.getInputStream();
      setState(getResourceString("DownloadThread.Sending_server_request")); //$NON-NLS-1$
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
      os.write(("Content-Length: " + Integer.toString(buf.length) + "\r\nContent-Encoding: gzip\r\n\r\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
      GZIPOutputStream gos = new GZIPOutputStream(os);
      gos.write(buf);
      gos.finish();
      os.flush();
      setState(getResourceString("DownloadThread.Receiving_server_reply")); //$NON-NLS-1$
      TrackDatabase reply = new TrackDatabase(new GZIPInputStream(is));
      is.close();
      os.close();
      //System.out.println("reply: ");
      //System.out.println(reply.toString());
      trackDatabase.add(reply);
      trackDatabase.save();

      String errorCode = reply.getErrorCode();
//if errorCode == "password" we can give a better prompt.
System.out.println("DownloadThread.java:326: " + errorCode); //$NON-NLS-1$
      if (errorCode.length() != 0)
        handleError(errorCode, reply.getErrorURLString());
      else//if no error incrmement serial
        trackDatabase.incrementSerial();

    }
    catch (UnknownHostException uhe) {
      handleError("nohost", "hostnotfound.html"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch (ConnectException ce) {
      if (ce.getMessage().equals("Connection timed out")) //$NON-NLS-1$
        handleError("conntimeout", "connectiontimeout.html"); //$NON-NLS-1$ //$NON-NLS-2$
      else if (ce.getMessage().equals("Connection refused")) //$NON-NLS-1$
        handleError("connrefused", "connectionrefused.html"); //$NON-NLS-1$ //$NON-NLS-2$
      else
        handleError("conntimeout", "connectionfailed.html"); //$NON-NLS-1$ //$NON-NLS-2$
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

  private void removeUpdateListener(UpdateListener updateListener) {
    for (int i = 0; i < updateListeners.size(); i++)
      if(updateListeners.elementAt(i)==updateListener) {
        updateListeners.removeElementAt(i);
        return;
      }
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
      setState(getResourceString("DownloadThread.Not_enough_rated_tracks")); //$NON-NLS-1$
    }
    else {
      int autoDownload = trackDatabase.getAutoDownload();
      int noOfUnrated = trackDatabase.getNoOfUnrated();
      if (noOfUnrated >= autoDownload)
        setState(noOfUnrated + getResourceString("DownloadThread.unrated_track") + (noOfUnrated == 1 ? "" : "s")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      else
        go();
    }
  }
  
  /**
   * Get a resource string from the properties file associated with this 
   * class.
   * 
   * This class can't determine it's own package, since it extends thread.  It has
   * to be stated explicitly.
   */
  private String getResourceString(String key) {
    return BaseResources.getString(LOCALE_RESOURCE_LOCATION, key); 
  }
}
