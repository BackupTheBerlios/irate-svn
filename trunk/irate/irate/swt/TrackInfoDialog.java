// Copyright 2003 Anthony Jones

package irate.swt;

import irate.common.LicensingScheme;
import irate.common.Track;
import irate.resources.BaseResources;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TrackInfoDialog {
  
  private Display display;

  private BaseDialog dialog;

  private Shell parent;
  private Client parentClient;
  private Track currentTrack;
  private LicensingScheme license;
  
  private Label trackTitle;
  private Label artist;

  private Label playTime;
  private Label copyright;

  private Label labelAlbum;

  private Label labelComment;

  private Label labelGenre;

  private Label labelYear;

  private Button closeButton;
  private Button wwwLink;
  private Button searchButton;
  private Button licenseButton;

  private Font m_fontHuge;

  private Font m_fontLarge;

  private Font m_fontMedium;
  
  private String creativeLink;

  public TrackInfoDialog(Display display, Shell parent) {
    this.display = display;
    this.parent = parent;
  }

  private void createFonts() {
    Font systemFont = display.getSystemFont();
    m_fontHuge = resizeFontTo(systemFont, 16);
    m_fontLarge = resizeFontTo(systemFont, 12);
    m_fontMedium = resizeFontTo(systemFont, 10);
  }

  private Label createLabel(Composite grid, String tag, String data,
                            Font font) {
    Label labelTag = new Label(grid, SWT.HORIZONTAL);
    labelTag.setText(tag);
    GridData gridData = new GridData();
    labelTag.setLayoutData(gridData);
    labelTag.setFont(font);
    Label labelData = new Label(grid, SWT.HORIZONTAL | SWT.WRAP);
    gridData = new GridData();
    labelData.setLayoutData(gridData);
    labelData.setFont(font);
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

  private Composite createMainGrid(Composite parent) {
    Composite mainGrid = new Composite(parent, SWT.NONE);
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
    Composite infoGrid = new Composite(parent, SWT.NONE);
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
                  currentTrack.getArtist(), m_fontLarge);
    String album = currentTrack.getAlbum();
    if (! album.trim().equals(""))
      labelAlbum =
        createLabel(infoGrid,
                    getResourceString("TrackInfoDialog.Label.Album"),
                    album, m_fontLarge);
    playTime =
      createLabel(infoGrid,
                  getResourceString("TrackInfoDialog.Label.Length"),
                  currentTrack.getPlayingTimeString(), m_fontLarge);
    String genre = currentTrack.getGenre();
    if (! genre.trim().equals(""))
      labelGenre =
        createLabel(infoGrid,
                    getResourceString("TrackInfoDialog.Label.Genre"),
                    genre, m_fontMedium);
    String year = currentTrack.getYear();
    if (! year.trim().equals(""))
      labelYear =
        createLabel(infoGrid,
                    getResourceString("TrackInfoDialog.Label.Year"),
                    year, m_fontMedium);
    String comment = currentTrack.getComment();
    if (! comment.trim().equals(""))
      labelComment =
        createLabel(infoGrid,
                    getResourceString("TrackInfoDialog.Label.Comment"),
                    comment, m_fontMedium);
    copyright =
      createLabel(infoGrid,
                  getResourceString("TrackInfoDialog.Label.Copyright"),
                  license.getFullText(), m_fontMedium);
    if(license.getIcon() != null && ! license.getIcon().equals("")) {    	
      licenseButton = new Button(infoGrid, SWT.FLAT);      
      try {
        Image licenseImage =
          new Image(dialog.getShell().getDisplay(),
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
    return infoGrid;
  }

  private Composite createButtonGrid(Composite parent) {
    Composite buttonGrid = new Composite(parent, SWT.NONE);
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
    if (currentTrack.getWebSite() == null
        || currentTrack.getWebSite().equals(""))  
      wwwLink.setEnabled(false);
    return buttonGrid;
  }

  private void disposeFonts() {
    m_fontHuge.dispose();
    m_fontHuge = null;
    m_fontLarge.dispose();
    m_fontLarge = null;
    m_fontMedium.dispose();
    m_fontMedium = null;
  }

  /** This method builds and shows a TrackInfoDialog. */
  private void buildDialog() {
    createFonts();
    String title =
      getResourceString("TrackInfoDialog.Title") + ": "
      + currentTrack.getTitle();
    dialog = new BaseDialog(display, title);
    Composite mainComposite = dialog.getMainComposite();
    mainComposite.setLayout(new GridLayout(1, false));
    
    trackTitle = new Label(mainComposite, SWT.CENTER);
    trackTitle.setFont(m_fontHuge);
    trackTitle.setText(currentTrack.getTitle());
    GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = true;
    trackTitle.setLayoutData(data);
    
    Label horiDivider = new Label(mainComposite, SWT.SEPARATOR|SWT.HORIZONTAL);
    data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = true;
    horiDivider.setLayoutData(data);

    Composite mainGrid = createMainGrid(mainComposite);

    Composite infoGrid = createInfoGrid(mainGrid);
    data = new GridData();
    data.verticalAlignment = GridData.BEGINNING;
    data.grabExcessHorizontalSpace = true;
    infoGrid.setLayoutData(data);

    Label vertDivider = new Label(mainGrid, SWT.SEPARATOR);
    data = new GridData();
    data.verticalAlignment = GridData.FILL;
    data.grabExcessVerticalSpace = true;
    vertDivider.setLayoutData(data);

    Composite buttonGrid = createButtonGrid(mainGrid);
    data = new GridData();
    data.verticalAlignment = GridData.BEGINNING;
    buttonGrid.setLayoutData(data);

    closeButton =
      dialog.addButton(getResourceString("TrackInfoDialog.Button.Close"));

    addListeners();

    dialog.getShell().pack();
    dialog.centerOn(parent);
    dialog.getShell().open();    
  }

  /**
   * Add the various listeners to the buttons on the dialogue.
   */
  private void addListeners() {
    // Add a listener on the shell so it will close properly
    dialog.getShell().addShellListener(new ShellAdapter() {
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
      parentClient.showURL(currentTrack.getWebSite());
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
        parentClient.showURL(new URL(www));
      }
      catch (Exception z) {
        z.printStackTrace();
      }
    }
  }); 
 
    // Add listener for the license button, if it exists
    if(licenseButton != null)
    {
      licenseButton.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          parentClient.showURL(license.getURL());
        }
      });
    } 
	}
	
  /**
   * Dispose of the shell when a user closes the dialogue box.
   */
  private void actionClose() {
    dialog.dispose();
    dialog = null;
    disposeFonts();
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

// Local Variables:
// c-file-style:"gnu"
// c-basic-offset:2
// indent-tabs-mode:nil
// tab-width:4
// End:
