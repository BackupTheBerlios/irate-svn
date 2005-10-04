package irate.buddy;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public class UserDb  {

  private final Db db;

  public UserDb(Context context, Transaction transaction) throws DatabaseException {
      db = new Db(context, transaction, "user.db");
  }

  public UniqueId getUserId(Transaction transaction, String account)
      throws DatabaseException {
    DatabaseEntry accountEntry = new DatabaseEntry(account.getBytes());
    DatabaseEntry userIdEntry = new DatabaseEntry();
    OperationStatus status = db.getDatabase().get(transaction, accountEntry,
        userIdEntry, LockMode.DEFAULT);

    if (status != OperationStatus.SUCCESS)
        return null;

    return new UniqueId(userIdEntry);
  }

  public void addUser(Transaction transaction, String account, UniqueId userId)
      throws DatabaseException {
    DatabaseEntry accountEntry = new DatabaseEntry(account.getBytes());
    DatabaseEntry userIdEntry = userId.createDatabaseEntry();
    OperationStatus status = db.getDatabase().putNoOverwrite(transaction, accountEntry,
        userIdEntry);

    if (status != OperationStatus.SUCCESS)
      throw new DatabaseOperationFailedException("Adding user account");
  }
  
  public void close() {
   db.close();   
  }
}
