// Copyright 2003 Anthony Jones, Taras Glek

package irate.download;

import irate.common.ErrorListener;
import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.UpdateListener;
import irate.common.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class DownloadThread extends Thread {

  /** The maximum number of files that can be downloaded simultaneously */
  private static final int MAX_SIMULTANEOUS_DOWNLOADS = 6;

  /**
   * The minimum number of files to be downloaded simultaneously before
   * contacting the server again
   */
  private static final int MIN_SIMULTANEOUS_DOWNLOADS = 1;

  private Vector updateListeners = new Vector();

  private Vector errorListeners = new Vector();

  private File downloadDir;

  private String state;

  private DownloadContext downloadContext;
  
  private RemoteServer server = new RemoteClassicServer() {
    public void setState(String state) {
      DownloadThread.this.state = state;
      notifyUpdateListeners();
    }
  };
  
  private RemoteServer buddy = new RemoteBuddyServer();

  public DownloadThread(TrackDatabase trackDatabase) {

    downloadContext = new DownloadContext(trackDatabase);

    downloadDir = trackDatabase.getDownloadDirectory();
    if (!downloadDir.exists()) downloadDir.mkdir();
    setDaemon(true);
  }

  public void run() {
    try {
      buddy.contactServer(downloadContext.getTrackDatabase());
    }
    catch (DownloadException de) {
      de.printStackTrace();
    }
    
    try {
      while (true) {
        synchronized (this) {
          wait();
        }

        try {
          downloadPendingTracks();
          if (downloadContext.getTracksBeingDownloaded().size() < MIN_SIMULTANEOUS_DOWNLOADS
              && areMoreTracksRequired()) {
            contactServer(downloadContext.getTrackDatabase());
            downloadPendingTracks();
          }
        }
        catch (IOException ioe) {
          ioe.printStackTrace();
        }
        catch (DownloadException e) {
          Thread.sleep(90000);
        }
      }
    }
    catch (InterruptedException ie) {
      ie.printStackTrace();
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

  public void contactServer(TrackDatabase trackDatabase) throws DownloadException {
    try {
      server.contactServer(trackDatabase);
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

  private void notifyUpdateListeners() {
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

}

// Local Variables:
// c-file-style:"gnu"
// c-basic-offset:2
// indent-tabs-mode:nil
// tab-width:4
// End:
