package irate.buddy;

import java.security.SecureRandom;
import java.util.Random;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public class PasswordDb {

  private final Db db;

  private Random random = new SecureRandom();

  public PasswordDb(Context context, Transaction transaction)
      throws DatabaseException {
    db = new Db(context, transaction, "password.db");
  }

  public String getPassword(Transaction transaction, UniqueId userId)
      throws DatabaseException {
    DatabaseEntry userIdEntry = userId.createDatabaseEntry();
    DatabaseEntry passwordEntry = new DatabaseEntry();
    OperationStatus status = db.getDatabase().get(transaction, userIdEntry,
        passwordEntry, LockMode.DEFAULT);

    if (status != OperationStatus.SUCCESS)
      throw new DatabaseOperationFailedException("Retrieving password");

    return new String(passwordEntry.getData());
  }

  public UniqueId addPassword(Transaction transaction, String password)
      throws DatabaseException {
    DatabaseEntry passwordEntry = new DatabaseEntry(password.getBytes());
    while (true) {
      UniqueId userId = new UniqueId(random);
      DatabaseEntry userIdEntry = userId.createDatabaseEntry();
      OperationStatus status = db.getDatabase().putNoOverwrite(transaction,
          userIdEntry, passwordEntry);

      if (status == OperationStatus.SUCCESS)
        return userId;

      if (status != OperationStatus.KEYEXIST)
        throw new DatabaseOperationFailedException("Adding password");

      // log something
    }
  }

  public void close() {
    db.close();
  }
}
