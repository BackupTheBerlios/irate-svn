package irate.buddy;

import java.util.Vector;

public class TrackRpc {

	private final Context context;
	private final TrackApi trackApi;
	private final SessionApi sessionApi;

	public TrackRpc(Context context, SessionApi sessionApi, TrackApi trackApi) {
		this.context = context;
		this.sessionApi = sessionApi;
		this.trackApi = trackApi;
	}
	
	public Vector getTrackData(String sessionId, Vector tracksId) {
		// fetch track data for the given list of track IDs 
		return null;
	}

}
