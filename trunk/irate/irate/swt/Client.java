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
  static Display display;
  static Shell shell;
  static ProgressBar progressBar;

  private Hashtable hashSongs = new Hashtable();
  private TrackDatabase trackDatabase;
  private PlayListManager playListManager;
  private PlayThread playThread;
  private DownloadThread downloadThread;

  private String strState = "                                                                     ";
  // private PlayThread playThread;
  
  
  public Client() throws Exception {
   
    File file = new File("trackdatabase.xml");
    try {
      trackDatabase = new TrackDatabase(file);
      trackDatabase.purge();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    playListManager = new PlayListManager(trackDatabase);
    playThread = new PlayThread(playListManager);
 
    initGUI();
    
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
      public void actionPerformed() {
        String state = downloadThread.getState();
        if (!strState.equals(state)) {
          strState = state;
        }
        display.asyncExec(new Runnable() {
          public void run() {
            int n = downloadThread.getPercentComplete();
            if(n > 0 && n < 100)
            {
              lblState.setText(strState + " "+n +"%");
              progressBar.setSelection(n);
            }else
              lblState.setText(strState);            
          }
        });
      }
    });
    downloadThread.start();
  

  }
  
  public void update(){    
    synchronizePlaylist(playListManager, tblSongs);
    Track track = playThread.getCurrentTrack();
    lblTitle.setText(""+track);
    TableItem item = (TableItem)hashSongs.get(track);
    tblSongs.select(tblSongs.indexOf(item));
    tblSongs.showItem(item);    
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
    ti.dispose();
    //reinsert the track with new info
    //should probly update the info inplace with setText :)
    hashSongs.remove(track);
    synchronizePlaylist(playListManager, tblSongs);
    update();
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
    
    final int the_column_index =  column_index;
    
    Comparator c = new Comparator(){
      public int compare(Object o1, Object o2){
        Object obj1[] = (Object[])o1;
        Object obj2[] = (Object[])o2;
        
        String[] s1 = (String[])obj1[1];
        String[] s2 = (String[])obj2[1];
        return s1[the_column_index].compareTo(s2[the_column_index]);
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
    td.sort();
    Track tracks[] = td.getTracks();
    for(int i=0;i<tracks.length;i++){
      if(hashSongs.containsKey(tracks[i]))
        continue;
      TableItem item = new TableItem(tblSongs,SWT.NULL);
      String[] data = {tracks[i].getArtist(),
      tracks[i].getTitle(),
      String.valueOf(tracks[i].getRating()),
      String.valueOf(tracks[i].getNoOfTimesPlayed()),
      tracks[i].getLastPlayed() };
      item.setText(data);
      hashSongs.put(tracks[i], item);
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
  
  void initGUI(){
    display = new Display();
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
    // Create the children of the composite.
    
    Menu menubar = new Menu(shell, SWT.BAR);
    shell.setMenuBar(menubar);
    
    MenuItem item1 = new MenuItem(menubar,SWT.CASCADE);
    item1.setText("Action");
    
    Menu menu1 = new Menu(item1);
    item1.setMenu(menu1);
    
    MenuItem item1_1 = new MenuItem(menu1,SWT.PUSH);
    item1_1.setText("Download More");
    item1_1.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        System.out.println("DOwnload more");
        downloadThread.go();
      }
    });    
    
    MenuItem item1_2 = new MenuItem(menu1,SWT.PUSH);
    item1_2.setText("Purge");
    
    //	MenuItem item1_3 = new MenuItem(menu1,SWT.SEPARATOR);
    
    MenuItem item1_4 = new MenuItem(menu1,SWT.PUSH);
    item1_4.setText("Quit");
    item1_4.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        quit();
      }
    });    
    
    
    
    MenuItem item2 = new MenuItem(menubar,SWT.CASCADE);
    item2.setText("Settings");
    
    Menu menu2 = new Menu(item2);
    item2.setMenu(menu2);
    
    MenuItem item2_1 = new MenuItem(menu2,SWT.PUSH);
    item2_1.setText("Account");
    
    
    lblTitle = new Label(shell, SWT.NONE);
    lblTitle.setText("Current song goes here");
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.horizontalSpan = 2;
    lblTitle.setLayoutData(gridData);
    
    tblSongs = new Table(shell, SWT.NONE);
    
    
    TableColumn col = new TableColumn(tblSongs,SWT.LEFT);
    col.setWidth(100);
    col.setText("Artist");
    col.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        SortTableByStringColumn(0, tblSongs);
      }
    });
    
    col = new TableColumn(tblSongs,SWT.LEFT);
    col.setWidth(100);
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
    col.setWidth(100);
    col.setText("Plays");
    col = new TableColumn(tblSongs,SWT.LEFT);
    col.setWidth(100);
    col.setText("Last");
    //col.setWidth(50);
    tblSongs.setHeaderVisible(true);
    synchronizePlaylist(playListManager, tblSongs);
    
    for(int i = 0;i< tblSongs.getColumns().length;i++)
      tblSongs.getColumns()[i].pack();
    
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.verticalAlignment = GridData.FILL;
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalSpan = 2;
    tblSongs.setLayoutData(gridData);
    tblSongs.pack();
  /*  new Button(shell, SWT.PUSH).setText("This sux");
    new Button(shell, SWT.PUSH).setText("Yawn");
    new Button(shell, SWT.PUSH).setText("Not bad");
    new Button(shell, SWT.PUSH).setText("Cool");
    new Button(shell, SWT.PUSH).setText("Love it");
    new Button(shell, SWT.PUSH).setText("||");
    new Button(shell, SWT.PUSH).setText(">>");
   */ 
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
    
    item = new ToolItem(toolbar,SWT.PUSH);
    item.setText("||");
    item.setEnabled(false);
    
    item = new ToolItem(toolbar,SWT.PUSH);
    item.setText(">>");
    item.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        playThread.reject();
      }
    });



    gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalSpan = 2;
    toolbar.setLayoutData(gridData);
    
    lblState = new Label(shell, SWT.NONE);
    lblState.setText(strState);
    
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.BEGINNING;
    gridData.grabExcessHorizontalSpace = true;
    lblState.setLayoutData(gridData);

    progressBar = new ProgressBar(shell, SWT.HORIZONTAL);
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    progressBar.setLayoutData(gridData);
    
    shell.pack();
    progressBar.setMinimum(0);
    progressBar.setMaximum(100);
    progressBar.setSelection(100);
    
    
    Rectangle rec = shell.getBounds();
    rec.height = 300;
    shell.setBounds(rec);
    Point p = progressBar.getSize();
    lblState.setSize(rec.width - p.x-5, p.y);
    
    shell.open();
  }
  
  public void run(){
    while (!shell.isDisposed()) {
    if (!display.readAndDispatch()) display.sleep();
    }		
  }
  
  public static void main(String[] args) throws Exception{
    new Client().run();
  }
}


