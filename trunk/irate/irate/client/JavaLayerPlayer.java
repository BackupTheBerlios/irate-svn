// Copyright 2003 Anthony Jones

package irate.client;

import java.io.*;
import javazoom.jl.decoder.JavaLayerException;

public class JavaLayerPlayer implements Player {

  private AudioDevice audio;
  private javazoom.jl.player.Player player;

  public JavaLayerPlayer() {
  }
  
  public void setPaused(boolean paused) {
    synchronized (this) {
      if (audio != null)
        audio.setPaused(paused);
    }
  }

  public boolean isPaused() {
    synchronized (this) {
      if (audio == null)
        return false;
      return audio.isPaused();
    }
  }

  public void play(File file) throws PlayerException {
    this.audio = new AudioDevice();
    try {
      player = new javazoom.jl.player.Player(new BufferedInputStream(
          new FileInputStream(file), 2048), audio);
      player.play();
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new PlayerException(e.toString());
    }
  }

  public void close() {
    synchronized (this) {
      if (player != null) {
        player.close();
        audio = null;
        player = null;
      }
    }
  }

}
