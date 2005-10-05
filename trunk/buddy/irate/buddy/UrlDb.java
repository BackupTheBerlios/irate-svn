/*
 * Created on 4/10/2005
 */
package irate.buddy;

import java.util.Map;

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;

public class UrlDb {

  private final Db db;

  private SerialBinding stringBinding;;

  private SerialBinding uniqueIdBinding;

  private Map<String, UniqueId> map;

  public UrlDb(Context context, Transaction transaction)
      throws DatabaseException {
    db = new Db(context, transaction, "url.db");
    StoredClassCatalog classCatalogue = context.getClassCatalogue(transaction);

    uniqueIdBinding = new SerialBinding(classCatalogue, UniqueId.class);
    stringBinding = new SerialBinding(classCatalogue, String.class);
    map = (Map<String, UniqueId>) new StoredMap(db.getDatabase(),
        stringBinding, uniqueIdBinding, true);
  }

  public Map<String, UniqueId> getMap() {
    return map;
  }

  public void close() {
    db.close();
  }

}
