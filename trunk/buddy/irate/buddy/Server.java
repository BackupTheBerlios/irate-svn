/*
 * Created on 27/09/2005
 */
package irate.buddy;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.xmlrpc.WebServer;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class Server {

  public static void main(String args[]) {

    String account = args[0];
    String password = args[1];

    Logger.getLogger("global");
    Logger.global.setLevel(Level.FINEST);
    Handler handler = new ConsoleHandler();
    handler.setFormatter(new SimpleFormatter());
    Logger.global.addHandler(handler);

    try {
      Server server = new Server();
      
      String userId = server.sessionRpc.login(account, password, true);
      System.out.println("Login: " + userId);
      server.sessionRpc.logout(userId);

      
      
      // server.startWebServer();
      server.close();
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }

  private Environment env;

  private SessionRpc sessionRpc;

  public Server() throws DatabaseException {
    env = openEnvironment(true);
    sessionRpc = new SessionRpc(env);
  }
  
  public void startWebServer() {
     WebServer webServer = new WebServer(8031);
     webServer.addHandler("Session", sessionRpc);
     webServer.start();      
  }

  private Environment openEnvironment(boolean allowCreate)
      throws DatabaseException {
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setTransactional(true);
    envConfig.setAllowCreate(allowCreate);
    envConfig.setCacheSize(1000000);

    return new Environment(new File("."), envConfig);
  }

  public void close() {
    if (sessionRpc != null) {
      sessionRpc.close();
      sessionRpc = null;
    }
    try {
      if (env != null) {
        env.close();
        env = null;
      }
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }

}
