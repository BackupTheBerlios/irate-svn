// Copyright 2003 Anthony Jones

package irate.swt;

import irate.common.LicensingScheme;
import irate.common.Track;
import irate.resources.Resources;

import java.io.IOException;
import java.net.URLEncoder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

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
  private Label comment;
  private Button closeButton;
  private Button wwwLink;
  private Button searchButton;
  private Button licenseButton;
  
  private String creativeLink;

  public TrackInfoDialog(Display display, Shell parent) {
    this.display = display;
    this.parent = parent;
  }

  /**
   * This method builds and shows a TrackInfoDialog.
   */
  private void buildDialog() {
    
    // Create the shell and setup the layout
    shell = new Shell(display);
    GridLayout layout = new GridLayout(2, false);
    shell.setLayout(layout);
    
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
    
    
    shell.setSize(480, 350);
    
    // Print the title of the current track across the top of the window
    trackTitle = new Label(shell,SWT.HORIZONTAL|SWT.CENTER);
    trackTitle.setFont(resizeFontTo(trackTitle.getFont(), 16));
    trackTitle.setText(currentTrack.getTitle());
    
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    trackTitle.setLayoutData(data);
    
    trackTitle.pack();
    
    // Create a horizontal divider, beneath the title.
    Label horiDivider = new Label(shell, SWT.SEPARATOR|SWT.HORIZONTAL);
    data = new GridData();
    data.horizontalSpan = 2;
    data.widthHint = 465;
    horiDivider.setLayoutData(data);
    horiDivider.pack();
    
    // Create the grid for the left hand side of the dialog
    // This grid is 2 columns wide -- one column will have a label and
    // the second the related information
    Composite leftGrid = new Composite(shell, SWT.NONE);
    
    GridData gridData = new GridData();
    gridData.verticalAlignment = SWT.TOP;
    leftGrid.setLayoutData(gridData);
    
    GridLayout leftGridLayout = new GridLayout(2, false);
    leftGridLayout.horizontalSpacing = 10;
    leftGridLayout.verticalSpacing = 5;
    leftGrid.setLayout(leftGridLayout);
     
    // Left Grid, Column 1, Row 1: Create an 'Artist:' label
    Label artistLabel = new Label(leftGrid, SWT.HORIZONTAL);
    artistLabel.setText(Resources.getString("irate.resources.swt","TrackInfoDialog.Label.Artist")); 
    
    gridData = new GridData();
    gridData.widthHint = 80;
    artistLabel.setLayoutData(gridData);
    
    artistLabel.setFont(resizeFontTo(artistLabel.getFont(),12));
    artistLabel.pack();
    
    // Left Grid, Column 2, Row 1: Label with the artist's name
    artist = new Label(leftGrid,SWT.HORIZONTAL);
    
    gridData = new GridData();
    gridData.widthHint = 300;
    
    artist.setLayoutData(gridData);
    artist.setFont(resizeFontTo(artist.getFont(),12));
    artist.setText(currentTrack.getArtist());
    artist.pack();
    
  
    // Left Grid, Column 1, Row 2: 'Album:' label
    Label albumLabel = new Label(leftGrid, SWT.HORIZONTAL);
    albumLabel.setText(Resources.getString("irate.resources.swt","TrackInfoDialog.Label.Album")); 
    albumLabel.setFont(resizeFontTo(albumLabel.getFont(),12));
    
    gridData = new GridData();
    gridData.widthHint = 80;
    
    albumLabel.setLayoutData(gridData);
    albumLabel.pack();
    
    // Left Grid, Column 2, Row 2: Album Label
    album = new Label(leftGrid,SWT.HORIZONTAL);
    gridData = new GridData();
    gridData.widthHint = 300;
    album.setLayoutData(gridData);
    
    album.setFont(resizeFontTo(album.getFont(),12));
    album.setText(currentTrack.getAlbum());
    album.pack();
    
    // Left Grid, Column 1, Row 3: 'Length:' label
    Label playTimeLabel = new Label(leftGrid, SWT.HORIZONTAL);
    playTimeLabel.setText(Resources.getString("irate.resources.swt","TrackInfoDialog.Label.Length")); 
    playTimeLabel.setFont(resizeFontTo(playTimeLabel.getFont(),12));
    
    gridData = new GridData();
    gridData.widthHint = 80;
   
    playTimeLabel.setLayoutData(gridData);
    playTimeLabel.pack();
    
    //	Left Grid, Column 2, Row 3: Playing time label
    playTime = new Label(leftGrid,SWT.HORIZONTAL);
   
    gridData = new GridData();
    gridData.widthHint = 300;
    playTime.setLayoutData(gridData);
   
    playTime.setFont(resizeFontTo(album.getFont(),12));
    playTime.setText(currentTrack.getPlayingTimeString());
    playTime.pack();
    
    //	Left Grid, Column 1, Row 4: 'Copyright:' label
    Label copyrightLabel = new Label(leftGrid, SWT.HORIZONTAL);
    copyrightLabel.setText(Resources.getString("irate.resources.swt","TrackInfoDialog.Label.Copyright")); 
    copyrightLabel.setFont(resizeFontTo(copyrightLabel.getFont(),12));
    
    gridData = new GridData();
    gridData.widthHint = 80;
    gridData.heightHint = 60;
    copyrightLabel.setLayoutData(gridData);
    
    copyrightLabel.pack();
    
    // Left Grid, Column 1, Row 4: Copyright information
    copyright = new Label(leftGrid,SWT.HORIZONTAL|SWT.WRAP);
    
    gridData = new GridData();
    gridData.widthHint = 300;
    gridData.heightHint = 60;
    gridData.verticalAlignment = SWT.BOTTOM;
    copyright.setLayoutData(gridData);
    
    copyright.setFont(resizeFontTo(album.getFont(),10));
    copyright.setText(license.getFullText());
    copyright.pack();
    
    // If this track has an icon associated with the license, then display it.
    if(license.getIcon() != null && !license.getIcon().equals("")) {	 
    	
    	licenseButton = new Button(leftGrid,SWT.FLAT);
      
      try {
        Image licenseImage = new Image(shell.getDisplay(), Resources.getResourceAsStream(license.getIcon()));
        licenseButton.setImage(licenseImage);
      }
      catch (IOException e) {
        e.printStackTrace();
        licenseButton.setText(license.getName());
      }
    	gridData = new GridData();
      gridData.horizontalSpan = 2;
      gridData.horizontalAlignment = SWT.RIGHT;
      gridData.horizontalIndent = 85;
      licenseButton.setLayoutData(gridData);
      
    	licenseButton.pack();
    
    }
    
    // Left Grid, Column 1, Row 5: 'Comment:' label
    Label commentLabel = new Label(leftGrid, SWT.HORIZONTAL);
    commentLabel.setText(Resources.getString("irate.resources.swt","TrackInfoDialog.Label.Comment")); 
    commentLabel.setFont(resizeFontTo(commentLabel.getFont(),12));
    
    gridData = new GridData();
    gridData.widthHint = 80;
    gridData.heightHint = 60; 
    gridData.horizontalSpan = 1;
    commentLabel.setLayoutData(gridData);
    
    commentLabel.pack();

    // Left Grid, Column 2, Row 5: Track Comment
    comment = new Label(leftGrid,SWT.WRAP);
    gridData = new GridData();
    gridData.widthHint = 300;
    gridData.heightHint = 60;
    comment.setLayoutData(gridData);
    
    comment.setFont(resizeFontTo(album.getFont(),10));
    comment.setText(currentTrack.getComment());
    
    comment.pack();
    
    // Build the grid for the right side of the dialog.
    // This grid will have a vertical divider down the first column, and
    // a column of buttons in the other.
    Composite rightGrid = new Composite(shell, SWT.NONE);
    
    gridData = new GridData();
    gridData.verticalAlignment = SWT.TOP;
    rightGrid.setLayoutData(gridData);
    
    GridLayout rightGridLayout = new GridLayout(2, false);
    rightGrid.setLayout(rightGridLayout);
    
    // Right Grid, Column 1, Row 1-8: Vertical divider
    Label divider = new Label(rightGrid, SWT.SEPARATOR | SWT.VERTICAL);
    gridData = new GridData();
    gridData.verticalSpan = 8;
    gridData.heightHint = 250;
    divider.setLayoutData(gridData);
    divider.pack();
      
    // Right Grid, Column 2, Row 2: Search button
    searchButton = new Button(rightGrid,0);
    searchButton.setText(Resources.getString("irate.resources.swt","TrackInfoDialog.Button.Search")); 
    searchButton.pack();
    
    // Right Grid, Column 2, Row 2: WWW Button
    wwwLink = new Button(rightGrid,0);
    wwwLink.setText(Resources.getString("irate.resources.swt","TrackInfoDialog.Button.WWW")); 
      
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    gridData.widthHint = searchButton.getSize().x;
    gridData.heightHint = searchButton.getSize().y;
    wwwLink.setLayoutData(gridData);
    
    // If the website isn't available, grey out this area.  
    if(currentTrack.getArtistWebsite() == null || currentTrack.getArtistWebsite().equals(""))  
    {
      wwwLink.setEnabled(false);
    }
    wwwLink.pack();
    
    // Right Grid, Column 2, Row 3: Close Button
    closeButton = new Button(rightGrid,0);
    closeButton.setText(Resources.getString("irate.resources.swt","TrackInfoDialog.Button.Close")); 
      
    gridData = new GridData();
    gridData.widthHint = searchButton.getSize().x;
    gridData.heightHint = searchButton.getSize().y;
    closeButton.setLayoutData(gridData);
      
    closeButton.pack();

    // Add the listeners and open the shell
    addListeners();
    shell.open();
    
  }

  /**
   * Add the various listeners to the buttons on the dialogue.
   */
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
      www = "\"" + currentTrack.getArtist() + "\" ";  //$NON-NLS-2$
      www += "\"" + currentTrack.getTitle() + "\"";  //$NON-NLS-2$
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
	
  /**
   * Dispose of the shell when a user closes the dialogue box.
   */
  private void actionClose() {
    shell.dispose();
    shell = null;
  }

  /**
   * Display a dialogue box based on the information about the
   * currently playing track.
   */
  public void displayTrackInfo(Track track, Client client) {
  	license = new LicensingScheme(track.getCopyrightInfo());
  	parentClient = client;
  	currentTrack = track;
    buildDialog();
  }
  
  /*
   *  Given a Font object, this method resizes that font to the given value.
   *  This method was taken off the Eclipse site.
   */
  private Font resizeFontTo(Font currentFont, int endSize) {
    FontData[] fontData = currentFont.getFontData();
    for (int i = 0; i < fontData.length; i++) {
      fontData[i].setHeight(endSize);
    }
    return(new Font(display, fontData));
  }
}
