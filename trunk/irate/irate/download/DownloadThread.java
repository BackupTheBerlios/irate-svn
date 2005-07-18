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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DownloadThread extends Thread {

  /** The maximum number of files that can be downloaded simultaneously */
  private static final int MAX_SIMULTANEOUS_DOWNLOADS = 6;

  /**
   * The minimum number of files to be downloaded simultaneously before
   * contacting the server again
   */
  private static final int MIN_SIMULTANEOUS_DOWNLOADS = 1;

  private static final String LOCALE_RESOURCE_LOCATION = "irate.download.locale";

  private Vector updateListeners = new Vector();

  private Vector errorListeners = new Vector();

  private File downloadDir;

  private String state;

  private int contactCount = 0;

  private DownloadContext downloadContext;

  public DownloadThread(TrackDatabase trackDatabase) {

    downloadContext = new DownloadContext(trackDatabase);

    downloadDir = trackDatabase.getDownloadDirectory();
    if (!downloadDir.exists()) downloadDir.mkdir();
    setDaemon(true);
  }

  public void run() {
    while (true) {
      try {
        synchronized (this) {
          wait();
        }

        try {
          if (areMoreTracksRequired()) {
            downloadPendingTracks();

            if (downloadContext.getTracksBeingDownloaded().size() < MIN_SIMULTANEOUS_DOWNLOADS) {
              contactServer(downloadContext.getTrackDatabase());
              downloadPendingTracks();
            }
          }
        }
        catch (IOException ioe) {
          ioe.printStackTrace();
        }
        catch (DownloadException e) {
          Thread.sleep(90000);
        }
      }
      catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
  }

  /**
   * This seems to be the sanctioned way of signalling that we should do stuff.
   */
  public void checkAutoDownload() {
    synchronized (this) {
      notifyAll();
    }
  }

  private List getTracksAvailableForDownload() throws IOException {
    Track[] tracks = downloadContext.getTrackDatabase().getTracks();
    TracksBeingDownloaded tracksBeingDownloaded = downloadContext
        .getTracksBeingDownloaded();
    ExponentialBackoffManager exponentialBackoffManager = downloadContext
        .getExponentialBackoffManager();

    boolean hasTrackDatabaseBeenModified = false;

    List tracksAvailableForDownload = new ArrayList();
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (!track.isHidden()) {
        File file = track.getFile();
        if (file == null || !file.exists()) {
          if (file != null) {
            track.unSetFile();
            hasTrackDatabaseBeenModified = true;
          }

          if (track.getDownloadAttempts() >= 10
              || tracksBeingDownloaded.contains(track)
              || tracksBeingDownloaded.isServerAlreadyUsed(track)
              || exponentialBackoffManager.isBackedOff(track.getURL())) {
            // Don't download
          }
          else {
            // Download the track
            tracksAvailableForDownload.add(track);
          }
        }
      }
    }

    if (hasTrackDatabaseBeenModified)
      downloadContext.getTrackDatabase().save();

    return tracksAvailableForDownload;
  }

  private void downloadPendingTracks() throws IOException {
    List downloadTracks = getTracksAvailableForDownload();

    // can't download anything..should contact server
    if (downloadTracks.size() == 0) return;

    // Scramble the list of download candidates into a random order
    Utils.scramble(downloadTracks);

    TracksBeingDownloaded tracksBeingDownloaded = downloadContext
        .getTracksBeingDownloaded();

    for (Iterator itr = downloadTracks.iterator(); itr.hasNext();) {

      // Limit the number of simultaneous downloads to a reasonable maximum
      if (downloadContext.getTracksBeingDownloaded().size() >= MAX_SIMULTANEOUS_DOWNLOADS)
        break;

      Track track = (Track) itr.next();

      if (!tracksBeingDownloaded.isServerAlreadyUsed(track)) {
        TrackDownloader td = new TrackDownloader(downloadContext, track);
        // silly gcj craps out if we call start fromt the TrackDownloader
        // constructor
        td.start();
      }
    }
  }

  /**
   * Connects to the server, uploads the trackdatabase and requests new tracks
   * to download.
   * 
   * @param trackDatabase
   *          the trackdatabase that we work with.
   */
  public void contactServer(TrackDatabase trackDatabase)
      throws DownloadException {
    try {
      setState(getResourceString("DownloadThread.Connecting_to_server")); //$NON-NLS-1$

      Socket socket = new Socket(trackDatabase.getHost(), trackDatabase
          .getPort());
      InputStream is = socket.getInputStream();
      setState(getResourceString("DownloadThread.Sending_server_request")); //$NON-NLS-1$

      OutputStream os = socket.getOutputStream();
      String str;

      if (contactCount++ > 0)
        str = trackDatabase.toSerialString();
      // send full db on first connect
      else
        str = trackDatabase.toString();

      // System.out.println("Request:");
      // System.out.println(str);
      byte[] buf = str.getBytes();
      os
          .write(("Content-Length: " + Integer.toString(buf.length) + "\r\nContent-Encoding: gzip\r\n\r\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
      GZIPOutputStream gos = new GZIPOutputStream(os);
      gos.write(buf);
      gos.finish();
      os.flush();
      setState(getResourceString("DownloadThread.Receiving_server_reply")); //$NON-NLS-1$
      TrackDatabase reply = new TrackDatabase(new GZIPInputStream(is));
      is.close();
      os.close();
      // System.out.println("reply: ");
      // System.out.println(reply.toString());
      trackDatabase.add(reply);
      trackDatabase.save();

      String errorCode = reply.getErrorCode();
      // if errorCode == "password" we can give a better prompt.
      System.out.println("DownloadThread.java:303: " + errorCode); //$NON-NLS-1$
      if (errorCode.length() != 0) {
        throw new DownloadException(errorCode, reply.getErrorURLString());
      }
      else
        // if no error incrmement serial
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

  public void addDownloadListener(DownloadListener downloadListener) {
    downloadContext.addDownloadListener(downloadListener);
  }

  // Made public for UI Tweak by Allen Tipper 14.9.03
  // Can we have more reasons here?
  public void setState(String state) {
    this.state = state;
    notifyUpdateListeners(null);
  }

  public void addUpdateListener(UpdateListener downloadListener) {
    updateListeners.add(downloadListener);
  }

  public void addErrorListener(ErrorListener errorListener) {
    errorListeners.add(errorListener);
  }

  public void removeDownloadListener(DownloadListener downloadListener) {
    downloadContext.removeDownloadListener(downloadListener);
  }

  public void removeUpdateListener(UpdateListener updateListener) {
    for (int i = 0; i < updateListeners.size(); i++)
      if (updateListeners.elementAt(i) == updateListener) {
        updateListeners.removeElementAt(i);
        return;
      }
  }

  public void removeErrorListener(ErrorListener errorListener) {
    errorListeners.remove(errorListener);
  }

  private void notifyUpdateListeners(Track track) {
    for (int i = 0; i < updateListeners.size(); i++) {
      UpdateListener updateListener = (UpdateListener) updateListeners
          .elementAt(i);
      updateListener.actionPerformed();
    }
  }

  private void notifyErrorListeners(String code, String urlString) {
    for (int i = 0; i < errorListeners.size(); i++) {
      ErrorListener errorListener = (ErrorListener) errorListeners.elementAt(i);
      errorListener.errorOccurred(code, urlString);
    }
  }

  private boolean areMoreTracksRequired() {
    TrackDatabase trackDatabase = downloadContext.getTrackDatabase();
    int noOfRated = trackDatabase.getNoOfRated();
    int noOfUnrated = trackDatabase.getNoOfUnrated();
    int noOfUnratedOnPlaylist = trackDatabase.getNoOfUnratedOnPlaylist();
    return (noOfRated > 3 && noOfUnrated < noOfUnratedOnPlaylist)
        || noOfUnrated < 5;
  }

  /**
   * Get a resource string from the properties file associated with this class.
   * 
   * This class can't determine it's own package, since it extends thread. It
   * has to be stated explicitly.
   */
  private String getResourceString(String key) {
    return BaseResources.getString(LOCALE_RESOURCE_LOCATION, key);
  }

}

// Local Variables:
// c-file-style:"gnu"
// c-basic-offset:2
// indent-tabs-mode:nil
// tab-width:4
// End:
