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
  private String identifier;
  private Vector queue = new Vector();
  private Object mutex = new Object();
  private boolean toTerminate;
  private boolean toKillProcessing;
  /**
   * The track being processed.
   */
  private Track beingProcessed;

  public HowLoudThread(PluginApplication app, String identifier)
  {
    this.app = app;
    this.identifier = identifier;
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
        toKillProcessing = false;
      }

      String loudness = "unknown";
      try {
        try {
          Bitstream bitstream = new Bitstream(is);
          Averager averager = new Averager(200);
          Decoder decoder = new Decoder(averager);
          int i = 0;
          while (!toKillProcessing) {
            Header h = bitstream.readFrame();
            if (h == null)
                break;
            decoder.decodeFrame(h, bitstream);
            bitstream.closeFrame();
          }
          if (!toKillProcessing) {
              // 'averageLevel' is the average level for a group of 40 tracks chosen
              // at random, so it should be a sufficiently good reference volume level.
            final double averageLevel = 0.0188;
            double trackLevel = averager.getLoudness();

            double ratio = trackLevel / averageLevel;
            double decibels = log10(ratio) * 20.0;
            loudness = (decibels < 0.0 ? "" : "+") + format.format(decibels);
          }
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
        if (!toKillProcessing) {
          beingProcessed.setProperty("loudness", loudness);
          System.out.println("auto-normalize: "+identifier+" track "+beingProcessed+" loudness of "+loudness+" dB");
          app.saveTrack(beingProcessed, false);
        }
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

  /**
   * If the specified track is being processed, then stop it being processed, and
   * prevent it from writing any loudness value to the track.
   */
  public void killProcessing(Track track)
  {
    synchronized (getMutex()) {
      removeFromQueue(track);
      while (beingProcessed != null && beingProcessed.equals(track)) {
        toKillProcessing = true;
        try {getMutex().wait();} catch (InterruptedException e) {}
      }
    }
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
      removeFromQueue(track);
        // Add the track at the end of the list, which will make it get processed
        // first after the one that's currently being processed.
      queue.add(objs);
      getMutex().notifyAll();
    }
  }

  private void removeFromQueue(Track track)
  {
      // If this queue already contains this track, then we remove it.
    for (int i = 0; i < queue.size(); i++) {
      Object[] thisObjs = (Object[]) queue.get(i);
      Track thisTrack = (Track) thisObjs[0];
      if (thisTrack.equals(track)) {
        queue.remove(i);
        break;
      }
    }
  }
}
