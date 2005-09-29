/*
 * Created on 27/09/2005
 */
package irate.buddy;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class Buddy {

  private final boolean allowCreate;

  private Environment env;

  public Buddy(boolean allowCreate) throws DatabaseException {
    this.allowCreate = allowCreate;
    openEnvironment();
  }

  private void openEnvironment() throws DatabaseException {
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setTransactional(true);
    envConfig.setAllowCreate(allowCreate);
    envConfig.setCacheSize(1000000);

    env = new Environment(new File("."), envConfig);
  }

  public String login(String account, String password, boolean create) {
    try {
        UserId userId = session.login(account, password, create);
        return userId.toString();
    } catch (DatabaseException e) {
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
      if (env != null) {
        env.close();
        env = null;
      }
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }

  public void finalize() {
    close();
  }
}
