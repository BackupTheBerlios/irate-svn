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
    int playListLength = trackDatabase.getPlayListLength();

      // Remove the track we've just played if necessary.
    if (playListIndex < playList.size()) {
      Track track = (Track) playList.get(playListIndex);
      if (!track.isOnPlayList()) {
        playList.remove(playListIndex);
        
          // Subtract one from the index because it'll get incremented later.
        playListIndex--;
      }
    }
         
      // Remove extra unwanted items.
    while (playList.size() > playListLength) 
      playList.remove(playList.size() - 1);  
    
      // Maintain a 'toOmit' hash table, which is used to make the choosing
      // of tracks not choose ones we have already got on the list.
    Hashtable toOmit = new Hashtable();
    int noOfUnrated = 0;
    for (int i = 0; i < playList.size(); i++) {
      Track track = (Track) playList.get(i);
      if (!track.isOnPlayList()) {
        if (track.getRating() == 0) {
          playList.remove(i);
          if (i < playListIndex)
            --playListIndex;
        }
      }
      else {
	toOmit.put(track, track);
        if (!track.isRated())
          noOfUnrated++;
      }
    }
    
      // Loop around one time for each track which needs to be added to the 
      // play list. Don't worry too much if we don't exactly that number of
      // tracks because it will likely get one next time around.
    for (int i = playListLength - playList.size(); i > 0; i--) {
      synchronized (trackDatabase) {
        Track track;
        if (noOfUnrated == 0 && playList.size() != 0)
          track = trackDatabase.chooseUnratedTrack(random, toOmit);
        else
          track = trackDatabase.chooseTrack(random, toOmit);
        if (track != null) {
          if (!track.isRated())
            noOfUnrated++;
	  toOmit.put(track, track);
	  playList.add(track);
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
        if (++playListIndex >= size)
          playListIndex = 0;
        selectedTrack = (Track) playList.get(playListIndex);
        if (selectedTrack != lastPlayedTrack)
          break;
      } while (playListIndex != currentIndex);
    }
        
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
