package irate.client;

import irate.common.Track;

/**
 * Given a suggested volume, an implementation of this class will make its own
 * decision about what the volume should be.
 * A chain of volume policies can be made, where each output volume becomes a
 * suggestion for the following policy.
 *
 * @author Stephen Blackheath
 */
public interface VolumePolicy
{
  /**
   * Determine the volume to use for this track, based on an internal policy,
   * and a suggested volume.
   * The volume is specified in negative or positive decibels, where 0.0
   * means the track will be left as it is.
   */
  public int determineVolume(Track track, int suggestedVolume);
}
