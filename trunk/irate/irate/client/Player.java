package irate.client;

import java.io.File;

public interface Player {

  public void setPaused(boolean paused);

  public boolean isPaused();

  public void play(File file) throws PlayerException;

  public void close();
}
