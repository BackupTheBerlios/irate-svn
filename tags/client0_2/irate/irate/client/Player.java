// Copyright 2003 Anthony Jones

package irate.client;

import java.io.File;

public interface Player {

  public String getName();

  public void setPaused(boolean paused);

  public boolean isPaused();

  public void play(File file) throws PlayerException;

  public void close();
}
