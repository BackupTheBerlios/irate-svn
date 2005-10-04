/*
 * Created on 27/09/2005
 */
package irate.buddy;

import java.io.File;

import org.apache.xmlrpc.WebServer;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;

public class Server {

  public static void main(String args[]) {

    String account = args[0];
    String password = args[1];

    try {
      Server server = new Server();

      // String userId = server.sessionRpc.login(account, password, true);
      // System.out.println("Login: " + userId);
      // server.sessionRpc.logout(userId);

      server.startWebServer();
//      server.close();
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }

  private Context context;

  private SessionRpc sessionRpc;
  
  private DataRpc dataRpc;

  public Server() throws DatabaseException {
    context = new Context(openEnvironment(true));

    Transaction transaction = context.env.beginTransaction(null, null);
    Session session = new Session(context, transaction);
    sessionRpc = new SessionRpc(context, session);
    
    Data data = new Data(context, transaction);
    dataRpc = new DataRpc(context, session, data);
    
    transaction.commit();
    
  }

  public void startWebServer() {
    context.logger.fine("Starting web server");
    WebServer webServer = new WebServer(8031);
    webServer.addHandler("Session", sessionRpc);
    webServer.addHandler("Data", dataRpc);
    webServer.start();
    context.logger.fine("Server running");
  }

  private Environment openEnvironment(boolean allowCreate)
      throws DatabaseException {
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setTransactional(true);
    envConfig.setAllowCreate(allowCreate);
    envConfig.setCacheSize(16000000);

    return new Environment(new File("."), envConfig);
  }

  public void close() {
    if (sessionRpc != null) {
//      sessionRpc.close();
      sessionRpc = null;
    }
    if (context != null) {
      context.close();
      context = null;
    }
  }

}
