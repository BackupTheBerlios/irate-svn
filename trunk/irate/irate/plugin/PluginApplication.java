// Copyright 2003 Stephen Blackheath

package irate.plugin;

import irate.common.Track;

/**
 * This is the interface through which plugins talk to the application.
 *
 * Some things we could add:
 *   Event registration for plugins that want to 
 *
 * @author Stephen Blackheath
 */
public interface PluginApplication
{
  /**
   * Return true if music play is paused.
   */
  public boolean isPaused();

  /**
   * Pause or unpause music play.
   */
  public void setPaused(boolean paused);

  /**
   * Skip to the next song.
   */
  public void skip();

  /**
   * Get the track that is currently being played.
   */
  public Track getPlayingTrack();

  /**
   * PluginApplication interface:
   * Get the track that is currently selected.
   */
  public Track getSelectedTrack();

  /**
   * Set rating for the specified track.
   */
  public void setRating(Track track, int rating);
}

