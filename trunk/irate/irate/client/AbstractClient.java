/*
 * Created on Sep 29, 2003
 */
package irate.client;

import java.io.File;
import java.io.IOException;

import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.UpdateListener;
import irate.download.DownloadThread;
import irate.plugin.PluginApplication;
import irate.plugin.PluginManager;
import irate.common.Preferences;

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

  private Track lastRatedTrack;
  private int lastTrackPreviousRank;

  public AbstractClient() {
		init();
    userPreferences = new Preferences();

    lastRatedTrack = null;
    lastTrackPreviousRank = -1;

    File home = new File(System.getProperties().getProperty("user.home"));
    File dir = null;
    File file = null;

    // Check to see if the iRATE directory exists in the user's home.
    // This needs to be there.
    dir = new File(home, "/irate");
    if (!dir.exists()) {
      dir.mkdir();
    }

    // Check to see if the user has a track database directory set in the
    // irate.xml file.  If so, this directory should point to the trackdatabase.xml
    // file.
    String preference = Preferences.getUserDownloadDirectoryPreference();
    if (preference != null) {
      file = new File(preference);
    }
    else {
      // If they don't have one set, fall back on the home directory.
      // If it doesn't exist in either location, then the user will need to fill in the
      // registration information.
      dir = new File(home, "irate");
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

    pluginManager = new PluginManager(this, dir);

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

    downloadThread.addUpdateListener(new UpdateListener() {
      boolean newState = false;
      public void actionPerformed() {
        setState(downloadThread.getState());
        if (downloadThread.getPercentComplete() == 100)
          updateTrackTable();
      }
    });

    System.out.println("Number of tracks "+trackDatabase.getNoOfTracks() );
		// If a track database couldn't be loaded from the file system, then we
    // need to create a new account.
    if (trackDatabase.getNoOfTracks() == 0) {
      createNewAccount();
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
    playThread.reject();
  }

  protected abstract void createNewAccount();
  public abstract Track getSelectedTrack();
  public abstract void handleError(String code, String urlString);
  public abstract void setState(String state);

  /** Update the display of all tracks. */
  public abstract void updateTrackTable();

  /** Update the display of only one track. */
  public abstract void updateTrack(Track track);

}
