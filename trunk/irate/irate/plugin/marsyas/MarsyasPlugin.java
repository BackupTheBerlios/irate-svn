/**
 * Feature extraction plugin
 * 
 * @author Taras Glek
 * 
 */
package irate.plugin.marsyas;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import irate.plugin.*;
import irate.common.Track;
import nanoxml.*;
import irate.client.TrackLifeCycleListener;

public class MarsyasPlugin
extends Plugin
implements TrackLifeCycleListener
{
  private boolean initialized = false;
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
    check(track);
    lazyInit();
  }
  
  /**
   * Do big stuff late in the game
   */
  private void lazyInit() {
    //once everything is loaded, crunch through the trackdb
    if(initialized)
      return;
  	
    initialized = true;
  	/*Track t[] = getApp().getTracks();
  	for(int i=0;i<t.length;i++) {
  		check(t[i]); 			
  	}*/
  	
    getApp().addTrackAction(Resources.getString("find_similar"), (SelectionListener)new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        Track track = getApp().getSelectedTrack();
        /*if(track.getProperty("marsyas") == null) {
          	check(track);
            System.err.println("No features for "+track);
          	return;
        }*/
        
        new MarsyasSimilaritySearch(MarsyasPlugin.this, getApp().getUserName(), getApp().getTracks(), track);
      }
    });

  }

  /** Utility method to let other parts of the plugin play stuff */
  public void playTrack(Track t) {
    if(!getApp().getPlayingTrack().equals(t))
      getApp().playTrack(t);
  }
  
  void dbg(String msg) {
    System.err.println("MarsyasSimilaritySearch: "+msg);
  }
	
  /** CHeck if the track needs to be processed by marsyas */
  private void check(Track track)
  {
    if(0 == 0)
      return;
    //System.err.println("Checking "+track);
    // If we don't know how loud it is, queue it to be processed.
    if (track.getFile() != null && track.isRated() && track.getProperty("marsyas") == null) {
      //extract doesn't like files over 3MB
      if(track.getFile().length() > 3*1000*1000)
        return;
      MarsyasExtractor.processTrack(track);
    }
  }
  
  
}

