/*
 * Created on Oct 24, 2003
 */
package irate.client;

/**
 * @author Anthony Jones
 */
public interface PlayerListener {
  
  /** Called once a second to update the track's current position. */
  public void positionUpdated(int position, int length);
  
  /** Called when the track starts playing. */
  public void bitRateUpdated(int bitRate);
}
