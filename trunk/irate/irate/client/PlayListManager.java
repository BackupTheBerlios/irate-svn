// Copyright 2003 Anthony Jones

package irate.client;

import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.Utils;

import java.util.*;

public class PlayListManager {

  private TrackDatabase trackDatabase;
  private List playList;
  private int playListIndex;

  private Track lastPlayedTrack;

  private Vector trackLifeCycleListeners = new Vector();

  public PlayListManager(TrackDatabase trackDatabase) {
    this.trackDatabase = trackDatabase;
    this.playList = new Vector();
    playListIndex = 0;
  }

  public TrackDatabase getTrackDatabase() {
    return trackDatabase;
  }

  public synchronized Track[] getPlayList() {
    return (Track[]) playList.toArray(new Track[playList.size()]);
  }

  public synchronized Track chooseTrack() {
     int playListLength = trackDatabase.getPlayListLength();
      int unratedPlayListCount = trackDatabase.getNoOfUnratedOnPlaylist();
    
      // Remove the track we've just played if necessary.
    if (playListIndex >= 0 && playListIndex < playList.size()) {
      Track track = (Track) playList.get(playListIndex);
      if (!track.isOnPlayList()) {
        notifyRemovedFromPlayList((Track)playList.get(playListIndex));
        playList.remove(playListIndex);

          // Subtract one from the index because it'll get incremented later.
        playListIndex--;
      }
    }

      // Remove extra unwanted items.
    while (playList.size() > playListLength) {
      int toRemove = playList.size() - 1;
      notifyRemovedFromPlayList((Track)playList.get(toRemove));
      playList.remove(toRemove);
    }

      // Maintain a 'toOmit' hash table, which is used to make the choosing
      // of tracks not choose ones we have already got on the list.
    Set toOmit = new HashSet();
    int noOfUnrated = 0;
    for (int i = 0; i < playList.size(); i++) {
      Track track = (Track) playList.get(i);
//      if (!track.isOnPlayList()) {
//        if (track.getRating() == 0) {
//          notifyRemovedFromPlayList((Track)playList.get(i));
//          playList.remove(i);
//          if (i < playListIndex)
//            --playListIndex;
//        }
//      }
//      else {
        toOmit.add(track);
        if (!track.isRated())
          noOfUnrated++;
//      }
    }

      // Loop around one time for each track which needs to be added to the
      // play list. Don't worry too much if we don't exactly that number of
      // tracks because it will likely get one next time around.
    for (int i = playListLength - playList.size(); i > 0; i--) {
      synchronized (trackDatabase) {
        Track track = null;

/**
 * Modified by Eric Dalquist - 11.09.2003
 *
 * Now multiple unrated tracks can be selected.
 */
        if (noOfUnrated <= unratedPlayListCount && playList.size() != 0)
          track = trackDatabase.chooseUnratedTrack(Utils.getRandom(), toOmit);
          
        if (track == null)
          track = trackDatabase.chooseTrack(Utils.getRandom(), toOmit);

          // We couldn't find a track so give up looking.
        if (track == null)
          break;
          
        if (!track.isRated())
          noOfUnrated++;

        toOmit.add(track);

        // We want "0% unrated on playlist" to be an absolute.
        if (unratedPlayListCount != 0 || track.isRated()) {
          int insertIndex = Utils.getRandom().nextInt(playList.size() + 1);
          playList.add(insertIndex, track);
          notifyAddedToPlayList(track);
          if (insertIndex <= playListIndex)
            ++playListIndex;
        }
      }
    }

    int size = playList.size();
    if (size < 1)
      return null;

    if (playListIndex >= size)
      playListIndex = 0;

    //
    // Make sure the track you select isn't the same as the track
    // that was previously played
    //

    Track selectedTrack;
    if (size == 1) {
      selectedTrack = (Track) playList.get(0);
    } else {
      int currentIndex = playListIndex;
      do {
        if (++playListIndex >= size || playListIndex < 0)
          playListIndex = 0;
        selectedTrack = (Track) playList.get(playListIndex);
        
        // This first statement says that if the user has 0% unrated on playlist
        // that we won't play an unrated song.  The playlist may have been generated
        // before they changed this setting, so we're just filtering.
        if(!(trackDatabase.getUnratedPlayListRatio() == 0 && !selectedTrack.isRated())) {
            // Also ensure the song isn't hidden -- if they Suxed a song, we don't want to play it.
            if (!selectedTrack.isHidden() && selectedTrack != lastPlayedTrack)
                break;
        }
      } while (playListIndex != currentIndex);
    }

    lastPlayedTrack = selectedTrack;

    System.out.println("---Play list---");
    for (int i = 0; i < playList.size(); i++) {
      Track track = (Track) playList.get(i);
      if (i == playListIndex)
        System.out.print("* ");
      else
        System.out.print("| ");
      System.out.println(track.toString());
    }

    return selectedTrack;
  }

  /**
   * Add a listener that will be notified when tracks are added or removed
   * from the playlist.
   */
  public void addTrackLifeCycleListener(TrackLifeCycleListener listener)
  {
    trackLifeCycleListeners.add(listener);
  }

  /**
   * Remove listener.
   */
  public void removeTrackLifeCycleListener(TrackLifeCycleListener listener)
  {
    trackLifeCycleListeners.remove(listener);
  }

  private void notifyAddedToPlayList(Track track)
  {
    for (int i = 0; i < trackLifeCycleListeners.size(); i++)
      ((TrackLifeCycleListener)trackLifeCycleListeners.get(i)).addedToPlayList(track);
  }

  private void notifyRemovedFromPlayList(Track track)
  {
    for (int i = 0; i < trackLifeCycleListeners.size(); i++)
      ((TrackLifeCycleListener)trackLifeCycleListeners.get(i)).removedFromPlayList(track);
  }

  /** Tests the playlist manager from the command line */
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      throw new Exception("USAGE: PlayListManager trackdatabase.xml");
    }
    TrackDatabase td = new TrackDatabase(new java.io.File(args[0]));
    PlayListManager pm = new PlayListManager(td);
    java.util.HashSet played = new java.util.HashSet();
    int count = 0;
    while (true) {
      Track t = pm.chooseTrack();
      count++;
      System.err.println("Chose: " + t);
      if (played.contains(t)) {
        System.err.println("Repeat track after " + count + " tracks");
        System.exit(0);
      }
      played.add(t);
    }
  }
}
