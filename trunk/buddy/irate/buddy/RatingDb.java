package irate.buddy;

import irate.buddy.Rating.RatingBinding;
import irate.buddy.Rating.RatingKey;

import java.util.Collection;
import java.util.Map;

import com.sleepycat.bind.EntityBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.BtreeStats;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseStats;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.Transaction;

public class RatingDb {
	private final Db db;

	private SecondaryDatabase userRatingDb;

	private SerialBinding ratingKeyBinding;

	private EntityBinding ratingEntityBinding;

	private SerialBinding uniqueIdBinding;

	private Map<RatingKey, Rating> map;

	private StoredMap userRatingMap;

	@SuppressWarnings("unchecked")
	public RatingDb(Context context, Transaction transaction)
			throws DatabaseException {
		db = new Db(context, transaction, "rating.db");
		StoredClassCatalog classCatalogue = context
				.getClassCatalogue(transaction);

		ratingKeyBinding = new SerialBinding(classCatalogue, RatingKey.class);
		ratingEntityBinding = new RatingBinding(classCatalogue);
		map = new StoredMap(db.getDatabase(),
				ratingKeyBinding, ratingEntityBinding, true);

		openUserRatingDb(context, transaction, classCatalogue);
		uniqueIdBinding = new SerialBinding(classCatalogue, UniqueId.class);
		userRatingMap = new StoredMap(
				userRatingDb, uniqueIdBinding, ratingEntityBinding, true);
	}

	private void openUserRatingDb(Context context, Transaction transaction,
			StoredClassCatalog classCatalogue) throws DatabaseException {
		SecondaryConfig secConfig = new SecondaryConfig();
		secConfig.setTransactional(true);
		secConfig.setAllowCreate(true);
		secConfig.setAllowPopulate(true);
		secConfig.setSortedDuplicates(true);

		secConfig.setKeyCreator(new RatingByUserKeyCreator(classCatalogue));
		String dbName = "rating_user_index.db";
		context.logger.finer("Opening " + dbName);
		userRatingDb = context.env.openSecondaryDatabase(transaction, dbName,
				db.getDatabase(), secConfig);

		DatabaseStats stats = userRatingDb.getStats(null);
		if (stats instanceof BtreeStats) {
			BtreeStats btreeStats = (BtreeStats) stats;
			context.logger.finest("Leaf nodes: "
					+ btreeStats.getLeafNodeCount());
		}
	}

	public Map<RatingKey, Rating> getMap() {
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Rating> getRatings(UniqueId userId)
	{
		return userRatingMap.duplicates(userId);
	}

	public void close() {
		try {
			userRatingDb.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		db.close();
	}

}
