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

  private final String name;
  private static final int expiryMillis = 5000;
  private LinkedList list = new LinkedList();
  private Object signal = new Object();
  
  public Cache(String name) {
    this.name = name;
    Thread t = new Thread() {
      public void run() {
        while (true) {
          try {
            synchronized (signal) {
              signal.wait();
            }
            while (true) { 
              synchronized (Cache.this) {
                if (list.size() == 0)
                  break;
              }
              Thread.sleep(2000);
              flush();
            }
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    };
    t.start();
  }
  
  synchronized public void put(Object key, Object value) {
    Pair pair = findPair(key);
    if (pair == null) {
      pair = new Pair();
      pair.key = key;
      pair.value = value;
      pair.millis = System.currentTimeMillis();
      list.add(new SoftReference(pair));
    }
    else {
      pair.value = value;
    }
    synchronized (signal) {
      signal.notify();
    }
  }
  
  synchronized public Object get(Object key) {
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
        if (pair.key.equals(key)) {
          pair.millis = System.currentTimeMillis();
          return pair;
        }
    }    
    return null;
  }
  
  synchronized private void flush() {
//    boolean removed = false;
    long millis = System.currentTimeMillis();
    for (Iterator itr = list.iterator(); itr.hasNext(); ) {
      SoftReference ref = (SoftReference) itr.next();
      Pair pair = (Pair) ref.get();
      if (pair == null) { 
        itr.remove();
      }
      else {        
        if (millis - pair.millis > expiryMillis) {
          itr.remove();
//          removed = true;
        }
      }
    }    
//    if (removed)
//      System.out.println("Packed " + name + " cache size " + list.size());
  }  

  class Pair {
    Object key;
    Object value;
    long millis;
  }
}

