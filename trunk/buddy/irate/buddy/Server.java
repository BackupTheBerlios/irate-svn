/*
 * Created on 27/09/2005
 */
package irate.buddy;

import java.io.File;

import org.apache.xmlrpc.WebServer;

import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.collections.TransactionWorker;
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

      server.populate();
      server.startWebServer();
      // server.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Context context;

  private SessionRpc sessionRpc;

  private Data data;

  private DataRpc dataRpc;

  public Server() throws DatabaseException {
    context = new Context(openEnvironment(true));

    Transaction transaction = context.env.beginTransaction(null, null);
    Session session = new Session(context, transaction);
    sessionRpc = new SessionRpc(context, session);

    data = new Data(context, transaction);
    dataRpc = new DataRpc(context, session, data);

    transaction.commit();

  }

  private void populate() throws DatabaseException, Exception {
    new TransactionRunner(context.env).run(new TransactionWorker() {
      public void doWork() {
        data
            .addTrack(new Track("Beth Quist",
                "http://magnatune.com/all/03-Monsters-Beth%20Quist.mp3",
                "Monsters"));
        data
            .addTrack(new Track(
                "Delectric",
                "http://artists.iuma.com/dl/Delectric/audio/Delectric_-_You_Run_Your_Mouth.mp3",
                "You Run Your Mouth"));
        data
            .addTrack(new Track(
                "djBonez",
                "http://files.mp3.com.au/MP3/djBonez/djBonez-None-Blown%20Away%20-%20Feat.%20Sunspot%20Jonz.mp3",
                "Blown Away - Feat. Sunspot Jonz"));
        data
            .addTrack(new Track(
                "Evolution",
                "http://assets.artistdirect.com/Downloads/artd/listen/evolution-walkingonfire.mp3",
                "Walking on fire"));
        data.addTrack(new Track("S-Tribe",
            "http://artists.iuma.com/dl/STribe/audio/STribe_-_Sweet_Mary.mp3",
            "Sweet Mary"));
        data.addTrack(new Track("Seismic Anamoly",
            "http://magnatune.com/all/04-Jack%20Rabbit-Seismic%20Anamoly.mp3",
            "Jack Rabbit"));
      }
    });
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
      // sessionRpc.close();
      sessionRpc = null;
    }
    if (context != null) {
      context.close();
      context = null;
    }
  }

}
