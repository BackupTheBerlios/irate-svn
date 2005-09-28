/*
 * Created on 27/09/2005
 */
package irate.buddy;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

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

  public String login(String account, String password, boolean create) {
    DatabaseEntry accountEntry = new DatabaseEntry(account.getBytes());
    DatabaseEntry passwordEntry = new DatabaseEntry();
    try {
      OperationStatus status = userDb.get(null, accountEntry, passwordEntry,
          LockMode.DEFAULT);

      System.out.println("Status: " + status);
      if (status == OperationStatus.SUCCESS) {
        String storedPassword = new String(passwordEntry.getData());
        if (password.equals(storedPassword)) return "sessionId";
      }
      else if (status == OperationStatus.NOTFOUND) {
        if (create) {
          passwordEntry = new DatabaseEntry(password.getBytes());
          status = userDb.putNoOverwrite(null, accountEntry, passwordEntry);
          System.out.println("Write status: " + status);
          if (status == OperationStatus.SUCCESS) return "sessionId";
        }
      }
    }
    catch (DatabaseException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void updateDatabase(String session, Vector trackData) {
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

  public void close() {
    try {
      if (userDb != null) userDb.close();
      if (env != null) env.close();
    }
    catch (DatabaseException e) {
      e.printStackTrace();
    }
  }

  public void finalise() {
    close();
  }
}
