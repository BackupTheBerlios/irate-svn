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

  // Disabled the cache because I've decided that it's a crap way to do things.
  // When I get time I will speed up the image processing using a better 
  // method

  public Cache(String name) {
  }
  
  synchronized public void put(Object key, Object value) {
  }
  
  synchronized public Object get(Object key) {
    return null;
  }
}

