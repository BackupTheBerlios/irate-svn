/*
 * Created on Oct 25, 2003
 */
package irate.swt;

import irate.common.Track;
import irate.common.TrackDatabase;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

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
  
  /** The current comparitor used to sort the table. */
  private Comparator comparator;
  
  /** The currently selected track. */
  private Track selected;
  
  /** Constructor to create a table contained in the given Shell where the
   * tracks are updated from the given TrackDatabase. 
   * @param shell         The Shell to add the Table to.
   * @param trackDatabase The database containing the list of tracks. Track
   *                      listings are automatically updated from this
   *                      database.
   */ 
  public TrackTable(Shell shell, TrackDatabase trackDatabase) {
    this.display = shell.getDisplay();
    this.trackDatabase = trackDatabase;
    table = new Table(shell, SWT.NONE);

    TableColumn col = new TableColumn(table, SWT.LEFT);
    col.setWidth(200);
    col.setText("Artist");
    addColumnListener(col, comparator = new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return new MagicString(track0.getArtist()).compareTo(new MagicString(track1.getArtist()));
      }        
    });

    col = new TableColumn(table, SWT.LEFT);
    col.setWidth(200);
    col.setText("Track");
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return new MagicString(track0.getTitle()).compareTo(new MagicString(track1.getTitle()));
      }        
    });

    col = new TableColumn(table, SWT.LEFT);
    col.setWidth(100);
    col.setText("Rating");
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return new MagicString(track0.getState()).compareTo(new MagicString(track1.getState()));
      }        
    });

    col = new TableColumn(table, SWT.LEFT);
    col.setWidth(50);
    col.setText("Plays");
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return new Integer(track0.getNoOfTimesPlayed()).compareTo(new Integer(track1.getNoOfTimesPlayed()));
      }        
    });

    col = new TableColumn(table, SWT.LEFT);
    col.setWidth(180);
    col.setText("Last");
    addColumnListener(col, new TrackComparator() {
      public int compareTrack(Track track0, Track track1) {
        return track0.getLastPlayed().compareTo(track1.getLastPlayed());
      }        
    });
    table.setHeaderVisible(true);

    updateTable();    

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
  private void addColumnListener(TableColumn column, final Comparator comparator) {
    //final Integer colNo = new Integer(columnNumber);
    column.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        if (TrackTable.this.comparator != comparator) {
          TrackTable.this.comparator = comparator;
          updateTable();
        }
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
    load();
  }
  
  /** Sorts the table and loads it into the Table (displays it). */
  private void load() {
    // Sort first
    Collections.sort(listOfTracks, comparator);
    
    System.out.println("TrackTable: Resizing");
    // Update the list of tracks
    int size = listOfTracks.size();
    while (table.getItemCount() > size)
      table.remove(table.getItemCount() - 1);
    while (table.getItemCount() < size)
      new TableItem(table, SWT.NONE);
      
    System.out.println("TrackTable: Updating");
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
    
    System.out.println("TrackTable: Done");
    // Make sure the correct track is selected
    if (selected != null)
      select(selected);
  }
  
  /** Loads the Track into the TableItem. */
  private void updateTableItem(TableItem tableItem, Track track) {
    tableItem.setText(new String[] {
      track.getArtist(),
      track.getTitle(),
      track.getState(),
      String.valueOf(track.getNoOfTimesPlayed()),
      track.getLastPlayed().toString()
    });
  }
  
  /** Updates a single track. */
  public void updateTrack(Track track) {
    TableItem tableItem = (TableItem) hashByTrack.get(track);
    if (tableItem != null) {
      updateTableItem(tableItem, track);
      
      // If the tracks are now out of order then we have to sort the list.
      int index = table.indexOf(tableItem);
      if (index != 0) {
        Track prevTrack = (Track) hashByTableItem.get(table.getItem(index - 1));
        if (comparator.compare(prevTrack, track) > 0) {
          load();
          return;          
        }
      }
      if (index + 1 < table.getItemCount()) {
        Track nextTrack = (Track) hashByTableItem.get(table.getItem(index + 1));
        if (comparator.compare(track, nextTrack) > 0)
          load();
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
    public int compare(Object o0, Object o1) {
      int comp = compareTrack((Track) o0, (Track) o1);
      if (comp != 0)
        return comp;
      return compareURL((Track) o0, (Track) o1);
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

}

