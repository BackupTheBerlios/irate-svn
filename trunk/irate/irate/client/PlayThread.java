package irate.client;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import irate.common.*;
//import javazoom.jl.player.Player;

public class PlayThread extends Thread {
 
  private Track currentTrack;
  private Track nextTrack;
  private Player player;
  private PlayListManager playListManager;
  private Vector actionListeners = new Vector();
  private Process playerProcess;
  private String externalPlayer;
  private Speech speech = new Speech();
  private boolean speaking;
  private boolean toKeepPlaying;
  
  public PlayThread(PlayListManager playListManager) {
    this.playListManager = playListManager;
  }

  public boolean isSpeechSupported() {
    return speech.isSupported();
  }

  public void run() {
    while (true) {
      playTrack();
    }
  }

  private void playFile(File file) throws Exception {
    if (externalPlayer.length() != 0) {
      playerProcess = Runtime.getRuntime().exec(new String[] { externalPlayer, file.getPath() });
      try {
        playerProcess.waitFor();
        if (playerProcess.exitValue() != 0) 
          throw new Exception("extern player returned " + playerProcess.exitValue());
      }
      catch (InterruptedException e) {
      }
    }
    else {
      player = new Player(new BufferedInputStream(
          new FileInputStream(file), 2048));
      try {
	player.play();
      }
      finally {
	  // Without this, RoboJock can't talk because it fights with the
	  // Java player over the sound device.
	player.close();
      }
      player = null;
    }
  }

  public synchronized void play(Track track)
  {
    nextTrack = track;
    reject();
  }
  
  private void playTrack() {
    try {
      synchronized (this) {
	speaking = playListManager.getPlayList().isRoboJockEnabled();
	  // If a next track has been chosen by the user, use that, otherwise
	  // pick one intelligently.
	currentTrack = nextTrack != null ? nextTrack : playListManager.chooseTrack();
	toKeepPlaying = true;
	nextTrack = null;
      }

      if (currentTrack != null) {
          // Work out which player we're planning to use
        externalPlayer = playListManager.getPlayList().getPlayer();
        notifyActionListeners();
        File file = currentTrack.getFile();
        if (file.exists()) {
          if (speaking) {
            try {
              speech.say((currentTrack.isRated() ? "" : "Unrated. ") + currentTrack.getTitle() + " by " + currentTrack.getArtist());
            }
            catch (Exception e) {
              e.printStackTrace();
            }
	    finally {
	      speaking = false;
	    }
          }
	  if (toKeepPlaying) {
	    playFile(file);
            if (toKeepPlaying) 
              currentTrack.incNoOfTimesPlayed();
          }
        }
	else
	  speaking = false;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        sleep(500);
      }
      catch (InterruptedException e) {
      }
    }
  }

  public Track getCurrentTrack() {
    return currentTrack;
  }

  public synchronized void reject() {
      // Clear the toKeepPlaying flag to instruct the play thread to co-operatively stop.
    toKeepPlaying = false;
    if (externalPlayer.length() != 0) {
      if (playerProcess != null) 
        playerProcess.destroy();
    }
    else {
      if (player != null)
        player.close();
    }
    if (speech != null && speaking) 
      speech.abort();
  }

  public void setRating(int rating) {
    currentTrack.setRating(rating);
  }

  public void setPaused(boolean paused) {
    if (player != null)
      player.setPaused(paused);
  }

  public boolean isPaused() {
    if (player == null)
      return false;
    return player.isPaused();
  }

  public boolean isPauseSupported() {
    return externalPlayer.length() == 0;
  }

  public void addActionListener(ActionListener actionListener) {
    actionListeners.add(actionListener);
  }
  
  private void notifyActionListeners() {
    for (int i = 0; i < actionListeners.size(); i++) {
      ActionListener actionListener = (ActionListener) actionListeners.elementAt(i);
      actionListener.actionPerformed(null);
    }
  }
}
