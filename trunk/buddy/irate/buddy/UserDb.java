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

  public UserDb(Transaction transaction, Environment env) throws DatabaseException {
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(true);

    database = env.openDatabase(transaction, "user.db", dbConfig);
  }

  public UserId getUserId(Transaction transaction, String account)
      throws DatabaseException {
    DatabaseEntry accountEntry = new DatabaseEntry(account.getBytes());
    DatabaseEntry userIdEntry = new DatabaseEntry();
    OperationStatus status = database.get(transaction, accountEntry,
        userIdEntry, LockMode.DEFAULT);

    if (status != OperationStatus.SUCCESS)
        return null;
//      throw new DatabaseOperationFailedException("Retrieving user account");

    return new UserId(userIdEntry);
  }

  public void addUser(Transaction transaction, String account, UserId userId)
      throws DatabaseException {
    DatabaseEntry accountEntry = new DatabaseEntry(account.getBytes());
    DatabaseEntry userIdEntry = userId.createDatabaseEntry();
    OperationStatus status = database.putNoOverwrite(transaction, accountEntry,
        userIdEntry);

    if (status != OperationStatus.SUCCESS)
      throw new DatabaseOperationFailedException("Adding user account");
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
