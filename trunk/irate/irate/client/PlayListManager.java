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
      Track[] tracks = playList.getTracks();
      int[] probs = new int[tracks.length]; 

      int totalProb = 0;
      for (int i = 0; i < tracks.length; i++) {
        if (tracks[i].getFile() != null) {
          totalProb += tracks[i].getProbability();
          probs[i] = totalProb;
        }
      }

      if (totalProb == 0)
        return null;

      int rand = (int) (Math.abs(random.nextFloat()) * totalProb);

      int i;
      for (i = 0; i < tracks.length - 1; i++) {
        if (rand < probs[i])
          break;
      }

      Track track = tracks[i];
      return track;
    }
  }
}
