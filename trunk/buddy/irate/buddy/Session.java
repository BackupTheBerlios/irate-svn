package irate.buddy;

import java.util.HashSet;
import java.util.Set;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;

public class Session {

  private Context context;

  private UserDb userDb;

  private PasswordDb passwordDb;

  private Set<UniqueId> sessions = new HashSet<UniqueId>();

  public Session(Context context, Transaction transaction)
      throws DatabaseException {
    this.context = context;
    userDb = new UserDb(context, transaction);
    passwordDb = new PasswordDb(context, transaction);
  }

  public UniqueId login(String account, String password, boolean create)
      throws DatabaseException {

    Transaction transaction = context.env.beginTransaction(null, null);

    UniqueId userId;
    try {
        context.logger.fine("Logging in " + account);
      userId = userDb.getUserId(transaction, account);
      if (userId == null) {
        if (!create) return null;

        userId = passwordDb.addPassword(transaction, password);
        userDb.addUser(transaction, account, userId);
        context.logger.fine("New user id=" + userId + " account=" + account
            + " password=" + password);
      }
      else {
        String storedPassword = passwordDb.getPassword(transaction, userId);
        if (password.equals(storedPassword)) {
          synchronized (sessions) {
            sessions.add(userId);
          }
          context.logger.fine("Logged id=" + userId);
        }
        else {
          userId = null;
          context.logger.fine("Invalid password id=" + userId);
        }
      }
    }
    finally {
      transaction.commit();
    }
    return userId;
  }

  public void logout(UniqueId userId) {
    synchronized (sessions) {
      sessions.remove(userId);
    }
  }

  public boolean verify(UniqueId userId) {
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
