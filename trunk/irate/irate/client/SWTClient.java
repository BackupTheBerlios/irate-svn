package irate.client;

import irate.common.TrackDatabase;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.io.*;


public class SWTClient {
	static Label lblTitle;
	static Table tblSongs;
	static Display display;
	static Shell shell;
	
	private TrackDatabase trackDatabase;
  
	
	public SWTClient() throws Exception {
		initGUI();
		/*File file = new File("trackdatabase.xml");
		try {
		  trackDatabase = new TrackDatabase(file);
		  trackDatabase.purge();
		}
		catch (IOException e) {
		  e.printStackTrace();
		}*/

	}
	
	void initGUI(){
		display = new Display();
		shell = new Shell(display);
		// Create the layout.
		GridLayout layout = new GridLayout(7, true);
		layout.numColumns = 7;
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

		
		new Button(shell, SWT.PUSH).setText("This sux");
		new Button(shell, SWT.PUSH).setText("Yawn");
		new Button(shell, SWT.PUSH).setText("Not bad");
		new Button(shell, SWT.PUSH).setText("Cool");
		new Button(shell, SWT.PUSH).setText("Love it");
		new Button(shell, SWT.PUSH).setText("||");
		new Button(shell, SWT.PUSH).setText(">>");
		
		shell.pack();
		shell.open();
	}
	
	public void run()
	{
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}		
	}

	public static void main(String[] args) throws Exception{
		new SWTClient().run();
	}
}

 