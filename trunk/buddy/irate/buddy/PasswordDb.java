package irate.buddy;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public class PasswordDb {

  private Database database;

  public PasswordDb(Environment env) throws DatabaseException {
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);

    database = env.openDatabase(null, "password.db", dbConfig);
  }

  public long getPassword(Transaction transaction, long userId) throws DatabaseException {
    DatabaseEntry accountEntry = new DatabaseEntry(Long.toString(userId).getBytes());
    DatabaseEntry userIdEntry = new DatabaseEntry();
    OperationStatus status = database.get(transaction, accountEntry, userIdEntry,
        LockMode.DEFAULT);

    if (status != OperationStatus.SUCCESS)
      throw new BuddyDatabaseException();

    return Long.parseLong(new String(userIdEntry.getData()));
  }

  public long addPassword(Transaction transaction, String password) throws DatabaseException {
    DatabaseEntry accountEntry = new DatabaseEntry(Long.toString(userId).getBytes());
    DatabaseEntry userIdEntry = new DatabaseEntry(Long.toString(userId)
        .getBytes());
    OperationStatus status = database.putNoOverwrite(transaction, accountEntry,
        userIdEntry);
    
    if (status != OperationStatus.SUCCESS)
      throw new BuddyDatabaseException();
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

  public void finalize() {
    close();
  }

}
