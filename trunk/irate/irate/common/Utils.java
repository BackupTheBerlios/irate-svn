package irate.common;

import java.util.Random;
import java.util.List;
import java.util.Vector;

public class Utils
{
  private static Random random = new Random();
  public static Random getRandom()
  {
    return random;
  }

  public static void scramble(List objects) {
    for (int i = 0; i < objects.size(); i++) {
      int swap = (Math.abs(random.nextInt()) % objects.size());
      if (swap != i) {
        Object o = objects.get(i);
        objects.set(i, objects.get(swap));
        objects.set(swap, o);
      }
    }
  }

  /**
   * Splits a string in multiple substrings. Implemented here instead of using String.split() in order
   * to remain compatible with JDK 1.3. Maximum of 4 string divisions.
   */
  public static String[] split(String data, char divisor) {
    Vector strings = new Vector();
    int currentSubString = 0;
    int lastpos = 0;
    int index;

    while ((index = data.indexOf(divisor, lastpos)) != -1) {
      strings.add(data.substring(lastpos, index));
      lastpos = index + 1;
    }
    
    strings.add(data.substring(lastpos));

    return (String[]) strings.toArray(new String[strings.size()]);
  }
  
}
