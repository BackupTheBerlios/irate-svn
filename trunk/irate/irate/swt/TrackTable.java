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
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

/**
 * @author Anthony Jones
 */
public class TrackTable {
  
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
  
  /** A hash keeping track of the image handles by table item. */
  private final Hashtable imageHandleHash = new Hashtable();
  
  /** The current comparitor used to sort the table. */
  private TrackComparator comparator;
  
  /** The currently selected track. */
  private Track selected;

  /** Used to reduce the number of instances of LicensingScheme. */
  private LicenseIndex licenseIndex = new LicenseIndex();

  /** Alpha blends images with the background color. */ 
  private ImageMerger imageMerger = new ImageMerger();

  /** Cache for caching generated images. */
  private Cache imageCache = new Cache("TrackTable");
  
  /** Stores skinable images automagically */
  private BasicSkinable basicSkinable; 
  
  /** Constructor to create a table contained in the given Shell where the
   * tracks are updated from the given TrackDatabase. 
   * @param shell         The Shell to add the Table to.
   * @param trackDatabase The database containing the list of tracks. Track
   *                      listings are automatically updated from this
   *                      database.
   * @param skinManager   The skin manager used to display graphics for the
   *                      table headings. 
   */ 
  public TrackTable(Shell shell, TrackDatabase trackDatabase, SkinManager skinManager) {
    display = shell.getDisplay();
    this.trackDatabase = trackDatabase;
    basicSkinable = new BasicSkinable(this.display);
    table = new Table(shell, SWT.NONE);
    table.setEnabled(false);

    TableColumn col = new TableColumn(table, SWT.LEFT);
    col.setWidth(200);
    addColumnListener(col, comparator = new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
          return new MagicString(track0.getArtist()).compareTo(new MagicString(track1.getArtist()));
      }        
    });
    skinManager.addItem(col, "TrackTable.Heading.Artist"); 

    col = new TableColumn(table, SWT.LEFT);
    col.setWidth(200);
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return new MagicString(track0.getTitle()).compareTo(new MagicString(track1.getTitle()));
      }        
    });
    skinManager.addItem(col, "TrackTable.Heading.Track"); 

    col = new TableColumn(table, SWT.LEFT);
    col.setWidth(120);
    // Setting the alighnment to left doesn't seem to make a difference for images on GTK
    col.setAlignment(SWT.LEFT);
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return new MagicString(track0.getState()).compareTo(new MagicString(track1.getState()));
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
        track = getSelectedTrack();
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
      imageHandleHash.remove(tableItem);
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
      select(selected);
  }
  
  private ImageHandle getStateImage(Color background, String state) {
    ImageHandle imageHandle;
    ImageData stateImageData = basicSkinable.getImageData(state);
    if (stateImageData != null) {
      ImageData mergedImageData = imageMerger.merge(background, stateImageData);
  //    mergedImageData.transparentPixel = mergedImageData.palette.getPixel(background.getRGB());
      imageHandle = (ImageHandle) imageCache.get(mergedImageData);
      if (imageHandle == null) {
        imageHandle = new ImageHandle(new Image(display, mergedImageData));
        imageCache.put(mergedImageData, imageHandle);
      }
    }
    else {
      /* We need to generate an image for the text */
      imageHandle = (ImageHandle) imageCache.get(state);
      if (imageHandle == null) {
        GC gc = new GC(table);      
        Point size = gc.stringExtent(state);
        gc.dispose();
        Image image = new Image(display, size.x, size.y);
        gc = new GC(image);
        gc.drawText(state, 0, 0, true);
        gc.dispose();
        imageCache.put(state, imageHandle = new ImageHandle(image));
      }
    }
    return imageHandle;
  }
  
  private ImageHandle getIconImage(Color background, String icon) {
    ImageHandle imageHandle = null;
    if (icon == null || icon.length() == 0)
      return null;
    ImageData scaledImageData = (ImageData) imageCache.get(icon);
    try {
      if (scaledImageData == null) {
        System.out.println("Loading image: " + icon);
        Image image = new Image(display, BaseResources.getResourceAsStream(icon));
        ImageData imageData = image.getImageData();
        image.dispose();
        int scaledHeight = 20;
        imageCache.put(icon, scaledImageData = imageData.scaledTo(
            imageData.width * scaledHeight / imageData.height, scaledHeight));
      }

      ImageData mergedImageData = imageMerger.merge(background, scaledImageData);
      imageHandle = (ImageHandle) imageCache.get(mergedImageData);
      if (imageHandle == null) {      
        imageHandle = new ImageHandle(new Image(display, scaledImageData));
        imageCache.put(mergedImageData, imageHandle);
      }
      return imageHandle;
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
  
    ImageHandle stateImageHandle = getStateImage(background, track.getState()); 
    tableItem.setImage(2, stateImageHandle.getImage());    
       
    ImageHandle licenseImageHandle = getIconImage(background, licenseIndex.get(track).getIcon());    
    tableItem.setImage(5, licenseImageHandle == null ? null : licenseImageHandle.getImage());
    
    /* Put the image handles into the hash table so the images don't get forgotten. */
    imageHandleHash.put(tableItem, new ImageHandle[] { stateImageHandle, licenseImageHandle });
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

  /** Add a lsitener which is notified when a TableItem is selected. The 
   * listener will need to call #getSelectedTrack() to find out which track
   * was selected. */
  public void addSelectionListener(SelectionListener selectionListener) {
    table.addSelectionListener(selectionListener);
  }
  
  /** Select a specified Track. */
  public void select(Track track) {
    System.out.println("TrackTable.select("+track+")");
    this.selected = track;
    TableItem tableItem = (TableItem) hashByTrack.get(track);
    if (tableItem == null) {
      table.deselectAll();
    }
    else {      
      table.setSelection(new TableItem[] { tableItem });
      table.showItem(tableItem);
    }
  }  
 
  /** Get the currently selected track. */
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
          
    public abstract int compareTrack(Track track0, Track track1);  
  }
  
    
  /** Set the a pop-up menu for the table. */
  public void setMenu(Menu menu) {
    table.setMenu(menu);
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

}

