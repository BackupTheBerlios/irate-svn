// Copyright 2003 Anthony Jones, Taras Glek

package irate.download;

import irate.common.ErrorListener;
import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.UpdateListener;
import irate.resources.BaseResources;
import irate.common.Utils;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DownloadThread extends Thread {

  private static final String LOCALE_RESOURCE_LOCATION = 
    "irate.download.locale";
  private Vector updateListeners = new Vector();
  private Vector errorListeners = new Vector();
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
  private int numThreads=0; // Counts the num of download threads running
  private ArrayList messageList; // Stores the messages from the threads
  
  private HashSet tracksBeingDownloaded = new HashSet();
    
  public DownloadThread(TrackDatabase trackDatabase) {
    this.trackDatabase = trackDatabase;
    downloadDir = trackDatabase.getDownloadDirectory();
    if (!downloadDir.exists())
      downloadDir.mkdir();
    setDaemon(true);
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
          notifyErrorListeners("continuousfailed", "continuousfailed.html"); //$NON-NLS-1$ //$NON-NLS-2$
        ioe.printStackTrace();
      }
      catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
  }

  /** This seems to be the sanctioned way of signalling that we should do stuff.
  */
  public void checkAutoDownload()
  {
    synchronized (this) {
      notifyAll();
    }
  }

  // Appears to not be called externally. Could be made private
  public void go() {
    synchronized (this) {
      ready = true;
      notifyAll();
    }
  }

  // Appears to not be called externally. Could be made private
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
    final int MAX_SIMULTANEOUS_DOWNLOADS = 6;
    
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
            if (tracksBeingDownloaded.contains(currentTrack))
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

  // Appears to not be called externally. Could be made private
  public void process() throws IOException {
    if (!downloadSinglePending()) {
      try {
		    contactServer(trackDatabase);
		    downloadSinglePending();
      }
      catch (DownloadException e) {
        try {
          Thread.sleep(90000);
        }
        catch (InterruptedException ie) {
        }
      }
    }
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
    urlString = URLDecoder.decode(urlString); //$NON-NLS-1$
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
    tracksBeingDownloaded.add(track);
    final URL url = getProxyURL(track.getURL());
    System.out.println(url);
    final File finishedFile = getFileName(url);
    final File downloadingFile = new File(finishedFile.toString() + ".part");
    if (finishedFile.exists()) {
      // We've already have the finished file then we rename it to .part in
      // case it's a partially downloaded file from a previous version of
      // the program.
      if (!finishedFile.renameTo(downloadingFile)) {
        System.out.println("Failed to rename " + finishedFile + " to " + downloadingFile);
        finishedFile.delete();
      }
    }

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
      long continueOffset = downloadingFile.exists() ? downloadingFile.length() : 0;
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
        final byte buf[] = new byte[8192];

          // If we are resuming a download, then we must feed the already-downloaded
          // part of the file to the download listeners.  They are only expected
          // to deal with whole files.
        if (resume && downloadListeners.size() != 0) {
          FileInputStream is = new FileInputStream(downloadingFile.toString());
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
        OutputStream os = new FileOutputStream(downloadingFile.toString(), resume);
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
                if (track.setPercentComplete(percent)) {
                  notifyUpdateListeners(track);
                  for (Iterator iter = downloadListeners.iterator(); iter.hasNext();) {
                    DownloadListener d = (DownloadListener) iter.next();
                    d.downloadProgressed(track, percent, Resources.getString("DownloadThread.Downloading_Tracks"));
                  }
                }
              }
            }
          }
          os.close();
          os = null;
          if (downloadingFile.renameTo(finishedFile)) {
            track.setFile(finishedFile);
            succeeded = true;
            System.out.println("Finished downloading to " + finishedFile);
          } else {
            // I don't think this could occur, since we've already
            // checked to see if finishedFile exists, but...
            System.out.println("For some reason we couldn't rename \"" 
                               + downloadingFile + "\" to \"" + finishedFile + "\"");
          }
          exponentialBackoffManager.succeeded(track.getURL());
        }
        finally {
          if (os != null)
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
      trackDatabase.save();
    }
    catch (Exception e) {
      e.printStackTrace();
      if (e instanceof FileNotFoundException) {
        setState(getResourceString("DownloadThread.Download_broken") + track.getName());
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
      tracksBeingDownloaded.remove(track);
    }
  }

  /**
   * Connects to the server, uploads the trackdatabase and requests
   * new tracks to download.
   *
   * @param trackDatabase the trackdatabase that we work with.
   */
  public void contactServer(TrackDatabase trackDatabase) throws DownloadException {
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
        throw new DownloadException(errorCode, reply.getErrorURLString());
      } else//if no error incrmement serial
        trackDatabase.incrementSerial();
    }
    catch (UnknownHostException uhe) {
      throw new DownloadException("nohost", "hostnotfound.html"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch (ConnectException ce) {
      if (ce.getMessage().equals("Connection timed out")) //$NON-NLS-1$
        throw new DownloadException("conntimeout", "connectiontimeout.html"); //$NON-NLS-1$ //$NON-NLS-2$
      else if (ce.getMessage().equals("Connection refused")) //$NON-NLS-1$
        throw new DownloadException("connrefused", "connectionrefused.html"); //$NON-NLS-1$ //$NON-NLS-2$
      else
        throw new DownloadException("conntimeout", "connectionfailed.html"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch (IOException e) {
      throw new DownloadException("serverioerror", "ioerror.html");
    }
    catch (DownloadException de) {
      notifyErrorListeners(de.getCode(), de.getURLString());
      throw de;
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

  public void addErrorListener(ErrorListener errorListener) {
  	errorListeners.add(errorListener);
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

  public void removeErrorListener(ErrorListener errorListener) {
  	errorListeners.remove(errorListener);
  }

  private void notifyUpdateListeners(Track track) {
    for (int i = 0; i < updateListeners.size(); i++) {
      UpdateListener updateListener = (UpdateListener) updateListeners.elementAt(i);
      updateListener.actionPerformed();
    }
  }
  
  private void notifyErrorListeners(String code, String urlString) {
  	for (int i = 0; i < errorListeners.size(); i++) {
  	  ErrorListener errorListener = (ErrorListener) errorListeners.elementAt(i);
  	  errorListener.errorOccurred(code, urlString);
  	}
  }

  //Made public for UI Tweak by Allen Tipper 14.9.03
  // Appears to not be called externally any more, could be made private
  public void doCheckAutoDownload() {
    int noOfRated = trackDatabase.getNoOfRated();
    int noOfUnrated = trackDatabase.getNoOfUnrated();
    int noOfUnratedOnPlaylist = trackDatabase.getNoOfUnratedOnPlaylist();
    if ((noOfRated > 3 && noOfUnrated < noOfUnratedOnPlaylist) || noOfUnrated < 5)
      go();
  }
  
  /**
   * Registers a new thread starting up, and gives it a unique number
   * that is used to identify who the status messages are coming
   * from. The numbers will be reused, but not until
   * <code>noteDeadThread()</code> is called with that number first.
   *
   * @return a unique thread ID number
   */
  private int noteNewThread() {
    int loc=-1;
    if (messageList == null)
      messageList = new ArrayList();
    if (numThreads < messageList.size()) {
      // Find an empty spot to avoid adding on to it.
      for (int i=0; i<messageList.size(); i++)
        if (messageList.get(i) == null) {
          loc = i;
          break;
        }
      messageList.set(loc,"");
    } else {
      messageList.add("");
      loc = messageList.size()-1;
    }
    numThreads++;
    return loc;
  }

  /**
   * Removes a thread from the list of status messages.
   *
   * @param t the thread's number to remove
   */
  private void noteDeadThread(int t) {
    messageList.set(t,null);
    numThreads--;
    if (numThreads == 0) {
      messageList = null;
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
      setDaemon(true); // This means the JVM is permitted to exit in the middle of a transfer
    }
    
    public void run() {
      try {
        dt.download(track);
      } catch(IOException ioe) {
        ioe.printStackTrace();
      } 
    }
  }
}

// Local Variables:
// c-file-style:"gnu"
// c-basic-offset:2
// indent-tabs-mode:nil
// tab-width:4
// End:
