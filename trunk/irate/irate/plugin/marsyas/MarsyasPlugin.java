/**
 * Feature extraction plugin
 * 
 * @author Taras Glek
 * 
 */
package irate.plugin.marsyas;

import irate.plugin.*;
import irate.common.Track;
import nanoxml.*;
import irate.client.TrackLifeCycleListener;
import irate.client.VolumePolicy;
import irate.download.DownloadListener;
import java.util.Hashtable;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MarsyasPlugin
  extends Plugin
  implements TrackLifeCycleListener
{
  /**
   * Get a short identifier for this Plugin.
   */
  public String getIdentifier()
  {
    return "marsyas";  // Used internally, don't internationalize
  }

  /**
   * Get a short description of this plugin.
   */
  public String getDescription()
  {
    return Resources.getString("description");
  }

  /**
   * Get a long description of this plugin, for tooltips
   */
  public String getLongDescription() 
  {
    return Resources.getString("long_description");
  }

  protected void doAttach()
  {
    getApp().addTrackLifeCycleListener(this);
    System.out.println("Hello there from Marsyas");
  }

  protected void doDetach()
  {
    getApp().removeTrackLifeCycleListener(this);
  }

  /**
   * Parse the configuration stored in the specified element.
   */
  public void parseConfig(XMLElement elt)
  {
  }

  /**
   * Format the configuration of this plugin by modifying the specified
   * element.
   */
  public void formatConfig(XMLElement elt)
  {
  }

  /**
   * Notification that the specified track has been added to the playlist.
   */
  public void addedToPlayList(Track track)
  {
      // Start calculating how loud it is if we don't already know.
    check(track);
  }

  /**
   * Notification that the specified track has been removed from the playlist.
   */
  public void removedFromPlayList(Track track)
  {
  }

  /**
   * Notification that the specified track is about to be played.
   */
  public void startingToPlay(Track track)
  {
      // Tell it to start calculating how loud it is if we don't already know.
      // If robo-jock is running, it will be calculating during robo-jock's
      // announcement.
    check(track);
  }

  private void check(Track track)
  {
  	System.err.println("Checking "+track);
      // If we don't know how loud it is, queue it to be processed.
  /*  if (track.getProperty("loudness") == null) {
      try {
        bgHowLoud.queue(track);
      }
      catch (FileNotFoundException e) {
        System.err.println("auto-normalize: Warning - File not found when trying to determine loudness of "+track);
      }
    }*/
  }

  
 }

