/**
 * Copyright (C) 2002 by Mark Stier, Germany
 */

package MAD;

import java.util.*;
import java.io.*;

public class MadPlayer {
    
    public static final String[] libpaths = new String[]{"lib/madplay.dll",
	                                                       "lib/madplay.so",
                                                         "lib/madplay.so"};
                                                         
    public static final String[] archs =  new String[]{"x86",
                                                       "i386",
                                                       "sparc"};
                                                       
    public static final String[] osNames = new String[]{"Windows*",
                                                        "Linux",
                                                        "SunOS"};

    public native int nativeMain(String[] args);

    private int returnValue;
    public static final boolean debug = false;
    private boolean stopped;
    private boolean running = false;
    private String[] args;
    private static final String lockObject = new String("lock object");
    private Vector madListeners = new Vector();

    private boolean pause;
    private boolean statePause;
    private int currentVolume = 0;
    private int newVolume = 0;

    // load the mad playback library
  static {
  	Properties p = System.getProperties();
    String osName = p.getProperty("os.name");
    String osVersion = p.getProperty("os.version");
    String osArch = p.getProperty("os.arch");

    if(debug) {
	    System.out.println("os.name = "+osName);
	    System.out.println("os.version = "+osVersion);
	    System.out.println("os.arch = "+osArch);
    }

    String libpath = null;
    for(int i=0; i<libpaths.length; i++) {
	    String namei = osNames[i];
	    String archi = archs[i];
	    boolean nameiWC = namei.endsWith("*");
	    boolean archiWC = archi.endsWith("*");
	    if(nameiWC) namei = namei.substring(0, namei.length()-1);
	    if(archiWC) archi = archi.substring(0, archi.length()-1);

	    boolean nameok = false;
      boolean archok = false;
      if(nameiWC)
	      nameok = startsWithIgnoreCase(osName, namei);
      else
	      nameok = startsWithIgnoreCase(osName, namei);
      if(archiWC)
	      archok = startsWithIgnoreCase(osArch, archi);
      else
	      archok = startsWithIgnoreCase(osArch, archi);
    
      if(nameok && archok) {
	      libpath = p.getProperty("user.dir")+File.separator+libpaths[i];
	      break;
      }
   }

    if(libpath == null) {
  	    if(debug) 
          System.out.println("System unkown. Cannot load library!");
  	} 
    else {
  	 try {
  	   if(debug) 
        System.out.println("Loading library \""+libpath+"\"");
  	   System.load(libpath);
  	 } catch (Exception e) {
  		e.printStackTrace(System.out);
  	 }
    }
  }

  public void pause() { 
    pause = !pause;
  }
   
  public boolean isPaused() { return pause; }

  public void start(String mp3path, int volume) {
    currentVolume = volume;
    newVolume = volume;
	  start(new String[]{"-a"+volume, "-v", "--tty-control", mp3path});
  }

  private synchronized void start(String[] args) {
    if(debug) 
      System.out.println("MadPlayer.start() entered");

    pause = false;
	  stopped = false;
	  running = true;
	  this.args = args;
    run();

    if(debug) 
      System.out.println("MadPlayer.start() finished");
  }

  public void stop() {
    stopped = true;
  }

  public void run() {
    if(debug) { 
      System.out.println("MadPlayer.run() entered");                    
		  System.out.println("MadPlayer.run(): starting with args:");
		  
      for(int i=0; i<args.length; i++) {
		    System.out.println("    args["+i+"] = \""+args[i]+"\"");
		  }
    }
	  
    statePause = false;
	  returnValue = nativeMain(args);
	  
    if(debug) {
		  System.out.println("MadPlayer.run(): return value = "+returnValue);
	    System.out.println("MadPlayer.run() finished");
    }

    running = false;
  }

  public void setVolume(int volume) {
    synchronized (lockObject) {
      newVolume = volume;
    } 
  }

    /**
     * The following two methods are solely accessed by
     * the dynamically loaded madplay library.
     */
  public int readkey(int blocking) {
	
    char result = 0;
   
    // If stopped, send the 'q' command to the library 
    if(stopped) {
      result = 'q'; 
    }
    // If we're paused, and we've already sent the command to the 
    // library, wait until we become !paused
    else if(statePause && blocking == 1) {
      while(pause) {
        try {
          Thread.sleep(500);
        } catch (Exception e) {} 
      }
      statePause = false;
      result = 'p';
    } 
    // If someone pressed paused, send the 'p' command to the library
    else if(pause && !statePause) {
      statePause = true;
      result = 'p'; 
    }
    // If the volume has been changed, change it by a single
    // interval.
    else if(currentVolume != newVolume) {
      synchronized (lockObject) {
        if(newVolume < currentVolume) { 
          currentVolume--;
          result = '-';
        }
        else {
          currentVolume++;
          result = '+';
        } 
      }
    }
    
  	if(debug && result != 0)
  	 System.out.println("MadPlayer.readkey() returns with \""+result+"\"");
  	
    return result;
  }

  private void fireMadEvent(String str) {
	
  if(debug) 
    System.out.println("MadPlayer.fireMadEvent(): str=\""+str+"\"");
	
  MadEvent me = new MadEvent(str);
    
	for(int i=0; i<madListeners.size(); i++)
	    ((MadListener)madListeners.elementAt(i)).actionPerformed(me);
  }
  
  public static boolean startsWithIgnoreCase(String a, String b) {
  	String b2 = b.toLowerCase();
	  String a2 = a.toLowerCase();
	  return a2.startsWith(b2);
  }

    /**
     * MadListener methods
     */
  public boolean removeListener(MadListener ml) {
	 return madListeners.remove(ml);
  }

  public void addListener(MadListener ml) {
	 madListeners.add(ml);
  }

  private String timeToStr(long secs) {
  	long hours = secs / 3600;
  	secs -= hours * 3600;
  	long mins = secs / 60;
  	secs -= mins * 60;
  	StringBuffer s = new StringBuffer();
  	if(hours < 10) 
      s.append("0"+hours);
  	else 
      s.append(""+hours);
  	
    s.append(":");
  	
    if(mins < 10) 
      s.append("0"+mins);
  	else 
      s.append(""+mins);
  	
    s.append(":");
  	
    if(secs < 10) 
      s.append("0"+secs);
  	else 
      s.append(""+secs);
  	
    return s.toString();
  }

    public int getCurrentVolume() {
      return currentVolume;
    }

}
