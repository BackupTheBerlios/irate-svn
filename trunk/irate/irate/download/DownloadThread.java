// Copyright 2003 Anthony Jones, Taras Glek

package irate.download;

import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.UpdateListener;
import irate.resources.BaseResources;
import irate.common.Utils;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DownloadThread extends Thread {

  private static final String LOCALE_RESOURCE_LOCATION = 
    "irate.download.locale";
  private Vector updateListeners = new Vector();
  private TrackDatabase trackDatabase;
  private File downloadDir;
  private String state;
  private int percentComplete;
  private boolean ready;
  private boolean continuous;
  private int contactCount = 0;
  private Vector downloadListeners = new Vector();
  ExponentialBackoffManager exponentialBackoffManager = 
    new ExponentialBackoffManager();
    
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
    Track[] tracks = trackDatabase.getTracks();
    //keep queued files there
    Vector downloadTracks = new Vector();
    //download threads keyed by host
    Hashtable downloadThreads = new Hashtable();
    boolean toSave = false;
      // The maximum number of files that can be downloaded simultaneously
    final int MAX_SIMULTANEOUS_DOWNLOADS = 4;
    
    for (int i = 0; i < tracks.length; i++) {
      Track currentTrack = tracks[i];
      if (!currentTrack.isHidden()) {
        File file = currentTrack.getFile();
        if ((file == null || !file.exists()) && currentTrack.getDownloadAttempts() < 10) {
          if (file != null) {
            currentTrack.unSetFile();
            toSave = true;
          }
          if (!exponentialBackoffManager.isBackedOff(currentTrack.getURL()))
            downloadTracks.add(currentTrack);
        }
      }
    }
    if (toSave)
      trackDatabase.save();

    //can't download anything..should contact server
    if(downloadTracks.size() == 0) 
      return false;

      // Scramble the list of download candidates into a random order
    Utils.scramble(downloadTracks);
      // Limit the number of simultaneous downloads to a reasonable maximum
    while (downloadTracks.size() > MAX_SIMULTANEOUS_DOWNLOADS)
      downloadTracks.remove(downloadTracks.size()-1);

    for (int i = 0; i < downloadTracks.size(); i++) {
      Track currentTrack = (Track) downloadTracks.get(i);
      //gcj doesnt like nulls in hashtables..so we later will replace the value with a thread
      downloadThreads.put(currentTrack.getURL().getHost(), currentTrack.getURL().getHost());
    }

    while(downloadTracks.size() > 0 || downloadThreads.size() > 0) {
      //loop through current threads &queued downloads
      Enumeration keyThreads = downloadThreads.keys();
      while (keyThreads.hasMoreElements()) {
        String host = (String)keyThreads.nextElement();
        Object  o = downloadThreads.get(host);
        TrackDownloader td;
        //if a slot is empty start a new download
        if(!(o instanceof TrackDownloader) || !(td=(TrackDownloader)o).isAlive()) {
          for (Iterator iter = downloadTracks.iterator(); iter.hasNext(); ) {
            Track track = (Track) iter.next();
            if(!host.equals(track.getURL().getHost()))
              continue;
            iter.remove();
            System.out.println("Simultaneously downloading url "+track.getURL() + " hidden="+track.isHidden());
            td = new TrackDownloader(this, track);
            //place a new download in the slot
            //silly gcj craps out if we call start fromt the TrackDownloader constructor
            td.start();
            downloadThreads.put(host, td);
          } 
        }
        if ((o instanceof TrackDownloader) && 
            !((TrackDownloader)o).isAlive()) {
          downloadThreads.remove(host);          
        }
      }//while
      //dont bother the system much
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }//while
    
    return true;
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
    setState(getResourceString("DownloadThread.Connecting_to")
             + url.getHost());
    try {
      long timeout = 120000;      
      DownloadConnection connection = new DownloadConnection(url);
      connection.connect(0, timeout);
      // Get rid of the problem where tracks are downloaded but in reality
      // they are 404 messages or some other html crud.
      String contentType = connection.getContentType();
      if (contentType == null)
        throw new ProtocolException("Something went wrong talking to the "+
                                    "download site");
      if (contentType.indexOf("text") != -1)
        throw new FileNotFoundException("Content type is Text");
      int contentLength = connection.getContentLength();
      long continueOffset = file.exists() ? file.length() : 0;
      // If the file isn't already the right length, then we need to download
      if (continueOffset != contentLength) {
        boolean resume = false;
        if (continueOffset > 0) {
          resume = true;
          DownloadConnection resumeConnection = null;
          try {
            resumeConnection = new DownloadConnection(url);
            resumeConnection.connect(continueOffset, timeout);
            System.out.println("Resuming download of "+track+" (offset="
                               + continueOffset + ")");
          }
          catch (DownloadConnection.ResumeNotSupportedException exception) {
            System.out.println("Server does not support resuming downloads");
            resume = false;
            continueOffset = 0;
          }
          if (resume) {
            try {
              connection.close(timeout);
            }
            catch (IOException e) {
            }
            connection = resumeConnection;
          }
        }
        setState(getResourceString("DownloadThread.Downloading")
                 + track.getName());
        for (int i = 0; i < downloadListeners.size(); i++)
          ((DownloadListener)downloadListeners.get(i)).downloadStarted(track);
        final byte buf[] = new byte[128000];

          // If we are resuming a download, then we must feed the already-downloaded
          // part of the file to the download listeners.  They are only expected
          // to deal with whole files.
        if (resume && downloadListeners.size() != 0) {
          FileInputStream is = new FileInputStream(file.toString());
          try {
            while (true) {
              int nbytes = is.read(buf, 0, buf.length);
              if (nbytes <= 0)
                break;
              for (int i = 0; i < downloadListeners.size(); i++)
                ((DownloadListener)downloadListeners.get(i)).downloadData(track, buf, 0, nbytes);
            }
          }
          finally {
            is.close();
          }
        }

        // If the continue offset is non-zero then we open the file in
        // append mode. If the file on the server is shorter than the one 
        // we have on disk then we just start again.
        OutputStream os = new FileOutputStream(file.toString(), resume);
        boolean succeeded = false;
        try {
          int totalBytes = (int)continueOffset;          
          while (true) {
            int nbytes = connection.read(buf, timeout);
            if (nbytes < 0)
              break;
            os.write(buf, 0, nbytes);
              // Stephen Blackheath: I am in two minds as to whether this should be a separate
              // listener, but it should be efficient enough as it is.
            for (int i = 0; i < downloadListeners.size(); i++)
              ((DownloadListener)downloadListeners.get(i)).downloadData(track, buf, 0, nbytes);
            if (contentLength >= 0) {
              totalBytes += nbytes;
              int percent = totalBytes * 100 / contentLength;
              if (percent != percentComplete) {
                percentComplete = percent;
                track.setPercentComplete(percent);
                notifyUpdateListeners(track);
                for (Iterator iter = downloadListeners.iterator(); iter.hasNext();) {
                  DownloadListener d = (DownloadListener) iter.next();
                  d.downloadProgressed(track, percent, Resources.getString("DownloadThread.Downloading_Tracks"));
                }
              }
            }
          }
          succeeded = true;
          exponentialBackoffManager.succeeded(track.getURL());
        }
        finally {
          os.close();
          try {
            connection.close(timeout);
          }
          catch (IOException exception) {
          }
          for (int i = 0; i < downloadListeners.size(); i++)
            ((DownloadListener)downloadListeners.get(i)).downloadFinished(track, succeeded);
        }
      }
      track.setFile(file);
      trackDatabase.save();
    }
    catch (Exception e) {
      e.printStackTrace();
      if (e instanceof FileNotFoundException) {
        setState("Broken download: " + track.getName()); //$NON-NLS-1$
        track.setBroken();
      } else if (e instanceof IOException) {
        exponentialBackoffManager.failed(track.getURL());
        setState(getResourceString("DownloadThread.Download_failure")
                 + track.getName());
        track.increaseDownloadAttempts();
      } else {
        System.out.println("Exception not handled");
      } 
    }
    finally {
      percentComplete = 0;
    }
  }

  /**
   * Connects to the server, uploads the trackdatabase and requests
   * new tracks to download.
   *
   * @param trackDatabase the trackdatabase that we work with.
   */
  public void contactServer(TrackDatabase trackDatabase) {
    try {
      setState(getResourceString("DownloadThread.Connecting_to_server")); //$NON-NLS-1$
      Socket socket =
        new Socket(trackDatabase.getHost(), trackDatabase.getPort());
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
System.out.println("DownloadThread.java:303: " + errorCode); //$NON-NLS-1$
      if (errorCode.length() != 0) {
        handleError(errorCode, reply.getErrorURLString());
        try {
          Thread.sleep(90000); // Pause for 15 minutes before trying again.
        } catch (InterruptedException e) {}
      } else//if no error incrmement serial
        trackDatabase.incrementSerial();
    }
    catch (UnknownHostException uhe) {
      handleError("nohost", "hostnotfound.html"); //$NON-NLS-1$ //$NON-NLS-2$
      try {
        Thread.sleep(90000);
      } catch (InterruptedException e) {}
    }
    catch (ConnectException ce) {
      if (ce.getMessage().equals("Connection timed out")) //$NON-NLS-1$
        handleError("conntimeout", "connectiontimeout.html"); //$NON-NLS-1$ //$NON-NLS-2$
      else if (ce.getMessage().equals("Connection refused")) //$NON-NLS-1$
        handleError("connrefused", "connectionrefused.html"); //$NON-NLS-1$ //$NON-NLS-2$
      else
        handleError("conntimeout", "connectionfailed.html"); //$NON-NLS-1$ //$NON-NLS-2$
      try {
        Thread.sleep(90000);
      } catch (InterruptedException e) {}
    }
    catch (IOException e) {
      e.printStackTrace();
      try {
        Thread.sleep(90000);
      } catch (InterruptedException ie) {}
    }
  }

  public String getDownloadState() {
    return state;
  }

  public int getPercentComplete() {
    return percentComplete;
  }
  
  //Made public for UI Tweak by Allen Tipper 14.9.03
  //Can we have more reasons here?
  public void setState(String state) {
    this.state = state;
    notifyUpdateListeners(null);
  }

  public void addUpdateListener(UpdateListener downloadListener) {
    updateListeners.add(downloadListener);
  }

  public void addDownloadListener(DownloadListener downloadListener) {
    downloadListeners.add(downloadListener);
  }

  public void removeDownloadListener(DownloadListener downloadListener) {
    downloadListeners.remove(downloadListener);
  }

  public void removeUpdateListener(UpdateListener updateListener) {
    for (int i = 0; i < updateListeners.size(); i++)
      if(updateListeners.elementAt(i)==updateListener) {
        updateListeners.removeElementAt(i);
        return;
      }
  }

  private void notifyUpdateListeners(Track track) {
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

  private static class TrackDownloader extends Thread{
    private Track track;
    private DownloadThread dt;
    
    public TrackDownloader(DownloadThread dthread, Track t) {
      track = t;
      dt = dthread;
    }
    
    public void run() {
      try {
        dt.download(track);
      } catch(IOException ioe) {
        ioe.printStackTrace();
      } 
    }
  };
}

// Local Variables:
// c-file-style:"gnu"
// c-basic-offset:2
// indent-tabs-mode:nil
// tab-width:4
// End:
