/**
 * Feature extraction plugin
 * 
 * @author Taras Glek
 * 
 */
package irate.plugin.marsyas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import irate.plugin.*;
import irate.common.Track;
import nanoxml.*;
import irate.client.TrackLifeCycleListener;

public class MarsyasPlugin
extends Plugin
implements TrackLifeCycleListener
{
  private boolean initialized = false;
  private boolean havePrereqs = false;
  /**
   * Get a short identifier for this Plugin.
   */
  public String getIdentifier()
  {
    return "marsyas";  // Used internally, don't internationalize
  }
  
  /**
   * Get a short description of this plugin.
   */
  public String getDescription()
  {
    return Resources.getString("description");
  }
  
  /**
   * Get a long description of this plugin, for tooltips
   */
  public String getLongDescription() 
  {
    return Resources.getString("long_description");
  }
  
  protected void doAttach()
  {
    getApp().addTrackLifeCycleListener(this);
    System.out.println("Hello there from Marsyas");
  }
  
  /**
   * @return
   */
  private void checkPrereqs() throws Exception{
    if(havePrereqs)
      return;
    
    boolean haveStuff = true;
    try{
      Process p = Runtime.getRuntime().exec("madplay");
      p.destroy();
    }catch(Exception e) {
      haveStuff = false;
      throw new Exception ("Please install madplay");
    }
    try{
      Process p = Runtime.getRuntime().exec(MarsyasExtractor.extractCMD);
      p.destroy();
    }catch(Exception e) {
      haveStuff= false;
      throw new Exception ("Failed find to run ("+MarsyasExtractor.extractCMD+").\n Please install childmarsyas: http://irate.sourceforge.net/snapshots/childmarsyas-0.2-taras.tar.bz2");
    }
    
    havePrereqs=haveStuff;
  }

  protected void doDetach()
  {
    getApp().removeTrackLifeCycleListener(this);
  }
  
  /**
   * Parse the configuration stored in the specified element.
   */
  public void parseConfig(XMLElement elt)
  {
  }
  
  /**
   * Format the configuration of this plugin by modifying the specified
   * element.
   */
  public void formatConfig(XMLElement elt)
  {
  }
  
  /**
   * Notification that the specified track has been added to the playlist.
   */
  public void addedToPlayList(Track track)
  {
    check(track);
  }
  
  /**
   * Notification that the specified track has been removed from the playlist.
   */
  public void removedFromPlayList(Track track)
  {
  }
  
  /**
   * Notification that the specified track is about to be played.
   */
  public void startingToPlay(Track track)
  {
    check(track);
    lazyInit();
  }
  
  /**
   * Do big stuff late in the game
   */
  private void lazyInit() {
    //once everything is loaded, crunch through the trackdb
    if(initialized)
      return;
  	
    initialized = true;
  	Track t[] = getApp().getTracks();
  	for(int i=0;i<t.length;i++) {
  		check(t[i]); 			
  	}
  	
    getApp().addTrackAction(Resources.getString("find_similar"), (SelectionListener)new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        try {
          checkPrereqs();
        } catch (final Exception e1) {
          final Display d = Display.findDisplay(Thread.currentThread());
          d.asyncExec(new Runnable() {
            public void run() {
              Shell fake = new Shell(d);
              MessageBox m = new MessageBox(fake,SWT.OK | SWT.ICON_ERROR| SWT.APPLICATION_MODAL);
             
              m.setText("MarsyasPlugin prerequisite check failed");
              m.setMessage("Could not find a prerequisite application:\r\n"+e1.getMessage());
              m.open();
              fake.dispose();
            }
          });        
          return;

        }
        Track track = getApp().getSelectedTrack();
        new MarsyasSimilaritySearch(MarsyasPlugin.this, getApp().getUserName(), getApp().getTracks(), track);
      }
    });

  }

  /** Utility method to let other parts of the plugin play stuff */
  public void playTrack(Track t) {
    if(!getApp().getPlayingTrack().equals(t))
      getApp().playTrack(t);
  }
  
  /** Utility method to let other parts of the plugin add tracks */
  public void addTrack(Track t) {
    getApp().addTrack(t);
  }
  
  void dbg(String msg) {
    System.err.println("MarsyasPlugin: "+msg);
  }
	
  /** CHeck if the track needs to be processed by marsyas */
  private void check(Track track)
  {
    // If we don't know how loud it is, queue it to be processed.
    if ( !track.isHidden() && !track.isNotDownloaded() && track.getProperty("marsyas") == null) {
      System.err.println("Checking "+track);
      //extract doesn't like files over 3MB
//      if(track.getFile().length() > 3*1000*1000)
  //      return;
      MarsyasExtractor.processTrack(track);
    }
  }
  
  
}

