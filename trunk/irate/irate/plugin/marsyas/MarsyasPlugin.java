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

public class MarsyasPlugin
  extends Plugin
  implements TrackLifeCycleListener
{
  private boolean scannedTracks = false;
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
    
    //once everything is loaded, crunch through the trackdb
    if(!scannedTracks) {
    	Track t[] = getApp().getTracks();
    	for(int i=0;i<t.length;i++) {
    		check(t[i]); 			
    	}
    }
  }

  /** CHeck if the track needs to be processed by marsyas */
  private void check(Track track)
  {
  	//System.err.println("Checking "+track);
      // If we don't know how loud it is, queue it to be processed.
    if (track.getFile() != null && track.isRated() && track.getProperty("marsyas") == null) {
      MarsyasExtractor.processTrack(track);
    }
  }

  
 }

