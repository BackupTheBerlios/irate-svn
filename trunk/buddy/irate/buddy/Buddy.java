/*
 * Created on 27/09/2005
 */
package irate.buddy;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;

public class Buddy {

  private final boolean allowCreate;

  private EnvironmentConfig envConfig;

  private Environment env;

  private DatabaseConfig dbConfig;

  private Database userDb;

  public Buddy(boolean allowCreate) throws DatabaseException {
    this.allowCreate = allowCreate;
    openEnvironment();
    openDatabase();
  }

  private void openEnvironment() throws DatabaseException {
    envConfig = new EnvironmentConfig();
    envConfig.setTransactional(true);
    envConfig.setAllowCreate(allowCreate);
    envConfig.setCacheSize(1000000);

    env = new Environment(new File("."), envConfig);
  }

  private void openDatabase() throws DatabaseException {
    dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);

    userDb = env.openDatabase(null, "user.db", dbConfig);
  }

  public String login(String account, String password) {
    try {
      DatabaseEntry accountEntry = new DatabaseEntry(account.getBytes());
      DatabaseEntry passwordEntry = new DatabaseEntry();
      userDb.get(null, accountEntry, passwordEntry, LockMode.DEFAULT);
      return "sessionId";
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String addUser(String account, String password) {
    return null;
  }

  public void setRatings(String session, Hashtable hashtable) {
    System.out.println("### Trackdatabase ###");
    for (Iterator itr = hashtable.keySet().iterator(); itr.hasNext();) {
      String key = (String) itr.next();
      Number rating = (Number) hashtable.get(key);
      System.out.println(key + " " + rating);
    }
    System.out.println("---|---");
  }

  public void close() {
    try {
      if (userDb != null)
        userDb.close();
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }

  public void finalise() {
    close();
  }
}
