// Copyright 2003 Anthony Jones

package irate.server;

import irate.common.*;

import java.util.*;

public class TrackAverageRating {

  private Hashtable hash;
  private ServerDatabase averages;
  
  public TrackAverageRating() {
    hash = new Hashtable();
    averages = new ServerDatabase();
  }

  public void add(ServerDatabase db, float weight) {
    Track[] tracks = db.getTracks();
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      Rating rating = (Rating) hash.get(track.getKey());
      if (rating == null)
        hash.put(track.getKey(), new Rating(track, weight));
      else
        rating.add(track, weight);
    }
  }

  public ServerDatabase getAverages() {
    return averages;
  }

  class Rating {
    private Track track;
    private float sum;
    private float weight;

    public Rating(Track track, float weight) {
      this.track = averages.add(track);
      this.sum = track.getRating() * weight;
      this.weight = weight;
    }

    public void add(Track track, float weight) {
      this.sum += track.getRating() * weight;
      this.weight += weight;
      this.track.setRating(sum / this.weight);
    }    
  }
}

