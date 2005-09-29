package irate.buddy;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

public class Session {

  private Environment env;

  private UserDb userDb;

  private PasswordDb passwordDb;

  private Set<UserId> sessions = new HashSet<UserId>();

  public Session(Transaction transaction, Environment env)
      throws DatabaseException {
    this.env = env;
    userDb = new UserDb(transaction, env);
    passwordDb = new PasswordDb(transaction, env);
  }

  public UserId login(String account, String password, boolean create)
      throws DatabaseException {

    Transaction transaction = env.beginTransaction(null, null);

    UserId userId;
    try {
      userId = userDb.getUserId(transaction, account);
      if (userId == null) {
        if (!create) return null;

        userId = passwordDb.addPassword(transaction, password);
        userDb.addUser(transaction, account, userId);
        Logger.global.fine("New user id=" + userId + " account=" + account
            + " password=" + password);
      }
      else {
        String storedPassword = passwordDb.getPassword(transaction, userId);
        if (password.equals(storedPassword)) {
          synchronized (sessions) {
            sessions.add(userId);
          }
          Logger.global.fine("Logged id=" + userId);
        }
        else {
          userId = null;
          Logger.global.fine("Invalid password id=" + userId);
        }
      }
    }
    finally {
      transaction.commit();
    }
    return userId;
  }

  public void logout(UserId userId) {
    synchronized (sessions) {
      sessions.remove(userId);
    }
  }

  public boolean verify(UserId userId) {
    synchronized (sessions) {
      return sessions.contains(userId);
    }
  }

  public void close() {
    if (userDb != null) {
      userDb.close();
      userDb = null;
    }
    if (passwordDb != null) {
      passwordDb.close();
      passwordDb = null;
    }
  }
}
