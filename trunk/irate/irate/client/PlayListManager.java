package irate.client;

import irate.common.Track;
import irate.common.TrackDatabase;

import java.util.*;

public class PlayListManager {

  private Random random = new Random();
  
  private int playListMaximumSize;
  
  private TrackDatabase trackDatabase;
  private TrackDatabase playList;
  
  public PlayListManager(TrackDatabase trackDatabase) {
    this.trackDatabase = trackDatabase;
    this.playList = trackDatabase;
  }
  
  public TrackDatabase getPlayList() {
    return playList;
  }

  public Track chooseTrack() {
    synchronized (playList) {
      return playList.chooseTrack(random);
    }
  }
}
