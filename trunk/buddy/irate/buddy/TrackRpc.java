package irate.buddy;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
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

    public Vector getDetails(String sessionId, Vector<String> trackIds) {
        context.logger.info("RPC: Track.getDetails " + sessionId);
        // fetch track data for the given list of track IDs
        Vector<Hashtable<String, String>> tracks = new Vector<Hashtable<String, String>>();
        for (String trackIdString : trackIds) {
            UniqueId trackId = new UniqueId(trackIdString);
            Track track = trackApi.getTrack(trackId);
            Hashtable<String, String> trackDetails = new Hashtable<String, String>();
            trackDetails.put("trackId", trackId.toString());
            trackDetails.put("artist", track.artist);
            trackDetails.put("title", track.title);
            trackDetails.put("url", track.url);
            
            if (track.www != null)
                trackDetails.put("www", track.www);
            tracks.add(trackDetails);
        }
        return tracks;
    }
}
