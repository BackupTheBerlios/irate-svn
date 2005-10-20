/*
 * Created on 4/10/2005
 */
package irate.buddy;

import java.security.SecureRandom;
import java.util.Random;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;

public class RatingApi {

  private TrackDb trackDb;

  private UrlDb urlDb;

  private RatingDb ratingDb;

  public RatingApi(Context context, Transaction transaction)
      throws DatabaseException {
    urlDb = new UrlDb(context, transaction);
    ratingDb = new RatingDb(context, transaction);
  }

  public void updateTrack(UniqueId userId, UniqueId trackId, float ratingValue) {
    RatingKey ratingKey = new RatingKey();
    ratingKey.userId = userId;
    ratingKey.trackId = trackId;

    Rating rating = new Rating();
    rating.rating = ratingValue;
    ratingDb.getMap().put(ratingKey, rating);
  }
  
//  public List<UniqueId> getTracks(UniqueId userId) {
//    List<UniqueId> tracks = new ArrayList<UniqueId>();
//    return tracks;
//  }

}
