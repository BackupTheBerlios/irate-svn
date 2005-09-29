package irate.buddy;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

public class Session {
  private Environment env;

  private UserDb userDb;

  private PasswordDb passwordDb;

  public Session(Transaction transaction, Environment env)
      throws DatabaseException {
    this.env = env;
    userDb = new UserDb(transaction, env);
    passwordDb = new PasswordDb(transaction, env);
  }

  public UserId login(String account, String password, boolean create)
      throws DatabaseException {

    Transaction transaction = env.beginTransaction(null, null);

    UserId userId = userDb.getUserId(transaction, account);
    if (userId == null) {
      if (!create)
        return null;
      
      userId = passwordDb.addPassword(transaction, password);
    }
    return userId;
  }
}
