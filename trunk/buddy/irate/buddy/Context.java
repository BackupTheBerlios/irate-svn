package irate.buddy;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

public class Context {

  public final Logger logger = Logger.getLogger("Buddy");

  public final Environment env;

  private StoredClassCatalog classCatalogue;

  public Context(Environment env) {
    this.env = env;

    logger.setUseParentHandlers(false);
    logger.setLevel(Level.FINE);
    Handler handler = new ConsoleHandler();
    handler.setFormatter(new BuddyFormatter());
    handler.setLevel(Level.FINE);
    logger.addHandler(handler);
  }

  public StoredClassCatalog getClassCatalogue(Transaction transaction)
      throws DatabaseException {
    if (classCatalogue == null) {
      Db db = new Db(this, transaction, "classCatalogue.db");
      classCatalogue = new StoredClassCatalog(db.getDatabase());
    }
    return classCatalogue;
  }

  public void close() {
    try {
      if (classCatalogue != null) {
        classCatalogue.close();
      }
      env.close();
    }
    catch (DatabaseException e) {
      e.printStackTrace();
    }
  }
}
