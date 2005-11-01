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

		// String account = args[0];
		// String password = args[1];

		try {
			Server server = new Server();

			// String userId = server.sessionRpc.login(account, password, true);
			// System.out.println("Login: " + userId);
			// server.sessionRpc.logout(userId);

			// server.populate();
			server.startWebServer();
			// server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Context context;

	private SessionRpc sessionRpc;

	private RatingApi ratingApi;

	private RatingRpc ratingRpc;

	private TrackApi trackApi;

	private TrackRpc trackRpc;

	public Server() throws DatabaseException {
		context = new Context(openEnvironment(true));

		Transaction transaction = context.env.beginTransaction(null, null);
		SessionApi session = new SessionApi(context, transaction);
		sessionRpc = new SessionRpc(context, session);

		trackApi = new TrackApi(context, transaction);
		trackRpc = new TrackRpc(context, session, trackApi);

		ratingApi = new RatingApi(context, transaction);
		ratingRpc = new RatingRpc(context, session, ratingApi, trackApi);

		transaction.commit();

	}

	private void populate() throws DatabaseException, Exception {
		Importer importer = new Importer(context, trackApi);
		importer.importFile(new File("masterdatabase.xml"));
	}

	public void startWebServer() {
		context.logger.fine("Starting web server");
		WebServer webServer = new WebServer(8031);
		webServer.addHandler("Session", sessionRpc);
		webServer.addHandler("Track", trackRpc);
		webServer.addHandler("Rating", ratingRpc);
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
