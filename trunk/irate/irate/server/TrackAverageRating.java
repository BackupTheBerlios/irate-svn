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

  public void add(ServerDatabase db) {
    Track[] tracks = db.getTracks();
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      Rating rating = (Rating) hash.get(track.getKey());
      if (rating == null)
        hash.put(track.getKey(), new Rating(track));
      else
        rating.add(track);
    }
  }

  public ServerDatabase getAverages() {
    return averages;
  }

  class Rating {
    private Track track;
    private float sum;
    private float weight;

    public Rating(Track track) {
      this.track = averages.add(track);
      sum = track.getRating();
      weight = track.getWeight();
    }

    public void add(Track track) {
      sum += track.getRating();
      weight += track.getWeight();
      track.setRating(sum / weight);
    }
  }
}

