package irate.plugin.unratednotifier;

import java.io.*;
import irate.plugin.*;
import irate.resources.BaseResources;
import nanoxml.*;
import irate.common.Track;
import java.util.Vector;

/**
 * Date Created: Feb 13, 2004
 * Date Updated: $Date: 2004/02/21 04:44:29 $
 * @author Creator: Mathieu Mallet
 * @author Updated: $Author: emh_mark3 $
 * @version $Revision: 1.2 $ */

public class UnratedNotifierPlugin extends Plugin {
  private PluginApplication app;
  private int unratedNotificationMode = 0;
  private boolean bPlayedNotificationSoundAlready = false;
  private Vector temporaryFiles;

  public UnratedNotifierPlugin() {
    temporaryFiles = new Vector();
  }

  /**
   * Attaches to the running application
   */
  protected synchronized void doAttach() {
    app = getApp();
  }

  /**
   * Detaches from the running application
   */
  protected synchronized void doDetach() {
    app = null;
  }

  /**
   * Parses the configuration provided to allow the plugin to be set up
   */
  public void parseConfig(XMLElement elt) {
    unratedNotificationMode = elt.getIntAttribute("unratedNotificationMode", unratedNotificationMode);
  }
  
  /**
   * Formats the configuration of the plugin by modifying the element
   */
  public void formatConfig(XMLElement elt) {
    elt.setIntAttribute("unratedNotificationMode", unratedNotificationMode);
  }  
  
  /**
   * Gets a short description of this plugin.
   */
  public String getDescription() {
    return "Unrated Tracks Notifier";
  }

  /**
   * Gets a long description of this plugin, for tooltips.
   */
  public String getLongDescription() {
    return "This plugin allows iRATE to play a sound event whenever an unrated track is playing.";
  }

  /**
   * Gets a short identifier for this plugin.
   */
  public String getIdentifier() {
    return "unrated-notifier";
  }
  
  public void setNotificationMode(int mode) {
    unratedNotificationMode = mode;
  }
  
  public int getNotificationMode() {
    return unratedNotificationMode;
  }
  
  public void eventNewTrack(Track track) {
    // New track -- reset notifier
    bPlayedNotificationSoundAlready = false;
  }  

  public void eventPositionUpdated(int position, int length) {
    if (app == null)
      return;   // don't play notification sound if plugin isn't attached (not that we could anyways)
    
    if (!app.getPlayingTrack().isRated() && !bPlayedNotificationSoundAlready) {
      switch (unratedNotificationMode) {
        case 1: // play notification sound at beginning of track
          playNotificationSound();
          bPlayedNotificationSoundAlready = true;
          break;
    
        case 2: // play notification sound at end of track
          if (position > length - 1) {
            playNotificationSound();
            bPlayedNotificationSoundAlready = true;
          }
          break;
  
        case 3: // play notification sound 30 seconds before end of track
          if (position > length - 30) {
            playNotificationSound();
            bPlayedNotificationSoundAlready = true;
          }
          break; 
      }
    }  
  
  }
  
  private void playNotificationSound() {
    //app.playSoundEvent(new File("irate/resources/notify_sound.mp3"), "Unrated track playing.");
    try {
      app.playSoundEvent(BaseResources.getResourceAsFile("notify_sound.mp3", temporaryFiles),
        "Unrated track playing.");
    }
    catch (IOException e) {
      e.printStackTrace();
    }    
  }
 
}
