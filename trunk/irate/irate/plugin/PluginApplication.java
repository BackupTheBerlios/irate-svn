package irate.plugin;

import java.io.File;

import irate.common.Track;
import irate.client.TrackLifeCycleListener;
import irate.download.DownloadListener;
import irate.client.VolumePolicy;

/**
 * This is the interface through which plugins talk to the application.
 *
 * @author Stephen Blackheath
 */
public interface PluginApplication
{
  /**
   * Get a factory that creates suitable UI objects, depending on the style of
   * user interface used in the application.
   */
  public PluginUIFactory getUIFactory();

  /**
   * Return true if music play is paused.
   */
  public boolean isPaused();

  /**
   * Pause or unpause music play.
   */
  public void setPaused(boolean paused);

  /**
   * Skip to the next or previous song.
   */
  public void skip(boolean reverse);

  /**
   * Get the track that is currently being played.
   */
  public Track getPlayingTrack();

  /**
   * Get the track that is currently selected.  In some implementations
   * this may be the same as the track that is playing.
   */
  public Track getSelectedTrack();

  /**
   * Set rating for the specified track.
   */
  public void setRating(Track track, int rating);

  /**
   * Plays a sound event on the client.
   */
  public void playSoundEvent(File file, String description);

  /**
   * Add a listener which allows plugins to monitor the lifecycle of tracks
   * through the application.
   */
  public void addTrackLifeCycleListener(TrackLifeCycleListener listener);

  /**
   * Remove a TrackLifeCycleListener.
   */
  public void removeTrackLifeCycleListener(TrackLifeCycleListener listener);

  /**
   * Add a listener that allows the plugin to monitor the downloading of files.
   */
  public void addDownloadListener(DownloadListener downloadListener);

  /**
   * Remove a download listener.
   */
  public void removeDownloadListener(DownloadListener downloadListener);

  /**
   * PluginApplication interface:
   * Save the information associated with the specified track.
   * If 'immediate' is true, it will save the track data immediately, otherwise
   * it will save it at some later stage.
   */
  public void saveTrack(Track track, boolean immediate);

  /** 
   * Get the complete list of tracks
   * @return array of tracks
   */
  public Track[] getTracks();
  /**
   * Add a policy for determining how loud tracks should be played.
   * See VolumeMeister class for more details.
   */
  public void addVolumePolicy(VolumePolicy policy, int priority);

  /**
   * Remove a policy for determining how loud tracks should be played.
   * See VolumeMeister class for more details.
   */
  public void removeVolumePolicy(VolumePolicy policy);

  /**
   * Add a status message.  The highest priority message will be the one that is
   * displayed.
   */
  public void addStatusMessage(int priority, String text);

  /**
   * Remove a status message added by addStatusMessage.  The passed string value must
   * be the same instance.
   */
  public void removeStatusMessage(String text);
}

