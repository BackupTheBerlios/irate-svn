package irate.common;

import java.util.Random;
import java.util.List;

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
}
