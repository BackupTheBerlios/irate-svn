// Copyright 2003 Anthony Jones

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
        // rating is strictly above 5. That means that there has to be at least
        // one person who think's it's better than just alright.
      if (rating >= 6.5F) {
        float prob = rating * rating;
        float weight = track.getWeight();
        if (!Float.isNaN(weight))
          return prob * weight;
        return prob;
      }
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
