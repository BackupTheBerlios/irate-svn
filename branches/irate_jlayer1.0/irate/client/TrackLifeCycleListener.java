package irate.client;

import irate.common.Track;

/**
 * A listener that allows plugins to monitor the life cycle of tracks
 * throughout the application.
 *
 * @author Stephen Blackheath
 */
public interface TrackLifeCycleListener
{
  /**
   * Notification that the specified track has been added to the playlist.
   */
  public void addedToPlayList(Track track);

  /**
   * Notification that the specified track has been removed from the playlist.
   */
  public void removedFromPlayList(Track track);

  /**
   * Notification that the specified track is about to be played.
   */
  public void startingToPlay(Track track);
}
