package irate.server;

import irate.common.*;

import java.io.*;
import java.util.*;

public class ServerDatabase extends TrackDatabase {
  
  private final int ratingScale = 1000;
  
  public ServerDatabase() {
  }
  
  public ServerDatabase(File file) throws IOException {
    super(file);
  }

  public ServerDatabase(InputStream is) throws IOException {
    super(is);
  }

  public float getProbability(Track track) {
    if (track.isRated()) 
      return track.getRating();
    return 0;
  }
  
/*
  private int getAverageRating() {
    Track[] tracks = getTracks();
    int total = 0;
    for (int i = 0; i < tracks.length; i++) 
      total += tracks[i].getRating();
    return total * ratingScale / tracks.length;
  }

  public int equate(ServerDatabase serverDatabase) {
    Track[] thatTracks = serverDatabase.getTracks();
    int thatAverage = serverDatabase.getAverageRating();
    Track[] thisTracks = getTracks();
    int thisAverage = getAverageRating();

    int sum = 0;
    for (int i = 0; i < thatTracks.length; i++) {
      int thisRating = thisTracks[i].getRating() * ratingScale - thisAverage;
      for (int j = 0; j < thisTracks.length; i++) {
        int thatRating = thisTracks[i].getRating() * ratingScale - thatAverage;
        sum += thisRating * thatRating;
      }
    }
    return sum;
  }
*/

  public Track randomTrack(Random random) {
    Track[] tracks = getTracks();
    if (tracks.length == 0)
      return null;
    return tracks[(random.nextInt() & 0x7fffffff) % tracks.length];
  }
}
