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
    
    for (int i = 0; i < playList.size(); i++) {
      Track track = (Track) playList.get(i);
      if (!track.isOnPlayList()) {
        playList.remove(i);
        if (i < playListIndex)
          --playListIndex;
      }
    }
    
      // Remove extra unwanted items.
    while (playList.size() > trackDatabase.getPlayListLength()) 
      playList.remove(playList.size() - 1);
    
      // Loop around one time for each track which needs to be added to the 
      // play list. Don't worry too much if we don't exactly that number of
      // tracks because it will likely get one next time around.
    for (int i = trackDatabase.getPlayListLength() - playList.size(); i > 0; i--) {
      synchronized (trackDatabase) {
        Track track = trackDatabase.chooseTrack(random);
        if (track != null) {
            // Only add it if it's not already in the list
          add: {
            for (int j = 0; j < playList.size(); j++)
              if (playList.get(j) == track)
                break add; 
            playList.add(track);
          }
        }
      }
    }
    
    int size = playList.size();
    if (playList.size() == 0)
      return null;
    
    if (++playListIndex >= size) {
      playListIndex = 0;
//      randomise(playList);
    }

    System.out.println("---Play list---");
    for (int i = 0; i < playList.size(); i++) {
      Track track = (Track) playList.get(i);
      if (i == playListIndex)
        System.out.print("* ");
      System.out.println(track.toString());
    }
    
    return (Track) playList.get(playListIndex);
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
