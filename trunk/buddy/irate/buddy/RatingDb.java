package irate.buddy;

import java.util.Map;

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;

public class RatingDb {
  private final Db db;

  private SerialBinding ratingKeyBinding;

  private SerialBinding ratingBinding;

  private Map<RatingKey, Rating> map;

  public RatingDb(Context context, Transaction transaction)
      throws DatabaseException {
    db = new Db(context, transaction, "rating.db");
    StoredClassCatalog classCatalogue = context.getClassCatalogue(transaction);

    ratingKeyBinding = new SerialBinding(classCatalogue, RatingKey.class);
    ratingBinding = new SerialBinding(classCatalogue, Rating.class);
    map = (Map<RatingKey, Rating>) new StoredMap(db.getDatabase(),
        ratingKeyBinding, ratingBinding, true);
  }

  public Map<RatingKey, Rating> getMap() {
    return map;
  }

  public void close() {
    db.close();
  }

}
