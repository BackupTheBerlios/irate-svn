// Copyright 2003 by authors listed below

package irate.swt;

import irate.client.AbstractClient;
import irate.client.Help;
import irate.client.Player;
import irate.common.Preferences;
import irate.common.Track;
import irate.common.UpdateListener;
import irate.plugin.Plugin;
import irate.plugin.PluginApplication;
import irate.plugin.PluginUIFactory;
import irate.resources.BaseResources;
import irate.swt.plugin.SWTPluginUIFactory;

import java.io.*;
import java.util.List;
import java.util.StringTokenizer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * @author Creator: Taras Glek
 * @author Creator: Anthony Jones
 * @author Updated: Eric Dalquist
 * @author Updated: Allen Tipper
 * @author Updated: Stephen Blackheath
 * @author Updated: Robin Sheat
 * @author Updated: Brion Vibber
 */
public class Client extends AbstractClient {

  private static final int VOLUME_RESOLUTION = 1;
  private static final int VOLUME_SPAN = 30;
  private static final int VOLUME_OFFSET = 0;

  private static final int UNRATED_ON_PLAYLIST_VALUE = 13;
  private static final int UNRATED_NOT_ON_PLAYLIST_VALUE = 0;
  
  private Composite topPanel;
  private Composite bottomPanel;
  private AlphaLabel lblState;
  private String lastStatusMessage = null;
  private Display display;
  private Shell shell;
  private AlphaLabel trackLabel;
  private ProgressBar progressBar;
  private Scale volumeScale;
//  private TrackProgressBar songProgressBar;

//  private SkinManager.SkinItem pauseSkin;
  private ThreeModeButton play;
  private ThreeModeButton previous;
  private ThreeModeButton next;
  private Track previousTrack;
  private Help help = new Help();
  private ErrorDialog errorDialog;
  private AboutDialog aboutDialog;
  private Composite trackGroup = null;  
  private Composite ratingGroup = null;
  //private Button expandButton = null;
  
  //  private SettingDialog settingDialog;
  private Object strStateLock = new Object();
  private String strState = null;
  private TrackTable trackTable;

  private SWTPluginUIFactory uiFactory;
  private SkinManager skinManager;
  
  private RatingFunction[] ratingFunctions = new RatingFunction[] {
    new RatingFunction(0, "button.this_sux"),
    new RatingFunction(2, "button.yawn"),
    new RatingFunction(5, "button.not_bad"),
    new RatingFunction(7, "button.cool"),
    new RatingFunction(10, "button.love_it")
  };
  
  public Client() {
    
    initGUI();
    aboutDialog = new AboutDialog(display, shell);
    errorDialog.setParent(shell);
		uiFactory = new SWTPluginUIFactory(display, (PluginApplication) this);
    createDropTarget();

    UpdateListener ul = new UpdateListener() {
      public void actionPerformed() {
        showDownloadStatus(downloadThread.getDownloadState());
      }//method
      public void newTrackStarted(Track track) { }
    };//ul
    downloadThread.addUpdateListener(ul);
    update();
  }

  /** Init gui. Called from constructor */
  public void init() {
    try {
      display = new Display();
    }catch(Exception e) {
      handleError(null, "missingswt.html");
      System.exit(1);
    }
    //need this baby to report problems with msg boxes :)
    errorDialog = new ErrorDialog(display, null);
  }
  
  public void run() {
    trackTable.updateTable();
    topPanel.layout();
    
    shell.layout();
    
    String maximize = Preferences.getUserPreference("isMaximized");
       
    String length = Preferences.getUserPreference("shellLength");
    String height = Preferences.getUserPreference("shellHeight");
    if(length != null && height != null) {
      shell.setSize(new Integer(length).intValue(),new Integer(height).intValue());
    }
    
    String xLocation = Preferences.getUserPreference("shellX");
    String yLocation = Preferences.getUserPreference("shellY");
    
    if(xLocation != null && yLocation != null) {
      // Make sure the location is onscreen; sizes and positions of screens
      // may change from one run to the next, particularly on laptops.
      //
      // FIXME: Display.getBounds() may return bogus results if there are
      // multiple screens which do not form an overall rectangle.
      // SWT 3.0 has a Display.getMonitors() by which we could check
      // individually; switch to use this if we require 3.0+ later.
      //
      // We could pull more gymnastics to keep the window entirely on
      // screen and sized to fit, but generally it should be enough to 
      // make sure that the title bar is visible; then the user can move
      // or size it as they wish.
      //
      // FIXME: This does not take into account user-inaccessible areas
      // of the screen like taskbars, menu bars, panels, docks. It may
      // still be possible to end up with a position where you can't move
      // the window.
      
      int shellX = new Integer(xLocation).intValue();
      int shellY = new Integer(yLocation).intValue();
      Rectangle bounds = display.getBounds();
      
      if (bounds.contains(shellX, shellY) ||
          bounds.contains(shellX + shell.getSize().x, shellY)) {
        shell.setLocation(shellX, shellY);
      }
    }
    
    if(maximize != null && maximize.equals("true")) {
      shell.setMaximized(true);
    }
    
      // Set initial volume level in the player.  We have already set it on the volume
      // slider widget when it was created.
    String volumeLevelStr = Preferences.getUserPreference("volumeLevel");
    if (volumeLevelStr != null) {
      final int volumeLevel = Integer.parseInt(volumeLevelStr);
      setPlayerVolume(volumeLevel);
        // If we don't do this on the display thread, then - for some reason - it seems
        // to set the slider in accordance with its default range rather than the range
        // we have configured it with.
      display.asyncExec(new Runnable() {
        public void run() {
          setVolumeSlider(volumeLevel);
        }
      });
    }
    
    shell.open();
   
    // Remember the sort order of the track table.  This has to be here.
    // The shell needs to be open before the sort will work propertly.
    String sortColumn = Preferences.getUserPreference("sortColumn");
    String sortDirection = Preferences.getUserPreference("sortDirection");
    
    if(sortDirection == null) {
        sortDirection = "true";
    }
    
    if(sortColumn != null) {
        trackTable.setSortColumn(new Integer(sortColumn).intValue(),new Boolean(sortDirection).booleanValue());
    }
    
    downloadThread.start();
    trackTable.addSelectionListener(new SelectionAdapter() {
        // Play track if the user double-clicks on it (or whatever the equivalent
        // is on the target platform).
      public void widgetDefaultSelected(SelectionEvent e) {
        Track track = trackTable.getClickedTrack();
        if ((track != null) && track.exists()) {
          setPaused(false);
          playThread.play(track);
        }
      }
    });
    setPaused(false);
    playThread.start();
    
    while (true) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    
   
    
  }

  public void quit() {
    Point closingSize = shell.getSize();
    Point closingLocation = shell.getLocation();
    Boolean isMaximized = new Boolean(shell.getMaximized());
    try {
        Preferences.savePreferenceToFile("isMaximized",isMaximized.toString());
        
        if (!isMaximized.booleanValue()) {
            Preferences.savePreferenceToFile("shellLength", new Integer(closingSize.x).toString());
            Preferences.savePreferenceToFile("shellHeight", new Integer(closingSize.y).toString());  
            Preferences.savePreferenceToFile("shellX",new Integer(closingLocation.x).toString());
            Preferences.savePreferenceToFile("shellY",new Integer(closingLocation.y).toString());
        }
        
        Preferences.savePreferenceToFile("volumeLevel",new Integer(getVolumeSlider()).toString());
        Preferences.savePreferenceToFile("sortColumn",new Integer(trackTable.getSortColumn()).toString());
        Preferences.savePreferenceToFile("sortDirection", new Boolean(trackTable.getSortDirection()).toString());
    } catch (IOException io) {}
    
    super.quit();
  }


  public void handleError(String code, String urlString) {
    //actionSetContinuousDownload(false);
    //System.out.println("Error code:"+code);
    //System.out.println("Error url:"+urlString);
    Reader r;
    try {
      if (urlString.indexOf(':') < 0) {
        urlString = "help/" + urlString;
        r = getResource(urlString);
        if (r == null)
          r = new StringReader("Could not load error msg from " + urlString);
      }
      else
        try {
          r = new InputStreamReader(new URL(urlString).openStream());
        }
        catch (MalformedURLException e) {
          e.printStackTrace();
          r = getResource("help/malformedurl.html");
        }
    }
    catch (IOException e) {
      r = getResource("help/errorerror.txt");
    }
    if(display != null) {
      final Reader finalReader = r;
      
      display.asyncExec(new Runnable() {
        public void run() {
          errorDialog.show(finalReader);
        }
      });
    }
    else {
      //ideally we chould also cache the error and pop it up later if/once display != null
      System.err.println("#######Error######");
      BufferedReader bf = new BufferedReader(r);
      String s;
      try {
      while((s = bf.readLine())!=null)
        System.err.println(s);
      } catch(IOException ioee) {
        //ignore ioee
      }
      System.err.println("##################");
    }
  }

  private void showDownloadStatus(String statusText)
  {
    synchronized(strStateLock) {
      removeStatusMessage(strState);
      strState = statusText;
      addStatusMessage(10, strState);
    }
  }

  /** This sets the statusbar */
  public void updateDownloadInfo(final Track track,final String state,final int percentageDone) {
      // Note: Track description says what percentage download, so we don't need to add it here.
    showDownloadStatus(state + " " + track.getName() + " " + percentageDone + "%");
    display.asyncExec(new Runnable() {
      public void run() {
        //int n = percentageDone;
        //boolean barVisible = progressBar.getVisible();
        //String status;
        /*
        if (n > 0 && n < 100) {
          status = strState + " " + n + "%";
          progressBar.setSelection(n);
          if (!barVisible)
            progressBar.setVisible(true);
        }
        else {
          status = strState;
          if (barVisible)
            progressBar.setVisible(false);
        }
        */
        trackTable.updateTrack(track);
      }
    });
  }

  /**
   * Instance must supply a method here to update the display of the status message.
   * It should call 'getHighestPriorityStatusMessage'.
   */
  protected void updateStatusMessage()
  {
    display.asyncExec(new Runnable() {
      public void run() {
        String text = getHighestPriorityStatusMessage();
        if (text == null)
          text = "";
        if (lastStatusMessage == null || !lastStatusMessage.equals(text)) {
          lastStatusMessage = text;
          lblState.setText(text);
        }
      }
    });
  }

  public void updateTrackTable() {
    System.out.println("updateTrackTable()");
    display.asyncExec(new Runnable() {
      public void run() {
        trackTable.updateTable();
        update();
      }
    });
  }

  public void updateTrack(final Track track) {
    // This needs to use asyncExec because it is called by the plugin interface.
    display.asyncExec(new Runnable() {
      public void run() {
        // Update the SWT GUI
        trackTable.updateTrack(track);
        update();
      }
    });
  }

  /**
   * Show the currently playing track on the title bar and select it in the track
   * table.  Also checks to see if auto-download should happen.
   */
  public void update()
  {
    update(playThread.getCurrentTrack(), false);
  }

  /**
   * Show the currently playing track on the title bar and select it in the track
   * table.  Also checks to see if auto-download should happen.
   * @param track The currently playing track.
   * @param newTrackStarted True if a new track has started playing.  This causes
   *     the trackTable to scroll to the new track.
   */
  private void update(Track track, boolean newTrackStarted) {
    if(trackGroup != null && trackGroup.isEnabled()==false) {
      trackGroup.setEnabled(true);
      trackTable.enable();
    }

    //synchronizePlaylist(playListManager, tblSongs);
    if (track == null) {
      trackTable.select(null, false);
      return;
    }
    if (isMac())
      shell.setText(track.toString());
    else
      shell.setText(track.toString() + " - " + 
        Resources.getString("titlebar.program_name"));
    trackLabel.setText(Resources.getString("title.now_playing") + " " + track.getArtist() + " / " + track.getTitle());
    for (int i = 0; i < ratingFunctions.length; i++) {
      RatingFunction rf = ratingFunctions[i];
      ThreeModeButton item = rf.getItem();
      item.setSelection(i != 0 && track.isRated() && rf.getValue() <= track.getRating());
    }
//    volumeScale.setSelection(
//      (track.getVolume() + VOLUME_OFFSET) / VOLUME_RESOLUTION);

      // Scroll the track table to the new track if update() if we have just started a new
      // track.
    trackTable.select(track, newTrackStarted);
    if (track != previousTrack) {
      if (previousTrack != null)
        trackTable.updateTrack(previousTrack);
      previousTrack = track;
    }
    downloadThread.checkAutoDownload();
    previous.setEnabled(playThread.hasHistory());
  }
  
  /**
   * Called from the playThread to indicate that a new track has started playing.
   */
  public void newTrackStarted(final Track track) {
    super.newTrackStarted(track);
    play.setEnabled(true);
    next.setEnabled(true);
    display.asyncExec(new Runnable() {
      public void run() {
        update(track, true);
      }
    });
  }

  /**
   * PluginApplication interface:
   * Get the track that is currently selected.  In some implementations
   * this may be the same as the track that is playing.
   */
  public Track getSelectedTrack() {
    // Return the track that is playing - ignoring the currently selected track.
    return playThread.getCurrentTrack();
  }

  /**
   * PluginApplication interface:
   * Pause or unpause music play.
   */
  public void setPaused(boolean paused) {
    super.setPaused(paused);
    final Boolean pausedFinal = new Boolean(paused);

    // We have to delegate to the SWT event thread, because we might be
    // called from a thread other than it, such as the remote control thread.
    display.asyncExec(new Runnable() {
      public void run() {
        if (pausedFinal.booleanValue()) {
          play.setSelection(false);
          play.setToolTipText(Resources.getString("button.play.tooltip"));
        }
        else {
          play.setSelection(true);
          play.setToolTipText(Resources.getString("button.pause.tooltip"));
        }
      }
    });
  }

  public void skip(final boolean reverse) {
    super.skip(reverse);

    // We have to delegate to the SWT event thread, because we might be
    // called from a thread other than it, such as the remote control thread.
    display.asyncExec(new Runnable() {
      public void run() {
        if (reverse && !playThread.goBack()) {
          previous.setEnabled(false);
        }
        // If the play/pause button is pressed, meaning it is paused, then
        // 'press' the button.  
        if(!play.isPressed()) {
          play.setSelection(false);
        }
      }
    });
  }

  void showAccountDialog() {
    new AccountDialog(display, trackDatabase, downloadThread);
  }

  void uncheckSiblingMenuItems(MenuItem self) {
    Menu parent = self.getParent();
    MenuItem items[] = parent.getItems();
    for (int i = 0; i < parent.getItemCount(); i++)
      parent.getItem(i).setSelection(false);
  }

  /** Create the main iRATE window */
  void initGUI() {
    createShell();
    skinManager = new SkinManager(shell);
    createMenu();
    
    topPanel = new Composite(shell, SWT.FLAT);
    skinManager.addControl(topPanel, "panel.play");
    
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
    gridData.grabExcessHorizontalSpace = true;
//    gridData.heightHint = 74;
    gridData.horizontalSpan = 3;
    
    topPanel.setLayoutData(gridData);
    GridLayout gridLayout = new GridLayout(6, false);
    gridLayout.marginHeight = 7;
    
    topPanel.setLayout(gridLayout);

    
    createToolBar();
    //    createTitle();
    trackTable = new TrackTable(shell, trackDatabase, skinManager);
    trackTable.setMenu(new TrackTableMenu(shell, skinManager, this, ratingFunctions));
    

  
    bottomPanel = new Composite(shell, SWT.FLAT);
    skinManager.addControl(bottomPanel, "panel.status");
    
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.verticalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    
    gridData.horizontalSpan = 3;
    bottomPanel.setLayout(new GridLayout(2, false));
    bottomPanel.setLayoutData(gridData);
    
    
    createState();
    createProgressBar();

    shell.pack();

    Rectangle rec = shell.getBounds();
    // pack doesn't work right on Mac; enforce minimum size
    if (isMac())
        if (rec.width < 920)
            rec.width = 920;
    rec.height = 300;
    shell.setBounds(rec);

    progressBar.setVisible(false);
    
    createTrayItem();
  }

  public void createShell() {
    shell = new Shell(display);
    shell.setText(Resources.getString("titlebar.startup"));
    
    // On Mac OS X we already have an icon set by the application bundle
    if (!isMac())
      try {
        shell.setImage(Resources.getIconImage(display, shell.getBackground()));
      } 
      catch(IOException e) {
        System.out.println("Couldn't load the silly Icon");
      }
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        quit();
      }
    });
    
    //probly should use filllayout..but i dont wanna figure it out
    //gridlayout is overkill for this
    GridLayout layout = new GridLayout(3, false);
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    // Set the layout into the composite.
    shell.setLayout(layout);
  }

  /** @deprecated */
  
  public Reader getResource(String s) {
    if (s.endsWith(".html"))
      s = s.substring(0, s.length() - 5) + ".txt";
    if(help == null)
      help = new Help();
    String str = help.get(s);
    if(str == null)
      return null;
    return new StringReader(str);
  }

  public void actionAbout() {
    aboutDialog.show(getResource("help/about.html"));
  }

  /** launches a web browser 
  @param url web address!
  */
  public void showURL(URL url) {
    try {
      showURLwithJNLP(url);
    }
    catch (Exception e) {
      System.out.println("JNLP:" + e);

      String wholeCmd;
      Runtime r = Runtime.getRuntime();

      // Windows and Mac OS X use standard commands:
      String os = System.getProperty("os.name");
      if (os.startsWith("Mac"))
        wholeCmd = "open";
      else if (os.startsWith("Windows"))
        wholeCmd = "rundll32 url.dll,FileProtocolHandler";
      else {
        // Check the browser preference on Linux/Unix.
        wholeCmd = Preferences.getUserPreference("browser");
        
        // If it's blank then try the GNOME and KDE defaults.
        // Do GNOME first as kfmclient returns 1 on success?
        if (wholeCmd == null || wholeCmd.length() == 0)
          wholeCmd = "gnome-open|kfmclient exec";
      }
      
      // Go through the list of possible commands (separated by |)
      StringTokenizer st = new StringTokenizer(wholeCmd, "|");
      while (st.hasMoreTokens()) {
        String cmd = st.nextToken();
          // We detach the last command in the list, that is, we don't wait for
          // its success or failure to be reported.
        boolean detach = !st.hasMoreTokens();
        int insertPt = cmd.indexOf("%u");
          // If '%u' is in the cmd, then we replace it with the
          // URL. Otherwise we just append the URL.
        if (insertPt != -1) {
          String leftBit = cmd.substring(0,insertPt);
          String rightBit = cmd.substring(insertPt+2);
          cmd = leftBit + url + rightBit;
        } else {
          cmd += " "+url;
        }
        System.out.println("Executing: "+cmd);
        try {
          Process p = r.exec(cmd);
          if (detach)
            break;
          int returnCode = p.waitFor();
            // If it succeeded, then stop looping.
          if (returnCode == 0)
            break;
          System.out.println("While executing, got return value "+returnCode);
        }
        catch (Exception ee) {
          System.out.println("While executing, got exception "+ee.toString());
        }
      }
    }
  }

  /** use webstart to launch a browser */
  private boolean showURLwithJNLP(URL url) throws Exception {
    Class serviceManagerClass = Class.forName("javax.jnlp.ServiceManager");

    Method lookupMethod =
      serviceManagerClass.getMethod("lookup", new Class[] { String.class });

    Object basicServiceObject =
      lookupMethod.invoke(null, new Object[] { "javax.jnlp.BasicService" });
    Method method =
      basicServiceObject.getClass().getMethod("showDocument", new Class[] { URL.class });

    Boolean resultBoolean =
      (Boolean) method.invoke(basicServiceObject, new Object[] { url });

    return resultBoolean.booleanValue();
  }

  public void createMenu() {
    // Needed to clean up on Mac regular quit event
    display.addListener(SWT.Close, new Listener() {
      public void handleEvent(Event e) {
        quit();
      }
    });
    
    Menu menubar = new Menu(shell, SWT.BAR);
    shell.setMenuBar(menubar);
    
    // Declare this as our main menu bar to prettify things on Mac OS X
    MacMenuFixer.setMenuBar(menubar);
    
    final int accel=(isMac() ? SWT.COMMAND : SWT.CTRL);

    MenuItem item1 = new MenuItem(menubar, SWT.CASCADE);
    skinManager.addItem(item1, "toolbar.menu_title.action");

    Menu menu1 = new Menu(item1);
    //Added for a nicer UI by Allen Tipper 14.9.03
    menu1.addMenuListener(new MenuAdapter() {
      public void menuHidden(MenuEvent e) {
        update();
      }
    });
    //end add

    item1.setMenu(menu1);

    MenuItem item_undo = new MenuItem(menu1, SWT.PUSH);
    item_undo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        undoLastRating();
      }
    });
    item_undo.addArmListener(new ToolTipArmListener(this, Resources.getString("toolbar.menu_item.tooltip.undo")));
    skinManager.addItem(item_undo, "toolbar.menu_item.undo");
    item_undo.setAccelerator('Z' + accel);

    // Disable the undo menu item if it's not available
    final MenuItem item_undoX=item_undo;
    menu1.addMenuListener(new MenuAdapter() {
      public void menuShown(MenuEvent e) {
        item_undoX.setEnabled(canUndoLastRating());
      }
    });
    
    MenuItem sep = new MenuItem(menu1, SWT.SEPARATOR);
    
    MenuItem play = new MenuItem(menu1, SWT.PUSH);
    skinManager.addItem(play, "toolbar.menu_item.play");
    play.setAccelerator(' ');
    play.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setPaused(!isPaused());
      }
    });
    
    MenuItem next = new MenuItem(menu1, SWT.PUSH);
    skinManager.addItem(next, "toolbar.menu_item.next");
    next.setAccelerator(SWT.ARROW_RIGHT + accel);
    next.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        skip(false);
      }
    });

    MenuItem prev = new MenuItem(menu1, SWT.PUSH);
    skinManager.addItem(prev, "toolbar.menu_item.prev");
    prev.setAccelerator(SWT.ARROW_LEFT + accel);
    prev.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        skip(true);
      }
    });
 
    MenuItem sep2 = new MenuItem(menu1, SWT.SEPARATOR);

// ------ start of "Sort by" submenu ------

    // SWT for Carbon doesn't pass table column clicks, so can't sort.
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=34160
    // hack added Brion Vibber 2004-05-26
    // Moved here by Stephen Blackheath, and made non-Mac-specific.
    MenuItem view = new MenuItem(menu1, SWT.CASCADE);
    skinManager.addItem(view, "toolbar.menu_title.sort");
    
    Menu mView = new Menu(view);
    view.setMenu(mView);
    
    MenuItem mSortArtist = new MenuItem(mView, SWT.PUSH);
    mSortArtist.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        trackTable.setSortColumn(0);
      }
    });
    skinManager.addItem(mSortArtist, "TrackTable.Heading.Artist");
    
    MenuItem mSortTrack = new MenuItem(mView, SWT.PUSH);
    mSortTrack.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        trackTable.setSortColumn(1);
      }
    });
    skinManager.addItem(mSortTrack, "TrackTable.Heading.Track");
    
    MenuItem mSortRating = new MenuItem(mView, SWT.PUSH);
    mSortRating.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        trackTable.setSortColumn(2);
      }
    });
    skinManager.addItem(mSortRating, "TrackTable.Heading.Rating");
    
    MenuItem mSortPlays = new MenuItem(mView, SWT.PUSH);
    mSortPlays.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        trackTable.setSortColumn(3);
      }
    });
    skinManager.addItem(mSortPlays, "TrackTable.Heading.Plays");
    
    MenuItem mSortLast = new MenuItem(mView, SWT.PUSH);
    mSortLast.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        trackTable.setSortColumn(4);
      }
    });
    skinManager.addItem(mSortLast, "TrackTable.Heading.Last");

// ------ end of "Sort by" submenu ------

    if (isMac()) {
      // Drag-n-drop is broken on SWT/Carbon; need another way to
      // let users get at the files behind their tracks.
      MenuItem mShowFile = new MenuItem(menu1, SWT.PUSH);
      mShowFile.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          Track track = getSelectedTrack();
          File trackfile = track.getFile();
          if (trackfile.exists()) {
            System.out.println("Show track file: " + trackfile);
            String macpath = "file \"" + trackfile.getName() + "\" of ";
            while ((trackfile = trackfile.getParentFile()) != null) {
              if (!trackfile.getName().equals(""))
                macpath = macpath + "folder \"" + trackfile.getName() + "\" of ";
            }
            macpath = macpath + "startup disk";
            System.out.println("Mac path: " + macpath);
            try {
              Process script = Runtime.getRuntime().exec("/usr/bin/osascript");
              PrintStream out = new PrintStream(script.getOutputStream());
              out.println("tell application \"Finder\"");
              out.println("    activate");
              out.println("    select " + macpath);
              out.println("end tell");
              out.close();
              script.waitFor();
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        }
      });
      skinManager.addItem(mShowFile, "toolbar.menu_item.showfile");
    } else {
      // Mac OS X already has Quit in the app menu
      MenuItem item1_4 = new MenuItem(menu1, SWT.PUSH);
      item1_4.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          quit();
        }
      });
      skinManager.addItem(item1_4, "toolbar.menu_item.quit");

      //Added for a nicer UI by Allen Tipper 14.9.03
      item1_4.addArmListener(new ToolTipArmListener(this, Resources.getString("toolbar.menu_item.tooltip.quit")));
      //end add
    }

    MenuItem item2 = new MenuItem(menubar, SWT.CASCADE);
    skinManager.addItem(item2, "toolbar.menu_title.settings");

    Menu mSettings = new Menu(item2);
    //Added for a nicer UI by Allen Tipper 14.9.03
    mSettings.addMenuListener(new MenuAdapter() {
      public void menuHidden(MenuEvent e) {
        update();
      }
    });
    //end add
    item2.setMenu(mSettings);

    /**
     * Added by Eric Dalquist - 11.09.2003
     *
     * Allows the user to select the number of unrated tracks to add to each playlist generation
     */
    MenuItem mNewUnrated = new MenuItem(mSettings, SWT.CHECK);
    skinManager.addItem(mNewUnrated, "toolbar.menu_item.unrated");
    mNewUnrated.addArmListener(new ToolTipArmListener(this, Resources.getString("toolbar.menu_item.tooltip.unrated")));
    if (trackDatabase.getUnratedPlayListRatio() > 0) {
        mNewUnrated.setSelection(true);
    }
    mNewUnrated.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          MenuItem self = (MenuItem) e.getSource();        
          if (self.getSelection()) {
              trackDatabase.setUnratedPlayListRatio(UNRATED_ON_PLAYLIST_VALUE);
          }
          else {
              trackDatabase.setUnratedPlayListRatio(UNRATED_NOT_ON_PLAYLIST_VALUE);
          }
        }
      });

      // Add "Enable RoboJock" menu item if the system has festival installed.
    if (playThread.isSpeechSupported()) {
      MenuItem mRoboJock = new MenuItem(mSettings, SWT.CHECK);
      skinManager.addItem(mRoboJock, "toolbar.menu_item.robojock");
      mRoboJock.addArmListener(new ToolTipArmListener(this, Resources.getString("toolbar.menu_item.tooltip.robojock")));
      if (Preferences.isRoboJockEnabled())
        mRoboJock.setSelection(true);
      mRoboJock.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          MenuItem self = (MenuItem) e.getSource();
          Preferences.setRoboJockEnabled(self.getSelection());
        }
      });
    }

    /*Menu menuNewUnrated = new Menu(mNewUnrated);
    mNewUnrated.setMenu(menuNewUnrated);

    int unratedPlayListRatio = trackDatabase.getUnratedPlayListRatio();
    int[] counts = new int[] { 0, 13 };
    for (int i = 0; i < counts.length; i++) {
      final Integer ratio = new Integer(counts[i]);
      MenuItem mRatio = new MenuItem(menuNewUnrated, SWT.CHECK, i);
      mRatio.setText(ratio + "%");
      mRatio.setSelection(ratio.intValue() == unratedPlayListRatio);
      mRatio.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          MenuItem self = (MenuItem) e.getSource();
          uncheckSiblingMenuItems(self);
          self.setSelection(true);

          trackDatabase.setUnratedPlayListRatio(ratio.intValue());
        }
      });

      //Added for a nicer UI by Allen Tipper 14.9.03
      mRatio.addArmListener(
        new ToolTipArmListener(this,
          Resources.getString("toolbar.submenu_item.tooltip.unrated") +
          " " + ratio + "%."));
      //end add

    }
    /****/

    Player players[] = playerList.getPlayers();
    if (players.length >= 2) {
      MenuItem mPlayers = new MenuItem(mSettings, SWT.CASCADE);
      skinManager.addItem(mPlayers, "toolbar.menu_item.player");
      Menu menu2 = new Menu(mPlayers);
      mPlayers.setMenu(menu2);
  
      //Added for a nicer UI by Allen Tipper 14.9.03
      mPlayers.addArmListener(new ToolTipArmListener(this, Resources.getString("toolbar.menu_item.tooltip.player")));
      //end add
  
      for (int i = 0; i < players.length; i++) {
        final String player = players[i].getName();
  
        MenuItem mPlayer = new MenuItem(menu2, SWT.CHECK, i);
        mPlayer.setText(player);
        if (Preferences.getPlayer().equals(player))
          mPlayer.setSelection(true);
        mPlayer.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            //stupid trick to make self the only selected item
            MenuItem self = (MenuItem) e.getSource();
            uncheckSiblingMenuItems(self);
            self.setSelection(true);
            Preferences.setPlayer(player);
            downloadThread.checkAutoDownload();
          }
        });
        mPlayer.addArmListener(
          new ToolTipArmListener(this, Resources.getString("toolbar.sub_menu_item.tooltip.player") + " " + player));
      }
    }
    
    Menu hidden;
    if (isMac()) {
      // On the Mac, we put some items in the application menu.
      // We use a hidden popup Menu to stick the MenuItems with
      // the selection listeners.
      hidden = new Menu(shell, SWT.POP_UP);
    } else {
      hidden = null;
    }

    MenuItem item2_1 = new MenuItem(isMac() ? hidden : mSettings, SWT.PUSH);
    skinManager.addItem(item2_1, "toolbar.menu_item.advanced");
    item2_1.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        showSettingDialog(SettingDialog.PLUGIN_PAGE);
      }
    });

    //Added for a nicer UI by Allen Tipper 14.9.03
    item2_1.addArmListener(new ToolTipArmListener(this, Resources.getString("toolbar.menu_item.tooltip.advanced")));
    //end add

    MenuItem item3 = new MenuItem(menubar, SWT.CASCADE);
    skinManager.addItem(item3, "toolbar.menu_title.info");

    Menu menu3 = new Menu(item3);

    menu3.addMenuListener(new MenuAdapter() {
      public void menuHidden(MenuEvent e) {
        update();
      }
    });

    item3.setMenu(menu3);

    MenuItem helpItem = new MenuItem(menu3, SWT.PUSH);
    if (isMac()) {
      helpItem.setAccelerator('?' | SWT.COMMAND);
      skinManager.addItem(helpItem, "toolbar.menu_item.help_mac");
	} else {
      helpItem.setAccelerator(SWT.F1);
      skinManager.addItem(helpItem, "toolbar.menu_item.help");
    }
    helpItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        try {
          // Might be nice to have more thorough docs stored locally.
          // For the mac app bundle version, they can be stored in the
          // app bundle and accessed through Help Viewer.
          showURL(new URL("http://irate.sourceforge.net/documentation.gettingstarted.html"));
        } catch (MalformedURLException ex) {
          ex.printStackTrace();
        }
      }
    });
    
    if(System.getProperty("os.name").toLowerCase().startsWith("win")) {
      MenuItem helpSep = new MenuItem(menu3, SWT.SEPARATOR);
    }
    
    MenuItem item3_1 = new MenuItem(isMac() ? hidden : menu3, SWT.PUSH);
    skinManager.addItem(item3_1, "toolbar.menu_item.credits");
    item3_1.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        actionAbout();
      }
    });
    item3_1.addArmListener(new ToolTipArmListener(this, Resources.getString("toolbar.menu_item.tooltip.credits")));
    
    if (isMac()) {
      try {
        /* Touch up the application menu on Mac OS X
         * I apologize for the reflection ugliness! --brion
         */
        Class appMenuClass = Class.forName("com.leuksman.swtdock.ApplicationMenu");
        Method enablePreferences = appMenuClass.getMethod(
          "enablePreferences", new Class[] {});
        Method insertMenuItem = appMenuClass.getMethod("insertMenuItem",
          new Class[] {String.class, java.lang.Integer.TYPE, String.class});
        Method insertSeparator = appMenuClass.getMethod(
          "insertSeparator", new Class[] {java.lang.Integer.TYPE});
        
        Class commandHandlerClass = Class.forName("com.leuksman.swtdock.CommandHandler");
        Method setAndListenFor = commandHandlerClass.getMethod("setAndListenFor",
          new Class[] {String.class, MenuItem.class});
        
        /* Add the 'About' item and enable 'Preferences' */
        Object appmenu = appMenuClass.newInstance();
        enablePreferences.invoke(appmenu, new Object[] {});
        insertMenuItem.invoke(appmenu, new Object[] {
          Resources.getString("toolbar.menu_item.about"),
          new Integer(0), "abou" });
        insertSeparator.invoke(appmenu, new Object[] {new Integer(1)});
        
        /* Attach these commands to the existing MenuItems */
        Object handler = commandHandlerClass.newInstance();
        setAndListenFor.invoke(handler, new Object[] {"abou", item3_1});
        setAndListenFor.invoke(handler, new Object[] {"pref", item2_1});
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void createToolBar() {
    trackGroup = new Composite(topPanel, SWT.BORDER);
    skinManager.addControl(trackGroup, "panel.track");
    trackGroup.setEnabled(false);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalSpan = 1;
    gridData.horizontalIndent = 10;
    trackGroup.setLayoutData(gridData);    
    {
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        trackGroup.setLayout(layout);
    }
    
    /*
    expandButton = new Button(trackGroup, SWT.ARROW|SWT.RIGHT);
    gridData = new GridData();
    gridData.horizontalSpan = 1;
    expandButton.setLayoutData(gridData);
    */
    
    trackLabel = new AlphaLabel(trackGroup, SWT.NONE);
    
      // If you click on the title of the playing track, then the track table will
      // jump to that track.
    trackLabel.addMouseListener(new MouseAdapter() {
      public void mouseDown(MouseEvent e) 
      {
        trackTable.select(getSelectedTrack(), true);
      }
    });
    skinManager.add(trackLabel, "label.track");
    gridData = new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
    trackLabel.setLayoutData(gridData);
 
    /*
    expandButton.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent arg0) {
          if(expandButton.getAlignment() == SWT.RIGHT) {
              expandRatingMenu();
          }
          else {
              collapseRatingMenu();
          }
        }    
      });
      */
    
    Composite trackToolbar = new Composite(trackGroup, SWT.FLAT);
    skinManager.addControl(trackToolbar, "panel.trackToolbar");
    {
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        trackToolbar.setLayoutData(gd);    
    }
    {
        GridLayout layout = new GridLayout(7, false);
        layout.horizontalSpacing = 3;
        layout.marginHeight = 0;
        trackToolbar.setLayout(layout);
    }
    final ThreeModeButton[] ratingButtons = new ThreeModeButton[ratingFunctions.length];
    for (int i = 0; i < ratingFunctions.length; i++) {
      RatingFunction rf = ratingFunctions[i];
      ThreeModeButton button = ratingButtons[i] = new ThreeModeButton(trackToolbar, SWT.FLAT);
      button.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
      final int value = rf.getValue();
      button.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          for (int i = 0; i < ratingButtons.length; i++) {
            ThreeModeButton button = ratingButtons[i];
            button.setSelection(button == e.widget);
          }
          Track track = getSelectedTrack();
          if (track != null)
              setRating(track, value);
        }
      });
      rf.setItem(button);
      button.setToolTipText(Resources.getString(rf.getName()));
      skinManager.add(button, i == 0 ? rf.getName() : "button.star");
    }
  
    new AlphaLabel(trackToolbar, SWT.CENTER).setText("  ");

    final ThreeModeButton info = new ThreeModeButton(trackToolbar, SWT.FLAT);
    info.setLayoutData(new GridData(GridData.FILL_VERTICAL));
    final Client clientToPass = this;
        
        info.addMouseListener(new MouseListener() {
            public void mouseDoubleClick(MouseEvent arg0) {}
            public void mouseDown(MouseEvent arg0) {}
            public void mouseUp(MouseEvent arg0) {
                if(info.getClientArea().contains(arg0.x,arg0.y))
                {
                    Track track = getSelectedTrack();
                    if (track == null)
                        return;
                
                    TrackInfoDialog trackInfoDialog = new TrackInfoDialog(display, shell);
                    trackInfoDialog.displayTrackInfo(track, clientToPass);
                }
            }    
          });

    info.setToolTipText(Resources.getString("button.info.tooltip"));
    skinManager.add(info, "button.info");

    /************ PREVIOUS BUTTON (<<) ****************/
    previous = new ThreeModeButton(topPanel, SWT.NONE);
    previous.setEnabled(false);

//    gridData = new GridData();
//    gridData.verticalAlignment = GridData.VERTICAL_ALIGN_END;
//    previous.setLayoutData(gridData);
    
    previous.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent arg0) {}
      public void mouseDown(MouseEvent arg0) {}
      public void mouseUp(MouseEvent arg0) {
          if(previous.getClientArea().contains(arg0.x,arg0.y))
          {
              if(previous.isEnabled()) {
                  skip(true);
              }
          }
      }    
    });
    
    previous.setToolTipText(Resources.getString("button.previous.tooltip"));
    skinManager.add(previous, "button.previous");
    
    /************ PLAY / PAUSE BUTTON  ****************/
    play = new ThreeModeButton(topPanel, SWT.NONE);
    
//    gridData = new GridData();
//    gridData.verticalAlignment = GridData.VERTICAL_ALIGN_END;
//    play.setLayoutData(gridData);
    play.setSelection(true);
    play.setEnabled(false);
    play.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent arg0) {}
      public void mouseDown(MouseEvent arg0) {}
      public void mouseUp(MouseEvent arg0) {
          if(play.getClientArea().contains(arg0.x,arg0.y))
          {
              setPaused(!isPaused());
          }
      }    
    });
    
    skinManager.add(play, "button.play");
    
    /************ NEXT BUTTON  ****************/
    next = new ThreeModeButton(topPanel, SWT.NONE);
    next.setEnabled(false);
    next.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent arg0) {}
      public void mouseDown(MouseEvent arg0) {}
      public void mouseUp(MouseEvent arg0) {
          if(next.getClientArea().contains(arg0.x,arg0.y))
          {
              skip(false);
          }
      }    
    });
    
    next.setToolTipText(Resources.getString("button.next.tooltip"));
    skinManager.add(next, "button.next");
    
    volumeScale = new Scale(topPanel, SWT.VERTICAL | SWT.FLAT);
    volumeScale.setIncrement(1);
    volumeScale.setPageIncrement(2);
    volumeScale.setMaximum(VOLUME_SPAN / VOLUME_RESOLUTION);
    volumeScale.setToolTipText(Resources.getString("slider.volume.tooltip"));
    volumeScale.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setPlayerVolume(getVolumeSlider());
      }
    });
    gridData = new GridData();
    gridData.heightHint = 75;
    volumeScale.setLayoutData(gridData);
    
    //songProgressBar = new TrackProgressBar(shell, SWT.NONE);
  }
  
  
  /*
  public void expandRatingMenu() {
      expandButton.setAlignment(SWT.DOWN);
      if(ratingGroup == null || ratingGroup.isDisposed()) {
          ratingGroup = new Composite(trackGroup,SWT.NONE);
          GridData gridData = new GridData();
          gridData.grabExcessHorizontalSpace = true;
          gridData.horizontalSpan = 2;
          gridData.horizontalIndent = 0;
          ratingGroup.setLayoutData(gridData);    
          ratingGroup.setLayout(new GridLayout(5, false));            

      gridData = new GridData();
      gridData.widthHint = 100;
          
      Button thisSux = new Button(ratingGroup,SWT.FLAT);
      thisSux.setLayoutData(gridData);
      thisSux.setText("This Sux");
      
      gridData = new GridData();
      gridData.widthHint = 100;
      
      Button yawn = new Button(ratingGroup,SWT.FLAT);
      yawn.setText("Yawn");
      yawn.setLayoutData(gridData);
      
      gridData = new GridData();
      gridData.widthHint = 100;
      
      Button notBad = new Button(ratingGroup,SWT.FLAT);
      notBad.setText("Not Bad");
      notBad.setLayoutData(gridData);
      
      gridData = new GridData();
      gridData.widthHint = 100;
      
      Button cool = new Button(ratingGroup,SWT.FLAT);
      cool.setText("Cool");
      cool.setLayoutData(gridData);
      
      gridData = new GridData();
      gridData.widthHint = 100;
      
      Button excellent = new Button(ratingGroup,SWT.FLAT);
      excellent.setText("Excellent");
      excellent.setLayoutData(gridData);
      
      shell.layout(true);
      topPanel.layout(true);
      trackGroup.layout(true); 
      }
  }
  */
  
  /*
  // Collapses the 
  public void collapseRatingMenu() {
      expandButton.setAlignment(SWT.RIGHT);
      if(!ratingGroup.isDisposed()) 
      {
          ratingGroup.dispose();
          shell.layout(true);
          topPanel.layout(true);
          trackGroup.layout(true);  
      }
  }
  */
  
  
  public int getVolumeSlider() {
    int volume;
    if (isMac())
      volume = VOLUME_OFFSET - (VOLUME_SPAN - volumeScale.getSelection()) * VOLUME_RESOLUTION;
    else
      volume = VOLUME_OFFSET - volumeScale.getSelection() * VOLUME_RESOLUTION;
    //System.out.println("getVolumeSlider: " + volume + " dB");
    return volume;
  }

  public void setVolumeSlider(final int volume) {
    //System.out.println("setVolumeSlider: " + volume + " dB");
    if (isMac())
      volumeScale.setSelection((VOLUME_SPAN - VOLUME_OFFSET + volume) / VOLUME_RESOLUTION);
    else
      volumeScale.setSelection((VOLUME_OFFSET - volume) / VOLUME_RESOLUTION);
  }
  
  
  public void createTrayItem() {
    final Menu trayMenu = new Menu(shell, SWT.POP_UP);
    
    char[] stars = {'\u2605', '\u2605', '\u2605', '\u2605', '\u2605'};
    MenuItem[] rate = new MenuItem[ratingFunctions.length];
    for (int i = 0; i < ratingFunctions.length; i++) {
      RatingFunction rf = ratingFunctions[i];
      rate[i] = new MenuItem(trayMenu, SWT.NONE);
      final int value = rf.getValue();
      rate[i].addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          setRating(getSelectedTrack(), value);
        }
      });
      if(i == 0) {
        // this sux
	    rate[i].setText(Resources.getString(rf.getName()));
	  } else {
	    // stars
	    rate[i].setText(new String(stars, 0, i));
	  }
    }
    
    MenuItem sep = new MenuItem(trayMenu, SWT.SEPARATOR);
    
    MenuItem play = new MenuItem(trayMenu, SWT.PUSH);
    skinManager.addItem(play, "toolbar.menu_item.play");
    play.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setPaused(!isPaused());
      }
    });
    
    MenuItem next = new MenuItem(trayMenu, SWT.PUSH);
    skinManager.addItem(next, "toolbar.menu_item.next");
    next.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        skip(false);
      }
    });

    MenuItem prev = new MenuItem(trayMenu, SWT.PUSH);
    skinManager.addItem(prev, "toolbar.menu_item.prev");
    prev.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        skip(true);
      }
    });
    
    if (isMac()) {
      /* Use the dock tile's menu
       * I apologize for the reflection ugliness... --brion
       */
      try {
        Class commandHandlerClass = Class.forName("com.leuksman.swtdock.CommandHandler");
        Method setAndListenFor = commandHandlerClass.getMethod("setAndListenFor",
          new Class[] {String.class, MenuItem.class});
        
        Class dockTileClass = Class.forName("com.leuksman.swtdock.DockTile");
        Method setMenu = dockTileClass.getMethod(
          "setMenu", new Class[] {Menu.class});
        
        Object dock = dockTileClass.newInstance();
        setMenu.invoke(dock, new Object[] {trayMenu});
        
        Object handler = commandHandlerClass.newInstance();
        for (int i = 0; i < ratingFunctions.length; i++) {
          setAndListenFor.invoke(handler, new Object[] {"Rat" + i, rate[i]});
        }
        setAndListenFor.invoke(handler, new Object[] {"Play", play});
        setAndListenFor.invoke(handler, new Object[] {"Next", next});
        setAndListenFor.invoke(handler, new Object[] {"Prev", prev});
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      /*
      // Try this out later... Requires SWT 3.0
      TrayItem tray = new TrayItem(display.getSystemTray(), SWT.NONE);
      tray.addListener (SWT.Selection, new Listener() {
        public void handleEvent (Event event) {
          trayMenu.setVisible(true);
        }
      });
      */
    }
  }

  public void createState() {
    lblState = new AlphaLabel(bottomPanel, SWT.NONE);
    skinManager.add(lblState, "label.status");

    GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_BOTH);
    gridData.horizontalIndent = 5;
    lblState.setLayoutData(gridData);
  }

  public void createProgressBar() {
    progressBar = new ProgressBar(bottomPanel, SWT.HORIZONTAL);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    //gridData.horizontalSpan = 2;
    gridData.horizontalIndent = 10;
    progressBar.setLayoutData(gridData);
    progressBar.setMinimum(0);
    progressBar.setMaximum(100);
  }

  /**
   * PluginApplication interface:
   * Get a factory that creates suitable UI objects, depending on the style of
   * user interface used in the application.
   **/
  public PluginUIFactory getUIFactory() {
    return uiFactory;
  }

  /** Display a preference
  @page SettingDialog.*_PAGE value.wish java had enums
  */
  public void showSettingDialog(int page) {
    //  if(settingDialog == null) {
    SettingDialog settingDialog =
      new SettingDialog(
        display,
        pluginManager,
        (PluginApplication) Client.this);
    //}
    settingDialog.setPage(page);
    settingDialog.open(display);
  }

  /** Create a DND DropTarger for the Shell. */
  public void createDropTarget() {
    DropTarget target = new DropTarget(shell, DND.DROP_LINK | DND.DROP_MOVE);
    target.setTransfer(new Transfer[] { FileTransfer.getInstance()});
    target.addDropListener(new DropTargetListener() {
      public void dragEnter(DropTargetEvent e) {
      }
      public void dragOver(DropTargetEvent e) {
      }
      public void dragLeave(DropTargetEvent e) {
      }
      public void dragOperationChanged(DropTargetEvent e) {
      }
      public void dropAccept(DropTargetEvent e) {
      }
      public void drop(DropTargetEvent e) {
        if (e.data == null)
          e.detail = DND.DROP_NONE;
        else {
          String[] filenames = (String[]) e.data;
          for (int i = 0; i < filenames.length; i++) {
            File file = new File(filenames[i]);
            if (file.exists()) {
              System.out.println("Import: " + file);
              try {
                Track track = new Track(file.toURL());
                if (trackDatabase.getTrack(track) == null)
                  trackDatabase.add(track);
              }
              catch (MalformedURLException mue) {
                mue.printStackTrace();
              }
              trackTable.updateTable();
            }
          }
        }
      }
    });
  }

  /**
   * This method is called whenever the track time changes, and it
   * updates the progress bar.
   */
  public void positionUpdated(int position, int length) {
    final int currentTime = position;
    int checkedTime;
    if(length == 0) {
      checkedTime = (int)playThread.getCurrentTrack().getPlayingTime();
    }
    else {
      checkedTime = length;
    }
    
    // Send update to all plugins
    List plugins = pluginManager.getPlugins();
    for (int i = 0; i < plugins.size(); i++) {
      Plugin plugin = (Plugin) plugins.get(i);
      plugin.eventPositionUpdated(position, length);
    }
    
//    final int finalTime = checkedTime;
    
//    display.asyncExec(new Runnable() {
//      public void run() {
//        songProgressBar.setCurrentTime(currentTime);
//        songProgressBar.setTotalTime(finalTime);
//      }
//    });
  }

  public void bitRateUpdated(int bitRate) {}

  protected void createNewAccount() {
    showAccountDialog();
  }

  public static void main(String[] args) throws Exception {
    Client client = new Client();
    InputStream skin = null;
    
    if (args.length == 2)
      if (args[0].equals("--skin"))
        skin = new FileInputStream(new File(args[1]));

    if (skin == null)
      try {
        skin = BaseResources.getResourceAsStream("skin.zip");
      }
      catch (IOException e) {
        e.printStackTrace();
      }    
      
    if (skin != null)
      client.skinManager.applySkin(skin);
      
    client.run();
  }

	/* Used by plugins to associate custom actions with tracks
	 * @param name name of the new menu entry..name is already internationalized
	 * @param listener handler for the event
	 * @see irate.plugin.PluginApplication#addTrackAction(java.lang.String, org.eclipse.swt.events.SelectionListener)
	 */
	public void addTrackAction(final String name, final SelectionListener listener) {
	  display.syncExec(new Runnable() {
        public void run() {
     Menu m = trackTable.getMenu();
 		 if(m == null) {
 		   System.err.println("trackTable.getMenu() shouldn't be null"); 
       return;
     }
 		 MenuItem item = new MenuItem(trackTable.getMenu(), SWT.NONE);
 		 item.setText(name);
		 item.addSelectionListener(listener);
        }
      });
	}

  /* (non-Javadoc)
   * @see irate.plugin.PluginApplication#playTrack(irate.common.Track)
   */
  public void playTrack(final Track track) {
    setPaused(false);
    playThread.play(track);
  }

  /* (non-Javadoc)
   * @see irate.plugin.PluginApplication#getUsername()
   */
  public String getUserName() {
    return trackDatabase.getUserName();
  }    
}
