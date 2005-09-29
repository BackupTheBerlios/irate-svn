package irate.buddy;

import java.security.SecureRandom;
import java.util.Random;

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

  private Random random = new SecureRandom();

  public PasswordDb(Transaction transaction, Environment env) throws DatabaseException {
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(true);

    database = env.openDatabase(transaction, "password.db", dbConfig);
  }

  public String getPassword(Transaction transaction, UserId userId)
      throws DatabaseException {
    DatabaseEntry userIdEntry = userId.createDatabaseEntry();
    DatabaseEntry passwordEntry = new DatabaseEntry();
    OperationStatus status = database.get(transaction, userIdEntry,
        passwordEntry, LockMode.DEFAULT);

    if (status != OperationStatus.SUCCESS)
      throw new DatabaseOperationFailedException("Retrieving password");

    return new String(passwordEntry.getData());
  }

  public UserId addPassword(Transaction transaction, String password)
      throws DatabaseException {
    DatabaseEntry passwordEntry = new DatabaseEntry(password.getBytes());
    while (true) {
      UserId userId = new UserId(random);
      DatabaseEntry userIdEntry = userId.createDatabaseEntry();
      OperationStatus status = database.putNoOverwrite(transaction,
          userIdEntry, passwordEntry);

      if (status == OperationStatus.SUCCESS)
        return userId;

      if (status != OperationStatus.KEYEXIST)
          throw new DatabaseOperationFailedException("Adding password");
      
      // log something      
    }
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
