// Copyright 2003 Anthony Jones

package irate.client;

import irate.common.Track;
import irate.common.TrackDatabase;

import java.util.*;

public class PlayListManager {

  private Random random = new Random();
  private int playListMaximumSize;
  
  private TrackDatabase trackDatabase;
  private List playList;
  private int playListIndex;
  
  private Track lastPlayedTrack;
  
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

      // Maintain a 'toOmit' hash table, which is used to make the choosing
      // of tracks not choose ones we have already got on the list.
    Hashtable toOmit = new Hashtable();
    for (int i = 0; i < playList.size(); i++) {
      Track track = (Track) playList.get(i);
      if (!track.isOnPlayList()) {
        playList.remove(i);
        if (i < playListIndex)
          --playListIndex;
      }
      else
	toOmit.put(track, track);
    }
    
      // Remove extra unwanted items.
    while (playList.size() > trackDatabase.getPlayListLength()) 
      playList.remove(playList.size() - 1);
    
      // Loop around one time for each track which needs to be added to the 
      // play list. Don't worry too much if we don't exactly that number of
      // tracks because it will likely get one next time around.
    for (int i = trackDatabase.getPlayListLength() - playList.size(); i > 0; i--) {
      synchronized (trackDatabase) {
        Track track = trackDatabase.chooseTrack(random, toOmit);
        if (track != null) {
	  toOmit.put(track, track);
	  playList.add(track);
        }
      }
    }
    
    int size = playList.size();
    if (playList.size() == 0)
      return null;
    
    //
    // Make sure the track you select isn't the same as the track
    // that was previously played
    //
    
    Track selectedTrack;
//    do {
      if (++playListIndex >= size) {
        playListIndex = 0;
      }
      selectedTrack = (Track) playList.get(playListIndex);
//    }  while (selectedTrack == lastPlayedTrack);

    lastPlayedTrack = selectedTrack;
    
    System.out.println("---Play list---");
    for (int i = 0; i < playList.size(); i++) {
      Track track = (Track) playList.get(i);
      if (i == playListIndex)
        System.out.print("* ");
      System.out.println(track.toString());
    }
    
    return selectedTrack;
  }
  
  private void randomise(List playList) {
    for (int i = 0; i < playList.size(); i++) {
      int swap = (Math.abs(random.nextInt()) % playList.size());
      if (swap != i) {
        Object o = playList.get(i);
        playList.set(i, playList.get(swap));
        playList.set(swap, o);
      }
    }
  }

}
