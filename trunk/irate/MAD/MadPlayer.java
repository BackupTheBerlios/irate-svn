/**
 * Copyright (C) 2002 by Mark Stier, Germany
 */

package MAD;

import java.util.*;
import java.io.*;

public class MadPlayer implements Runnable {
    public static final String[] libpaths =
        new String[]{"MAD\\libs\\Win32\\madplay.dll",
	             "MAD/libs/linux-i386/madplay.so",
                     "MAD/libs/sunos-sparc/madplay.so"};
    public static final String[] archs =
        new String[]{"x86",
                     "i386",
	             "sparc"};
    public static final String[] osNames =
	new String[]{"Windows*",
                     "Linux",
                     "SunOS"};

    private int returnValue;
    public static final boolean debug = true;
    private boolean stopped;
    private boolean running = false;
    Thread thread = null;
    public native int nativeMain(String[] args);
    private String[] args;
    private static final String lockObject = new String("lock object");
    private Vector madListeners = new Vector();

    private boolean pause;
    private boolean statePause;
    static boolean supported = false;

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
	    if(debug) System.out.println("System unkown. Cannot load library!");
	} else {
	    try {
		if(debug) System.out.println("Loading library \""+libpath+"\"");
		System.load(libpath);
                supported = true;
	    } catch (Exception e) {
		e.printStackTrace(System.out);
	    }
	}
    }

    public static boolean isSupported() {
        return supported;
    }

    public void pause() { pause = true; }
    public void resume() {
	if(!pause) return;
	pause = false;
	thread.interrupt();
    }
    public boolean isPaused() { return pause; }

    public void start(String mp3path) {
	start(new String[]{"-v", "--tty-control", mp3path});
    }

    /**
     * @param secs Offset time point in seconds.
     */
    public void start(String mp3path, long secs) {
	start(new String[]{"-v", "--tty-control", "-s", timeToStr(secs), mp3path});
    }

    /**
     * @param time Offset time point, format: "HH:MM:SS".
     */
    public void start(String mp3path, String time) {
	start(new String[]{"-v", "--tty-control", "-s", time, mp3path});
    }

    private synchronized void start(String[] args) {
	if(debug) System.out.println("MadPlayer.start() entered");
	stopThread();
	synchronized ( lockObject ) {
	    pause = false;
	    stopped = false;
	    running = true;
	    this.args = args;
//	    thread = new Thread(this, "MadPlayer Thread");
//	    thread.start();	    
            run();
	}
	if(debug) System.out.println("MadPlayer.start() finished");
    }

    /** blocking stop method */
    public void stopThread() {
	if(debug) System.out.println("MadPlayer.stopThread() entered");
	if(!running) {
	    if(debug) System.out.println("MadPlayer.stopThread(): "
					 +"playback thread already dead");
	    if(debug) System.out.println("MadPlayer.stopThread() finished");
	    return;
	}
	stopped = true;
	
	if(debug) System.out.println("MadPlayer.stopThread(): "
				     +"playback thread still running");
	
	while(running) {
	    try {
		if(debug) System.out.println("MadPlayer.stopThread(): "
					     +"joining playback thread");
		thread.join(1000l);
		if(debug) {
		    System.out.println("MadPlayer.stopThread(): "
				       +"stopped="+stopped);
		}
	    } catch(Exception e) {
		if(debug)
		    e.printStackTrace(System.out);
	    }
	}
	if(debug) System.out.println("MadPlayer.stopThread() finished");
    }

    /** non-blocking stop method */
    public void stop() {
	stopped = true;
    }

    public void run() {
	synchronized ( lockObject ) {
	    if(debug) System.out.println("MadPlayer.run() entered");
	    if(debug) {
		System.out.println("MadPlayer.run(): starting with args:");
		for(int i=0; i<args.length; i++) {
		    System.out.println("    args["+i+"] = \""+args[i]+"\"");
		}
	    }
	    statePause = false;
	    returnValue = nativeMain(args);
	    if(debug)
		System.out.println("MadPlayer.run(): "
				   +"return value = "+returnValue);
	    if(debug) System.out.println("MadPlayer.run() finished");
	    running = false;
	}
    }

    /**
     * The following two methods are solely accessed by
     * the dynamically loaded madplay library.
     */
    public int readkey(int blocking) {
	char result = 0;

	if(statePause && blocking == 1) {
	    while(pause) {
		try {
		    thread.wait();
		} catch(Exception e) {
		    if(debug) e.printStackTrace(System.out);
		}
	    }
	    statePause = false;
	    return 'p';
	}

	if(stopped) result = 'q';
	else if(pause && !statePause) {
	    statePause = true;
	    return 'p';
	}
	/*
	else if(!pause && statePause) {
	    statePause = false;
	    return 'p';
	}
	*/
	if(debug && result != 0)
	    System.out.println("MadPlayer.readkey() returns with \""+result+"\"");
	return result;
    }

    private void fireMadEvent(String str) {
	if(debug) System.out.println("MadPlayer.fireMadEvent(): str=\""+str+"\"");
	MadEvent me = new MadEvent(str);
	for(int i=0; i<madListeners.size(); i++)
	    ((MadListener)madListeners.elementAt(i)).actionPerformed(me);
    }

    public static void main(String[] args) {
	MadPlayer player = new MadPlayer();
	player.start(args);
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
	if(hours < 10) s.append("0"+hours);
	else s.append(""+hours);
	s.append(":");
	if(mins < 10) s.append("0"+mins);
	else s.append(""+mins);
	s.append(":");
	if(secs < 10) s.append("0"+secs);
	else s.append(""+secs);
	return s.toString();
    }
}
