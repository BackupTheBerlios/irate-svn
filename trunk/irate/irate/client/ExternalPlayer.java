// Copyright 2003 Anthony Jones

package irate.client;

import java.io.File;
import java.io.FileNotFoundException;

public ExternalPlayer extends Player {

  private String name;
  private String path;
  private boolean paused;
  
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
    throw new FileNotFoundException(msg);
  }

  public String getName() {
    return name;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
    process.destroy();
  }

  public boolean isPaused() {
    return paused;
  }

  public void play(File file) throws PlayerException {
    while (true) {
      process = Runtime.getRuntime().exec(new String[] { path, file.getPath() });
      try {
        process.waitFor();
        if (process.exitValue() != 0) 
          throw new Exception("extern player returned " + process.exitValue());
      }
      catch (InterruptedException e) {
        do { 
          if (process == null)
            return; 

          try {
            Thread.sleep(100);
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        } while (isPaused());
      }
      process = null;
      break;
    }
  }

  public void close() {
    if (process != null) {
      Process process = this.process;
      this.process = null;
      process.destroy();
    }
  }
}
