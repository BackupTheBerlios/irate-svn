/*
 * Created on 4/10/2005
 */
package irate.buddy;

import java.util.ArrayList;
import java.util.List;

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

	public void updateTrack(Rating rating) {
		ratingDb.getMap().put(rating.getKey(), rating);
	}

	public List<UniqueId> getTracks(UniqueId userId) {
		List<UniqueId> tracks = new ArrayList<UniqueId>();
		return tracks;
	}

}
