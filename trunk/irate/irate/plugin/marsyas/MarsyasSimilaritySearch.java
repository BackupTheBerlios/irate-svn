/*
 * Created on Aug 29, 2004
 *
 
 */
package irate.plugin.marsyas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.eclipse.swt.widgets.Display;

import irate.common.Track;

/**
 * @author Taras Glek
 * This class provides track similarity matching capabilities
 * based on Abe's similarto.pl
 */
public class MarsyasSimilaritySearch  {
  final int FEATURE_COUNT = 30;
  private float features[][];
  private Track tracks[];
  private float selectedFeatures[] = new float[FEATURE_COUNT];
  private float min[] = new float[FEATURE_COUNT];
  private float max[] = new float[FEATURE_COUNT];
  private float range[] = new float[FEATURE_COUNT];
  private Track selectedTrack;
  private MarsyasPlugin plugin;
  /**
   * @param plugin
   * @param tracks
   * @param selectedTrack
   */
  public MarsyasSimilaritySearch(MarsyasPlugin plugin, Track[] tracks, Track selectedTrack) {
    this.selectedTrack = selectedTrack;
    this.plugin = plugin;
    filterInputs(tracks, selectedTrack);
    doSearch();
  }

  public class ComparableTrack implements Comparable {
    Double distance;
    Track track;
    
    public ComparableTrack(Double d, Track t) {
      distance = d;
      track = t;
    }
    
    /* Sort in reverse
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
      return distance.compareTo(((ComparableTrack)o).distance);
    }
  }
  
  /**
   * Do the actual searching & sorting/etc 
   */
  private void doSearch() {
    ArrayList al = new ArrayList();
    
    for (int i = 0; i < features.length; i++) {
      al.add(new ComparableTrack(new Double(distance(features[i],selectedFeatures)), tracks[i]));
    }
    
    Collections.sort(al);
    dbg("features.length="+features.length+" ts.size="+al.size());
    
    for (Iterator iter = al.iterator(); iter.hasNext();) {
      ComparableTrack ct = ((ComparableTrack) iter.next());
      dbg("Similar:"+ct.track+" by "+ct.distance);
    }
  
    new MarsyasResultDialog(plugin, selectedTrack, al.subList(0,10));
  }

  private double distance(float a[], float b[]) {
    float res = 0;
    for (int i = 0; i < a.length; i++) {
      float x = a[i] - b[i];
      x *= x;
      res += x;
    }
    return Math.sqrt(res);
  }
  
  void dbg(String msg) {
    System.err.println("MarsyasSimilaritySearch: "+msg);
  }
  
  /** This makes nice floats out of the marsyas string
   * @param tracks
   * @param selectedTrack
   */
  private void filterInputs(Track[] inTracks, Track selectedTrack) {
    Vector featured = new Vector();
    //pick out tracks that have been processed
    for (int i = 0; i < inTracks.length; i++) {
      if((inTracks[i].getProperty("marsyas")) != null) {
        featured.add(inTracks[i]);
      }
    }
    
    //setup various arrays
    features = new float[featured.size()][FEATURE_COUNT];    
    tracks = new Track[featured.size()];
    featured.copyInto(tracks);
    
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      
      //parse features
      StringTokenizer st = new StringTokenizer(track.getProperty("marsyas"),",");
      int j=0;
      while(st.hasMoreElements())
        features[i][j++] = Float.parseFloat(st.nextToken());
      
      //deal with max & mins
      if(i == 0) {
        System.arraycopy(features[i],0,min,0,FEATURE_COUNT);
        System.arraycopy(features[i],0,max,0,FEATURE_COUNT);  
      }
      for (j = 0; j < FEATURE_COUNT; j++) {
        if(features[i][j] < min[j])
          min[j] = features[i][j];
        if(features[i][j] > max[j])
          max[j] = features[i][j];
      }

    }
    
    //figure out the range between min/max
    for (int i = 0; i < range.length; i++) {
      range[i] = max[i] - min[i];
    }
    
    //normalize the features
    for (int i = 0; i < features.length; i++) {
      for (int j = 0; j < features[i].length; j++) {
        if(range[j] < 0.000000001)
          features[i][j]=0;
        else
          features[i][j]=(features[i][j] - min[j])/range[j];
      }
      //figure out the query features
      if(tracks[i].equals(selectedTrack)) {
        System.arraycopy(features[i],0,selectedFeatures,0,FEATURE_COUNT);
        dbg("Query on "+tracks[i]);
      }
    }
    //"0.0641889,0.0545305,0.849531,206,255,106.095,52.2849,103.088,169.498,51.0185,193.248,657.809,16848.8,686.592,0.015838,-42.516,1.31342,1.81546,1.28711,1.15597,3.82557,0.29341,0.129088,0.09859,0.0699894,143.13,20,7.4802,10,-1"
  }
  
}
