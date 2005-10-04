/*
 * Created on 4/10/2005
 */
package irate.buddy;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


public class BuddyFormatter extends Formatter {
  private static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
  public String format(LogRecord record) {
    Date date = new Date(record.getMillis());
    return dateFormat.format(date) + "  " + record.getMessage() + "\n";
  }
}
