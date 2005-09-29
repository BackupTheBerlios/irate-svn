package irate.buddy;

import java.util.Arrays;
import java.util.Random;

import com.sleepycat.je.DatabaseEntry;

public class UserId {
  private byte[] bytes;

  public UserId(Random random) {
    bytes = new byte[8];
    random.nextBytes(bytes);
  }

  public UserId(DatabaseEntry dbEntry) {
    this.bytes = dbEntry.getData();
  }

  public DatabaseEntry createDatabaseEntry() {
    return new DatabaseEntry(bytes);
  }

  public boolean equals(Object object) {
    if (getClass() != object.getClass())
      return false;
    return Arrays.equals(bytes, ((UserId) object).bytes);
  }

  public String toString() {
    long l = 0;
    for (byte b : bytes) {
      l = (l << 8) + b;
    }
    return Long.toString(l);
  }
}
