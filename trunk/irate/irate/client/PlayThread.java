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
  private boolean playing;
  
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
      player.play();
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
	  // If a next track has been chosen by the user, use that, otherwise
	  // pick one intelligently.
	currentTrack = nextTrack != null ? nextTrack : playListManager.chooseTrack();
	playing = true;
	nextTrack = null;
      }

      if (currentTrack != null) {
          // Work out which player we're planning to use
        externalPlayer = playListManager.getPlayList().getPlayer();
        notifyActionListeners();
        File file = currentTrack.getFile();
        if (file.exists()) {
          if (playListManager.getPlayList().isRoboJockEnabled()) {
            try {
              speech.say((currentTrack.isRated() ? "" : "Unrated. ") + currentTrack.getTitle() + " by " + currentTrack.getArtist());
            }
            catch (Exception e) {
              e.printStackTrace();
            }
          }
	  if (playing) {
	    playFile(file);
            if (playing) 
              currentTrack.incNoOfTimesPlayed();
          }
        }
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
      // Clear the playing flag to instruct the play thread to co-operatively stop.
    playing = false;
    if (externalPlayer.length() != 0) {
      if (playerProcess != null) 
        playerProcess.destroy();
    }
    else {
      if (player != null)
        player.close();
    }
    if (speech != null) 
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
