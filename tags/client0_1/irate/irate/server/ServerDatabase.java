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
    if (track.isRated()) {
      float rating = track.getRating();
      
        // This will mean that a track is only recommended to a user if the
        // rating is above 5.
      if (rating >= 5)
        return rating * rating;
    }
    return 0;
  }

  public Track randomTrack(Random random) {
    Track[] tracks = getTracks();
    if (tracks.length == 0) 
      return null; 
    return tracks[(random.nextInt() & 0x7fffffff) % tracks.length];
  }
}
