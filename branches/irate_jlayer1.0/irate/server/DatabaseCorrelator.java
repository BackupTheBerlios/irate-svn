// Copyright 2003 Anthony Jones

package irate.server;

import irate.common.*;

import java.io.IOException;
import java.util.*;

public class DatabaseCorrelator {

  private ServerDatabase db0;
  private DatabaseReference ref;
  private ServerDatabase sparesDatabase;
  private Vector spares;
  private float correlation;
  
  public DatabaseCorrelator(ServerDatabase db0, DatabaseReference ref) {
    this.db0 = db0;
    this.ref = ref;
  }
  
  public DatabaseReference getDatabaseReference() {
    return ref;
  }

  private boolean isRatingValid(Track track) {
    float rating = track.getRating();
    return 
        rating == 0.0F ||
        rating == 2.0F ||
        rating == 5.0F ||
        rating == 7.0F ||
        rating == 10.0F;
  }

  public void process() throws IOException {
    ServerDatabase db1 = ref.getServerDatabase();
    Track[] tracks1 = db1.getTracks();

    Vector spares = new Vector();
    sparesDatabase = new ServerDatabase();
    correlation = 0;

      // The lowest of the two lengths.
    int minLength = db0.getNoOfTracks() < tracks1.length ? db0.getNoOfTracks() : tracks1.length;
    if (minLength == 0)
      return;

    float[] intersect0 = new float[minLength];
    float[] intersect1 = new float[minLength];
    int intersectLength = 0;

    float total0 = 0;
    float total1 = 0;
    for (int i = 0; i < tracks1.length; i++) {
      Track track1 = tracks1[i];
      if (track1.isRated() && isRatingValid(track1)) {
        float rating1 = track1.getRating();
        Track track0 = db0.getTrack(track1);
        if (track0 == null) {
            // It exists in db1 but not in db0 so it's a candidate for
            // returning to the user.
          spares.add(track1);
        }
        else {
          if (track0.isRated() && isRatingValid(track0)) {
            float rating0 = track0.getRating();

              // Add this to the intersect list
            intersect0[intersectLength] = rating0;
            intersect1[intersectLength] = rating1;
            intersectLength++;
            
            total0 += rating0;
            total1 += rating1;
          }
        }
      }
    }

      // Now that we have the totals we can calculate the actual correlation.
    float sum = 0;
//    float offset0 = total0 / intersectLength;
//    float offset1 = total1 / intersectLength;
    final float offset0 = 5;
    final float offset1 = 5;
    for (int i = 0; i < intersectLength; i++)
      sum += (intersect0[i] - offset0) * (intersect1[i] - offset1);
//    correlation = sum * intersectLength * intersectLength / (total0 * total1);

      // Dividing by the intersectLength would give the average track
      // correlation.
    correlation = sum / intersectLength;

      // Weight it a little towards large intersections
    if (intersectLength >= 20)
      correlation *= 2;
    
      // Cube it to make it weight highly towards good correlations.
    correlation = correlation * correlation * correlation;
    

      // Set the correlation value for each track.
    for (int i = 0; i < spares.size(); i++) {
      Track track = (Track) spares.elementAt(i);
      track = sparesDatabase.add(track);
      track.setWeight(correlation);
    }
    ref.discard();
  }

  public float getCorrelation() {
    return correlation;
  }

  public ServerDatabase getSpares() {
    return sparesDatabase;
  }
}
