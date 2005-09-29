package irate.buddy;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public class UserDb {

  private Database database;

  public UserDb(Environment env) throws DatabaseException {
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);

    database = env.openDatabase(null, "user.db", dbConfig);
  }

  public long getUserId(Transaction transaction, String account) throws DatabaseException {
    DatabaseEntry accountEntry = new DatabaseEntry(account.getBytes());
    DatabaseEntry userIdEntry = new DatabaseEntry();
    OperationStatus status = database.get(transaction, accountEntry, userIdEntry,
        LockMode.DEFAULT);

    if (status != OperationStatus.SUCCESS)
      throw new BuddyDatabaseException();

    return Long.parseLong(new String(userIdEntry.getData()));
  }

  public void addUser(Transaction transaction, String account, long userId) throws DatabaseException {
    DatabaseEntry accountEntry = new DatabaseEntry(account.getBytes());
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
