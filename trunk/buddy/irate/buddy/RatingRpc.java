/*
 * Created on 4/10/2005
 */
package irate.buddy;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.collections.TransactionWorker;

public class RatingRpc {

	private final Context context;

	private final SessionApi session;

	private RatingApi ratingApi;

	private final TransactionRunner transactionRunner;

	private final TrackApi trackApi;

	public RatingRpc(Context context, SessionApi session, RatingApi ratingApi,
			TrackApi trackApi) {
		this.context = context;
		this.session = session;
		this.ratingApi = ratingApi;
		this.trackApi = trackApi;
		transactionRunner = new TransactionRunner(context.env);
	}

	public void setTrackData(String sessionId,
			Vector<Hashtable<String, Object>> trackData) {
		context.logger.info("RPC: Rating.setTrackData " + sessionId);
		final UniqueId userId = new UniqueId(sessionId);
		if (!session.verify(userId))
			return;

		int noOfTracksUpdated = 0;
		for (final Hashtable<String, Object> track : trackData) {
			context.logger.finest("Track:");
			for (String key : track.keySet()) {
				Object object = track.get(key);
				context.logger.finest("  " + key + " " + object);
			}
			try {
				transactionRunner.run(new TransactionWorker() {
					public void doWork() {
						String key = (String) track.get("trackId");

						UniqueId trackId;
						if (key == null) {
							String url = (String) track.get("url");
							trackId = trackApi.getTrackId(url);
						} else {
							trackId = new UniqueId(key);
						}
						Number rating = (Number) track.get("rating");

						ratingApi.updateTrack(new Rating(userId, trackId, rating
								.floatValue()));
					}
				});
				noOfTracksUpdated++;
			} catch (Exception e) {
				e.printStackTrace();
				context.logger.log(Level.FINER, "Database update failed", e);
			}
		}
		context.logger.fine("Updated " + noOfTracksUpdated);
	}

	public Vector getTracks(String sessionId) {
		context.logger.info("RPC: Rating.getTracks " + sessionId);
		final UniqueId userId = new UniqueId(sessionId);
		if (!session.verify(userId))
			return null;

		Vector tracks = new Vector();
		try {
			ratingApi.getTracks(userId);
		} catch (Exception e) {
			e.printStackTrace();
			context.logger.log(Level.FINER, "Database update failed", e);
		}
		context.logger.finest("Got " + tracks.size() + " tracks");
		return tracks;
	}
}
