/**
 * Auto-normalize plugin for the iRate project. Automatically normalizes track volume levels.
 *
 * @author Stephen Blackheath <stephen@blacksapphire.com>
 */
package irate.plugin.autonormalize;

import java.io.*;
import java.util.Vector;
import irate.common.Track;
import irate.plugin.PluginApplication;
import java.text.DecimalFormat;

public class HowLoudThread
  implements Runnable
{
  private PluginApplication app;
  private Vector queue = new Vector();
  private Object mutex = new Object();
  private boolean toTerminate;
  /**
   * The track being processed.
   */
  private Track beingProcessed;

  public HowLoudThread(PluginApplication app)
  {
    this.app = app;
    toTerminate = false;
    Thread t = new Thread(this);
    t.start();
  }

  public void requestTerminate()
  {
    synchronized (getMutex()) {
      toTerminate = true;
      getMutex().notifyAll();
    }
  }

  public Object getMutex()
  {
    return mutex;
  }

  private static DecimalFormat format = new DecimalFormat("0.000");

  public void run()
  {
    while (true) {
        // Pop a track off the processing queue
      InputStream is;
      synchronized (getMutex()) {
        beingProcessed = null;
        getMutex().notifyAll();
        while (queue.size() == 0) {
            // Only terminate when the queue is empty.
          if (toTerminate)
            return;
          try {
            getMutex().wait();
          }
          catch (InterruptedException e) {
          }
        }
        Object[] objs = (Object[]) queue.get(queue.size()-1);
        beingProcessed = (Track) objs[0];
        is = (InputStream) objs[1];
        queue.remove(queue.size()-1);
      }

      String loudness = "unknown";
      try {
        try {
          Bitstream bitstream = new Bitstream(is);
          Averager averager = new Averager(200);
          Decoder decoder = new Decoder(averager);
          int i = 0;
          while (true) {
            Header h = bitstream.readFrame();
            if (h == null)
                break;
            decoder.decodeFrame(h, bitstream);
            bitstream.closeFrame();
          }
            // 'averageLevel' is the average level for a group of 40 tracks chosen
            // at random, so it should be a sufficiently good reference volume level.
          final double averageLevel = 0.0188;
          double trackLevel = averager.getLoudness();

          double ratio = trackLevel / averageLevel;
          double decibels = log10(ratio) * 20.0;
          System.out.println("auto-normalize: calculated track "+beingProcessed+" loudness of "+(decibels < 0.0 ? "" : "+") + format.format(decibels)+" dB");
          loudness = Integer.toString((int) Math.floor(decibels));
          app.saveTrack(beingProcessed, false);
        }
        catch (RuntimeException ex)
        {
          ex.printStackTrace();
        }
        catch (JavaLayerException ex)
        {
          ex.printStackTrace();
        }
          /* Just in case - we do NOT want this thread to die */
        catch (Throwable ex) {
          ex.printStackTrace();
        }
      }
      finally {
        beingProcessed.setProperty("loudness", loudness);
        try {is.close();} catch (IOException e) {}
      }
    }
  }

  private static double log10(double x)
  {
    return Math.log(x) / 2.302585093;
  }

  public void queue(Track track)
    throws FileNotFoundException
  {
    queue(track, new FileInputStream(track.getFile()));
  }

  public void queue(Track track, InputStream is)
  {
    Object[] objs = new Object[2];
    objs[0] = track;
    objs[1] = is;
    synchronized (getMutex()) {
        // If this track is currently being processed, then we need not do any more.
      if (beingProcessed != null && beingProcessed.equals(track))
        return;
        // If this queue already contains this track, then we remove it.
      for (int i = 0; i < queue.size(); i++) {
        Object[] thisObjs = (Object[]) queue.get(i);
        Track thisTrack = (Track) thisObjs[0];
        if (thisTrack.equals(track)) {
          queue.remove(i);
          break;
        }
      }
        // Add the track at the end of the list, which will make it get processed
        // first after the one that's currently being processed.
      queue.add(objs);
      getMutex().notifyAll();
    }
  }
}
