// Copyright 2003 Anthony Jones

package irate.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ExternalPlayer extends AbstractPlayer {

  /** Fudge factor (in ms) that we add to the playing time when restarting 
   * a track.  Compensates for time spent starting and stopping the player. */
  private static final long TIME_PLAYED_FUDGE_FACTOR_MS = 150;

  private String path;
  private boolean playing = false;
  private boolean paused;
  private int volume;
  private Process process;
  private long playTime;


  /** Thread from which we are running the external player application.  Be
   * sure to syncronize access to this field. */
  private Thread playingThread = null;
  synchronized Thread getPlayingThread() { return playingThread; }
  synchronized void setPlayingThread(Thread val) { playingThread = val; }

  /** Set to TRUE if the state of this object has changed since we last
   * invoked an external player process.  */
  private boolean stateDirty = true;

  public ExternalPlayer(String path) throws FileNotFoundException {
    this(new String[] { path });
  }

  /**
  @param name of the mp3 player. Used for display purposes
  @param paths list of possible locations/names of the player. paths[0] should be the executable name */
  public ExternalPlayer(String[] paths) throws FileNotFoundException {
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

  /** Notify the player thread (if it exists) of any state changes. */
  private void notifyPlayThread() {
    synchronized(this) {
      stateDirty = true;
    }
    // Make a copy, in case the player thread decides to go away.
    Thread pt = getPlayingThread();
    if (pt != null) {
      // We synchronize here to make sure that we don't interrupt the player
      // thread at an inconvenient time for it.
      synchronized(this) {
        pt.interrupt();
      }
    }

  }

  public void setPaused(boolean paused) {
    this.paused = paused;
    notifyPlayThread();
  }

  public void setVolume(int volume) {
    this.volume = volume;
    notifyPlayThread();
  }
  
  public int getVolume() {
    return volume;
  }

  public boolean isPaused() {
    return paused;
  }

  /** Stops the external player and shuts down the thread controlling it.*/
  public void close() {
    synchronized(this){ playing = false; }
    notifyPlayThread();
    
    // Wait until the external application has stopped before declaring the
    // close operation a success.
    Process p = process;
    if (p != null) {
      try {
      p.waitFor();
      } catch (InterruptedException e) { }
    }
    
    // Wait until the thread exits the play() function.
    while (getPlayingThread() != null) {
      try{
        Thread.sleep(10);
      } catch (InterruptedException e) {

      }
    }
  }


  public String[] formatResumeArgument()
  {
    return new String[0];
  }
  
  public String[] formatVolumeArgument() {
    return new String[0];
  }
  
  public String[] formatOtherArguments() {
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

  /** Function that sleeps in spite of interruptions. */
  private void deepSleep(long msec) {
    long start = System.currentTimeMillis();
    long end = -1, elapsed = -1;
    do {
      try { 
        Thread.sleep(msec - elapsed);
      } catch (InterruptedException e) { }

      end = System.currentTimeMillis();
      elapsed = end - start;
    } while (elapsed < msec);
  }

  public void play(File file) throws PlayerException {

    playTime = 0;
    if (getPlayingThread() != null) {
      throw new PlayerException(
        "Tried to play two songs at once");
    }
    setPlayingThread(Thread.currentThread());
    
    synchronized(this){ playing = true; }

    while (playing) {
      synchronized(this) {
        stateDirty = false;
      }

      if (paused) {
        try {
        Thread.sleep(1000); 
        } catch (InterruptedException e) {
          synchronized(this) {
            if (!stateDirty) {
              setPlayingThread(null);
              throw new PlayerException(
                "player thread interrupted without a state change");
            }
          }
        }
      } else {
        // Not paused -- start up a player process
        try {
          String[] args = joinArray(new String[][] {
            new String[] { path },
            formatResumeArgument(),
            formatVolumeArgument(),
            formatOtherArguments(),
            new String[] { file.getPath() } 
          });
  //        for (int i = 0 ; i < args.length; i++) 
  //          System.out.print(args[i] + " ");
  //        System.out.println();
          // We don't want to be interrupted while we're spawing the
          // background process.
          synchronized(this) {
            process = Runtime.getRuntime().exec(args);
            route(process.getInputStream(), System.out, "player out");
            route(process.getErrorStream(), System.out, "player err");
          }
        }
        catch (IOException e) {
          // Couldn't grab stdin/stdout of the player process! 
          e.printStackTrace();
          setPlayingThread(null);
          throw new PlayerException(e.toString());
        }

        long start = System.currentTimeMillis();
        try {
          process.waitFor();

          // If we get here, the player subprocess has gone away.
          try {
            process.getOutputStream().close();
            process.getInputStream().close();
            process.getErrorStream().close();
          } catch (IOException e) {
          } catch (NullPointerException e) {}
          playing = false;
          if (process.exitValue() != 0) {
            setPlayingThread(null);
            throw new PlayerException(
              "extern player returned " + process.exitValue());
          }
        } catch (InterruptedException e) {
          // We should arrive here when someone changes the internal state of
          // this ExternalPlayer object.
          // Bring the state of the player in sync with this object's state.
          // To do this, we stop the player.
          // First we delay for a brief amount of time (currently 1/10 sec) to
          // prevent respawning the player too quickly.  Starting and stopping
          // the external player too fast can cause crashes with older sound 
          // drivers.
          deepSleep(100);
          try {
            try {
              process.getOutputStream().close();
              process.getInputStream().close();
              process.getErrorStream().close();
            } catch (IOException ex) {
            } catch (NullPointerException ex) {}
            process.destroy();
            process.waitFor();
          } catch (InterruptedException foo) {}

          synchronized(this) {
            if (!stateDirty) {
              setPlayingThread(null);
              throw new PlayerException(
                "player thread interrupted without a state change");
            }
          }

          long timePlayed = System.currentTimeMillis() - start
              + TIME_PLAYED_FUDGE_FACTOR_MS;
          playTime += timePlayed;
        }
      } // End of if-then block
    } // End of while loop

    setPlayingThread(null);
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
