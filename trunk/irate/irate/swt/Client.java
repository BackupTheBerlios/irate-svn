// Copyright 2003 by authors listed below

package irate.swt;

import irate.common.Track;
import irate.client.*;
import irate.resources.Resources;
import irate.swt.plugin.SWTPluginUIFactory;
import irate.plugin.PluginApplication;
import irate.plugin.PluginUIFactory;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

/**
 * Date Updated: $Date: 2003/11/13 03:19:53 $
 * @author Creator: Taras Glek
 * @author Creator: Anthony Jones
 * @author Updated: Eric Dalquist
 * @author Updated: Allen Tipper
 * @author Updated: Stephen Blackheath
 * @version $Revision: 1.100 $
 */
public class Client extends AbstractClient {

  private static final int VOLUME_RESOLUTION = 3;
  private static final int VOLUME_SPAN = 30;
  private static final int VOLUME_OFFSET = VOLUME_SPAN / 2;

  private Label lblState;
  private Display display = new Display();
  private Shell shell;
  private ProgressBar progressBar;
  private Scale volumeScale;

  private ToolItem pause;
  private ToolItem previous;
  private Track previousTrack;
  private Help help = new Help();
  private ErrorDialog errorDialog;

  //  private SettingDialog settingDialog;
  private String strState = "";
  private TrackTable trackTable;

  private SWTPluginUIFactory uiFactory;
  
  private RatingFunction[] ratingFunctions = new RatingFunction[] {
    new RatingFunction(0, "This sux", "Stop playing the current track and never play it again."),
    new RatingFunction(2, "Yawn", "Rate the current track as 2 out of 10."),
    new RatingFunction(5, "Not bad", "Rate the current track as 5 out of 10."),
    new RatingFunction(7, "Cool", "Rate the current track as 7 out of 10."),
    new RatingFunction(10, "Love it", "Rate the current track as 10 out of 10.")
  };
  
  public Client() {
    initGUI();
    errorDialog = new ErrorDialog(display, shell);
		uiFactory = new SWTPluginUIFactory(display, (PluginApplication) this);

    if (trackDatabase.getNoOfTracks() == 0)
      showAccountDialog();

    createDropTarget();
    shell.open();
    downloadThread.start();
    trackTable.addSelectionListener(new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent e) {
        setPaused(false);
        playThread.play(trackTable.getSelectedTrack());
      }
    });
    playThread.start();
  }

  public void handleError(String code, String urlString) {
    //actionSetContinuousDownload(false);
    Reader r;
    try {
      if (urlString.indexOf(':') < 0) {
        r = getResource("help/" + urlString);
        if (r == null)
          throw new NullPointerException();
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
    final Reader finalReader = r;
    display.asyncExec(new Runnable() {
      public void run() {
        errorDialog.show(finalReader);
      }
    });
  }

  public void setState(String state) {
    final boolean newState = !strState.equals(state);
    strState = state;
    display.asyncExec(new Runnable() {
      public void run() {
        int n = downloadThread.getPercentComplete();
        boolean barVisible = progressBar.getVisible();
        if (n > 0 && n < 100) {
          lblState.setText(strState + " " + n + "%");
          progressBar.setSelection(n);
          if (!barVisible)
            progressBar.setVisible(true);
        }
        else {
          lblState.setText(strState);
          if (barVisible)
            progressBar.setVisible(false);
        }
        lblState.pack();
      }
    });
  }

  public void updateTrackTable() {
    display.asyncExec(new Runnable() {
      public void run() {
        trackTable.updateTable();
      }
    });
  }

  public void update() {
    display.asyncExec(new Runnable() {
      public void run() {
        //synchronizePlaylist(playListManager, tblSongs);
        Track track = playThread.getCurrentTrack();
        if (track == null)
          return;
        shell.setText(
          "iRATE radio" + (track == null ? "" : " - " + track.toString()));
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
    });
  }

  //called from playThread.addUpdateListener(this);
  public void actionPerformed() {
    // now update the UI. We don't depend on the result,
    // so use async.
    display.asyncExec(new Runnable() {
      public void run() {
        update();
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
   * Set rating for the specified track.
   * Override the base class so that we can update the GUI after the rating
   * has been changed.
   */
  public void setRating(final Track track, int rating) {
    // Return if there's no track to rate.
    if (track == null)
      return;

    // Call the super method to deal with updating the track and DB.    
    super.setRating(track, rating);

    // Update the SWT GUI
    display.asyncExec(new Runnable() {
      public void run() {
        trackTable.updateTrack(track);
      }
    });
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
          pause.setText("|>");
          pause.setToolTipText("Resume from pause.");
        }
        else {
          pause.setText("||");
          pause.setToolTipText("Pause.");
        }
      }
    });
  }

  public void skip(boolean reverse) {
    super.skip(reverse);
    if (reverse && !playThread.goBack()) {
      previous.setEnabled(false);
    }
  }

  public void quit() {
    super.quit();
    try {
      shell.setVisible(false);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    try {
      trackDatabase.save();
    }
    catch (IOException ee) {
      ee.printStackTrace();
    }
    System.exit(0);
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

  void initGUI() {
    createShell();
    createMenu();
    //    createTitle();
    trackTable = new TrackTable(shell, trackDatabase);
    createToolBar();
    createTableMenu();
    createState();
    createProgressBar();

    shell.pack();

    Rectangle rec = shell.getBounds();
    rec.height = 300;
    shell.setBounds(rec);

    progressBar.setVisible(false);

  }

  public void createShell() {
    shell = new Shell(display);
    shell.setText("iRATE radio");

    try {
      ImageData icon =
        new ImageData(Resources.getResourceAsStream("icon.gif"));
      int whitePixel = icon.palette.getPixel(new RGB(255, 255, 255));
      icon.transparentPixel = whitePixel;
      shell.setImage(new Image(display, icon));
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        quit();
      }
    });
    //probly should use filllayout..but i dont wanna figure it out
    //gridlayout is overkill for this
    GridLayout layout = new GridLayout(2, false);
    layout.horizontalSpacing = 0;
    // Set the layout into the composite.
    shell.setLayout(layout);
  }

  public Reader getResource(String s) {
    if (s.endsWith(".html"))
      s = s.substring(0, s.length() - 5) + ".txt";
    return new StringReader(help.get(s));
  }

  public void actionAbout() {
    errorDialog.show(getResource("help/about.html"));
  }

  /** launches a web browser 
  @param url web address!
  */
  public void showURL(String url) {
    try {
      showURLwithJNLP(new URL(url));
    }
    catch (Exception e) {
      System.out.println("JNLP:" + e);
    }

    String cmd;
    Runtime r = Runtime.getRuntime();
    try {
      cmd = "kfmclient exec " + url + "";
      System.out.println(cmd);
      r.exec(cmd);
    }
    catch (Exception ex) {
      try {
        //win32 way
        cmd = "rundll32 url.dll,FileProtocolHandler '" + url + "'";
        System.out.println(cmd);
        r.exec(cmd);
      }
      catch (Exception eee) {

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
      serviceManagerClass.getMethod("showDocument", new Class[] { URL.class });

    Boolean resultBoolean =
      (Boolean) method.invoke(basicServiceObject, new Object[] { url });

    return resultBoolean.booleanValue();
  }

  public void createMenu() {
    Menu menubar = new Menu(shell, SWT.BAR);
    shell.setMenuBar(menubar);

    MenuItem item1 = new MenuItem(menubar, SWT.CASCADE);
    item1.setText("Action");

    Menu menu1 = new Menu(item1);
    //Added for a nicer UI by Allen Tipper 14.9.03
    menu1.addMenuListener(new MenuAdapter() {
      public void menuHidden(MenuEvent e) {
        update();
      }
    });
    //end add

    item1.setMenu(menu1);

    MenuItem item1_1 = new MenuItem(menu1, SWT.PUSH);
    item1_1.setText("Download");
    item1_1.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        downloadThread.go();
      }
    });

    //Added for a nicer UI by Allen Tipper 16.9.03
    item1_1.addArmListener(new ToolTipArmListener("Download a new track"));
    //end add

    MenuItem item1_4 = new MenuItem(menu1, SWT.PUSH);
    item1_4.setText("Quit");
    item1_4.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        quit();
      }
    });

    //Added for a nicer UI by Allen Tipper 14.9.03
    item1_4.addArmListener(new ToolTipArmListener("Quit iRATE radio"));
    //end add

    MenuItem item2 = new MenuItem(menubar, SWT.CASCADE);
    item2.setText("Settings");

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
    mDownload.setText("Auto download");

    //Added for a nicer UI by Allen Tipper 14.9.03
    mDownload.addArmListener(new ToolTipArmListener("Automatically download"));
    //end add

    Menu menu2 = new Menu(mDownload);
    mDownload.setMenu(menu2);

    int[] counts = new int[] { 0, 5, 11, 17, 23, 29, 37 };
    int autoDownload = trackDatabase.getAutoDownload();
    for (int i = 0; i < counts.length; i++) {
      MenuItem mTimes = new MenuItem(menu2, SWT.CHECK, i);
      final int count = counts[i];
      mTimes.setText(i == 0 ? "Disabled" : "< " + count + " unrated tracks");
      mTimes.setSelection(count == autoDownload);
      mTimes.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          //stupid trick to make self the only selected item
          MenuItem self = (MenuItem) e.getSource();
          uncheckSiblingMenuItems(self);
          self.setSelection(true);
          trackDatabase.setAutoDownload(count);
          downloadThread.checkAutoDownload();
        }
      });

      //Added for a nicer UI by Allen Tipper 14.9.03
      mTimes.addArmListener(
        new ToolTipArmListener(
          "Automatically download when the number of unrated tracks is less than "
            + count));
      //end add

    }

    MenuItem mPlayList = new MenuItem(mSettings, SWT.CASCADE);
    mPlayList.setText("Play list");

    //Added for a nicer UI by Allen Tipper 14.9.03
    mPlayList.addArmListener(new ToolTipArmListener("Set playlist length"));
    //end add

    Menu menuPlayList = new Menu(mPlayList);
    mPlayList.setMenu(menuPlayList);

    counts = new int[] { 5, 7, 13, 19, 31 };
    int playListLength = trackDatabase.getPlayListLength();
    for (int i = 0; i < counts.length; i++) {
      MenuItem mTimes = new MenuItem(menuPlayList, SWT.CHECK, i);
      final int count = counts[i];
      mTimes.setText(count + " tracks");
      mTimes.setSelection(count == playListLength);
      mTimes.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          MenuItem self = (MenuItem) e.getSource();
          uncheckSiblingMenuItems(self);
          self.setSelection(true);
          trackDatabase.setPlayListLength(count);
        }
      });

      //Added for a nicer UI by Allen Tipper 14.9.03
      mTimes.addArmListener(
        new ToolTipArmListener("Set songs in playlist to " + count));
      //end add

    }

    /**
     * Added by Eric Dalquist - 11.09.2003
     *
     * Allows the user to select the number of unrated tracks to add to each playlist generation
     */
    MenuItem mNewUnrated = new MenuItem(mSettings, SWT.CASCADE);
    mNewUnrated.setText("Unrateds on List");
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
          "Set percentage of unrated songs in playlist to " + ratio + "%."));
      //end add

    }
    /****/

    MenuItem mPlayers = new MenuItem(mSettings, SWT.CASCADE);
    mPlayers.setText("Player");
    menu2 = new Menu(mPlayers);
    mPlayers.setMenu(menu2);

    //Added for a nicer UI by Allen Tipper 14.9.03
    mPlayers.addArmListener(new ToolTipArmListener("Set mp3 player"));
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
        new ToolTipArmListener("Set mp3 player to " + player));
    }

    MenuItem item2_1 = new MenuItem(mSettings, SWT.PUSH);
    item2_1.setText("Advanced");
    item2_1.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        showSettingDialog(SettingDialog.PLUGIN_PAGE);
      }
    });

    //Added for a nicer UI by Allen Tipper 14.9.03
    item2_1.addArmListener(new ToolTipArmListener("Select Plugins"));
    //end add

    MenuItem item3 = new MenuItem(menubar, SWT.CASCADE);
    item3.setText("Info");

    Menu menu3 = new Menu(item3);

    menu3.addMenuListener(new MenuAdapter() {
      public void menuHidden(MenuEvent e) {
        update();
      }
    });

    item3.setMenu(menu3);

    MenuItem item3_1 = new MenuItem(menu3, SWT.PUSH);
    item3_1.setText("Credits");
    item3_1.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        actionAbout();
      }
    });
    item3_1.addArmListener(new ToolTipArmListener("Show the Credits"));
  }

  public void createToolBar() {
    ToolBar toolbar = new ToolBar(shell, SWT.FLAT);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = false;
    gridData.horizontalSpan = 1;
    toolbar.setLayoutData(gridData);

    for (int i = 0; i < ratingFunctions.length; i++) {
      RatingFunction rf = ratingFunctions[i];
      ToolItem item = new ToolItem(toolbar, SWT.PUSH);
      item.setText(rf.getName());
      item.setToolTipText(rf.getToolTip());
      final int value = rf.getValue();
      item.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          setRating(getSelectedTrack(), value);
        }
      });
    }

    new ToolItem(toolbar, SWT.SEPARATOR);

    pause = new ToolItem(toolbar, SWT.PUSH);
    setPaused(false);
    pause.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setPaused(!isPaused());
      }
    });

    ToolItem item;
    item = new ToolItem(toolbar, SWT.PUSH);
    item.setText("<<");
    item.setToolTipText("Return to previous track");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        skip(true);
      }
    });
    previous = item;
    previous.setEnabled(false);

    item = new ToolItem(toolbar, SWT.PUSH);
    item.setText(">>");
    item.setToolTipText("Skip to the next track.");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        skip();
      }
    });

    new ToolItem(toolbar, SWT.SEPARATOR);

    item = new ToolItem(toolbar, SWT.PUSH);
    item.setText("Info");
    item.setToolTipText("Display information about the track.");
    final Client clientToPass = this;
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        Track track = getSelectedTrack();
        if (track == null)
          return;
        
        TrackInfoDialog trackInfoDialog = new TrackInfoDialog(display, shell);
        trackInfoDialog.displayTrackInfo(track, clientToPass);
        
//        String www = track.getArtistWebsite();
//
//        if (www == null) {
//          www = "\"" + track.getArtist() + "\" ";
//          www += "\"" + track.getTitle() + "\"";
//          try {
//            // We need to use the deprecated version of this method because the
//            // un-deprecated version isn't implemented in GCJ 3.0.4. We can change
//            // this when we drop support for Debian Woody (when Sarge becomes 
//            // stable).
//            www = "http://www.google.com/search?q=" + URLEncoder.encode(www);
//          }
//          catch (Exception eee) {
//            System.out.println(e.toString());
//          }
//        }
//        showURL(www);
      }
    });

    volumeScale = new Scale(shell, SWT.HORIZONTAL | SWT.FLAT);
    volumeScale.setIncrement(1);
    volumeScale.setPageIncrement(1);
    volumeScale.setMaximum(VOLUME_SPAN / VOLUME_RESOLUTION);
    volumeScale.setToolTipText("Adjust the volume for the current track.");
    volumeScale.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setVolume(
          volumeScale.getSelection() * VOLUME_RESOLUTION - VOLUME_OFFSET);
      }
    });
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = false;
    gridData.horizontalSpan = 1;
    volumeScale.setLayoutData(gridData);
  }
  
  public void createTableMenu() {
    Menu menu = new Menu(shell, SWT.POP_UP);
    trackTable.setMenu(menu);
    for (int i = 0; i < ratingFunctions.length; i++) {
      RatingFunction rf = ratingFunctions[i];
      MenuItem item = new MenuItem(menu, SWT.NONE);
      item.setText(rf.getName());
      item.addArmListener(new ToolTipArmListener(rf.getToolTip()));
      final int value = rf.getValue();
      item.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          setRating(trackTable.getSelectedTrack(), value);
        }
      });
    }
  }

  public void createState() {
    lblState = new Label(shell, SWT.NONE);
    lblState.setText(strState);

    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.BEGINNING;
    gridData.grabExcessHorizontalSpace = true;
    lblState.setLayoutData(gridData);
  }

  public void createProgressBar() {
    progressBar = new ProgressBar(shell, SWT.HORIZONTAL);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    progressBar.setLayoutData(gridData);
    progressBar.setMinimum(0);
    progressBar.setMaximum(100);
  }

  public void run() {
    while (true) {
      if (!display.readAndDispatch())
        display.sleep();
    }
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
      lblState.setText(strState = str);
      lblState.pack();
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

  public static void main(String[] args) throws Exception {
    new Client().run();
  }
  
  private class RatingFunction {
    
    private int value;
    private String name;
    private String toolTip;
    
    public RatingFunction(int value, String name, String toolTip) {
      this.value = value;
      this.name = name;
      this.toolTip = toolTip;
    }
    
    public int getValue() { return value; }
    public String getName() { return name; }
    public String getToolTip() { return toolTip; }
    
  }

}
