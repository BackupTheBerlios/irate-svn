/*
 * Created on Oct 25, 2003
 */
package irate.common;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

/** A wrapper for java.util.Calendar which supports a simple toString
 * opration.
 *
 * @author Anthony Jones
 */
public class Date {
  
  static DateFormat dateFormat;
  static { 
    try {
      dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
    }
    catch (NullPointerException e) {
      // A null pointer exception is caught in GCJ-3.0.4 when an unsupported
      // locale is used. The program still works fine if you ignore it.
      e.printStackTrace();
    }
  }
  
  private String cacheString;
  
  private Calendar c;
  
  public Date(Calendar c) {
    this.c = c;
  }
  
  public String toString() {
    if (cacheString == null)
      cacheString = createString();
    return cacheString;
  }

  public String createString() {
    if (c != null) 
      try {
        return dateFormat.format(c.getTime());
      }
      catch (Exception e) {
        return
            c.get(Calendar.YEAR) + "-" + 
            (c.get(Calendar.MONTH) + 1) + "-" +
            c.get(Calendar.DAY_OF_MONTH) + " " +
            c.get(Calendar.HOUR_OF_DAY) + ":" + 
            c.get(Calendar.MINUTE) + ":" +
            c.get(Calendar.SECOND);
      }
    return "";
  }
  
  public int compareTo(Date date) {
    if (date.c != null && (c == null || c.before(date.c)))
      return -1;
    return 1;
  }
    
}
