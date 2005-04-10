package irate.download;

import java.net.URL;
import java.util.Hashtable;

/**
 * Manages exponential back-off for downloads.  Back-off is done on a per-host
 * basis.
 *
 * @author Stephen Blackheath
 */
public class ExponentialBackoffManager
{
  private Hashtable backoffs = new Hashtable();

  private static class Backoff
  {
    public int noOfFailures;
    public long timeOfFailure;
  }

  public ExponentialBackoffManager()
  {
  }

  /**
   * Return true if we must not contact this site now because of exponential
   * back-off.
   */
  public synchronized boolean isBackedOff(URL url)
  {
    Backoff backoff = (Backoff) backoffs.get(url.getHost());
    if (backoff == null)
      return false;
    else {
      long now = System.currentTimeMillis();
      long since = now - backoff.timeOfFailure;
        // If time since is negative, then this means the system clock has been
        // set.
      if (since < 0)
        return false;
        // Min time: 2 minute.  Max time: Just over two hours.
      long backoffTime = 120000L << (backoff.noOfFailures < 6 ? backoff.noOfFailures : 6);
      return since < backoffTime;
    }
  }

  public synchronized void failed(URL url)
  {
    Backoff backoff = (Backoff) backoffs.get(url.getHost());
    if (backoff == null) {
      backoff = new Backoff();
      backoff.noOfFailures = 0;
      backoffs.put(url.getHost(), backoff);
    }
    backoff.noOfFailures++;
    backoff.timeOfFailure = System.currentTimeMillis();
  }

  public synchronized void succeeded(URL url)
  {
    backoffs.remove(url.getHost());
  }
}
