/*
 * Created on Oct 25, 2003
 */
package irate.swt;

/** Allows numbers to be compared fairly against numbers and strings to be
 * fairly compared against strings. This is used for sorting where pure
 * numbers come first (sorted numerically), followed by strings.
 * 
 * @author Anthony Jones
 */
public class MagicString {
  
  private String s;
  private Float f;
  
  public MagicString(String s) {
    this.s = s;
    try {
      f = new Float(s);
    }
    catch (NumberFormatException e) {
    }
  }
  
  public int compareTo(MagicString ms) {
    if (f == null) {
      if (ms.f == null)
        return s.compareToIgnoreCase(ms.s);
      return 1; 
    }
    if (ms.f == null)
      return -1;
    return f.compareTo(ms.f);
  }
}

