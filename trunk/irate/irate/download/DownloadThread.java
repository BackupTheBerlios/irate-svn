// Copyright 2003 Anthony Jones

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
        if (file == null) 
          download(currentTrack);
        else if (!file.exists()) {
          currentTrack.unSetFile();
          try {
            trackDatabase.save();
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

  public void download(Track track) throws IOException {
    try {
      OutputStream os = null;
      InputStream is = null;
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
        URLConnection conn = url.openConnection();
        conn.connect();
        int contentLength = conn.getContentLength();
        setState("Downloading " + track.getName());
        is = conn.getInputStream();
        os = new FileOutputStream(file);
        byte buf[] = new byte[128000];
        int totalBytes = 0;
        while (true) {
          int nbytes = is.read(buf);
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
        os.close(); os = null;
        is.close(); is = null;
        track.setFile(file);
        trackDatabase.save();
      }
      finally {
        if (is != null) try { is.close(); } catch (IOException e) { e.printStackTrace(); }
        if (os != null) try { os.close(); } catch (IOException e) { e.printStackTrace(); }
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
    String state = null;
    synchronized (trackDatabase) {
      int autoDownload = trackDatabase.getAutoDownload();
      int autoDownloadCount = trackDatabase.getAutoDownloadCount();
      if (autoDownload == 0) 
        state = " ";
      else if (autoDownloadCount < autoDownload) 
        state = "Download in " + (autoDownload - autoDownloadCount) + " plays";
      else
        trackDatabase.setAutoDownloadCount(0);
    }
    if (state == null)
      go();
    else
      setState(state);
  }
}
