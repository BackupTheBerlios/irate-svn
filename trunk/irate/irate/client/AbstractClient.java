/*
 * Created on Sep 29, 2003
 */
package irate.client;

import irate.common.DiskControl;
import irate.common.Preferences;
import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.UpdateListener;
import irate.download.DownloadListener;
import irate.download.DownloadThread;
import irate.plugin.Plugin;
import irate.plugin.PluginApplication;
import irate.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * @author Anthony Jones
 */
public abstract class AbstractClient
  implements UpdateListener, PlayerListener, PluginApplication {

  protected TrackDatabase trackDatabase;
  protected PlayListManager playListManager;
  protected PlayerList playerList;
  protected PlayThread playThread;
  protected DownloadThread downloadThread;
  protected PluginManager pluginManager;
  protected Preferences userPreferences;
  protected SoundEventPlayer soundEventPlayer;

  private Track lastRatedTrack;
  private int lastTrackPreviousRank;

  private Vector trackLifeCycleListeners = new Vector();

  private static class StatusMessage {
    public StatusMessage(int priority, String text) {this.priority = priority; this.text = text;}
    public int priority;
    public String text;
  };
  private Vector statusMessages = new Vector();

  public AbstractClient() {
		init();
    userPreferences = new Preferences();

    lastRatedTrack = null;
    lastTrackPreviousRank = -1;

    File home = new File(System.getProperties().getProperty("user.home"));
    File dir = null;
    File file = null;
    File downloadDir = null;

    // Check to see if the iRATE directory exists in the user's home.
    // This needs to be there.
    dir = Preferences.getPrefsDirectory();
    if (!dir.exists()) {
      dir.mkdir();
    }

    // Check to see if the user has a track database directory set in the
    // irate.xml file.  If so, this directory should point to the trackdatabase.xml
    // file.
    String preference = Preferences.getUserDownloadDirectoryPreference();
    if (preference != null) {
      file = new File(preference);
      downloadDir = new File(file.getParentFile(), "download/");
      
    }
    else {
      // If they don't have one set, fall back on the home directory.
      // If it doesn't exist in either location, then the user will need to fill in the
      // registration information.
      downloadDir = new File(dir, "download/");
      file = new File(dir, "trackdatabase.xml");
    }

    try {
      trackDatabase = new TrackDatabase(file);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    playerList = new PlayerList();

    // Add the client as a player listener on each player.
    // If this doesn't make sense in the future, we'll have to look
    // at doing this another way.
    for (int i = 0; i < playerList.getPlayers().length; ++i) {
      playerList.getPlayers()[i].addPlayerListener(this);
    }

    playListManager = new PlayListManager(trackDatabase);
    playThread = new PlayThread(playListManager, playerList);
    soundEventPlayer  = new SoundEventPlayer(playListManager);

    if (playerList.getPlayers().length == 0)
      handleError(null, "missingplayer.html");

    playThread.addUpdateListener(this);

    downloadThread = new DownloadThread(trackDatabase) {
      public void process() throws IOException {
        super.process();
        // perhapsDisableAccount();
      }

      public void handleError(String code, String urlString) {
        AbstractClient.this.handleError(code, urlString);
      }
    };

    downloadThread.addDownloadListener(new DownloadListener() {
      public void downloadStarted(Track track) {}
      public void downloadProgressed(Track track, int percentComplete, String state) {
        updateDownloadInfo(track, state, percentComplete);
      }
      public void downloadData(Track track, byte[] buffer, int offset, int length) {}
      public void downloadFinished(Track track, boolean succeeded) {}
    });

    pluginManager = new PluginManager(this, dir);

    System.out.println("Number of tracks "+trackDatabase.getNoOfTracks() );
		// If a track database couldn't be loaded from the file system, then we
    // need to create a new account.
    if (trackDatabase.getNoOfTracks() == 0) {
      createNewAccount();
    }
    
    
    // Deal with some file cleanup here -- if the user has the maximum disk amount
    // set then we need to mark some files to be deleted.
    String downloadLimit = Preferences.getUserPreference("downloadLimit");
    if (downloadLimit != null) {
        DiskControl dc = new DiskControl(downloadDir, trackDatabase);
        dc.clearDiskSpace(new Integer(downloadLimit).intValue()); 
    }
    
    
  }
	
	/** Called from AbstractClient's constructor so we can intialize 
	the gui in a sensible way. Needed for stuff like errors before 
	main gui is up*/
	public abstract void init();

  /**
   * PluginApplication interface:
   * Get the track that is currently being played.
   */
  public Track getPlayingTrack() {
    return playThread.getCurrentTrack();
  }

  /**
   * PluginApplication interface:
   * Set rating for the specified track.
   */
  public void setRating(final Track track, int rating) {
    lastRatedTrack = track;
    lastTrackPreviousRank = -1;
    if (track.isRated())
      lastTrackPreviousRank = (int) track.getRating();

    // Update the Track Rating
    track.setRating(rating);

    if (rating == 0 && track == getSelectedTrack()) {
      playThread.reject();
    }

    // Save the database with the updated rating
    try {
      trackDatabase.save();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    // Send update to all plugins
    List plugins = pluginManager.getPlugins();
    for (int i = 0; i < plugins.size(); i++) {
      Plugin plugin = (Plugin) plugins.get(i);
      plugin.eventRatingApplied(track, rating);
    }

    updateTrack(track);
  }

  /**
   * Switch the last track that was ranked back to its previous ranking.
   */
  public void undoLastRating() {

    if (lastRatedTrack == null) {
      return;
    }

    if (lastTrackPreviousRank != -1) {
      lastRatedTrack.setRating(lastTrackPreviousRank);
    }
    else {
      lastRatedTrack.unSetRating();
    }

    //  Save the database with the updated rating
    try {
      trackDatabase.save();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    updateTrack(lastRatedTrack);
  }

  public void setVolume(final int volume) {

    final Integer volumeInt = new Integer(volume);

    playThread.setVolume(volumeInt.intValue());

    //save the updated volume
    try {
      trackDatabase.save();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * PluginApplication interface:
   * Return true if music play is paused.
   */
  public boolean isPaused() {
    return playThread.isPaused();
  }

  /**
   * PluginApplication interface:
   * Pause or unpause music play.
   */
  public void setPaused(boolean paused) {
    playThread.setPaused(paused);
  }

  /**
   * PluginApplication interface:
   * Skip to the next song.
   */
  public void skip() {
    skip(false);
  }

  public void skip(boolean reverse) {
    setPaused(false);
    if (!reverse) {
      playThread.reject();
      downloadThread.checkAutoDownload();
    }
  }

  public void quit() {
    trackDatabase.purge();
    try {
      trackDatabase.save();
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
    playThread.shutdown();
    System.exit(0);
  }

  protected abstract void createNewAccount();
  public abstract Track getSelectedTrack();
  public abstract void handleError(String code, String urlString);
  public abstract void updateDownloadInfo(Track track, String state, int percentageDone);

  /** Update the display of all tracks. */
  public abstract void updateTrackTable();

  /** Update the display of only one track. */
  public abstract void updateTrack(Track track);
  
  /**
   * PluginApplication interface:
   * Plays a sound event on the client.
   */
  public void playSoundEvent(File file, String description) {
    System.out.println("Trying to play sound event (" + description + ")"); 

    if (soundEventPlayer == null)
      System.out.println("No SoundEventPlayer found!");
    else {
      try {
        soundEventPlayer.PlaySoundEvent(file);
      }
      catch (PlayerException e) {
        System.out.println("Exception playing sound event: " + file.getPath());
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Called by PlayThread when a new track is played.
   */
  public void newTrackStarted(Track track) {
    // Send event to all plugins
    List plugins = pluginManager.getPlugins();
    for (int i = 0; i < plugins.size(); i++) {
      Plugin plugin = (Plugin) plugins.get(i);
      plugin.eventNewTrack(track);
    } 
    notifyStartingToPlay(track);
  }

  /**
   * PluginApplication interface:
   * Add a listener which allows plugins to monitor the lifecycle of tracks
   * through the application.
   */
  public void addTrackLifeCycleListener(TrackLifeCycleListener listener)
  {
    trackLifeCycleListeners.add(listener);
    playListManager.addTrackLifeCycleListener(listener);
  }

  /**
   * PluginApplication interface:
   * Remove a TrackLifeCycleListener.
   */
  public void removeTrackLifeCycleListener(TrackLifeCycleListener listener)
  {
    trackLifeCycleListeners.remove(listener);
    playListManager.removeTrackLifeCycleListener(listener);
  }

  private void notifyStartingToPlay(Track track)
  {
    for (int i = 0; i < trackLifeCycleListeners.size(); i++)
      ((TrackLifeCycleListener)trackLifeCycleListeners.get(i)).startingToPlay(track);
  }

  /**
   * PluginApplication interface:
   * Add a listener that allows the plugin to monitor the downloading of files.
   */
  public void addDownloadListener(DownloadListener listener)
  {
    downloadThread.addDownloadListener(listener);
  }

  /**
   * PluginApplication interface:
   * Remove a download listener.
   */
  public void removeDownloadListener(DownloadListener listener)
  {
    downloadThread.removeDownloadListener(listener);
  }

  /**
   * PluginApplication interface:
   * Save the information associated with the specified track.
   * If 'immediate' is true, it will save the track data immediately, otherwise
   * it will save it at some later stage.
   */
  public void saveTrack(Track track, boolean immediate)
  {
    // Be lazy for now:  The information will be saved fairly soon because the whole
    // track database gets saved any time anyone rates something.
    // If written properly, this method needs to be written CAREFULLY because it could
    // be called from any thread.
  }

  /**
   * PluginApplication interface:
   * Add a policy for determining how loud tracks should be played.
   * See VolumeMeister class for more details.
   */
  public void addVolumePolicy(VolumePolicy policy, int priority)
  {
    playThread.getVolumeMeister().addVolumePolicy(policy, priority);
  }

  /**
   * PluginApplication interface:
   * Remove a policy for determining how loud tracks should be played.
   * See VolumeMeister class for more details.
   */
  public void removeVolumePolicy(VolumePolicy policy)
  {
    playThread.getVolumeMeister().removeVolumePolicy(policy);
  }

  /**
   * PluginApplication interface:
   * Add a status message.  The highest priority message will be the one that is
   * displayed.
   */
  public void addStatusMessage(int priority, String text)
  {
    if (text != null) {
      synchronized (statusMessages) {
        statusMessages.add(new StatusMessage(priority, text));
      }
      updateStatusMessage();
    }
  }

  /**
   * PluginApplication interface:
   * Remove a status message added by addStatusMessage.  The passed string value must
   * be the same instance.
   */
  public void removeStatusMessage(String text)
  {
    if (text != null) {
      synchronized (statusMessages) {
        for (int i = 0; i < statusMessages.size(); i++) {
          StatusMessage sm = (StatusMessage) statusMessages.get(i);
          if (text == sm.text) {
            statusMessages.remove(i);
            break;
          }
        }
      }
      updateStatusMessage();
    }
  }

  /**
   * Out of all the status messages that are competing to be displayed (added through
   * addStatusMessage/removeStatusMessage), get the highest priority one.
   */
  protected String getHighestPriorityStatusMessage()
  {
    synchronized (statusMessages) {
      int highestPriority = 0;
      String text = null;
      for (int i = 0; i < statusMessages.size(); i++) {
        StatusMessage sm = (StatusMessage) statusMessages.get(i);
        if (i ==0 || sm.priority > highestPriority) {
          highestPriority = sm.priority;
          text = sm.text;
        }
      }
      return text;
    }
  }

  /**
   * Instance must supply a method here to update the display of the status message.
   * It should call 'getHighestPriorityStatusMessage'.
   */
  protected abstract void updateStatusMessage();

  /**
   * For controlling some platform-specific behavior
   */
  public static boolean isMac() {
    return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
  }
}
