/*
 * Created on 27/09/2005
 */
package irate.buddy;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import com.sleepycat.collections.CurrentTransaction;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.collections.TransactionWorker;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.TransactionConfig;

public class SessionRpc {

  private final Context context;

  private final TransactionRunner transactionRunner;

  private Session session;

  public SessionRpc(Context context) throws DatabaseException {
    this.context = context;
    transactionRunner = new TransactionRunner(context.env);
    session = new Session(context, null);
  }

  public String login(String account, String password/*, boolean create*/) {
    try {
        boolean create = true;
      UniqueId userId = session.login(account, password, create);
      return userId.toString();
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void logout(String userIdString) {
    UniqueId userId = new UniqueId(userIdString);
    session.logout(userId);
  }

  public void updateDatabase(final String userIdString, final Vector trackData) {
    try {
      transactionRunner.run(new TransactionWorker() {
        public void doWork() {
          UniqueId userId = new UniqueId(userIdString);
          if (!session.verify(userId))
            return;

          System.out.println("### Trackdatabase ###");
          for (Object trackObject : trackData) {
            Hashtable<String, String> track = (Hashtable<String, String>) trackObject;
            for (String key : track.keySet()) {
              String value = track.get(key);
              System.out.println(key + " " + value);
            }
          }
          System.out.println("---|---");
        }
      });
    } catch (Exception e) {
      context.logger.log(Level.SEVERE, "Database update failed", e);
    }
  }

  public void close() {
    if (session != null) {
      session.close();
      session = null;
    }
  }
}
