/*
 * Created on Aug 28, 2004
 *
 */
package irate.plugin.marsyas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Vector;

import irate.common.Track;

/**
 * @author Taras Glek
 * This class creates the metadata
 */
public class MarsyasExtractor extends Thread{
  private static MarsyasExtractor me = new MarsyasExtractor();
  final private String extractCMD = "extract";
  final private String featureName = "GENRE";
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
      dbg("Extracting features from "+ track+ ". "+queue.size() + " left");
      File wav = new File(path+"out.wav");
      //make sure we arent using a stale file
      wav.delete();
      
      try {
        Process p = Runtime.getRuntime().exec(new String[]{"madplay","--mono","--sample-rate","22050","--output","wav:" + wav.getAbsolutePath(), track.getFile().getAbsolutePath()});
        p.waitFor();
      } catch (Exception e) {
        err("Failed to convert "+track+" mp3 to wav");
        e.printStackTrace();
        return;
      }
      
      if(!wav.exists()) {
        err("Madplay didn't listen to me");
        continue;
      }
      
      try {
        //run extract GENRE out.wav
        String line;
        Process p = Runtime.getRuntime().exec(new String[]{extractCMD, featureName, wav.getAbsolutePath()}, null, dir);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        //consume output...stupid java doesn't let extract finish otherwise 
        while(null != (line=br.readLine())) {
          if(line.substring(0,7).equals("Problem")) {
            err("Detected a problem with the feature extractor");
            p.destroy();
            continue;
          }
        }
        
        File genreFile = new File(path + "out.GENRE.mff");
        LineNumberReader ln = new LineNumberReader(new FileReader(genreFile));
        
        //line #7 of the feature file contains the needed data
        while(null != (line=ln.readLine())){
          if(ln.getLineNumber()==7)
            break;
        }
        ln.close();
        genreFile.delete();
        //replace tabs with commas and remove trailing comma
        if(line.length()!=0) {	
          line = line.replace('\t',',');
          line = line.substring(0,line.length()-1);
        }
        dbg("Features are:"+line);
        track.setProperty("marsyas",line);
      } catch (Exception e) {
        err("Failed to process "+track);
        track.setProperty("marsyas","failed");
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
