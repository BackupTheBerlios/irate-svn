// Copyright 2003 Anthony Jones

package irate.client;

import java.io.File;

public interface Player {

  public String getName();

  public void setPaused(boolean paused);

  public boolean isPaused();

  public void play(File file) throws PlayerException;
  
  public void setVolume(int volume);

  public void close();
  
  /** Add a listener which gets called when something changes in the track play
   * (including the once per second position update). */
  public void addPlayerListener(PlayerListener playerListener);
  
}
