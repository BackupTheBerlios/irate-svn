/*
 * Created on Apr 22, 2004
 */
package irate.swt;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Anthony Jones
 */
public class Cache  {
  
  private LinkedList list = new LinkedList();
  
  public Cache() {
  }
  
  public void put(Object key, Object value) {
    Pair pair = findPair(key);
    if (pair == null) {
      pair = new Pair();
      pair.key = key;
      pair.value = value;
      list.add(new SoftReference(pair));
    }
    else {
      pair.value = value;
    }
  }
  
  public Object get(Object key) {
    Pair pair = findPair(key);
    if (pair == null)
      return null;
    return pair.value;
  }
  
  private Pair findPair(Object key) {
    for (Iterator itr = list.iterator(); itr.hasNext(); ) {
      SoftReference ref = (SoftReference) itr.next();
      Pair pair = (Pair) ref.get();
      if (pair == null) 
        itr.remove();
      else
        if (pair.key.equals(key))
          return pair;
    }    
    return null;
  }

  class Pair {
    Object key;
    Object value;
  }
}

