// Copyright 2003 Anthony Jones

package irate.swt;

import irate.common.LicensingScheme;
import irate.common.Track;
import irate.resources.BaseResources;

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

  private Shell createShell() {
    Shell shell = new Shell(display);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    shell.setLayout(layout);    
    shell.setText(getResourceString("TrackInfoDialog.Title") + ": "
                  + currentTrack.getTitle());
    try {
      ImageData icon =
        new ImageData(BaseResources.getResourceAsStream("icon.gif")); 
      int whitePixel = icon.palette.getPixel(new RGB(255, 255, 255));
      icon.transparentPixel = whitePixel;
      shell.setImage(new Image(display, icon));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return shell;
  }

  private Label createLabel(Composite grid, String tag, String data,
                            int dataFontSize) {
    Label labelTag = new Label(grid, SWT.HORIZONTAL);
    labelTag.setText(tag);
    GridData gridData = new GridData();
    labelTag.setLayoutData(gridData);
    labelTag.setFont(resizeFontTo(labelTag.getFont(), 12));
    Label labelData = new Label(grid, SWT.HORIZONTAL);
    gridData = new GridData();
    labelData.setLayoutData(gridData);
    labelData.setFont(resizeFontTo(labelData.getFont(), dataFontSize));
    labelData.setText(data);
    return labelData;
  }

  private Button createButton(Composite grid, String text) {
    Button button = new Button(grid, 0);
    button.setText(text);
    GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    button.setLayoutData(data);
    return button;
  }

  private Composite createMainGrid(Shell shell) {
    Composite mainGrid = new Composite(shell, SWT.NONE|SWT.PUSH);
    GridData data = new GridData();
    data.verticalAlignment = GridData.FILL;
    data.grabExcessVerticalSpace = true;
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = true;
    mainGrid.setLayoutData(data);
    GridLayout mainLayout = new GridLayout();
    mainLayout.numColumns = 3;
    mainGrid.setLayout(mainLayout);
    return mainGrid;
  }

  private Composite createInfoGrid(Composite parent) {
    Composite infoGrid = new Composite(parent, SWT.NONE|SWT.PUSH);
    GridLayout infoLayout = new GridLayout();
    infoLayout.numColumns = 2;
    infoGrid.setLayout(infoLayout);
    GridData data = new GridData();
    data.verticalAlignment = GridData.BEGINNING;
    data.grabExcessHorizontalSpace = true;
    infoGrid.setLayoutData(data);
    artist =
      createLabel(infoGrid,
                  getResourceString("TrackInfoDialog.Label.Artist"),
                  currentTrack.getArtist(), 12);
    album =
      createLabel(infoGrid,
                  getResourceString("TrackInfoDialog.Label.Album"),
                  currentTrack.getAlbum(), 12);
    playTime =
      createLabel(infoGrid,
                  getResourceString("TrackInfoDialog.Label.Length"),
                  currentTrack.getPlayingTimeString(), 12);
    copyright =
      createLabel(infoGrid,
                  getResourceString("TrackInfoDialog.Label.Copyright"),
                  license.getFullText(), 10);
    if(license.getIcon() != null && ! license.getIcon().equals("")) {    	
      licenseButton = new Button(infoGrid, SWT.FLAT);      
      try {
        Image licenseImage =
          new Image(shell.getDisplay(),
                    BaseResources.getResourceAsStream(license.getIcon()));
        licenseButton.setImage(licenseImage);
      }
      catch (IOException e) {
        e.printStackTrace();
        licenseButton.setText(license.getName());
      }
      GridData gridData = new GridData();
      gridData.horizontalSpan = 2;
      gridData.horizontalAlignment = GridData.END;
      licenseButton.setLayoutData(gridData);
    }
    comment =
      createLabel(infoGrid,
                  getResourceString("TrackInfoDialog.Label.Comment"),
                  currentTrack.getComment(), 10);
    return infoGrid;
  }

  private Composite createButtonGrid(Composite parent) {
    Composite buttonGrid = new Composite(parent, SWT.NONE|SWT.PUSH);
    GridData data = new GridData();
    data.verticalAlignment = GridData.BEGINNING;
    buttonGrid.setLayoutData(data);
    GridLayout buttonLayout = new GridLayout();
    buttonLayout.numColumns = 1;
    buttonGrid.setLayout(buttonLayout);
    searchButton =
      createButton(buttonGrid,
                   getResourceString("TrackInfoDialog.Button.Search"));
    wwwLink =
      createButton(buttonGrid,
                   getResourceString("TrackInfoDialog.Button.WWW"));
    if (currentTrack.getArtistWebsite() == null
        || currentTrack.getArtistWebsite().equals(""))  
      wwwLink.setEnabled(false);
    closeButton =
      createButton(buttonGrid,
                   getResourceString("TrackInfoDialog.Button.Close"));
    return buttonGrid;
  }

  /**
   * This method builds and shows a TrackInfoDialog.
   */
  private void buildDialog() {
    shell = createShell();

    trackTitle = new Label(shell, SWT.CENTER|SWT.PUSH);
    trackTitle.setFont(resizeFontTo(trackTitle.getFont(), 16));
    trackTitle.setText(currentTrack.getTitle());
    GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = true;
    trackTitle.setLayoutData(data);
    
    Label horiDivider =
        new Label(shell, SWT.SEPARATOR|SWT.HORIZONTAL|SWT.PUSH);
    data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = true;
    horiDivider.setLayoutData(data);

    Composite mainGrid = createMainGrid(shell);

    Composite infoGrid = createInfoGrid(mainGrid);
    data = new GridData();
    data.verticalAlignment = GridData.BEGINNING;
    data.grabExcessHorizontalSpace = true;
    infoGrid.setLayoutData(data);

    Label vertDivider = new Label(mainGrid, SWT.SEPARATOR|SWT.PUSH);
    data = new GridData();
    data.verticalAlignment = GridData.FILL;
    data.grabExcessVerticalSpace = true;
    vertDivider.setLayoutData(data);

    Composite buttonGrid = createButtonGrid(mainGrid);
    data = new GridData();
    data.verticalAlignment = GridData.BEGINNING;
    buttonGrid.setLayoutData(data);

    shell.pack();

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
  
  /**
   * Get a resource string from the properties file associated with this 
   * class.
   */
  private String getResourceString(String key) {
    return Resources.getString(key); 
  }
  
}
