package irate.buddy;

import com.sleepycat.je.BtreeStats;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseStats;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

public class Db {

	private Database database;

	public Db(Context context, Transaction transaction, String dbName)
			throws DatabaseException {
		Environment env = context.env;

		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(env.getConfig().getAllowCreate());
		dbConfig.setTransactional(env.getConfig().getTransactional());

		context.logger.finer("Opening " + dbName);
		database = env.openDatabase(transaction, dbName, dbConfig);
		DatabaseStats stats = database.getStats(null);
		if (stats instanceof BtreeStats) {
			BtreeStats btreeStats = (BtreeStats) stats;
			context.logger.finest("Leaf nodes: "
					+ btreeStats.getLeafNodeCount());
		}
	}

	public Database getDatabase() {
		return database;
	}

	public void close() {
		try {
			if (database != null) {
				database.close();
				database = null;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

}
