/*
 * Created on Aug 29, 2004
 *
 
 */
package irate.plugin.marsyas;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import irate.common.Track;
import irate.common.TrackDatabase;

/**
 * @author Taras Glek
 * This class provides track similarity matching capabilities
 * based on Abe's similarto.pl
 */
public class MarsyasSimilaritySearch extends Thread  {
  final int FEATURE_COUNT = 30;
  private float features[][];
  private ArrayList tracks = new ArrayList();
  private float selectedFeatures[] = new float[FEATURE_COUNT];
  private float min[] = new float[FEATURE_COUNT];
  private float max[] = new float[FEATURE_COUNT];
  private float range[] = new float[FEATURE_COUNT];
  private Track selectedTrack;
  private MarsyasPlugin plugin;
  private MarsyasResultDialog resultDialog = null;
  private String username;
  private String tmpString;
  LinkedList serverTracks;
  Display display = Display.findDisplay(Thread.currentThread());
  Comparator trackComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      String t1 = ((Track) o1).getURL().toString(),
      	t2 = ((Track) o2).getURL().toString();
      return t1.compareTo(t2);
    }
  };
 
  /**
   * @param plugin
   * @param username
   * @param tracks
   * @param selectedTrack
   */
  public MarsyasSimilaritySearch(MarsyasPlugin plugin, String username, Track[] tracks, Track selectedTrack) {
    this.username = username;
    this.selectedTrack = selectedTrack;
    this.plugin = plugin;
    for (int i = 0; i < tracks.length; i++) {
      this.tracks.add(tracks[i]);
    }
    Collections.sort(this.tracks, trackComparator);
    //this.tracks = tracks;
    //filterInputs(tracks, selectedTrack);
    //doSearch();
    start();
  }

  public class ServerTrack  {
    String str_data[];
    Track track = null;
    final int URL_ID = 1;
    final int FEATURES_ID = 2;
    final int COPYRIGHT_ID = 3;
    final int TITLE_ID = 5;
    final int ARTIST_ID = 4;
    
    
    public ServerTrack(String line) {
      StringTokenizer st = new StringTokenizer(line,",",true);
      //six blanks
      str_data = new String[]{"","","","","",""};
      int i = 0;
      //java's stringtokenizer is designed to be annoying and skip empty tokens
      //work around
      while(st.hasMoreElements()) {
        String t = st.nextToken();
        if(t.equals(","))
          i++;
        else
          str_data[i] = URLDecoder.decode(t);
      }
      integrate();
    }
    
    /** this tries to find a matching track in the db
     * if it does the servertrack is local
     * if it does and the local track doesnt have
     * features, they are merged into the local track
     */
    private void integrate() {
      String url = str_data[URL_ID];
      //should do a binary search here
      for (Iterator iter = tracks.iterator(); iter.hasNext();) {
        Track t = (Track) iter.next();
        if(t.getURL().toString().equals(url)){
          this.track = t;
          //merge missing features
          if(track.getProperty("marsyas")==null)
            track.setProperty("marsyas",str_data[FEATURES_ID]);
          break;
        }
      }
    }
    
    public String[] getColumns() {
      return new String[] {Resources.getString("Distance"),Resources.getString("Artist"),Resources.getString("Title")};
    }
    
    public boolean isLocal() {
      return track != null;
    }
    
    public String[] getData() {
      return new String[] {str_data[0],str_data[4],str_data[5]};
    }

    /**
     * plays the track
     */
    public void play() {
      if(!isLocal())
        return;
      plugin.playTrack(track);
    }

    /**
     *  Add's track to trackdatabase and downloads it
     */
    public boolean download() {
      if(isLocal())
        return false;
      
      try {
        track = new Track(new URL(str_data[URL_ID]));
        track.setArtist(str_data[ARTIST_ID]);
        track.setTitle(str_data[TITLE_ID]);
        track.setProperty("copyright",str_data[COPYRIGHT_ID]);
        track.setProperty("marsyas",str_data[FEATURES_ID]);
        } catch (MalformedURLException e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }
  }
  
  /**
   * Do the actual searching & sorting/etc 
   */
  private void doSearch() {
    ArrayList al = new ArrayList();
    
    for (int i = 0; i < features.length; i++) {
   //   al.add(new ComparableTrack(new Double(distance(features[i],selectedFeatures)), tracks[i]));
    }
    
    Collections.sort(al);
    
    resultDialog = new MarsyasResultDialog(plugin, selectedTrack, al.subList(0,10));
    start();
  }
  
  /** most inefficient string escape ever */
  private String escape(String str) {
    return URLEncoder.encode(str);
  }
  
  /** do the remote track comparison in a thread */
  public void run() {
    StringBuffer sb = new StringBuffer();
    //sb.append(""+tracks.length+"\n");
    for (Iterator iter = tracks.iterator(); iter.hasNext();) {
      Track track = (Track) iter.next();
      sb.append(escape(track.getURL().toString()));
      sb.append(',');
      String marsyas = track.getProperty("marsyas");
      if(marsyas == null)
        marsyas = "";
      sb.append(escape(marsyas));
      sb.append(',');
      sb.append(escape(track.getCopyrightInfo()));
      sb.append(',');
      sb.append(escape(track.getArtist()));
      sb.append(',');
      sb.append(escape(track.getTitle()));
      sb.append(',');
      String rating = "";
      if(track.isRated())
        rating = ""+new Float(track.getRating()).intValue();
      sb.append(rating);
      
      sb.append("\n");
    }
    
    URL url = null;
    BufferedReader br = null;
    try {
      System.err.println("Username "+username + " Url "+selectedTrack.getURL().toString());
      url = new URL("http://glek.net/cgi-bin/similarto?url="+escape(selectedTrack.getURL().toString())+"&username="+escape(username));
      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      con.setDoOutput(true);
      con.setUseCaches(false);
      con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      con.setRequestProperty("Content-Length", ""+sb.length());
      
      PrintStream ps = new PrintStream(con.getOutputStream ());
      //PrintStream ps = new PrintStream(new FileOutputStream("/home/taras/Projects/george/http.txt"));
      ps.print(sb.toString());
      ps.flush();
      br = new BufferedReader(new InputStreamReader(con.getInputStream()));
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    try {
      String line = br.readLine();
      if(!line.substring(0,2).equals("OK")){
        System.err.println("Fatal error on the server: "+line);
        tmpString = line;
        display.asyncExec(new Runnable() {
          public void run() {
            Shell fake = new Shell(display);
            MessageBox m = new MessageBox(fake);
            m.setText("Similarity search failed");
            m.setMessage("Server said:\r\n"+tmpString);
            m.open();
            fake.dispose();
          }
        });        
        return;
      }
      serverTracks = new LinkedList();
      while((line=br.readLine()) != null){
        serverTracks.add(new ServerTrack(line));
      }
      display.syncExec(new Runnable() {
        public void run() {
          new MarsyasResultDialog(plugin, selectedTrack,  serverTracks);
        }
      });
    } catch (IOException e) {
      // TODO: handle exception
    }
    //dbg(sb.toString());
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
  /*  Vector featured = new Vector();
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
      System.err.println("marsyas=''"+track.getProperty("marsyas")+" st.countTokens()"+st.countTokens());
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
*/  }
  
}
