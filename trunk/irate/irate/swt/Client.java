package irate.swt;

import irate.common.TrackDatabase;
import irate.common.Track;
import irate.client.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.io.*;


public class Client {
  static Label lblTitle;
  static Table tblSongs;
  static Display display;
  static Shell shell;
  
  private TrackDatabase trackDatabase;
  private PlayListManager playListManager;
  private PlayThread playThread;
  // private PlayThread playThread;
  
  
  public Client() throws Exception {
    initGUI();
    File file = new File("trackdatabase.xml");
    try {
      trackDatabase = new TrackDatabase(file);
      trackDatabase.purge();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    playListManager = new PlayListManager(trackDatabase);
    fillPlaylist(playListManager, tblSongs);
    playThread = new PlayThread(playListManager);
    playThread.start();
    // playThread = new PlayThread(playListManager);
    //playPanel = new PlayPanel(playListManager, playThread);
    // playThread.start();
    
  }
  
  void fillPlaylist(PlayListManager playListManager, Table tblSongs){
    int itemCount = tblSongs.getItemCount();
    TrackDatabase td = playListManager.getPlayList();
    td.sort();
    Track tracks[] = td.getTracks();
    for(int i=0;i<tracks.length;i++){
      TableItem item = new TableItem(tblSongs,SWT.NULL);
      String[] data = {tracks[i].getArtist(),
      tracks[i].getTitle(),
      String.valueOf(tracks[i].getRating()),
      String.valueOf(tracks[i].getNoOfTimesPlayed()),
      tracks[i].getLastPlayed() };
      item.setText(data);
    }
  
  
  }
  
  void initGUI(){
    display = new Display();
    shell = new Shell(display);
    shell.setText("iRate");
    // Create the layout.
    GridLayout layout = new GridLayout(3, true);
    layout.numColumns = 1;
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
    item1_1.setText("Download");
    
    MenuItem item1_2 = new MenuItem(menu1,SWT.PUSH);
    item1_2.setText("Purge");
    
    //	MenuItem item1_3 = new MenuItem(menu1,SWT.SEPARATOR);
    
    MenuItem item1_4 = new MenuItem(menu1,SWT.PUSH);
    item1_4.setText("Quit");
    
    
    
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
    gridData.horizontalSpan = 7;
    lblTitle.setLayoutData(gridData);
    
    tblSongs = new Table(shell, SWT.NONE);
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.verticalAlignment = GridData.FILL;
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalSpan = 7;
    tblSongs.setLayoutData(gridData);
    
    TableColumn col;
    col = new TableColumn(tblSongs,SWT.LEFT);
    col.setWidth(100);
    col.setText("Artist");
    col = new TableColumn(tblSongs,SWT.LEFT);
    col.setWidth(100);
    col.setText("Track");
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
    
    
  /*  new Button(shell, SWT.PUSH).setText("This sux");
    new Button(shell, SWT.PUSH).setText("Yawn");
    new Button(shell, SWT.PUSH).setText("Not bad");
    new Button(shell, SWT.PUSH).setText("Cool");
    new Button(shell, SWT.PUSH).setText("Love it");
    new Button(shell, SWT.PUSH).setText("||");
    new Button(shell, SWT.PUSH).setText(">>");
   */ 
    ToolBar toolbar = new ToolBar(shell,SWT.FLAT);
    new ToolItem(toolbar,SWT.PUSH).setText("This sux");
    new ToolItem(toolbar,SWT.PUSH).setText("Yawn");
    new ToolItem(toolbar,SWT.PUSH).setText("Not bad");
    new ToolItem(toolbar,SWT.PUSH).setText("Cool");
    new ToolItem(toolbar,SWT.PUSH).setText("Love it");
    new ToolItem(toolbar,SWT.PUSH).setText("||");
    new ToolItem(toolbar,SWT.PUSH).setText(">>");
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    //gridData.horizontalSpan = 7;
    toolbar.setLayoutData(gridData);
        
    shell.pack();
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


