// Copyright 2003 Anthony Jones, Taras 

package irate.client;

import irate.common.Track;
import irate.common.UpdateListener;

import java.io.*;
import java.util.*;

public class PlayThread extends Thread {
 
  private Track currentTrack;
  private Track nextTrack;
  private Player player;
  private PlayerList playerList;
  private PlayListManager playListManager;
  private Vector updateListeners = new Vector();
  private Speech speech = new Speech();
  private boolean speaking;
  private boolean toKeepPlaying;
  Vector history = new Vector();
  private boolean reverse = false;
	
  public PlayThread(PlayListManager playListManager, PlayerList playerList) {
    this.playListManager = playListManager;
    this.playerList = playerList;
  }

  public boolean isSpeechSupported() {
    return speech.isSupported();
  }

  public void run() {
    while (true) {
      playTrack();
    }
  }

  private void playFile(File file, int volume) throws Exception {
    player = playerList.getPlayer(playListManager.getTrackDatabase().getPlayer());
    try {
      player.setVolume(volume);
      player.play(file);
    }
    catch (PlayerException e) {
//      e.printStackTrace();
    }
    finally {
        // Without this, RoboJock can't talk because it fights with the
        // Java player over the sound device.
      player.close();
    }
  }

  public synchronized void play(Track track) {
    nextTrack = track;
    reject();
  }
  
  private void playTrack() {
    try {
      synchronized (this) {
        speaking = playListManager.getTrackDatabase().isRoboJockEnabled();
        // If a next track has been chosen by the user, use that, otherwise
        // pick one intelligently.
        currentTrack =
          nextTrack != null ? nextTrack : playListManager.chooseTrack();
        toKeepPlaying = true;
        nextTrack = null;
      }

      if (currentTrack != null) {
        notifyUpdateListeners();
        File file = currentTrack.getFile();
        if (file != null && file.exists()) {
          if (speaking) {
            try {
              speech.say(
                (currentTrack.isRated() ? "" : "Unrated. ")
                  + currentTrack.getTitle()
                  + " by "
                  + currentTrack.getArtist());
            }
            catch (Exception e) {
              e.printStackTrace();
            }
            finally {
              speaking = false;
            }
          }
          if (toKeepPlaying) {
            
            playFile(file, currentTrack.getVolume());
            if(!reverse){
              history.add(currentTrack);
              //System.out.println("Added to history:"+currentTrack+" reverse="+reverse);
            }else
              this.reverse = false;
            

            if (toKeepPlaying) {
              playListManager.getTrackDatabase().incNoOfPlays();
              currentTrack.incNoOfTimesPlayed();
            }
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
  
  /**
  Skips to the next song
  @param reverse indicates whether we should go back in history
  */
  private void skipSong(boolean reverse) {
      // It must not be paused if you want to reject a track.
    setPaused(false);
   
		//record current track in history
		this.reverse = reverse;
    if(reverse) {
      if(hasHistory()) {
        nextTrack = (Track)history.elementAt(history.size()-1);
        history.removeElementAt(history.size()-1);
      }
      else
        return;
    }
      // Clear the toKeepPlaying flag to instruct the play thread to co-operatively stop.
    toKeepPlaying = false;
    if (player != null)
      player.close();

      // Stop RoboJock if it's talking.
    if (speech != null && speaking) 
      speech.abort();
  }

	public synchronized void reject(){
    skipSong(false);			
	}

  /** Play the previous song 
  @returns false if we are at the begining */
	public synchronized boolean goBack(){
    if(!hasHistory())
      return false;
    skipSong(true);			
    return true;
	}
	
	public boolean hasHistory() {
		return history.size()!=0;
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
  
  public synchronized void setVolume(int volume) {
    currentTrack.setVolume(volume);
    player.setVolume(volume);
  }

  public void addUpdateListener(UpdateListener updateListener) {
    updateListeners.add(updateListener);
  }
  
  private void notifyUpdateListeners() {
    for (int i = 0; i < updateListeners.size(); i++) {
      UpdateListener updateListener = (UpdateListener) updateListeners.elementAt(i);
      updateListener.actionPerformed();
    }
  }
}
