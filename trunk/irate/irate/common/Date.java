/*
 * Created on Oct 25, 2003
 */
package irate.common;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.util.Calendar;

/** A wrapper for java.util.Calendar which supports a simple toString
 * opration.
 *
 * @author Anthony Jones
 */
public class Date {
  
  static DateFormat dateFormat = DateFormat.getDateTimeInstance();
  
  private SoftReference cacheString;
  
  private Calendar c;
  
  public Date(Calendar c) {
    this.c = c;
  }
  
  public String toString() {
    String s = cacheString == null ? null : (String) cacheString.get();
    if (s == null) {
      s = createString();
      cacheString = new SoftReference(s);
    }
    return s;
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
