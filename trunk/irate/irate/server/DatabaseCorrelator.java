package irate.server;

import irate.common.*;

import java.util.*;

public class DatabaseCorrelator {

  private ServerDatabase db0;
  private Track[] tracks1;
  private ServerDatabase sparesDatabase;
  private Vector spares;
  private float correlation;
  
  public DatabaseCorrelator(ServerDatabase db0, ServerDatabase db1) {
    this.db0 = db0;
    tracks1 = db1.getTracks();
  }

  public void process() {
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
      if (track1.isRated()) {
        float rating1 = track1.getRating();
        Track track0 = db0.getTrack(track1);
        if (track0 == null) {
            // It exists in db1 but not in db0 so it's a candidate for
            // returning to the user.
          spares.add(track1);
        }
        else {
          if (track0.isRated()) {
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
    float offset0 = total0 / intersectLength;
    float offset1 = total1 / intersectLength;
    for (int i = 0; i < intersectLength; i++) 
      sum += (intersect0[i] - offset0) * (intersect1[i] - offset1);
    correlation = sum / (total0 * total1);
    
      // Set the correlation value for each track.
    for (int i = 0; i < spares.size(); i++) {
      Track track = (Track) spares.elementAt(i);
      track = sparesDatabase.add(track);
      track.setRating(correlation * track.getRating());
    }
  }

  public float getCorrelation() {
    return correlation;
  }

  public ServerDatabase getSpares() {
    return sparesDatabase;
  }
}
