package irate.client;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import irate.common.*;

public class PlayThread extends Thread {
 
  private Track currentTrack;
  private Track nextTrack;
  private Player player;
  private PlayListManager playListManager;
  private Vector actionListeners = new Vector();
  private Process playerProcess;
  private String externalPlayer = "/usr/bin/mpg123";
  
  public PlayThread(PlayListManager playListManager) {
    this.playListManager = playListManager;
    if (!new File(externalPlayer).exists())
      externalPlayer = "";
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
    }
  }

  public void play(Track track)
  {
    nextTrack = track;
    reject();
  }
  
  private void playTrack() {
    try {
        // If a next track has been chosen by the user, use that, otherwise
	// pick one intelligently.
      currentTrack = nextTrack != null ? nextTrack : playListManager.chooseTrack();
      nextTrack = null;

      if (currentTrack != null) {
        notifyActionListeners();
        File file = currentTrack.getFile();
        if (file.exists()) {
          playFile(file);
          currentTrack.incNoOfTimesPlayed();
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

  public void reject() {
    if (externalPlayer.length() != 0) {
      if (playerProcess != null)
        playerProcess.destroy();
    }
    else {
      if (player != null)
        player.stop();
    }
  }

  public void setRating(int rating) {
    currentTrack.setRating(rating);
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
