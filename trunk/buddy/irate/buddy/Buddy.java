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

  private Environment env;

  private Session session;
  
  public Buddy(boolean allowCreate) throws DatabaseException {
    env = openEnvironment(allowCreate);
    session = new Session(null, env);
  }

  private Environment openEnvironment(boolean allowCreate)
      throws DatabaseException {
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setTransactional(true);
    envConfig.setAllowCreate(allowCreate);
    envConfig.setCacheSize(1000000);

    return new Environment(new File("."), envConfig);
  }

  public String login(String account, String password, boolean create) {
    try {
      UserId userId = session.login(account, password, create);
      return userId.toString();
    }
    catch (DatabaseException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public void logout(String userIdString) {
    UserId userId = new UserId(userIdString);
    session.logout(userId);
  }

  public void updateDatabase(String userIdString, Vector trackData) {
    UserId userId = new UserId(userIdString);
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

  public void close() {
    if (session != null) {
      session.close();
      session = null;
    }

    try {
      if (env != null) {
        env.close();
        env = null;
      }
    }
    catch (DatabaseException e) {
      e.printStackTrace();
    }
  }
}
