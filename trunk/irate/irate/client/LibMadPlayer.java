/*
 * Created on 4-Dec-2003
*/
package irate.client;

import java.io.File;

import MAD.MadEvent;
import MAD.MadListener;
import MAD.MadPlayer;


public class LibMadPlayer extends AbstractPlayer implements MadListener {

  MadPlayer player;
  File currentFile;
  private int cachedVolume;
  
  public LibMadPlayer() {
    super();
    player = new MadPlayer(); 
    player.addListener(this);
  }
  

  public String getName() {
    return "LibMad";
  }

  public void setPaused(boolean paused) {
    player.pause();
  }


  public boolean isPaused() {
    return player.isPaused();
  }

  public void play(File file) throws PlayerException {
    currentFile = file;
    player.start(currentFile.getAbsolutePath(),this.cachedVolume);
  }
  
  public void setVolume(int volume) {
     cachedVolume = volume;
     player.setVolume(volume);
  }


  public void close() {
    player.stop();
  }


  public void actionPerformed(MadEvent me) {
    this.notifyPosition(me.getTimeSeconds(), 0);
  }


}
