/*
 * Created on Oct 25, 2003
 */
package irate.swt;

import irate.common.LicenseIndex;
import irate.common.Track;
import irate.common.TrackDatabase;
import irate.resources.BaseResources;

import java.io.IOException;
import java.util.*;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Control;

/**
 * @author Creator: Anthony Jones
 * @author Updated: Stephen Blackheath
 */
public class TrackTable
  implements MenuListener
{
  
  /** The SWT display object associated with the Shell and Table. */
  private Display display;
  
  /** The database containing the list of tracks to display in this table. */ 
  private TrackDatabase trackDatabase;
  
  /** The actual table widget.*/
  private Table table;
  
  /** The list of tracks grabbed out of the TrackDatabase and sorted. */
  private List listOfTracks;
  
  /** A hash table which finds the TableItem associated with a given Track. */
  private Hashtable hashByTrack;
  
  /** A hash table which finds the Track associated with a given TableItem. */
  private Hashtable hashByTableItem;
  
  /** The current comparitor used to sort the table. */
  private TrackComparator comparator;
  
  /** The currently selected track. */
  private Track selected;
  
  /**
   * The track that the user has clicked on.  This is used temporarily while
   * handling mouse events.
   */
  private Track clickedTrack;

  /**
   * The track that is currently selected with a pop-up menu, or null if the pop-up
   * menu is not active.
   */
  private Track menuSelectedTrack;

  /** Used to reduce the number of instances of LicensingScheme. */
  private LicenseIndex licenseIndex = new LicenseIndex();

  /** Alpha blends images with the background color. */ 
  private ImageMerger imageMerger = new ImageMerger();

  /** Hash table for caching generated images. */
  private Hashtable imageHash = new Hashtable();
  
  /** Stores skinable images automagically */
  private BasicSkinable basicSkinable; 
  
  private TrackTableMenu popupMenu;
  
  /** Constructor to create a table contained in the given Shell where the
   * tracks are updated from the given TrackDatabase. 
   * @param composite     The Composite to add the Table to.
   * @param trackDatabase The database containing the list of tracks. Track
   *                      listings are automatically updated from this
   *                      database.
   * @param skinManager   The skin manager used to display graphics for the
   *                      table headings. 
   */ 
  public TrackTable(Composite composite, TrackDatabase trackDatabase, SkinManager skinManager) {
    display = composite.getDisplay();
    this.trackDatabase = trackDatabase;
    basicSkinable = new BasicSkinable(this.display);
    table = new Table(composite, SWT.FULL_SELECTION);
    table.setEnabled(false);

    TableColumn col = new TableColumn(table, SWT.LEFT);
    col.setWidth(200);
    addColumnListener(col, comparator = new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
          return magicStringCompare(track0.getArtist(), track1.getArtist());
      }        
    });
    skinManager.addItem(col, "TrackTable.Heading.Artist"); 

    col = new TableColumn(table, SWT.LEFT);
    col.setWidth(200);
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return magicStringCompare(track0.getTitle(), track1.getTitle());
      }        
    });
    skinManager.addItem(col, "TrackTable.Heading.Track"); 

    col = new TableColumn(table, SWT.LEFT);
    col.setWidth(120);
    // Setting the alighnment to left doesn't seem to make a difference for images on GTK
    col.setAlignment(SWT.LEFT);
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return magicStringCompare(track0.getState(), track1.getState());
      }        
    });
    skinManager.addItem(col, "TrackTable.Heading.Rating");

    col = new TableColumn(table, SWT.LEFT);
    col.setAlignment(SWT.LEFT);
    col.setWidth(50);
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return new Integer(track0.getNoOfTimesPlayed()).compareTo(new Integer(track1.getNoOfTimesPlayed()));
      }        
    });
    skinManager.addItem(col, "TrackTable.Heading.Plays");

    col = new TableColumn(table, SWT.LEFT);
    col.setWidth(120);
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return track0.getLastPlayed().compareTo(track1.getLastPlayed());
      }        
    });
    table.setHeaderVisible(true);
    skinManager.addItem(col, "TrackTable.Heading.Last"); 

    col = new TableColumn(table, SWT.LEFT);
    col.setWidth(60);
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
          return licenseIndex.get(track0).compareTo(licenseIndex.get(track1));
      }        
    });
    skinManager.addItem(col, "TrackTable.Heading.License");
    skinManager.add(basicSkinable, "TrackTable");
    
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.verticalAlignment = GridData.FILL;
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalSpan = 3;
    table.setLayoutData(gridData);
    table.pack();

//    This code updates table items as they get selected and unselected. It would be good
//    if the background colour for selected items could be predicted however there doesn't
//    appear to be an easy way to find this colour.
//    
//    table.addSelectionListener(new SelectionAdapter() {
//      private TableItem[] selection;
//      private void update(TableItem[] tableItems) {
//        if (tableItems != null)
//          for (int i = 0 ; i < tableItems.length; i++) {
//            TableItem tableItem = tableItems[i];
//            Track track = (Track) hashByTableItem.get(tableItems[i]);
//            if (track != null)
//              updateTableItem(tableItem, track);
//          }
//      }
//      public void widgetSelected(SelectionEvent e) {
//        update(selection);
//        selection = table.getSelection();
//        update(selection);
//      }
//    });
    
    DragSource source = new DragSource(table, DND.DROP_COPY);
    source.setTransfer(new Transfer[] { FileTransfer.getInstance() });
    source.addDragListener(new DragSourceListener() {
      
      /** The track that we're dragging. */
      private Track track;
      
      public void dragStart(DragSourceEvent e) {
        track = clickedTrack;
        e.doit = track != null;
      }
      
      public void dragSetData(DragSourceEvent e) {
        try {
          e.data = new String[] { track.getFile().getAbsoluteFile().getAbsolutePath() };
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      } 
      
      public void dragFinished(DragSourceEvent e) {
      }
    });

    table.addMouseListener(new MouseAdapter() {
      public void mouseDown(MouseEvent e) 
      {
        // Carbon tables have some extra column margin width.
        // Table.EXTRA_WIDTH is hardcoded to 24, and of course not public.
        // There are also about 4 pixels on the right due for the focus ring.
        final int margin = Client.isMac() ? 24 : 0;
        final int gutter = Client.isMac() ? 4 : 0;
        
        // On Mac OS X, column header selection is broken. We need to check
        // here for a column header hit to provide sort-on-click:
        if (Client.isMac() && e.y < 20) {
          final int sizeHandle = 4; // Resizing columns
          int colStart = gutter;
          for(int i = 0; i < table.getColumnCount(); i++ ) {
            int colWidth = table.getColumn(i).getWidth() + margin;
            if( e.x >= colStart && e.x - colStart < colWidth - sizeHandle ) {
              System.out.println("mouseDown column hit on " + i);
              setSortColumn(i);
              return;
            }
            colStart += colWidth;
          }
          System.out.println("mouseDown couldn't find a column");
        }
        int colZeroWidth = table.getColumn(0).getWidth() + margin;
        int colOneWidth = table.getColumn(1).getWidth() + margin;
        int colTwoWidth = table.getColumn(2).getWidth() + margin;
        
        int colTwoX = gutter + (margin / 2) + colZeroWidth + colOneWidth;
        
        TableItem item = table.getItem(new Point(margin, e.y));
        if (item == null)
          clickedTrack = null;
        else
          clickedTrack = (Track) hashByTableItem.get(item);
          // If the user clicked on the rating column, then bring up the
          // pop-up menu.
          // NOTE: WIDTH OF RATING ICON IS HARD-CODED HERE!
        System.out.println("mouseDown: " + e.x + "," + e.y +
          "  widths " + colZeroWidth + "," + colOneWidth + "," + colTwoWidth +
          "  colTwoX " + colTwoX);
        if (e.x >= colTwoX && e.x < (colTwoX+colTwoWidth) && e.x < (colTwoX+80)) {
          if (popupMenu != null && clickedTrack != null) {
            menuSelectedTrack = clickedTrack;
              // Select and scroll to the selected track.
            select(selected, true);
              // Put the menu 10 pixels below the mouse position so there is less risk of
              // choosing "THIS SUX" accidentally.
            popupMenu.popUp(menuSelectedTrack, table.toDisplay(e.x, e.y+10));
              // Note: We will now receive a 'widgetSelected' event.  This will call
              // select(selected, false) - this will ensure the correct track is
              // highlighted, because menuSelectedTrack overrides the 'selected' track.
          }
        }
      }
    });

    table.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
          // Prevent the track table from ever having a track other than the
          // currently playing one selected by switching back to the currently
          // playing one whenever a track is selected.
          // (Unless there is a track selected by the pop-up menu.)
        if (menuSelectedTrack == null)
          select(selected, false);
      }
    });
  }

  public void addSelectionListener(SelectionListener listener)
  {
    table.addSelectionListener(listener);
  }
  
  /** Used to create a listener which sorts using the given Comparator.
   * @param column     The table column to add the column listener to.
   * @param comparator The comparator used to sort by the specified column.
   */
  private void addColumnListener(TableColumn column, final TrackComparator comparator) {
    //final Integer colNo = new Integer(columnNumber);
    column.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        if (TrackTable.this.comparator == comparator) {
          comparator.setDirection(!comparator.direction);
        }
        else {
          TrackTable.this.comparator = comparator;
          comparator.setDirection(true);
        }
        updateTable();
      } 
    });
  }
  
  /** Reads the table from the TrackDatabase, sorts it and displays it. */
  public void updateTable() {
    // Get the list of tracks
    Track[] tracks = trackDatabase.getTracks();
    listOfTracks = new Vector(tracks.length);
    for (int i = 0; i < tracks.length; i++) {
      Track track = tracks[i];
      if (!track.isHidden()) 
        listOfTracks.add(track);
    }
    sort();
  }
  
  /** Enables the table widget */
  public void enable() {
    table.setEnabled(true);
  }
  /** Sorts the table and loads it into the Table (displays it). */
  private void sort() {
    System.out.println("TrackTable: Sorting"); //$NON-NLS-1$
    Collections.sort(listOfTracks, comparator);
    load();
  }
  
  /** Loads the table assuming that it is already sorted. */  
  private void load() {
    System.out.println("TrackTable: Resizing"); //$NON-NLS-1$
    // Update the list of tracks
    int size = listOfTracks.size();
    while (table.getItemCount() > size) {
      int index = table.getItemCount() - 1;
      TableItem tableItem = table.getItem(index);
      table.remove(index);
    }
    while (table.getItemCount() < size)
      new TableItem(table, SWT.NONE);
      
    System.out.println("TrackTable: Updating"); //$NON-NLS-1$
    // Update the table    
    int i = 0;
    hashByTrack = new Hashtable();
    hashByTableItem = new Hashtable();
    for (Iterator itr = listOfTracks.iterator(); itr.hasNext(); i++) {
      TableItem tableItem = table.getItem(i);
      Track track = (Track) itr.next();
      updateTableItem(tableItem, track);
      hashByTrack.put(track, tableItem);
      hashByTableItem.put(tableItem, track);
    }
    
    System.out.println("TrackTable: Done"); //$NON-NLS-1$
    // Make sure the correct track is selected
    if (selected != null)
      select(selected, true);
  }
  
  private Image getStateImage(Color background, String state) {
  	Image image = (Image) imageHash.get(state);
  	if (image == null) 
  	{
  	  System.out.println("Creating image " + state);
      ImageData stateImageData = basicSkinable.getImageData(state);
      if (stateImageData != null) {
        ImageData mergedImageData = imageMerger.merge(background, stateImageData);
        image = new Image(display, mergedImageData);
      }
      else {
        /* We need to generate an image for the text */
        GC gc = new GC(table);      
        Point size = gc.stringExtent(state);
        Font font = gc.getFont();

        int x = 80;
        int y = 20;
        if(size.x > x) {
          // Shrink to fit! The downloading messages
          // are too big on Mac OS X at least.
          FontData fd = gc.getFont().getFontData()[0];
          font = new Font(display, fd.getName(), fd.getHeight() * x / size.x, fd.getStyle());
          gc.setFont(font);
          size = gc.stringExtent(state);
        }
        gc.dispose();

        image = new Image(display, x, y);     
        gc = new GC(image);
        gc.setFont(font);
        gc.drawText(state, (x - size.x) / 2, (y - size.y) / 2, true);
        gc.dispose();
      }
      imageHash.put(state, image);
  	}
    return image;
  }
  
  private Image getIconImage(Color background, String icon) {
    if (icon == null || icon.length() == 0)
      return null;
    
    try {
      Image image = (Image) imageHash.get(icon);
      if (image == null) {
        System.out.println("Loading image: " + icon);
        // Get the image
        Image baseImage = new Image(display, BaseResources.getResourceAsStream(icon));
        ImageData imageData = baseImage.getImageData();
        baseImage.dispose();
        
        // Scale the image
        int scaledHeight = 20;
        ImageData scaledImageData = imageData.scaledTo(imageData.width * scaledHeight / imageData.height, scaledHeight);

        // Merge with the background colour
        ImageData mergedImageData = imageMerger.merge(background, scaledImageData);
        image = new Image(display, scaledImageData);
        imageHash.put(icon, image);
      }
      return image;
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  /** Loads the Track into the TableItem. */
  private void updateTableItem(TableItem tableItem, Track track) {
    
    tableItem.setText(new String[] {
      track.getArtist(),
      track.getTitle(),
      "",
      String.valueOf(track.getNoOfTimesPlayed()),
      track.getLastPlayed().toString(),
      ""
    });
    
    Color background = tableItem.getBackground();
//  Can't get it to work out the current background color. COLOR_LIST_SELECTION
//  is correct only when the TrackTable has focus (at least on GTK).
//
//  TableItem[] selection = table.getSelection();
//  if (selection != null)
//    for (int i = 0; i < selection.length; i++) 
//      if (tableItem == selection[i])
//        background = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
  
    tableItem.setImage(2, getStateImage(background, track.getState())); 
    tableItem.setImage(5, getIconImage(background, licenseIndex.get(track).getIcon()));    
  }
  
  /** Remove the specified track from the table. */
  private void removeTrack(Track track, int index) {
    updateTable();
  }
  
  /** Insert the specified track into the table. */
  private void insertTrack(Track track) {
    updateTable();
  }
  
  /** Move a track further down the list. */
  private void moveTrackDown(Track track, int index) {
    sort();
  }
  
  /** Move the track further up the list. */
  private void moveTrackUp(Track track, int index) {
    sort();
  }  
  
  /** Updates a single track. */
  public void updateTrack(Track track) {
    TableItem tableItem = (TableItem) hashByTrack.get(track);
    if (tableItem != null) {
      if (track.isHidden()) {
        // The track has become hidden so we remove it from the table.
        removeTrack(track, table.indexOf(tableItem));
      }
      else {
        updateTableItem(tableItem, track);
      
        // If the tracks are now out of order then we have to sort the list.
        int index = table.indexOf(tableItem);
        if (index != 0) {
          Track prevTrack = (Track) hashByTableItem.get(table.getItem(index - 1));
          if (comparator.compare(prevTrack, track) > 0) {
            moveTrackDown(track, index);
            return;          
          }
        }
        if (index + 1 < table.getItemCount()) {
          Track nextTrack = (Track) hashByTableItem.get(table.getItem(index + 1));
          if (comparator.compare(track, nextTrack) > 0) {
            moveTrackUp(track, index);
          }
        }
      }
    }
    else {
      if (!track.isHidden()) {
        // The track has become unhidden so we add it to the table.
        insertTrack(track);
      }
    }
  }
  
  /** Select a specified Track. */
  public void select(Track track, boolean scrollToIt) {
    this.selected = track;

      // If the pop-up menu is active, then we select that track instead. 
    if (menuSelectedTrack != null)
      track = menuSelectedTrack;

    TableItem tableItem = track != null ? (TableItem) hashByTrack.get(track) : null;
    if (tableItem == null) {
      table.deselectAll();
    }
    else {
        // We would really like the table to select the item without scrolling
        // to it.  Unfortunately SWT won't do this, so instead, I am having it
        // just deselect
      if (scrollToIt) {
        table.setSelection(new TableItem[] { tableItem });
        table.showItem(tableItem);
      }
      else
        table.deselectAll();
    }
  }  

  /**
   * Get the track on which the user has just clicked.  For use during event handlers.
   */
  public Track getClickedTrack()
  {
    return clickedTrack;
  }
 
  /** Get the currently selected track. */
/* THERE IS NO LONGER ANY SUCH THING AS THE SELECTED TRACK.
  public Track getSelectedTrack() {
    final Track[] track = new Track[1];
    final Object[] monitor = new Object[1];
    Runnable r = new Runnable() {
      public void run() {
        try {
          TableItem[] selection = table.getSelection();
          if (selection.length == 1)
            track[0] = (Track) hashByTableItem.get(selection[0]);
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
    return track[0];
  }
  */

  /** A helper class used to make it easy to write clean track comparators
   * without too much casting. */
  private abstract class TrackComparator implements Comparator {
    
    private boolean direction = true;
    
    public void setDirection(boolean direction) {
      this.direction = direction; 
    }
    
    public int compare(Object o0, Object o1) {
      int comp = compareTrack((Track) o0, (Track) o1);
      
      if(direction) {
        if (comp != 0)
          return comp;
        return compareURL((Track) o0, (Track) o1);
      } else {
        if(comp < 0)
          return Math.abs(comp);
        else if (comp > 0) {
          return -1*comp;   
        }
        else {
          return compareURL((Track) o0, (Track) o1);
        } 
      }
    }

    public int compareURL(Track track0, Track track1) {
      return track0.getURL().toString().compareTo(track1.getURL().toString());
    }
    
    private int valueOf(String s) {
       int value = 0;
       for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (!Character.isDigit(c))
         	break;
         value = (value * 10) + (c - '0');
       }
       return value;
    }
    
    public int magicStringCompare(String str0, String str1) {
      // If the strings are not equal and not empty ...
      if (!str0.equals(str1) && str0.length() != 0 && str1.length() != 0)
      {
        // Get the first character of each
        char c0 = str0.charAt(0);
        char c1 = str1.charAt(0);
        // If both characters are digits, we can compare these strings as numbers
        if (Character.isDigit(c0) && Character.isDigit(c1)) {
          int i0 = valueOf(str0);
          int i1 = valueOf(str1);
          if (i0 < i1)
          	return -1;
          if (i0 > i1)
          	return 1;
        }
        
        // However, one of them might be a string.  If we're comparing a string
        // and a number, the number will always be smaller.
        if (Character.isDigit(c0) && !Character.isDigit(c1)) 
        {
            return 1;
        }
        if (!Character.isDigit(c0) && Character.isDigit(c1)) 
        {
            return -1;
        }
      }
      // Finally, if we compare two strings, it's easy.
      return str0.compareToIgnoreCase(str1);
    }
          
    public abstract int compareTrack(Track track0, Track track1);  
  }

  /** Set the a pop-up menu for the table. */
  public void setMenu(TrackTableMenu menu) {
    if (popupMenu != null)
      popupMenu.removeMenuListener(this);
    popupMenu = menu;
    if (popupMenu != null)
      popupMenu.addMenuListener(this);
    //table.setMenu(menu);
  }

  public void menuHidden(MenuEvent e)
  {
      // When the pop-up menu disappears, we switch back to the selected track.
    if (menuSelectedTrack != null) {
      menuSelectedTrack = null;
      select(selected, false);
    }
  }

  public void menuShown(MenuEvent e)
  {
  }

  /**
   * Get a resource string from the properties file associated with this 
   * class.
   */
  private String getResourceString(String key) {
    return Resources.getString(key); 
  }
  
  /**
   * Sort the table according to a given column
   * (simulates column header click for platforms where this is broken in SWT)
   */
  public void setSortColumn(int colnum) {
    TableColumn col = table.getColumn(colnum);
    col.notifyListeners(SWT.Selection, new Event());
    sort();
  }

  /**
   * @return context menu for this table
   */
  public Menu getMenu() {
    return table.getMenu();
  }

}

