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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

/**
 * @author Anthony Jones
 */
public class TrackTable extends BasicSkinable {
  
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
  
  private LicenseIndex licenseIndex = new LicenseIndex();
  
  private Cache imageCache = new Cache();
  
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
    super(shell.getDisplay());
    this.display = shell.getDisplay();
    this.trackDatabase = trackDatabase;
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
    col.setWidth(100);
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return new MagicString(track0.getState()).compareTo(new MagicString(track1.getState()));
      }        
    });
    skinManager.addItem(col, "TrackTable.Heading.Rating");

    col = new TableColumn(table, SWT.LEFT);
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
    col.setWidth(55);
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
          return licenseIndex.get(track0).compareTo(licenseIndex.get(track1));
      }        
    });
    skinManager.addItem(col, "TrackTable.Heading.License");
    skinManager.add(this, "TrackTable");
    
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.verticalAlignment = GridData.FILL;
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalSpan = 3;
    table.setLayoutData(gridData);
    table.pack();
    
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
    while (table.getItemCount() > size)
      table.remove(table.getItemCount() - 1);
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
  
  /** Loads the Track into the TableItem. */
  private void updateTableItem(TableItem tableItem, Track track) {
    
    String state = track.getState();
    //System.out.println("WOOT: " + state);
    ImageData stateImageData = getImageData(state);
    if (stateImageData != null) {
       state = "";
       Image stateImage = (Image) imageCache.get(state);
       if (stateImage == null)
         imageCache.put(stateImageData, stateImage = new Image(display, stateImageData));
       tableItem.setImage(2, stateImage);
    }
    else {
      tableItem.setImage(2, null);
    }
    
    tableItem.setText(new String[] {
      track.getArtist(),
      track.getTitle(),
      state,
      String.valueOf(track.getNoOfTimesPlayed()),
      track.getLastPlayed().toString(),
      ""
    });
       
    /*
     * Get the license image.
     */
    String icon = licenseIndex.get(track).getIcon();
    ImageHandle imageHandle = (ImageHandle) imageCache.get(icon);
    if (imageHandle == null && icon != null && icon.length() != 0) {
      try {
        System.out.println("Loading image: " + icon);
        Image image = new Image(display, BaseResources.getResourceAsStream(icon));
        ImageData imageData = image.getImageData();
        image.dispose();
        int scaledHeight = 20;
        image = new Image(display, imageData.scaledTo(
            imageData.width * scaledHeight / imageData.height, scaledHeight));
        imageHandle = new ImageHandle(image);
        imageCache.put(icon, imageHandle);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (imageHandle != null)
      tableItem.setImage(5, imageHandle.getImage());
    else
      tableItem.setImage(5, null);
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

}

