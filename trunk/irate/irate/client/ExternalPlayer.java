// Copyright 2003 Anthony Jones

package irate.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExternalPlayer implements Player {

  private String name;
  private String path;
  private final int ACTION_PAUSE = 0;
  private final int ACTION_PLAY = 1;
  private final int ACTION_CLOSE = 2;
  private int action;
  private boolean paused;
  private int volume;
  private Process process;
  private long playTime;
  
  public ExternalPlayer(String name, String path) throws FileNotFoundException {
    this(name, new String[] { path });
  }

  /**
  @param name of the mp3 player. Used for display purposes
  @param paths list of possible locations/names of the player. paths[0] should be the executable name */
  public ExternalPlayer(String name, String[] paths) throws FileNotFoundException {
    this.name = name;
    for (int i = 0; i < paths.length; i++) {
      if (new File(paths[i]).exists()) {
        this.path = paths[i];
        return;
      }
    }
    //couldnt find player by probing
    //try using the first item in array
    try {
      Runtime.getRuntime().exec(paths[0]);
      path = paths[0];
      //if the above worked, we've located the mp3 player
      return;
    }
    catch (IOException ioe) {
      //too bad, couldn't find the player
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
    if (paused && process != null) {
      action = ACTION_PAUSE;
      process.destroy();
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void setVolume(int volume) {
    this.volume = volume;
    if (!isPaused()) {
      setPaused(true);
      setPaused(false);
    }
  }
  
  public int getVolume() {
    return volume;
  }

  public boolean isPaused() {
    return paused;
  }

  public String[] formatResumeArgument()
  {
    return new String[0];
  }
  
  public String[] formatVolumeArgument() {
    return new String[0];
  }

  /**
   * Get the number of milliseconds of play time so far for the song
   * currently being played.
   */
  protected long getPlayTime()
  {
    return playTime;
  }
  
  private String[] joinArray(String[][] a) {
    int length = 0;
    for (int i = 0; i < a.length; i++) 
      length += a[i].length;
    String[] r = new String[length];
    int dst = 0;
    for (int i = 0; i < a.length; i++) {
      System.arraycopy(a[i], 0, r, dst, a[i].length); 
      dst += a[i].length;
    }
    return r;
  } 

  public void play(File file) throws PlayerException {
    playTime = 0;
    do {
      try {
        action = ACTION_PLAY;
        String[] args = joinArray(new String[][] {
          new String[] { path },
          formatResumeArgument(),
          formatVolumeArgument(),
          new String[] { file.getPath() } 
        });
//        for (int i = 0 ; i < args.length; i++) 
//          System.out.print(args[i] + " ");
//        System.out.println();
        process = Runtime.getRuntime().exec(args);
        route(process.getInputStream(), System.out, "player out");
        route(process.getErrorStream(), System.out, "player err");
      }
      catch (IOException e) {
        e.printStackTrace();
        throw new PlayerException(e.toString());
      }
      long start = System.currentTimeMillis();
      try {
        process.waitFor();
        if (!paused && process.exitValue() != 0)
          throw new PlayerException(
            "extern player returned " + process.exitValue());
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (paused) {
        long timePlayed = System.currentTimeMillis() - start;
        playTime += timePlayed;
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
    }
    while (action != ACTION_PLAY);
  }

  public void close() {
    if (process != null) {
      action = ACTION_CLOSE;
      process.destroy();
    }
  }

  public void route(final InputStream is, final OutputStream os, String title) {
    new Thread(title) {
      public void run() {
        try {
          byte[] buf = new byte[128];
          while (true) {
            int avail = is.available();
            if (avail == 0)
              avail = 1;
            else 
              if (avail > buf.length)
                avail = buf.length;
            int nbytes = is.read(buf, 0, avail);
            if (nbytes < 0)
              break;
            if (nbytes != 0) {
              os.write(buf, 0, nbytes);
              os.flush();
            }
          } 
        } 
        catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }.start();
  }
}
