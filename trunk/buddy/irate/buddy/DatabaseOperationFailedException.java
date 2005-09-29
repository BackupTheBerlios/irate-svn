package irate.buddy;

import com.sleepycat.je.DatabaseException;

public class DatabaseOperationFailedException extends DatabaseException {
  public DatabaseOperationFailedException(String message) {
    super(message);
  }
}
