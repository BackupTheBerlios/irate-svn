// Copyright 2003 Anthony Jones

package irate.client;

import java.io.File;

import javazoom.jlGui.BasicPlayer;
import javazoom.jlGui.BasicPlayerListener;

public class JavaLayerPlayer implements Player {

  private BasicPlayer player;
  private boolean paused;
  private int volume;  

  public JavaLayerPlayer() {
  }

  public String getName() {
    return "javalayer";
  }
  
  public void setPaused(boolean paused) {
    synchronized (this) {
      if (player != null && paused != this.paused)
        if (this.paused = paused)
          player.pausePlayback();
        else
          player.resumePlayback();
    }
  }

  public boolean isPaused() {
    synchronized (this) {
      return paused;
    }
  }
  
  public void setVolume(int volume) {
    this.volume = volume;
    if (player != null)
      setPlayerVolume(volume);
  }
  
  private void setPlayerVolume(int volume) {
    double minGainDB = player.getMinimum();
    double ampGainDB = ((10 / 20) * player.getMaximum()) - player.getMinimum();
    double cste = Math.log(10) / 20;
//  double valueDB = minGainDB + (1 / cste) * Math.log(1 + (Math.exp(cste * ampGainDB) - 1) * fGain);
    double gain = Math.exp((volume  - minGainDB) * cste) / (Math.exp(cste * ampGainDB) - 1);
//    System.out.println("Gain = " + gain);
    player.setGain((float) gain); 
  }

  public void play(File file) throws PlayerException {
    try {
      if (player == null)
        player = new BasicPlayer(new BasicPlayerListener() {
          public void updateCursor(int cursor, int total) {
          }
          public void updateMediaData(byte[] data) {
          }
          public void updateMediaState(String state) {
            if (state.equals("EOM")) {
              System.out.println("Done");
              synchronized (JavaLayerPlayer.this) { 
                JavaLayerPlayer.this.notify();
              }
            }
          }          
        });
      player.setDataSource(file);
      player.startPlayback();
      setPlayerVolume(volume);
      if (paused)
        player.pausePlayback();
        
      synchronized (this) {
        wait();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new PlayerException(e.toString());
    }
  }

  public void close() {
    synchronized (this) {
      if (player != null) {
        player.stopPlayback();
        synchronized (this) {
          notify();
        }
        paused = false;
      }
    }
  }

}
