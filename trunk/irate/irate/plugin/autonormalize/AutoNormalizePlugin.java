/**
 * Auto-normalize plugin for the iRate project. Automatically normalizes track volume levels.
 *
 * @author Stephen Blackheath <stephen@blacksapphire.com>
 */
package irate.plugin.autonormalize;

import irate.plugin.*;
import irate.common.Track;
import nanoxml.*;
import irate.client.TrackLifeCycleListener;
import irate.client.VolumePolicy;
import irate.download.DownloadListener;
import java.util.Hashtable;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Plugin to automatically normalize track volumes.
 *
 * @author Stephen Blackheath
 */
public class AutoNormalizePlugin
  extends Plugin
  implements TrackLifeCycleListener, DownloadListener, VolumePolicy
{
  /**
   * Hashtable of piped output streams.
   */
  private Hashtable downloadHash;
  private HowLoudThread hlThread;

  public AutoNormalizePlugin()
  {
  }

  /**
   * Get a short identifier for this Plugin.
   */
  public String getIdentifier()
  {
    return "auto-normalize";
  }

  /**
   * Get a short description of this plugin.
   */
  public String getDescription()
  {
    return "auto-normalize";
  }

  /**
   * Get a long description of this plugin, for tooltips
   */
  public String getLongDescription() 
  {
    return "This plugin automatically normalizes the volumes of tracks, that is, it makes all tracks about the same volume level.";
  }

  protected void doAttach()
  {
    hlThread = new HowLoudThread(getApp(), "calculated");
    downloadHash = new Hashtable();
    getApp().addTrackLifeCycleListener(this);
    getApp().addDownloadListener(this);
    getApp().addVolumePolicy(this, 80);
  }

  protected void doDetach()
  {
    getApp().removeTrackLifeCycleListener(this);
    getApp().removeDownloadListener(this);
    getApp().removeVolumePolicy(this);
    hlThread.requestTerminate();
    hlThread = null;
      // To do: Clean up any dangling stuff in downloadHash
    downloadHash = null;
  }

  /**
   * Parse the configuration stored in the specified element.
   */
  public void parseConfig(XMLElement elt)
  {
  }

  /**
   * Format the configuration of this plugin by modifying the specified
   * element.
   */
  public void formatConfig(XMLElement elt)
  {
  }

  /**
   * Notification that the specified track has been added to the playlist.
   */
  public void addedToPlayList(Track track)
  {
    check(track);
  }

  /**
   * Notification that the specified track has been removed from the playlist.
   */
  public void removedFromPlayList(Track track)
  {
  }

  /**
   * Notification that the specified track is about to be played.
   */
  public void startingToPlay(Track track)
  {
    check(track);
  }

  private void check(Track track)
  {
      // If we don't know how loud it is, queue it to be processed.
    if (track.getProperty("loudness") == null) {
      try {
        hlThread.queue(track);
      }
      catch (FileNotFoundException e) {
        System.err.println("auto-normalize: Warning - File not found when trying to determine loudness of "+track);
      }
    }
  }

  public void downloadStarted(Track track)
  {
    try {
      PipedInputStream pis = new PipedInputStream();
      PipedOutputStream pos = new PipedOutputStream(pis);
      HowLoudThread hlThread = new HowLoudThread(getApp(), "downloaded");
      hlThread.queue(track, pis);
        // Request this hlThread to terminate once it has emptied its queue.
      hlThread.requestTerminate();
      Object[] objs = new Object[2];
      objs[0] = pos;
      objs[1] = hlThread;
      downloadHash.put(track, objs);
    }
    catch (IOException e) {
        // This should not happen: PipedInputStream and PipedOutputStream exist entirely
        // in memory, so there is nothing that can go wrong with their creation.
      throw new RuntimeException(e.toString());
    }
  }

  public void downloadProgressed(Track track, int percentComplete, String state)
  {
  }

  public void downloadData(Track track, byte[] buffer, int offset, int length)
  {
    Object[] objs = (Object[]) downloadHash.get(track);
    if (objs != null) {
      PipedOutputStream pos = (PipedOutputStream) objs[0];
      HowLoudThread hlThread = (HowLoudThread) objs[1];
      try {
        pos.write(buffer, offset, length);
      }
      catch (IOException e) {
        System.err.println("auto-normalize: "+e.toString());
        hlThread.killProcessing(track);
        downloadHash.remove(track);
        track.setProperty("loudness", "unknown");
        getApp().saveTrack(track, false);
        try {pos.close();} catch (IOException e2) {}
      }
    }
  }

  public void downloadFinished(Track track, boolean succeeded)
  {
    Object[] objs = (Object[]) downloadHash.get(track);
    if (objs != null) {
      PipedOutputStream pos = (PipedOutputStream) objs[0];
      HowLoudThread hlThread = (HowLoudThread) objs[1];
      downloadHash.remove(track);

      try {
        pos.close();
      }
      catch (IOException e) {
        System.err.println("auto-normalize: "+e.toString());
      }

      String loudness;
      if (!succeeded) {
        hlThread.killProcessing(track);
        track.setProperty("loudness", null);
        getApp().saveTrack(track, false);
      }
      else {
          // Block until the loudness has been determined.
        synchronized (hlThread.getMutex()) {
          while ((loudness = track.getProperty("loudness")) == null) {
            try {
              hlThread.getMutex().wait();
            }
            catch (InterruptedException e) {
            }
          }
        }
      }
    }
  }

  /**
   * Determine the volume to use for this track, based on an internal policy,
   * and a suggested volume.
   * The volume is specified in negative or positive decibels, where 0.0
   * means the track will be left as it is.
   */
  public int determineVolume(Track track, int suggestedVolume)
  {
      // Note: The suggestedVolume is only used when we don't know what the volume is.

    String loudness = track.getProperty("loudness");
      // If we don't know how loud it is...
    if (loudness == null) {
        // Then queue it for processing - top priority.
      try {
        hlThread.queue(track);
          // Wait until we have determined how loud it is.
        synchronized (hlThread.getMutex()) {
          final String message = "Please wait... Need to determine track loudness";
          getApp().addStatusMessage(100, message);
          try {
            while ((loudness = track.getProperty("loudness")) == null)
              hlThread.getMutex().wait();
          }
          catch (InterruptedException e) {
          }
          getApp().removeStatusMessage(message);
        }
      }
      catch (FileNotFoundException e) {
        System.err.println("auto-normalize: Warning - File not found when trying to determine loudness of "+track);
        return 0;
      }
    }
    try {
      int loudnessDb = (int) Math.floor(Float.parseFloat(loudness));
        // Negate the loudness to make it a volume adjustment.
      int adj = -loudnessDb;
      System.out.println("auto-normalize: playing "+track+" with volume adjustment of "+(adj<0?"":"+")+adj+" dB");
      return -loudnessDb;
    }
    catch (NumberFormatException e) {
      return suggestedVolume;
    }
  }
}

