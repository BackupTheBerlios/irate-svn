// Copyright 2003 Anthony Jones

package irate.swt;

import irate.resources.Resources;
import irate.common.LicensingScheme;
import irate.common.Track;

import java.io.IOException;
import java.net.URLEncoder;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;

public class TrackInfoDialog {
  
  private Display display;
  private Shell shell;
  private Shell parent;
  private Client parentClient;
  private Track currentTrack;
  private LicensingScheme license;
  
  private Label trackTitle;
  private Label artist;
  private Label album;
  private Label playTime;
  private Label copyright;
  private Button closeButton;
  private Button wwwLink;
  private Button searchButton;
  private Button licenseButton;
  
  private String creativeLink;
  
  private final int NUMBER_OF_COLUMNS = 4;

  public TrackInfoDialog(Display display, Shell parent) {
    this.display = display;
    this.parent = parent;
  }

  public void buildDialog() {
    
    // Create the shell and setup the layout
    shell = new Shell(display);
    GridLayout layout = new GridLayout(NUMBER_OF_COLUMNS, false);
    layout.makeColumnsEqualWidth = false;
    layout.horizontalSpacing = 5;
    layout.verticalSpacing = 5;
    shell.setLayout(layout);
    shell.setSize(450, 300);
    
    // Print the title of the current track across the top of the window
    trackTitle = new Label(shell,SWT.HORIZONTAL|SWT.CENTER);
    trackTitle.setFont(resizeFontTo(trackTitle.getFont(), 16));
    trackTitle.setText(currentTrack.getTitle());
    
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = NUMBER_OF_COLUMNS;
    trackTitle.setLayoutData(data);
    
    trackTitle.pack();
    
    // Create a horizontal divider
    Label horiDivider = new Label(shell, SWT.SEPARATOR|SWT.HORIZONTAL);
    data = new GridData();
    data.horizontalSpan = NUMBER_OF_COLUMNS;
    data.widthHint = 450;
    horiDivider.setLayoutData(data);
    horiDivider.pack();
    
    // Create an 'Artist:' label
    Label artistLabel = new Label(shell, SWT.HORIZONTAL);
    artistLabel.setText("Artist:");
    
    GridData gridData = new GridData();
    gridData.widthHint = 80;
    artistLabel.setLayoutData(gridData);
    
    artistLabel.setFont(resizeFontTo(artistLabel.getFont(),12));
    artistLabel.pack();
    
    // Create a label with the artist in it.
    artist = new Label(shell,SWT.HORIZONTAL);
    gridData = new GridData();
    gridData.widthHint = 300;
    artist.setLayoutData(gridData);
    artist.setFont(resizeFontTo(artist.getFont(),12));
    artist.setText(currentTrack.getArtist());
    artist.pack();
    
    // Create a vertical divider
    Label divider = new Label(shell, SWT.SEPARATOR | SWT.VERTICAL);
    gridData = new GridData();
    gridData.verticalSpan = 5;
    gridData.heightHint = 200;
    divider.setLayoutData(gridData);
    divider.pack();
    
    // Create a search button
    searchButton = new Button(shell,0);
    try {
      Image image = new Image(shell.getDisplay(), Resources.getResourceAsStream("magnify-icon.gif"));
      image.setBackground(searchButton.getBackground());
      searchButton.setImage(image);
    } catch (IOException e) {
      e.printStackTrace();
      searchButton.setText("Search"); 
    }
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    searchButton.setLayoutData(gridData);
    
    searchButton.pack();
    
    // Create the 'Album:' label
    Label albumLabel = new Label(shell, SWT.HORIZONTAL);
    albumLabel.setText("Album:");
    albumLabel.setFont(resizeFontTo(albumLabel.getFont(),12));
    gridData = new GridData();
    gridData.widthHint = 80;
    albumLabel.setLayoutData(gridData);
    albumLabel.pack();
    
    // Create label that displays the album
    album = new Label(shell,SWT.HORIZONTAL);
    gridData = new GridData();
    gridData.widthHint = 300;
    album.setLayoutData(gridData);
    album.setFont(resizeFontTo(album.getFont(),12));
    album.setText(currentTrack.getAlbum());
    album.pack();
    
    // Create a WWW link
    wwwLink = new Button(shell,0);
    wwwLink.setText("WWW");
    
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    gridData.widthHint = searchButton.getSize().x;
    gridData.heightHint = searchButton.getSize().y;
    wwwLink.setLayoutData(gridData);
    
    wwwLink.pack();
    
    
    // Create the 'Length:' label
    Label playTimeLabel = new Label(shell, SWT.HORIZONTAL);
    playTimeLabel.setText("Length:");
    playTimeLabel.setFont(resizeFontTo(playTimeLabel.getFont(),12));
    gridData = new GridData();
    gridData.widthHint = 80;
    gridData.heightHint = searchButton.getSize().y;
    playTimeLabel.setLayoutData(gridData);
    playTimeLabel.pack();
    
    //	Create label that displays the playing time
    playTime = new Label(shell,SWT.HORIZONTAL);
    gridData = new GridData();
    gridData.widthHint = 300;
    gridData.heightHint = searchButton.getSize().y;
    playTime.setLayoutData(gridData);
    playTime.setFont(resizeFontTo(album.getFont(),12));
    playTime.setText(currentTrack.getPlayingTimeString());
    playTime.pack();
    
    // Add a CLOSE button
    closeButton = new Button(shell,0);
    closeButton.setText("Close");
    
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    gridData.widthHint = searchButton.getSize().x;
    gridData.heightHint = searchButton.getSize().y;
    closeButton.setLayoutData(gridData);
    
    closeButton.pack();
    	
    //	Create the 'Copyright:' label
    Label copyrightLabel = new Label(shell, SWT.HORIZONTAL);
    copyrightLabel.setText("Copyright:");
    copyrightLabel.setFont(resizeFontTo(copyrightLabel.getFont(),12));
    gridData = new GridData();
    gridData.widthHint = 80;
    gridData.heightHint = 60;
    copyrightLabel.setLayoutData(gridData);
    copyrightLabel.pack();
    
    // Display the copyright information
    copyright = new Label(shell,SWT.HORIZONTAL|SWT.WRAP|SWT.TOP);
    gridData = new GridData();
    gridData.widthHint = 300;
    gridData.heightHint = 60;
    copyright.setLayoutData(gridData);
    copyright.setFont(resizeFontTo(album.getFont(),10));
    copyright.setText(license.getFullText());
    copyright.pack();
    
    Label blank2 = new Label(shell,0);
    gridData = new GridData();
    gridData.heightHint = 60;
    blank2.setLayoutData(gridData);
    blank2.pack(); 
    
    Label blank3 = new Label(shell,0);
    gridData = new GridData();
    //gridData.heightHint = 60;
    //gridData.horizontalSpan = 3;
    blank3.setLayoutData(gridData);
    blank3.pack(); 
    
    // If this track has an icon associated with the license, then display it.
    if(license.getIcon() != null && !license.getIcon().equals("")) {	
    	
    	licenseButton = new Button(shell,SWT.FLAT);
      
      try {
        Image licenseImage = new Image(shell.getDisplay(), Resources.getResourceAsStream(license.getIcon()));
        licenseButton.setImage(licenseImage);
      }
      catch (IOException e) {
        e.printStackTrace();
        licenseButton.setText(license.getName());
      }
    	gridData = new GridData();
    	licenseButton.setLayoutData(gridData);
    	licenseButton.pack();
    
    }
  // Add the listeners and open the shell
  addListeners();
  shell.open();
    
}

private void addListeners() {
	
  // Add a listener on the shell so it will close properly
  shell.addShellListener(new ShellAdapter() {
  	public void shellClosed(ShellEvent e){
  		actionClose();
  	}
  });

  // Add listener for the close button
  closeButton.addSelectionListener(new SelectionAdapter() {
    public void widgetSelected(SelectionEvent e) {
      actionClose();
    }
  });  

  // Add listener for the www button
  wwwLink.addSelectionListener(new SelectionAdapter() {
    public void widgetSelected(SelectionEvent e) {
      parentClient.showURL(currentTrack.getArtistWebsite());
    }
  }); 
		
  // Add listener for the search button
  searchButton.addSelectionListener(new SelectionAdapter() {
    public void widgetSelected(SelectionEvent e) {
    
      String www = "";
      www = "\"" + currentTrack.getArtist() + "\" ";
      www += "\"" + currentTrack.getTitle() + "\"";
      try {
        www = "http://www.google.com/search?q=" + URLEncoder.encode(www);
      }
      catch (Exception z) {
        z.printStackTrace();
      }
      parentClient.showURL(www);
    }
  }); 
 
    // Add listener for the license button, if it exists
    if(licenseButton != null)
    {
      licenseButton.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          parentClient.showURL(license.getURL().toString());
        }
      });
    } 
	}
	
  private void actionClose() {
    shell.dispose();
    shell = null;
  }

  protected void displayTrackInfo(Track track, Client client) {
  	license = new LicensingScheme(track.getCopyrightInfo());
  	parentClient = client;
  	currentTrack = track;
    buildDialog();
  }
  
  private Font resizeFontTo(Font currentFont, int endSize) {
    FontData[] fontData = currentFont.getFontData();
    for (int i = 0; i < fontData.length; i++) {
      fontData[i].setHeight(endSize);
    }
    return(new Font(display, fontData));
  }
}
