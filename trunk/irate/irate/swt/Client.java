// Copyright Taras

package irate.swt;

import irate.common.TrackDatabase;
import irate.common.Track;
import irate.common.UpdateListener;
import irate.client.*;
import irate.download.DownloadThread;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.io.*;
import java.util.*;
import java.net.*;




public class Client implements UpdateListener {
  static Label lblTitle;
  static Label lblState;
  static Table tblSongs;
  static Display display = new Display();
  static Shell shell;
  static ProgressBar progressBar;

  private Hashtable hashSongs = new Hashtable();
  private TrackDatabase trackDatabase;
  private PlayListManager playListManager;
  private PlayerList playerList;
  private PlayThread playThread;
  private DownloadThread downloadThread;
  private ToolItem pause;
  private Track previousTrack;
  
  private String strState = "";
  // private PlayThread playThread;
  private final static boolean debug = true;
  
  
  public Client() {

    File file = new File("trackdatabase.xml");
  
    try {
      trackDatabase = new TrackDatabase(file);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  
    playerList = new PlayerList();
      //try to do a nice initial experience for theuser
      //do as much handholding as possible
    if(!file.exists())
    {
      new AccountDialog(display, trackDatabase);
      Player players[] = playerList.getPlayers();
      trackDatabase.setAutoDownload(2);
      if(players.length > 0)
        trackDatabase.setPlayer(players[0].getName());
      
    }
    playListManager = new PlayListManager(trackDatabase);
    playThread = new PlayThread(playListManager, playerList);
 
    initGUI();
    
    if(playerList.getPlayers().length == 0){
      MessageBox msg = new MessageBox(shell, SWT.ICON_ERROR);
      msg.setMessage("Couldn't locate a commandline mp3 player. Please install madplay, mpg321 or mpg123 into /usr/bin or /usr/local/bin.\n iRate will be unable to play mp3s.");
      msg.open();
    }

    playThread.addUpdateListener(this);
    playThread.start();
    
    
    
    downloadThread = new DownloadThread(trackDatabase) {
      public void process() {
        super.process();
       // perhapsDisableAccount();
      }

      public void handleError(String code, String urlString) {
        //actionSetContinuousDownload(false);
        URL url;
        if (urlString.indexOf(':') < 0)
          url = getClass().getResource("help/" + urlString);
        else 
          try {
            url = new URL(urlString);
          }
          catch (MalformedURLException e) {
            e.printStackTrace();
            url = getClass().getResource("help/malformedurl.html");
          }
          MessageBox msg = new MessageBox(shell, SWT.ICON_ERROR);
          msg.setMessage("Error with url:"+url);
          msg.open();
        }
      };
      final Client client = this;
      downloadThread.addUpdateListener(new UpdateListener() {
      boolean newState = false;
      public void actionPerformed() {
        String state = downloadThread.getState();
        newState = false;
        if (!strState.equals(state)) {
          strState = state;
          newState = true;
        }
        display.asyncExec(new Runnable() {
          public void run() {
            int n = downloadThread.getPercentComplete();
            boolean barVisible = progressBar.getVisible();
            if(n > 0 && n < 100)
            {
              lblState.setText(strState + " "+n +"%");
              progressBar.setSelection(n);
              if(!barVisible)
                progressBar.setVisible(true);
            }else 
            {
              lblState.setText(strState);            
              if(barVisible)
                progressBar.setVisible(false);
            }
            lblState.pack();
            if(newState)
              synchronizePlaylist(playListManager, tblSongs);
          }
        });
      }
    });
    downloadThread.start();
  
    //if this is the first run of irate do that
    if(!file.exists())
      downloadThread.go();

  }

  public void update(){    
    //synchronizePlaylist(playListManager, tblSongs);
    Track track = playThread.getCurrentTrack();
    lblTitle.setText(""+track);
    TableItem item = (TableItem)hashSongs.get(track);
    tblSongs.select(tblSongs.indexOf(item));
    tblSongs.showItem(item);    
    //just in case :)
    track2TableItem(track, item);
    
    if(track != previousTrack) {
      if(previousTrack != null)
        track2TableItem(previousTrack, (TableItem)hashSongs.get(previousTrack));
      previousTrack = track;
    }
    downloadThread.checkAutoDownload();   
  }
  
  //called from playThread.addUpdateListener(this);
  public void actionPerformed(){
    // now update the UI. We don't depend on the result,
    // so use async.  
    display.asyncExec(new Runnable() {
      public void run() {
        update();
      }
    });
  }

  Track getTrackByTableItem(TableItem i)
  {
    Enumeration e = hashSongs.keys();
    while(e.hasMoreElements()){
      Track track = (Track)e.nextElement();
      TableItem  t = (TableItem)hashSongs.get(track);
      if(t == i)
        return track;
    }
    return null;
  }
  
  void track2TableItem(Track track, TableItem tableItem) {
    String[] data = {
      track.getArtist(),
      track.getTitle(),
      track.getState(),
      String.valueOf(track.getNoOfTimesPlayed()),
      track.getLastPlayed() 
    };
    tableItem.setText(data);
  }
  
  public void setRating(int rating) {
  //    int index = list.getSelectedIndex();
    int index = tblSongs.getSelectionIndex();
    Track track; 
    if (index < 0)
      track = playThread.getCurrentTrack();
    else
      track = getTrackByTableItem(tblSongs.getItems()[index]);
    track.setRating(rating);
    
    TableItem ti = (TableItem) hashSongs.get(track);
    track2TableItem(track, ti);
    update();
    //save the precious ratings :)
    try{
      trackDatabase.save();
    }catch(Exception e){
      e.printStackTrace();
    }
  } 
 
  public void setPaused(boolean paused) {
    playThread.setPaused(paused);
    pause.setText(paused ? ">" : "||");
  }
  
  void SortTableByStringColumn(int column_index, Table table)
  {
    //problem with code below is that it loses track/tableitem relationship
    TableItem[] items = table.getItems();
    Vector v = new Vector();
    int column_count = table.getColumnCount();
    table.setVisible(false);
    
    Enumeration e = hashSongs.keys();
    while(e.hasMoreElements()){
      Object obj[] = new Object[2];
      obj[0] = e.nextElement();
      TableItem item = (TableItem)hashSongs.get(obj[0]);
      String values[] = new String[column_count];
      for(int j=0;j<column_count;j++)
        values[j]=item.getText(j);
      item.dispose();
 
      obj[1] = values;
      v.add(obj);
    }
    hashSongs.clear();
    
    final Integer the_column_index = new Integer(column_index);
    
    Comparator c = new Comparator(){
      public int compare(Object o1, Object o2){
        Object obj1[] = (Object[])o1;
        Object obj2[] = (Object[])o2;
        
        String[] s1 = (String[])obj1[1];
        String[] s2 = (String[])obj2[1];
        return s1[the_column_index.intValue()].compareTo(s2[the_column_index.intValue()]);
      }
    };
    Collections.sort(v, c);
    
    for(int i=0;i< v.size();i++)
    {
      //System.out.println(items[i].getText(column_index));
     // items[i].dispose();
      TableItem new_item = new TableItem(table, SWT.NONE, i);
      Object obj[] = (Object[])v.elementAt(i);
      new_item.setText((String[])obj[1]);
      hashSongs.put(obj[0], new_item);
      
    }
    table.setVisible(true);
    
    
    update();
  }
  
  /* todo update # of times played */
  void synchronizePlaylist(PlayListManager playListManager, Table tblSongs){
    int itemCount = tblSongs.getItemCount();
    TrackDatabase td = playListManager.getPlayList();
    Track tracks[] = td.getTracks();
    for(int i=0;i<tracks.length;i++){
      Track track = tracks[i];
      if (!track.isHidden()) {
        TableItem item;
        if(hashSongs.containsKey(track)) {
          item = (TableItem)hashSongs.get(track);
        }
        else{
          item = new TableItem(tblSongs,SWT.NULL);
          hashSongs.put(track, item);
        }
        track2TableItem(track, item);
      }
    }
  }
  
  void quit()
  {
    shell.setVisible(false);
    try {
      trackDatabase.save();
    }
    catch (IOException ee) {
      ee.printStackTrace();
    }
    playThread.reject();
    System.exit(0);
  }
  
  void uncheckSiblingMenuItems(MenuItem self) {
  Menu parent = self.getParent();
  MenuItem items[] = parent.getItems();
  for(int i = 0;i< parent.getItemCount();i++)
    parent.getItem(i).setSelection(false);
  }
  
  void initGUI(){
    createShell();
    createMenu();
    createTitle();
    createSongTable();
    createToolBar();
    createState();
    createProgressBar();

    shell.pack();
    
    Rectangle rec = shell.getBounds();
    rec.height = 300;
    shell.setBounds(rec);
 
    progressBar.setVisible(false);
    shell.open();
  }

  public void createShell() {
    shell = new Shell(display);
    shell.setText("iRATE radio");
    shell.addShellListener(new ShellAdapter()
    {
      public void shellClosed(ShellEvent e){
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
    
  public void createTitle() {
    lblTitle = new Label(shell, SWT.NONE);
//    lblTitle.setText("Current song goes here");
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.horizontalSpan = 2;
    lblTitle.setLayoutData(gridData);
  }
    
  public void createSongTable() {  
    tblSongs = new Table(shell, SWT.NONE);
    
    TableColumn col = new TableColumn(tblSongs,SWT.LEFT);
    col.setWidth(200);
    col.setText("Artist");
    col.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        SortTableByStringColumn(0, tblSongs);
      }
    });
    
    col = new TableColumn(tblSongs,SWT.LEFT);
    col.setWidth(200);
    col.setText("Track");
    col.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        SortTableByStringColumn(1, tblSongs);
      }
    });

    col = new TableColumn(tblSongs,SWT.LEFT);
    col.setWidth(100);
    col.setText("Rating");
    col = new TableColumn(tblSongs,SWT.LEFT);
    col.setWidth(50);
    col.setText("Plays");
    col = new TableColumn(tblSongs,SWT.LEFT);
    col.setWidth(100);
    col.setText("Last");
    col.setWidth(150);
    tblSongs.setHeaderVisible(true);
    synchronizePlaylist(playListManager, tblSongs);
    tblSongs.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
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
  
  public void createMenu() {
    Menu menubar = new Menu(shell, SWT.BAR);
    shell.setMenuBar(menubar);
    
    MenuItem item1 = new MenuItem(menubar,SWT.CASCADE);
    item1.setText("Action");
    
    Menu menu1 = new Menu(item1);
    item1.setMenu(menu1);
    
    MenuItem item1_1 = new MenuItem(menu1,SWT.PUSH);
    item1_1.setText("Download");
    item1_1.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        downloadThread.go();
      }
    });    
    
    MenuItem item1_2 = new MenuItem(menu1,SWT.PUSH);
    item1_2.setText("Purge");
    item1_2.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        trackDatabase.purge();
        update();
      }
    });    
    
    //  MenuItem item1_3 = new MenuItem(menu1,SWT.SEPARATOR);
    
    MenuItem item1_4 = new MenuItem(menu1,SWT.PUSH);
    item1_4.setText("Quit");
    item1_4.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        quit();
      }
    });    
    
    MenuItem item2 = new MenuItem(menubar,SWT.CASCADE);
    item2.setText("Settings");
    
    Menu mSettings = new Menu(item2);
    item2.setMenu(mSettings);
    
    //MenuItem item2_1 = new MenuItem(menu2,SWT.PUSH);
    //item2_1.setText("Account");
    
    MenuItem mDownload = new MenuItem(mSettings, SWT.CASCADE);
    mDownload.setText("Auto Download");
    Menu menu2 = new Menu(mDownload);
    mDownload.setMenu(menu2);
  
    int counts[] = new int[] {0,2, 5, 11, 17, 23, 29};
    int autoDownload = trackDatabase.getAutoDownload(); 
    for(int i=0;i< counts.length;i++){
          MenuItem mTimes = new MenuItem(menu2, SWT.CHECK, i);
      final int count = counts[i];
      mTimes.setText(i==0?"Disabled":"Every " + count + " times" );
      mTimes.setSelection(count == autoDownload);
      mTimes.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        //stupid trick to make self the only selected item
        MenuItem self = (MenuItem)e.getSource();
        uncheckSiblingMenuItems(self);
        self.setSelection(true);
      
        trackDatabase.setAutoDownload(count);
        downloadThread.checkAutoDownload();
      }
      });
    }
      
    MenuItem mPlayers = new MenuItem(mSettings, SWT.CASCADE);
    mPlayers.setText("Player");
    menu2 = new Menu(mPlayers);
    mPlayers.setMenu(menu2);
  
    Player players[] = playerList.getPlayers();
    for(int i=0;i<players.length;i++)
    {
      final String player = players[i].getName();
     
      MenuItem mPlayer = new MenuItem(menu2, SWT.CHECK, i);
      mPlayer.setText(player);
      if(trackDatabase.getPlayer().equals(player))
        mPlayer.setSelection(true);
        mPlayer.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent e){
          //stupid trick to make self the only selected item
          MenuItem self = (MenuItem)e.getSource();
          uncheckSiblingMenuItems(self);
          self.setSelection(true);
      
          trackDatabase.setPlayer(player);
          downloadThread.checkAutoDownload();
        }
      });
    }
  }

  public void createToolBar() {
    ToolBar toolbar = new ToolBar(shell,SWT.FLAT);
    ToolItem item;
    item = new ToolItem(toolbar,SWT.PUSH);
    item.setText("This sux");
    item.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        setRating(0);
        playThread.reject();
      }
    });
    
    item = new ToolItem(toolbar,SWT.PUSH);
    item.setText("Yawn");
    item.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        setRating(2);
      }
    });

    item = new ToolItem(toolbar,SWT.PUSH);
    item.setText("Not bad");
    item.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        setRating(5);
      }
    });

    item = new ToolItem(toolbar,SWT.PUSH);
    item.setText("Cool");
    item.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        setRating(7);
      }
    });

    item = new ToolItem(toolbar,SWT.PUSH);
    item.setText("Love it");
    item.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        setRating(10);
      }
    });

    new ToolItem(toolbar,SWT.SEPARATOR);    
    
    pause = new ToolItem(toolbar,SWT.PUSH);
    pause.setText("||");
    pause.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        setPaused(!playThread.isPaused());
      }
    });
 
    item = new ToolItem(toolbar,SWT.PUSH);
    item.setText(">>");
    item.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        setPaused(false);
        playThread.reject();
        downloadThread.checkAutoDownload();
      }
    });
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalSpan = 2;
    toolbar.setLayoutData(gridData);
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

  public void run(){
    while (true) {
      if (!display.readAndDispatch()) display.sleep();
    }   
  }
  
  
  public static void main(String[] args) throws Exception{
    new Client().run();
  }
}


