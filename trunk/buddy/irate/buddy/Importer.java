package irate.buddy;

import irate.common.TrackDatabase;

import java.io.File;

import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.collections.TransactionWorker;
import com.sleepycat.je.DatabaseException;

public class Importer {

	private final TrackApi trackApi;

	private final Context context;

	public Importer(Context context, TrackApi trackApi) {
		this.context = context;
		this.trackApi = trackApi;
	}

	public void importFile(final File file) throws DatabaseException, Exception {
		TrackDatabase trackDatabase = new TrackDatabase(file);
		final irate.common.Track[] commonTracks = trackDatabase.getTracks();
		context.logger.fine("Importer: importing " + commonTracks.length
				+ " tracks from " + file);
		new TransactionRunner(context.env).run(new TransactionWorker() {
			public void doWork() {
				for (irate.common.Track commonTrack : commonTracks) {
					Track track = new Track(commonTrack.getURL().toString(),
							commonTrack.getArtist(), commonTrack.getTitle());
					trackApi.addTrack(track);
					// context.logger.finest(track.url);
				}
			}
		});
	}
}
