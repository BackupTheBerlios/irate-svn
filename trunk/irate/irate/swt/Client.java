// Copyright 2003 by authors listed below

package irate.swt;

import irate.common.TrackDatabase;
import irate.common.Track;
import irate.client.*;
import irate.swt.plugin.SWTPluginUIFactory;
import irate.plugin.PluginApplication;
import irate.plugin.PluginUIFactory;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.io.*;
import java.util.*;
import java.net.*;

/**
 * Date Updated: $Date: 2003/10/04 05:23:34 $
 * @author Creator: Taras Glek
 * @author Creator: Anthony Jones
 * @author Updated: Eric Dalquist
 * @author Updated: Allen Tipper
 * @author Updated: Stephen Blackheath
 * @version $Revision: 1.81 $
 */
public class Client extends AbstractClient {

  private static final int VOLUME_RESOLUTION = 3;
  private static final int VOLUME_SPAN = 30;
  private static final int VOLUME_OFFSET = VOLUME_SPAN / 2;

  private Label lblState;
  private Table tblSongs;
  private Display display = new Display();
  private Shell shell;
  private ProgressBar progressBar;
  private Scale volumeScale;

  private Hashtable hashSongs = new Hashtable();
  private ToolItem pause;
  private ToolItem previous;
  private Track previousTrack;
  private Help help = new Help();
  private ErrorDialog errorDialog;

  private String strState = "";

  private SWTPluginUIFactory uiFactory;

  public Client() {
    initGUI();
    errorDialog = new ErrorDialog(display, shell);
    uiFactory = new SWTPluginUIFactory(display, (PluginApplication) this);
    
    if (trackDatabase.getNoOfTracks() == 0)
      showAccountDialog();      
    
    shell.open();    
    downloadThread.start();
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
        if (newState)
          synchronizePlaylist(playListManager, tblSongs);
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
        shell.setText("iRATE radio" + (track == null ? "" : " - " + track.toString()));
        volumeScale.setSelection(
          (track.getVolume() + VOLUME_OFFSET) / VOLUME_RESOLUTION);
        TableItem item = (TableItem) hashSongs.get(track);
        tblSongs.select(tblSongs.indexOf(item));
        tblSongs.showItem(item);
        //just in case :)
        track2TableItem(track, item);

        if (track != previousTrack) {
          if (previousTrack != null)
            track2TableItem(
              previousTrack,
              (TableItem) hashSongs.get(previousTrack));
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

  private Track getTrackByTableItem(TableItem i) {
    Enumeration e = hashSongs.keys();
    while (e.hasMoreElements()) {
      Track track = (Track) e.nextElement();
      TableItem t = (TableItem) hashSongs.get(track);
      if (t == i)
        return track;
    }
    return null;
  }

  private void track2TableItem(Track track, TableItem tableItem) {
    String[] data =
      {
        track.getArtist(),
        track.getTitle(),
        track.getState(),
        String.valueOf(track.getNoOfTimesPlayed()),
        track.getLastPlayed()};
    tableItem.setText(data);
  }

  /**
   * PluginApplication interface:
   * Get the track that is currently being played.
   */
  public Track getPlayingTrack() {
    return playThread.getCurrentTrack();
  }

  /**
   * PluginApplication interface:
   * Get the track that is currently selected.  In some implementations
   * this may be the same as the track that is playing.
   */
  public Track getSelectedTrack() {
    final Track[] answer = new Track[1];
    final Object[] monitor = new Object[1];
    Runnable r = new Runnable() {
      public void run() {
        try {
          int index = tblSongs.getSelectionIndex();
          Track track;
          if (index < 0)
            answer[0] = getPlayingTrack();
          else
            answer[0] = getTrackByTableItem(tblSongs.getItems()[index]);
        }
        finally {
          if (monitor[0] != null)
            synchronized (monitor[0]) {
              monitor[0].notify();
            }
        }
      }
    };
    if (display.getThread().equals(Thread.currentThread()))
      r.run();
    else {
      // If this isn't the SWT event thread, then we must delegate to it,
      // because we might be called from a thread other than it, such as
      // the remote control thread.
      monitor[0] = new Object();
      synchronized (monitor[0]) {
        display.asyncExec(r);
        try {
          monitor[0].wait();
        }
        catch (InterruptedException e) {
        };
      }
    }
    return answer[0];
  }

  /**
   * PluginApplication interface:
   * Set rating for the specified track.
   */
  public void setRating(final Track track, int rating) {
    // We have to delegate to the SWT event thread, because we might be
    // called from a thread other than it, such as the remote control thread.
    final Integer ratingInt = new Integer(rating);
    display.asyncExec(new Runnable() {
      public void run() {
        track.setRating(ratingInt.intValue());
        if (ratingInt.intValue() == 0 && track == getSelectedTrack())
          playThread.reject();

        TableItem ti = (TableItem) hashSongs.get(track);
        track2TableItem(track, ti);
        update();
        //save the precious ratings :)
        try {
          trackDatabase.save();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void setVolume(final int volume) {
    // We have to delegate to the SWT event thread, because we might be
    // called from a thread other than it, such as the remote control thread.
    final Integer volumeInt = new Integer(volume);
    display.asyncExec(new Runnable() {
      public void run() {
        playThread.setVolume(volumeInt.intValue());

        //save the updated volume
        try {
          trackDatabase.save();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * PluginApplication interface:
   * Return true if music play is paused.
   */
  public boolean isPaused() {
    return playThread.isPaused();
  }

  /**
   * PluginApplication interface:
   * Pause or unpause music play.
   */
  public void setPaused(boolean paused) {
    playThread.setPaused(paused);
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

  /**
   * PluginApplication interface:
   * Skip to the next song.
   */
  public void skip() {
    skip(false);
  }

  public void skip(boolean reverse) {
    setPaused(false);
    if (!reverse) {
      playThread.reject();
      downloadThread.checkAutoDownload();
    }
    else {
      playThread.goBack();
    }
  }

  void sortTable(Table table, Comparator c) {
    //problem with code below is that it loses track/tableitem relationship
    TableItem[] items = table.getItems();
    Vector v = new Vector();
    int column_count = table.getColumnCount();
    table.setVisible(false);

    Enumeration e = hashSongs.keys();
    while (e.hasMoreElements()) {
      Object obj[] = new Object[2];
      obj[0] = e.nextElement();
      TableItem item = (TableItem) hashSongs.get(obj[0]);
      String values[] = new String[column_count];
      for (int j = 0; j < column_count; j++)
        values[j] = item.getText(j);
      item.dispose();

      obj[1] = values;
      v.add(obj);
    }
    hashSongs.clear();

    Collections.sort(v, c);

    for (int i = 0; i < v.size(); i++) {
      //System.out.println(items[i].getText(column_index));
      // items[i].dispose();
      TableItem new_item = new TableItem(table, SWT.NONE, i);
      Object obj[] = (Object[]) v.elementAt(i);
      new_item.setText((String[]) obj[1]);
      hashSongs.put(obj[0], new_item);

    }
    table.setVisible(true);

    update();
  }

  /* todo update # of times played */
  void synchronizePlaylist(PlayListManager playListManager, Table tblSongs) {
    int itemCount = tblSongs.getItemCount();
    TrackDatabase td = playListManager.getTrackDatabase();
    Track tracks[] = td.getTracks();
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (!track.isHidden()) {
        TableItem item;
        if (hashSongs.containsKey(track)) {
          item = (TableItem) hashSongs.get(track);
        }
        else {
          item = new TableItem(tblSongs, SWT.NULL);
          hashSongs.put(track, item);
        }
        track2TableItem(track, item);
      }
    }
  }

  void quit() {
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
    trackDatabase.purge();
    playThread.reject();
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
    createSongTable();
    createToolBar();
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
    /*ImageData icon = new ImageData("/tmp/irate-logo-daniel.png");
    shell.setImage(new Image(display, icon));
    */
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

  private void addColumnListener(TableColumn col, final Comparator c) {
    //final Integer colNo = new Integer(columnNumber);
    col.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        sortTable(tblSongs, c);
      }
    });
  }

  public void createSongTable() {
    tblSongs = new Table(shell, SWT.NONE);

    TableColumn col = new TableColumn(tblSongs, SWT.LEFT);
    col.setWidth(200);
    col.setText("Artist");
    addColumnListener(col, new MagicComparator(0));

    col = new TableColumn(tblSongs, SWT.LEFT);
    col.setWidth(200);
    col.setText("Track");
    addColumnListener(col, new MagicComparator(1));

    col = new TableColumn(tblSongs, SWT.LEFT);
    col.setWidth(100);
    col.setText("Rating");
    addColumnListener(col, new MagicComparator(2));

    col = new TableColumn(tblSongs, SWT.LEFT);
    col.setWidth(50);
    col.setText("Plays");
    addColumnListener(col, new MagicComparator(3));

    col = new TableColumn(tblSongs, SWT.LEFT);
    col.setWidth(150);
    col.setText("Last");
    tblSongs.setHeaderVisible(true);
    synchronizePlaylist(playListManager, tblSongs);
    tblSongs.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setPaused(false);
        playThread.play(getTrackByTableItem(tblSongs.getSelection()[0]));
      }
    });

    //    for(int i = 0;i< tblSongs.getColumns().length;i++)
    //      tblSongs.getColumns()[i].pack();

    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.verticalAlignment = GridData.FILL;
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalSpan = 2;
    tblSongs.setLayoutData(gridData);
    tblSongs.pack();
  }

  public Reader getResource(String s) {
    if (s.endsWith(".html"))
      s = s.substring(0, s.length() - 5) + ".txt";
    return new StringReader(help.get(s));
  }

  public void actionAbout() {
    errorDialog.show(getResource("help/about.html"));
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
    item2_1.setText("Plug-ins");
    item2_1.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        new PluginDialog(
          display,
          pluginManager,
          (PluginApplication) Client.this);
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
    ToolItem item;
    item = new ToolItem(toolbar, SWT.PUSH);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = false;
    gridData.horizontalSpan = 1;
    toolbar.setLayoutData(gridData);

    item.setText("This sux");
    item.setToolTipText(
      "Stop playing the current track and never play it again.");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setRating(getSelectedTrack(), 0);
      }
    });

    item = new ToolItem(toolbar, SWT.PUSH);
    item.setText("Yawn");
    item.setToolTipText("Rate the current track as 2 out of 10.");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setRating(getSelectedTrack(), 2);
      }
    });

    item = new ToolItem(toolbar, SWT.PUSH);
    item.setText("Not bad");
    item.setToolTipText("Rate the current track as 5 out of 10.");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setRating(getSelectedTrack(), 5);
      }
    });

    item = new ToolItem(toolbar, SWT.PUSH);
    item.setText("Cool");
    item.setToolTipText("Rate the current track as 7 out of 10.");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setRating(getSelectedTrack(), 7);
      }
    });

    item = new ToolItem(toolbar, SWT.PUSH);
    item.setText("Love it");
    item.setToolTipText("Rate the current track as 10 out of 10.");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setRating(getSelectedTrack(), 10);
      }
    });

    new ToolItem(toolbar, SWT.SEPARATOR);

    pause = new ToolItem(toolbar, SWT.PUSH);
    setPaused(false);
    pause.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setPaused(!isPaused());
      }
    });

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

  public static void main(String[] args) throws Exception {
    new Client().run();
  }

}
