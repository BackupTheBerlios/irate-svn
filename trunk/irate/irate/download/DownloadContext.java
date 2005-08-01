/*
 * Created on 15/07/2005
 */
package irate.download;

import irate.common.Track;
import irate.common.TrackDatabase;

import java.util.List;
import java.util.Vector;

public class DownloadContext {

  private TrackDatabase trackDatabase;

  private TracksBeingDownloaded tracksBeingDownloaded = new TracksBeingDownloaded();

  private ExponentialBackoffManager exponentialBackoffManager = new ExponentialBackoffManager();

  private List downloadListeners = new Vector();

  public DownloadContext(TrackDatabase trackDatabase) {
    this.trackDatabase = trackDatabase;
  }

  /**
   * @return Returns the exponentialBackoffManager.
   */
  public ExponentialBackoffManager getExponentialBackoffManager() {
    return exponentialBackoffManager;
  }

  /**
   * @return Returns the trackDatabase.
   */
  public TrackDatabase getTrackDatabase() {
    return trackDatabase;
  }

  /**
   * @return Returns the tracksBeingDownloaded.
   */
  public TracksBeingDownloaded getTracksBeingDownloaded() {
    return tracksBeingDownloaded;
  }

  public boolean hasListeners() {
    return downloadListeners.size() != 0;
  }

  public void notifyDownloadStarted(Track track) {
    for (int i = 0; i < downloadListeners.size(); i++)
      ((DownloadListener) downloadListeners.get(i)).downloadStarted(track);
  }

  public void notifyDownloadProgressed(Track track, int percent, String state) {
    for (int i = 0; i < downloadListeners.size(); i++)
      ((DownloadListener) downloadListeners.get(i)).downloadProgressed(track,
          percent, state);
  }

  public void notifyDownloadFinished(Track track, boolean succeeded) {
    for (int i = 0; i < downloadListeners.size(); i++)
      ((DownloadListener) downloadListeners.get(i)).downloadFinished(track,
          succeeded);
  }

  public void notifyDownloadData(Track track, byte[] buf, int offset, int length) {
    for (int i = 0; i < downloadListeners.size(); i++)
      ((DownloadListener) downloadListeners.get(i)).downloadData(track, buf,
          offset, length);
  }

  public void addDownloadListener(DownloadListener downloadListener) {
    downloadListeners.add(downloadListener);
  }
  
  public void removeDownloadListener(DownloadListener downloadListener) {
    downloadListeners.remove(downloadListener);
  }

}
