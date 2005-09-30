package irate.buddy;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

public class Db {

  private Database database;

  public Db(Transaction transaction, Environment env, String dbName)
      throws DatabaseException {
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(env.getConfig().getAllowCreate());
    dbConfig.setTransactional(env.getConfig().getTransactional());

    database = env.openDatabase(transaction, dbName, dbConfig);
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
