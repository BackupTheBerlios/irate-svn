/*
 * Created on Oct 24, 2003
 */
package irate.client;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author Anthony Jones
 */
abstract class AbstractPlayer implements Player {
  
  private List listeners = new Vector();

  public void addPlayerListener(PlayerListener playerListener) {
    listeners.add(playerListener);
  }
  
  /** Notify all the listeners of the new track position. */
  protected void notifyPosition(int position, int length) {
    for (Iterator itr = listeners.iterator(); itr.hasNext(); ) {
      PlayerListener listener = (PlayerListener) itr.next();
      listener.positionUpdated(position, length);
    }
  }

  /** Notify all the listeners of the new bit rate. */
  protected void notifyBitRate(int bitRate) {
    for (Iterator itr = listeners.iterator(); itr.hasNext(); ) {
      PlayerListener listener = (PlayerListener) itr.next();
      listener.bitRateUpdated(bitRate);
    }
  }

}
