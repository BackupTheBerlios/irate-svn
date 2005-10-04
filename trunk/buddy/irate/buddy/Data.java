/*
 * Created on 4/10/2005
 */
package irate.buddy;

import java.security.SecureRandom;
import java.util.Random;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;

public class Data {

  private TrackDb trackDb;

  private UrlDb urlDb;

  private RatingDb ratingDb;
  
  private Random random = new SecureRandom();

  public Data(Context context, Transaction transaction)
      throws DatabaseException {
    trackDb = new TrackDb(context, transaction);
    urlDb = new UrlDb(context, transaction);
    ratingDb = new RatingDb(context, transaction);
  }
  
  public UniqueId getTrackId(String url) {
    UniqueId trackId = urlDb.getMap().get(url);
    if (trackId == null) {
      do {
        trackId = new UniqueId(random);
      }
      while (trackDb.getMap().containsKey(trackId));
      
      Track track = new Track();
      track.url = url;
      trackDb.getMap().put(trackId, track);
      urlDb.getMap().put(url, trackId);
    }
    return trackId;
  }
  
  public void updateTrack(UniqueId userId, UniqueId trackId, float ratingValue) {
    RatingKey ratingKey = new RatingKey();
    ratingKey.userId = userId;
    ratingKey.trackId = trackId;
    
    Rating rating = new Rating();
    rating.rating = ratingValue;
    ratingDb.getMap().put(ratingKey, rating);
  }
}
