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

/**
 * @author Anthony Jones
 */
public abstract class AbstractClient implements UpdateListener, PluginApplication {
  
  protected TrackDatabase trackDatabase;
  protected PlayListManager playListManager;
  protected PlayerList playerList;
  protected PlayThread playThread;
  protected DownloadThread downloadThread;
  protected PluginManager pluginManager;
  
  public AbstractClient() {
    File home = new File(System.getProperties().getProperty("user.home"));

    // Check the current directory for an existing trackdatabase.xml for
    // compatibility reasons only.
    File dir = new File(".");
    File file = new File(dir, "trackdatabase.xml");
    if (!file.exists()) {
      dir = new File("/irate");
      file = new File(dir, "trackdatabase.xml");
      if (!file.exists()) {
        dir = new File(home, "irate");
        if (!dir.exists())
          dir.mkdir();
        file = new File(dir, "trackdatabase.xml");
      }
    }

    try {
      trackDatabase = new TrackDatabase(file);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    playerList = new PlayerList();
    playListManager = new PlayListManager(trackDatabase);
    playThread = new PlayThread(playListManager, playerList);

    pluginManager = new PluginManager(this, dir);

    if (playerList.getPlayers().length == 0)
      handleError(null, "help/missingplayer.html");

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
        if(downloadThread.getPercentComplete()==100)
          updateTrackTable();     
      }
    });
  }
  
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
		final Integer ratingInt = new Integer(rating);
			
		// Update the Track Rating
		track.setRating(ratingInt.intValue());
		 
		if (ratingInt.intValue() == 0 && track == getSelectedTrack()) {
			playThread.reject();
		}

	  // Save the database with the updated rating
		try {
			trackDatabase.save();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public abstract Track getSelectedTrack();
  public abstract void handleError(String code, String urlString);
  public abstract void setState(String state);
  public abstract void updateTrackTable();

}
