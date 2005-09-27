/*
 * Created on 27/09/2005
 */
package irate.buddy;

import java.util.Hashtable;
import java.util.Iterator;


public class Buddy {
   public void setRatings(Hashtable hashtable)
   {
     System.out.println("### Trackdatabase ###");
     for (Iterator itr = hashtable.keySet().iterator(); itr.hasNext();){
       String key = (String) itr.next();
       Number rating = (Number) hashtable.get(key);
       System.out.println(key + " " + rating);
     }
     System.out.println("---|---");
   }
}
