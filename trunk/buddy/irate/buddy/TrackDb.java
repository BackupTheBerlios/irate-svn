package irate.buddy;

import java.util.Map;

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;

public class TrackDb {

	private final Db db;

	private SerialBinding uniqueIdBinding;

	private SerialBinding trackBinding;

	private Map<UniqueId, Track> map;

	@SuppressWarnings("unchecked")
	public TrackDb(Context context, Transaction transaction)
			throws DatabaseException {
		db = new Db(context, transaction, "track.db");
		StoredClassCatalog classCatalogue = context
				.getClassCatalogue(transaction);

		uniqueIdBinding = new SerialBinding(classCatalogue, UniqueId.class);
		trackBinding = new SerialBinding(classCatalogue, Track.class);
		map = (Map<UniqueId, Track>) new StoredMap(db.getDatabase(),
				uniqueIdBinding, trackBinding, true);
	}

	public Map<UniqueId, Track> getMap() {
		return map;
	}

	public void close() {
		db.close();
	}
}
