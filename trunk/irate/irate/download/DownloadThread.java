package irate.download;

import irate.common.*;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class DownloadThread extends Thread {
  
  private Vector actionListeners = new Vector();
  private TrackDatabase trackDatabase;
  private Track currentTrack;
  private File downloadDir;
  private String state;
  private int percentComplete;
  private boolean ready;
  
  public DownloadThread(TrackDatabase trackDatabase) {
    this.trackDatabase = trackDatabase;
    downloadDir = new File("download");
    if (!downloadDir.exists()) 
      downloadDir.mkdir();
  }

  public void run() {
    while (true) {
      setState(" ");
      
      try {
        while (!ready) {
          sleep(100);
        }
        process();
        ready = false;
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void go() {
    ready = true;
  }
  
  public void process() {
    contactServer(trackDatabase);
    Track[] tracks = trackDatabase.getTracks();
    for (int i = 0; i < tracks.length; i++) {
      currentTrack = tracks[i];
      File file = currentTrack.getFile();
      if (file == null) 
        download(currentTrack);
      else if (!file.exists()) {
        currentTrack.unSetFile();
        try {
          trackDatabase.save();
          download(currentTrack);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void handleError(String code, String urlString) {
    System.out.println("Server error: " + code);
  }

  public void download(Track track) {
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
              notifyActionListeners();
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
    notifyActionListeners();
  }
  
  public void addActionListener(ActionListener actionListener) {
    actionListeners.add(actionListener);
  }
  
  private void notifyActionListeners() {
    for (int i = 0; i < actionListeners.size(); i++) {
      ActionListener actionListener = (ActionListener) actionListeners.elementAt(i);
      actionListener.actionPerformed(null);
    }
  }
}
