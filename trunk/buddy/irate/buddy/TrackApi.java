package irate.buddy;

import java.security.SecureRandom;
import java.util.Random;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;

public class TrackApi {
	private TrackDb trackDb;

	private UrlDb urlDb;

	private Random random = new SecureRandom();

	public TrackApi(Context context, Transaction transaction)
			throws DatabaseException {
		trackDb = new TrackDb(context, transaction);
		urlDb = new UrlDb(context, transaction);
	}

	public UniqueId getTrackId(String url) {
		UniqueId trackId = urlDb.getMap().get(url);
		if (trackId == null) {
			do {
				trackId = new UniqueId(random);
			} while (trackDb.getMap().containsKey(trackId));

			Track track = new Track(url, "", "");
			track.url = url;
			trackDb.getMap().put(trackId, track);
			urlDb.getMap().put(url, trackId);
		}
		return trackId;
	}

	public Track getTrack(UniqueId trackId) {
		return trackDb.getMap().get(trackId);
	}

	public void addTrack(Track track) {
		UniqueId trackId = getTrackId(track.url);
		if (trackDb.getMap().containsKey(trackId))
			return;

		trackDb.getMap().put(trackId, track);
	}
}
