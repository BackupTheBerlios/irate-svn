package irate.client;

import java.io.InputStream;
import javazoom.jl.decoder.JavaLayerException;

public class Player extends javazoom.jl.player.Player {

  private AudioDevice audio;

  public Player(InputStream is, AudioDevice audio) throws JavaLayerException {
    super(is, audio);
    this.audio = audio;
  }
  
  public void setPaused(boolean paused) {
    audio.setPaused(paused);
  }

  public boolean isPaused() {
    return audio.isPaused();
  }

}
