// Copyright 2003 Taras, Anthony Jones

package irate.swt;

import java.util.Comparator;

public class MagicComparator implements Comparator {
  
  private int columnIndex;
  
  public MagicComparator(int the_column_index) {
    this.columnIndex = the_column_index;
  }
    
  public int compare(Object o1, Object o2) {
    Object obj1[] = (Object[]) o1;
    Object obj2[] = (Object[]) o2;
    
    MagicString s1 = new MagicString(((String[]) obj1[1])[columnIndex]);
    MagicString s2 = new MagicString(((String[]) obj2[1])[columnIndex]);
    return s1.compareTo(s2);
  }
  
}

class MagicString {
  
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
