// Copyright 2003 Stephen Blackheath

package irate.plugin;

import java.io.File;

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
   * Skip to the next song.
   */
  public void skip();

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
  
}

