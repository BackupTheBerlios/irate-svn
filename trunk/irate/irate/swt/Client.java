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
 */
public class Client extends AbstractClient {

  private static final int VOLUME_RESOLUTION = 3;
  private static final int VOLUME_SPAN = 30;
  private static final int VOLUME_OFFSET = VOLUME_SPAN / 2;

  private Composite topPanel;
  private Composite bottomPanel;
  private AlphaLabel lblState;
  private Display display;;
  private Shell shell;
  private AlphaLabel trackLabel;
  private ProgressBar progressBar;
  private Scale volumeScale;
//  private TrackProgressBar songProgressBar;

//  private SkinManager.SkinItem pauseSkin;
  private ThreeModeButton play;
  private ThreeModeButton previous;
  private Track previousTrack;
  private Help help = new Help();
  private ErrorDialog errorDialog;
  private AboutDialog aboutDialog;
  private Composite trackGroup = null;  

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
        showDownloadStatus(downloadThread.getState());
      }//method
      public void newTrackStarted(Track track) { }
    };//ul
    downloadThread.addUpdateListener(ul);
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
    shell.open();
    downloadThread.start();
    trackTable.addSelectionListener(new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent e) {
        setPaused(false);
        playThread.play(trackTable.getSelectedTrack());
      }
    });
    setPaused(false);
    playThread.start();
    
    while (true) {
      if (!display.readAndDispatch())
        display.sleep();
    }
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
    showDownloadStatus(state+" "+track);
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
        lblState.setText(text);
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

  public void update() {
    if(trackGroup != null && trackGroup.isEnabled()==false) {
      trackGroup.setEnabled(true);
      trackTable.enable();
    }
    
    //synchronizePlaylist(playListManager, tblSongs);
    Track track = playThread.getCurrentTrack();
    if (track == null)
      return;
    shell.setText(track.toString() + " - " + 
      Resources.getString("titlebar.program_name"));
    trackLabel.setText(Resources.getString("title.now_playing") + " " + track.getArtist() + " / " + track.getTitle());
    for (int i = 0; i < ratingFunctions.length; i++) {
      RatingFunction rf = ratingFunctions[i];
      ThreeModeButton item = rf.getItem();
      item.setSelection(i != 0 && track.isRated() && rf.getValue() <= track.getRating());
    }
    volumeScale.setSelection(
      (track.getVolume() + VOLUME_OFFSET) / VOLUME_RESOLUTION);
    trackTable.select(track);
    if (track != previousTrack) {
      if (previousTrack != null)
        trackTable.updateTrack(previousTrack);
      previousTrack = track;
    }
    downloadThread.checkAutoDownload();
    previous.setEnabled(playThread.hasHistory());
  }
  
  /** called from playThread.addUpdateListener(this); */
  public void actionPerformed() {
    System.out.println("actionPerformed()");
    display.asyncExec(new Runnable() {
      public void run() {
        update();
        Track track = playThread.getCurrentTrack();
        if (track != null)
          trackTable.updateTrack(track);
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
    final Boolean pausedFinal = Boolean.valueOf(paused);

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

  public void skip(boolean reverse) {
    super.skip(reverse);
    if (reverse && !playThread.goBack()) {
      previous.setEnabled(false);
    }
    // If the play/pause button is pressed, meaning it is paused, then
    // 'press' the button.  
    if(play.isPressed()) {
      play.setSelection(false);
    }
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
    GridLayout gridLayout = new GridLayout(5, false);
    gridLayout.marginHeight = 7;
    
    topPanel.setLayout(gridLayout);

    
    createToolBar();
    //    createTitle();
    trackTable = new TrackTable(shell, trackDatabase, skinManager);
    createTableMenu();
    
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

  }

  public void createShell() {
    shell = new Shell(display);
    shell.setText("iRATE Radio - Initializing");
    try {
      shell.setImage(Resources.getIconImage(display));
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

      String cmd;
      Runtime r = Runtime.getRuntime();
      try {
        // Check the browser preference. 
        cmd = Preferences.getUserPreference("browser");
  
        // If it's blank then try the KDE default and if that fails then we
        // try the Windows default.
        if (cmd == null || cmd.length() == 0) {
          try {
            cmd = "kfmclient exec " + url;
            System.out.println(cmd);
            r.exec(cmd);
            return;
          }
          catch (Exception ex) {
          }
          cmd = "rundll32 url.dll,FileProtocolHandler";
        }
        // If '%u' is in the cmd, then we replace it with the
        // URL. Otherwise we just append the URL.
        int insertPt = cmd.indexOf("%u");
        if (insertPt != -1) {
          String leftBit = cmd.substring(0,insertPt);
          String rightBit = cmd.substring(insertPt+2);
          cmd = leftBit + url + rightBit;
        } else {
          cmd += " " + url;
        }
        System.out.println(cmd);
        r.exec(cmd);
      }
      catch (Exception ee) {
        System.err.println(ee.toString());
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
    item_undo.addArmListener(new ToolTipArmListener(Resources.getString("toolbar.menu_item.tooltip.undo")));
    skinManager.addItem(item_undo, "toolbar.menu_item.undo");
    
    if (!isMac()) {
      // Mac OS X already has Quit in the app menu
      MenuItem item1_4 = new MenuItem(menu1, SWT.PUSH);
      item1_4.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          quit();
        }
      });
      skinManager.addItem(item1_4, "toolbar.menu_item.quit");

      //Added for a nicer UI by Allen Tipper 14.9.03
      item1_4.addArmListener(new ToolTipArmListener(Resources.getString("toolbar.menu_item.tooltip.quit")));
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

    MenuItem mDownload = new MenuItem(mSettings, SWT.CASCADE);
    skinManager.addItem(mDownload, "toolbar.menu_item.auto_download");

    //Added for a nicer UI by Allen Tipper 14.9.03
    mDownload.addArmListener(new ToolTipArmListener(Resources.getString("toolbar.menu_item.tooltip.auto_download")));
    //end add

    Menu menu2 = new Menu(mDownload);
    mDownload.setMenu(menu2);

    int[] counts = new int[] { 0, 5, 11, 17, 23, 29, 37 };
    int autoDownload = trackDatabase.getAutoDownload();
    for (int i = 0; i < counts.length; i++) {
      MenuItem mTimes = new MenuItem(menu2, SWT.CHECK, i);
      final Integer acount = new Integer(counts[i]);
      final int dummy = 0; // workaround for gcj-3.0.4 bug
      mTimes.setText(i == 0 ? Resources.getString("toolbar.sub_menu_item.auto_download.disabled") : "< " + acount + " " + Resources.getString("toolbar.sub_menu_item.auto_download.unrated_tracks"));
      mTimes.setSelection(acount.intValue() == autoDownload);
      mTimes.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          //stupid trick to make self the only selected item
          MenuItem self = (MenuItem) e.getSource();
          uncheckSiblingMenuItems(self);
          self.setSelection(true);
          trackDatabase.setAutoDownload(acount.intValue());
          downloadThread.checkAutoDownload();
        }
      });

      //Added for a nicer UI by Allen Tipper 14.9.03
      mTimes.addArmListener(
        new ToolTipArmListener(
          Resources.getString("toolbar.sub_menu_item.auto_download.tooltip.unrated_tracks")
          + " " + acount));
      //end add

    }

    MenuItem mPlayList = new MenuItem(mSettings, SWT.CASCADE);
    skinManager.addItem(mPlayList, "toolbar.menu_item.play_list");

    //Added for a nicer UI by Allen Tipper 14.9.03
    mPlayList.addArmListener(new ToolTipArmListener(Resources.getString("toolbar.menu_item.tooltip.play_list")));
    //end add

    Menu menuPlayList = new Menu(mPlayList);
    mPlayList.setMenu(menuPlayList);

    counts = new int[] { 13, 19, 31, 49 };
    int playListLength = trackDatabase.getPlayListLength();
    for (int i = 0; i < counts.length; i++) {
      MenuItem mTimes = new MenuItem(menuPlayList, SWT.CHECK, i);
      final int dummy = 0; // workaround for gcj-3.0.4 bug
      final Integer pcount = new Integer(counts[i]);
      mTimes.setText(pcount + " " + Resources.getString("toolbar.sub_menu_item.play_list.tracks"));
      mTimes.setSelection(pcount.intValue() == playListLength);
      mTimes.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          MenuItem self = (MenuItem) e.getSource();
          uncheckSiblingMenuItems(self);
          self.setSelection(true);
          trackDatabase.setPlayListLength(pcount.intValue());
        }
      });

      //Added for a nicer UI by Allen Tipper 14.9.03
      mTimes.addArmListener(
        new ToolTipArmListener(Resources.getString("toolbar.sub_menu_item.play_list.tooltip.tracks") + " " + pcount));
      //end add

    }

    /**
     * Added by Eric Dalquist - 11.09.2003
     *
     * Allows the user to select the number of unrated tracks to add to each playlist generation
     */
    MenuItem mNewUnrated = new MenuItem(mSettings, SWT.CASCADE);
    skinManager.addItem(mNewUnrated, "toolbar.menu_item.unrated");
    mNewUnrated.addArmListener(new ToolTipArmListener(Resources.getString("toolbar.menu_item.tooltip.unrated")));

    Menu menuNewUnrated = new Menu(mNewUnrated);
    mNewUnrated.setMenu(menuNewUnrated);

    int unratedPlayListRatio = trackDatabase.getUnratedPlayListRatio();
    counts = new int[] { 0, 13, 29, 47, 63, 79, 97 };
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
        new ToolTipArmListener(
          Resources.getString("toolbar.submenu_item.tooltip.unrated") +
          " " + ratio + "%."));
      //end add

    }
    /****/

    MenuItem mPlayers = new MenuItem(mSettings, SWT.CASCADE);
    skinManager.addItem(mPlayers, "toolbar.menu_item.player");
    menu2 = new Menu(mPlayers);
    mPlayers.setMenu(menu2);

    //Added for a nicer UI by Allen Tipper 14.9.03
    mPlayers.addArmListener(new ToolTipArmListener(Resources.getString("toolbar.menu_item.tooltip.player")));
    //end add

    Player players[] = playerList.getPlayers();
    for (int i = 0; i < players.length; i++) {
      final String player = players[i].getName();

      MenuItem mPlayer = new MenuItem(menu2, SWT.CHECK, i);
      mPlayer.setText(player);
      if (trackDatabase.getPlayer().equals(player))
        mPlayer.setSelection(true);
      mPlayer.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          //stupid trick to make self the only selected item
          MenuItem self = (MenuItem) e.getSource();
          uncheckSiblingMenuItems(self);
          self.setSelection(true);

          trackDatabase.setPlayer(player);
          downloadThread.checkAutoDownload();
        }
      });
      mPlayer.addArmListener(
        new ToolTipArmListener(Resources.getString("toolbar.sub_menu_item.tooltip.player") + " " + player));
    }

    MenuItem item2_1 = new MenuItem(mSettings, SWT.PUSH);
    skinManager.addItem(item2_1, "toolbar.menu_item.advanced");
    item2_1.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        showSettingDialog(SettingDialog.PLUGIN_PAGE);
      }
    });

    //Added for a nicer UI by Allen Tipper 14.9.03
    item2_1.addArmListener(new ToolTipArmListener(Resources.getString("toolbar.menu_item.tooltip.advanced")));
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

    MenuItem item3_1 = new MenuItem(menu3, SWT.PUSH);
    skinManager.addItem(item3_1, "toolbar.menu_item.credits");
    item3_1.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        actionAbout();
      }
    });
    item3_1.addArmListener(new ToolTipArmListener(Resources.getString("toolbar.menu_item.tooltip.credits")));
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
    trackGroup.setLayout(new GridLayout(2, false));
    
    trackLabel = new AlphaLabel(trackGroup, SWT.CENTER);
    skinManager.add(trackLabel, "label.track");
    gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
    gridData.horizontalSpan = 2;
    trackLabel.setLayoutData(gridData);
    
    Composite trackToolbar = new Composite(trackGroup, SWT.FLAT);
    skinManager.addControl(trackToolbar, "panel.trackToolbar");
    trackToolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));    
    trackToolbar.setLayout(new GridLayout(6, true));
    final ThreeModeButton[] ratingButtons = new ThreeModeButton[ratingFunctions.length];
    for (int i = 0; i < ratingFunctions.length; i++) {
      RatingFunction rf = ratingFunctions[i];
      ThreeModeButton button = ratingButtons[i] = new ThreeModeButton(trackToolbar, SWT.FLAT);
      button.setLayoutData(new GridData(GridData.FILL_BOTH));
      final int value = rf.getValue();
      button.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          for (int i = 0; i < ratingButtons.length; i++) {
            ThreeModeButton button = ratingButtons[i];
            button.setSelection(button == e.widget);
          }
          setRating(getSelectedTrack(), value);
        }
      });
      rf.setItem(button);
      skinManager.add(button, rf.getName());
    }
  
//    new ToolItem(trackToolbar, SWT.SEPARATOR);

    ThreeModeButton info = new ThreeModeButton(topPanel, SWT.NONE);
//    gridData = new GridData();
//    gridData.verticalAlignment = GridData.VERTICAL_ALIGN_END;
//    info.setLayoutData(gridData);
    final Client clientToPass = this;
    info.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        Track track = getSelectedTrack();
        if (track == null)
          return;
        
        TrackInfoDialog trackInfoDialog = new TrackInfoDialog(display, shell);
        trackInfoDialog.displayTrackInfo(track, clientToPass);   
      }
    });
    skinManager.add(info, "button.info");

    volumeScale = new Scale(trackGroup, SWT.HORIZONTAL | SWT.FLAT);
    volumeScale.setIncrement(1);
    volumeScale.setPageIncrement(1);
    volumeScale.setMaximum(VOLUME_SPAN / VOLUME_RESOLUTION);
    volumeScale.setToolTipText(Resources.getString("slider.volume.tooltip"));
    volumeScale.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setVolume(
          volumeScale.getSelection() * VOLUME_RESOLUTION - VOLUME_OFFSET);
      }
    });
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    gridData.grabExcessHorizontalSpace = false;
    volumeScale.setLayoutData(gridData);
    
    
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
        if(previous.isEnabled()) {
          skip(true);
        }
      }    
    });
    
    skinManager.add(previous, "button.previous");
    
    /************ PLAY / PAUSE BUTTON  ****************/
    play = new ThreeModeButton(topPanel, SWT.NONE);
    
//    gridData = new GridData();
//    gridData.verticalAlignment = GridData.VERTICAL_ALIGN_END;
//    play.setLayoutData(gridData);
    
    play.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent arg0) {}
      public void mouseDown(MouseEvent arg0) {}
      public void mouseUp(MouseEvent arg0) {
        setPaused(!isPaused());
      }    
    });
    
    skinManager.add(play, "button.play");
    
    /************ NEXT BUTTON  ****************/
    ThreeModeButton next = new ThreeModeButton(topPanel, SWT.NONE);
    
//    gridData = new GridData();
//    gridData.verticalAlignment = GridData.VERTICAL_ALIGN_END;
//    next.setLayoutData(gridData);
    
    next.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent arg0) {}
      public void mouseDown(MouseEvent arg0) {}
      public void mouseUp(MouseEvent arg0) {
        skip();
      }    
    });
    
    skinManager.add(next, "button.next");
    
//    ToolBar toolbar = new ToolBar(topPanel, SWT.FLAT);
//    gridData = new GridData();
//    gridData.horizontalAlignment = GridData.CENTER;
//    gridData.grabExcessHorizontalSpace = true;
//    gridData.horizontalSpan = 1;
//    toolbar.setLayoutData(gridData);
//
//    previous = new ToolItem(toolbar, SWT.PUSH);
//    previous.setToolTipText(Resources.getString("button.previous.tooltip"));
//    previous.addSelectionListener(new SelectionAdapter() {
//      public void widgetSelected(SelectionEvent e) {
//        skip(true);
//      }
//    });
//    previous.setEnabled(false);
//    skinManager.add(previous, "button.previous");
//
//    pause = new ToolItem(toolbar, SWT.PUSH);
//    setPaused(false);
//    pause.addSelectionListener(new SelectionAdapter() {
//      public void widgetSelected(SelectionEvent e) {
//        setPaused(!isPaused());
//      }
//    });
//    pauseSkin = skinManager.add(pause, "button.pause");
//
//    ToolItem next = new ToolItem(toolbar, SWT.PUSH);
//    next.setToolTipText(Resources.getString("button.next.tooltip"));
//    next.addSelectionListener(new SelectionAdapter() {
//      public void widgetSelected(SelectionEvent e) {
//        skip();
//      }
//    });
//    skinManager.add(next, "button.next");
    
    
//    songProgressBar = new TrackProgressBar(shell, SWT.NONE);
  }
  
  /**
   * Creates the menu that is presented when a track is right-clicked on.
   */
  public void createTableMenu() {
    Menu menu = new Menu(shell, SWT.POP_UP);
    trackTable.setMenu(menu);
    if (isMac()) {
      // SWT for Carbon doesn't pass table column clicks, so can't sort.
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=34160
      // hack added Brion Vibber 2004-05-26
      MenuItem view = new MenuItem(menu, SWT.CASCADE);
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
      // end add
    }
    MenuItem info = new MenuItem(menu, SWT.NONE);
    info.addArmListener(new ToolTipArmListener(Resources.getString("button.info.tooltip")));
    final Client clientToPass = this;
    info.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          Track track = trackTable.getSelectedTrack();
          if (track == null)
            return;
          
          TrackInfoDialog trackInfoDialog = 
            new TrackInfoDialog(display, shell);
          trackInfoDialog.displayTrackInfo(track, clientToPass);   
        }
      });
    skinManager.addItem(info, "button.info");

    for (int i = 0; i < ratingFunctions.length; i++) {
      RatingFunction rf = ratingFunctions[i];
      MenuItem item = new MenuItem(menu, SWT.NONE);
      item.addArmListener(new ToolTipArmListener(Resources.getString(rf.getName() + ".tooltip")));
      final int value = rf.getValue();
      item.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          setRating(trackTable.getSelectedTrack(), value);
        }
      });
      skinManager.addItem(item, rf.getName());
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

  /** Class to show tooltips in the statusbar */
  class ToolTipArmListener implements ArmListener {
    private String str;
    public ToolTipArmListener(String str) {
      this.str = str;
    }
    public void widgetArmed(ArmEvent e) {
      lblState.setText(str);
    }
  }

  /** Create a DND DropTarger for the Shell. */
  public void createDropTarget() {
    DropTarget target = new DropTarget(shell, DND.DROP_LINK | DND.DROP_MOVE);
    target.setTransfer(new Transfer[] { FileTransfer.getInstance()});
    target.addDropListener(new DropTargetListener() {
      public void dragEnter(DropTargetEvent e) {
      };
      public void dragOver(DropTargetEvent e) {
      };
      public void dragLeave(DropTargetEvent e) {
      };
      public void dragOperationChanged(DropTargetEvent e) {
      };
      public void dropAccept(DropTargetEvent e) {
      };
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

  private static class RatingFunction {
    
    private int value;
    private String name;
    private ThreeModeButton item;
    
    public RatingFunction(int value, String name) {
      this.value = value;
      this.name = name;
    }
    
    public int getValue() { return value; }
    public String getName() { return name; }
    
    public void setItem(ThreeModeButton item) { this.item = item; }
    public ThreeModeButton getItem() { return item; }    
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
}
