/**
 * <p>This handles the contents of the status bar. It has three levels of
 * status messages:</p>
 * <ul><li>High-priority: there may only be one of these, and it will
 * always be displayed. For things like menu hints.
 * <li>Mid-priority: there may be any number of these, and the display
 * is updated every <code>STATUS_CHANGE_DELAY</code> ms. For things
 * like download progress.
 * <li>Low-priority: this is the default text if nothing else is being
 * shown. There may also be any number of these, and they will be
 * rotated through. For things like the unrated tracks count.</ul>
 *
 * <p>The mid- and low-priority messages are contained in
 * <code>MutableString</code> objects, and so the caller can change
 * them whenever necessary, if desired.</p>
 *
 * @author Creator: Robin Sheat
 *
 * $Id: AbstractStatus.java,v 1.1 2004/08/27 16:10:32 eythian Exp $
 */
public abstract class AbstractStatus {

  /**
   * Determines the delay between updates of the status bar.
   */
  int STATUS_CHANGE_DELAY = 3000;

  /**
   * The high-priority message. If <code>null</code>, then there is no
   * message set.
   */
  private String hpMessage = null;

  /**
   * The collection of medium-priority messages.
   */
  private MutableString[] mpMessages = null;

  /**
   * The collection of low-priority messages.
   */
  private MutableString[] lpMessages = null;

  /**
   * A count of how many medium-priority messages we have, to avoid
   * recounting each time we want to insert something new.
   */
  private int numMpMessages = 0;

  /**
   * A count of how many low-priority messages we have.
   */
  private int numLpMessages = 0;

  /**
   * Thread that updates the status bar
   */
  private StatusUpdater statusThread = new StatusUpdater(this);

  /**
   * Sets the value of the high-priority message. Note that if a
   * message is currently set, this replaces it.
   *
   * @param msg the message
   */
  public void setHighPriMessage(String s) {
    hpMessage = s;
    updateStatus();
  }

  /**
   * Removes the high-priority message.
   */
  public void removeHighPriMessage() {
    hpMessage = null;
    updateStatus();
  }

  /**
   * Adds a medium-priority message.
   *
   * @param s the message
   * @return a message number that is later used in order to remove
   * this message
   */
  public synchronized int setMedPriMessage(MutableString s) {
    // Track the location to be returned to the caller
    int loc=-1;
    if (numMpMessages == 0) {
      // Create a new array to store them
      mpMessages = new MutableString[1];
      mpMessages[0] = s;
      loc = 0;
    } else if (numMpMessages == mpMessages.length) {
      // We need to grow the array. This happens in steps of two, just
      // to avoid doing it every single time.
      MutableString[] tmpMsgs = new MutableString[numMpMessages+2];
      for (int i=0; i<numMpMessages; i++) {
        tmpMsgs[i] = mpMessages[i];
      }
      mpMessages = tmpMsgs;
      // Add the new string near the end
      mpMessages[numMpMessages] = s;
      loc = numMpMessages;
    } else {
      // We have a space somewhere in our array, so we'll search for
      // it. Backwards, because odds are it's at the end.
      for (int i=mpMessages.length-1; i>=0; i--) {
        if (mpMessages[i] == null) {
          loc = i;
          break;
        }
      }
      mpMessages[loc] = s;
    }
    numMpMessages++;
    return loc;
  }

  /**
   * Removes a medium-priority message.
   *
   * @param m the message number to be removed
   */
  public synchronized void removeMedPriMessage(int m) {
    mpMessages[m] = null;
    numMpMessages--;
    if (numMpMessages == 0) {
      mpMessages = null;
    }
  }

  /**
   * Adds a low-priority message.
   *
   * @param s the message
   * @return a message number that is later used in order to remove
   * this message
   */
  public synchronized int setLowPriMessage(MutableString s) {
    // Track the location to be returned to the caller
    int loc=-1;
    if (numLpMessages == 0) {
      // Create a new array to store them
      lpMessages = new MutableString[1];
      lpMessages[0] = s;
      loc = 0;
    } else if (numLpMessages == lpMessages.length) {
      // We need to grow the array. This happens in steps of two, just
      // to avoid doing it every single time.
      MutableString[] tmpMsgs = new MutableString[numLpMessages+2];
      for (int i=0; i<numLpMessages; i++) {
        tmpMsgs[i] = lpMessages[i];
      }
      lpMessages = tmpMsgs;
      // Add the new string near the end
      lpMessages[numLpMessages] = s;
      loc = numLpMessages;
    } else {
      // We have a space somewhere in our array, so we'll search for
      // it. Backwards, because odds are it's at the end.
      for (int i=lpMessages.length-1; i>=0; i--) {
        if (lpMessages[i] == null) {
          loc = i;
          break;
        }
      }
      lpMessages[loc] = s;
    }
    numLpMessages++;
    return loc;
  }

  /**
   * Removes a low-priority message.
   *
   * @param m the message number to be removed
   */
  public synchronized void removeLowPriMessage(int m) {
    lpMessages[m] = null;
    numLpMessages--;
    if (numLpMessages == 0) {
      lpMessages = null;
    }
  }

  /**
   * Tells the thread that keeps the status bar up to date to update
   * it immediatly.
   */
  private void updateStatus() {
    statusThread.interrupt();
  }

  /**
   * Sets the text on the status bar.
   *
   * @param s the new contents of the status bar
   */
  protected abstract void updateStatus(String s);

  /**
   * Sets the text on the status bar, from a <code>MutableString</code>.
   *
   * @param s the new contents of the status bar
   */
  protected void updateStatus(MutableString s) {
    updateStatus(s.toString());
  }

  /**
   * This class runs continuously, and updates the status bar text. If
   * it is interrupted, it updates immediatly.
   */
  protected class StatusUpdater extends Thread {

    AbstractStatus caller;
    
    /**
     * Constructor for the status updating thread. Needs the
     * <code>AbstractStatus<code> object in order to synchronise
     * access to data that can change whenever.
     *
     * @param c the calling object, on which data access will be
     * synchronised.
     */
    public StatusUpdater(AbstractStatus c) {
      caller = c;
    }

    /**
     * Keeps running forever. Never do we want to be without status!
     */
    public void run() {
      int lastMpMessage = 0;
      int lastLpMessage = 0;

      while (true) {
        synchronized (caller) {
          if (hpMessage != null) {
            updateStatus(hpMessage);
          } else if (numMpMessages != 0) {
            lastMpMessage++;
            if (lastMpMessage > mpMessages.length-1) {
              // wrap around, or account for changes in the array size
              // since we last looked.
              lastMpMessage = 0;
            }
            while (mpMessages[lastMpMessage] == null) {
              // Skip any removed message slots.
              lastMpMessage++;
            }
            updateStatus(mpMessages[lastMpMessage]);
          } else if (numLpMessages != 0) {
            lastLpMessage++;
            if (lastLpMessage > lpMessages.length-1) {
              lastLpMessage = 0;
            }
            while (lpMessages[lastLpMessage] == null) {
              lastLpMessage++;
            }
            updateStatus(lpMessages[lastLpMessage]);
          }
        }
        try {
          wait(STATUS_CHANGE_DELAY);
        } catch (InterruptedException e) {}
      }


    }

  }

}
