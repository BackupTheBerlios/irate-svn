// Copyright 2003 Anthony Jones

package irate.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ExternalPlayer implements Player {

  private String name;
  private String path;
  private final int ACTION_PAUSE = 0;
  private final int ACTION_PLAY = 1;
  private final int ACTION_CLOSE = 2;
  private int action;
  private boolean paused;
  private Process process;
  
  public ExternalPlayer(String name, String path) throws FileNotFoundException {
    this(name, new String[] { path });
  }

  public ExternalPlayer(String name, String[] paths) throws FileNotFoundException {
    this.name = name;
    for (int i = 0; i < paths.length; i++) {
      if (new File(paths[i]).exists()) {
        this.path = paths[i];
        return;
      }
    }
    StringBuffer msg = new StringBuffer();
    for (int i = 0; i < paths.length; i++) {
      if (i != 0)
        msg.append(" ");
      msg.append(paths[i]);
    }
    throw new FileNotFoundException(msg.toString());
  }

  public String getName() {
    return name;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
    if (paused) {
      action = ACTION_PAUSE;
      process.destroy();
    }
  }

  public boolean isPaused() {
    return paused;
  }

  public void play(File file) throws PlayerException {
    do {
      try {
        action = ACTION_PLAY;
        process = Runtime.getRuntime().exec(new String[] { path, file.getPath() });
      }
      catch (IOException e) {
        e.printStackTrace();
        throw new PlayerException(e.toString());
      }
      try {
        process.waitFor();
        if (!paused && process.exitValue() != 0) 
          throw new PlayerException("extern player returned " + process.exitValue());
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
      while (paused) { 
        try {
          Thread.sleep(100);
        }
        catch (InterruptedException ie) {
          ie.printStackTrace();
        }
      }
      if (action == ACTION_CLOSE) 
        throw new PlayerException("extern player closed");
    } while (action != ACTION_PLAY);
  }

  public void close() {
    if (process != null) {
      action = ACTION_CLOSE;
      process.destroy();
    }
  }
}
