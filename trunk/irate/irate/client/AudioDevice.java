// Copyright 2003 Anthony Jones

package irate.client;

import javax.sound.sampled.SourceDataLine;
import javazoom.jl.decoder.JavaLayerException;

public class AudioDevice extends JavaSoundAudioDevice {

  private boolean paused;
  
  public AudioDevice() {
  }
  
  public void setPaused(boolean paused) {
    if (paused != this.paused) {
      this.paused = paused;
      if (paused)
        source.stop();
      else
        source.start();
    }
  }

  public boolean isPaused() {
    return paused;
  }
  
  protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException {
    if (source == null)
      createSource();

    byte[] b = toByteArray(samples, offs, len);

    int nbytes = len * 2;
    int pos = 0;
    while (true) {
      pos += source.write(b, pos, nbytes - pos);

      if (pos == nbytes || !paused)
        break;
      
      try {
        Thread.sleep(250);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
}
