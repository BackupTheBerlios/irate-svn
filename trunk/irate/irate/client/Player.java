package irate.client;

import java.io.InputStream;
import javazoom.jl.decoder.JavaLayerException;

public class Player extends javazoom.jl.player.Player {

  private boolean paused;

  public Player(InputStream is) throws JavaLayerException {
    super(is);
  }
  
  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  public boolean isPaused() {
    return paused;
  }
  
  public boolean play(int frames) throws JavaLayerException {
    for (int i = 0; i < frames; i++) {
      while (paused) {
        try {
          Thread.sleep(500);
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      
      if (!decodeFrame()) {
        super.play(0);
        return false;
      }
    }
    return true;
  }
}
