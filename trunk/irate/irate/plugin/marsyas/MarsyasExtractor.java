/*
 * Created on Aug 28, 2004
 *
 */
package irate.plugin.marsyas;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Vector;

import irate.common.Track;

/**
 * @author Taras Glek
 * This class creates the metadata
 */
public class MarsyasExtractor extends Thread{
  private static MarsyasExtractor me = new MarsyasExtractor();
  final private String extractCMD = "/usr/local/bin/extract";
  final private String featureName = "SVMFCC";
  private Vector queue = new Vector();
  
  private MarsyasExtractor() {
    //setPriority(Thread.MIN_PRIORITY);
  }
  
  void dbg(String msg) {
    System.err.println("MarsyasExtractor: "+msg);
  }
  
  void err(String msg) {
    System.err.println("MarsyasExtractor- Error: "+msg);
  }
  
  public void run() {
    dbg("I'm alive");
    String path = System.getProperty("java.io.tmpdir");
    if(path == null) {
      err("Couldn't not get java.io.tmpdir");
      return;
    }
    path += File.separatorChar + "irate_marsyas" + File.separatorChar;
    File dir = new File(path);
    
    if(!dir.exists() && !dir.mkdirs()) {
      err("Couldn't setup temp dir "+path);
      return;
    }
    
    while(queue.size() > 0) {
      Track track = (Track)queue.remove(0);
      if(track.getProperty("marsyas") != null) {
        dbg(track+" already has features in it. skipping");
        continue;
      }
      dbg("Extracting features from "+ track+ ". "+queue.size() + " left");
      File wav = new File(path+"out.wav");
      //make sure we arent using a stale file
      wav.delete();
      
      try {
        Process p = Runtime.getRuntime().exec(new String[]{"madplay","-Q","--mono","--sample-rate","22050","--output","wav:" + wav.getAbsolutePath(), track.getFile().getAbsolutePath()});
        int ret = p.waitFor();
        if(ret != 0) {
          dbg("Madplay failed to convert "+track + " with code "+ret);
          return;
        }
      } catch (Exception e) {
        err("Failed to convert "+track+" mp3 to wav");
        e.printStackTrace();
        return;
      }
      dbg("Madplay produced "+wav);
      if(!wav.exists()) {
        err("Madplay didn't listen to me");
        continue;
      }
      
      try {
        //run extract GENRE out.wav
        Process p = Runtime.getRuntime().exec(new String[]{extractCMD,"-e",featureName, wav.getAbsolutePath()}, null, dir);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String features = br.readLine();
        br.close();
//      replace tabs with commas and remove trailing comma
        if(features.length()!=0) {	
          features = features.replace('\t',',');
          features = features.substring(0,features.length()-1);
        }
        
        int ret = p.waitFor();
        if(ret != 0)
        {
          dbg("extract failed for "+track+" with error code "+ret + "and output '"+features+"'");
          return;
        }
        dbg("Features are:"+features);
        track.setProperty("marsyas", features);
     
      } catch (Exception e) {
        err("Failed to process "+track);
        //track.setProperty("marsyas","failed");
        e.printStackTrace();
        
        return;
      }
      wav.delete();
    }
    
  }
  
  /** Queues the track to be processed for feature
   * extraction
   * @param track gets a marsyas xml tag fith the features extracted
   */
  public static synchronized void processTrack(Track track) {
    me.queue.add(track);
    if(!me.isAlive())
      me.start();
  }
}
