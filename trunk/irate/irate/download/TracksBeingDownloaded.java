/*
 * Created on 14/07/2005
 */
package irate.download;

import irate.common.Track;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TracksBeingDownloaded {

  private Set tracks = new HashSet();

  public TracksBeingDownloaded() {
  }

  public synchronized void add(Track track) {    
    tracks.add(track);
  }

  public synchronized void remove(Track track) {
    tracks.remove(track);
  }

  public synchronized boolean contains(Track track) {
    return tracks.contains(track);
  }

  public synchronized int size() {
    return tracks.size();
  }

  public synchronized boolean isServerAlreadyUsed(Track requestedTrack) {
    String requestedHost = requestedTrack.getURL().getHost();
    for (Iterator itr = tracks.iterator(); itr.hasNext();) {
      Track track = (Track) itr.next();
      if (requestedHost.equals(track.getURL().getHost())) return true;
    }
    return false;
  }
  
}
