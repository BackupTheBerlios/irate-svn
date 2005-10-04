/*
 * Created on 4/10/2005
 */
package irate.buddy;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.collections.TransactionWorker;

public class DataRpc {

  private final Context context;

  private final Session session;

  private Data data;

  private final TransactionRunner transactionRunner;

  public DataRpc(Context context, Session session, Data data) {
    this.context = context;
    this.session = session;
    this.data = data;
    transactionRunner = new TransactionRunner(context.env);
  }

  public void setTrackData(String sessionId, Vector<Hashtable<String, Object>> trackData) {
    context.logger.info("RPC: setTrackData " + sessionId);
    final UniqueId userId = new UniqueId(sessionId);
    if (!session.verify(userId)) return;

    int noOfTracksUpdated = 0;
    for (final Hashtable<String, Object> track: trackData) {
      context.logger.finest("Track:");
      for (String key : track.keySet()) {
        Object object = track.get(key);
        context.logger.finest("  " + key + " " + object);
      }
      try {
        transactionRunner.run(new TransactionWorker() {

          public void doWork() {
            String key = (String) track.get("trackId");

            UniqueId trackId;
            if (key == null) {
              String url = (String) track.get("url");
              trackId = data.getTrackId(url);
            }
            else {
              trackId = new UniqueId(key);
            }
            Number rating = (Number) track.get("rating");

            data.updateTrack(userId, trackId, rating.floatValue());
          }
        });
        noOfTracksUpdated++;
      }
      catch (Exception e) {
        e.printStackTrace();
        context.logger.log(Level.FINER, "Database update failed", e);
      }
    }
    context.logger.fine("Updated " + noOfTracksUpdated); 
  }
}
