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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Anthony Jones
 */
public class TrackTable {
  
  private Display display;
  private TrackDatabase trackDatabase;
  private Table table;
  private List listOfTracks;
  private Hashtable hashByTrack;
  private Hashtable hashByTableItem;
  private Comparator comparator;
  private Track selected;
  
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
    col.setWidth(150);
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
    gridData.horizontalSpan = 2;
    table.setLayoutData(gridData);
    table.pack();
  }
  
  private void addColumnListener(TableColumn col, final Comparator c) {
    //final Integer colNo = new Integer(columnNumber);
    col.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        sortBy(c);
      } 
    });
  }
  
  public void sortBy(Comparator comparator) {
    if (this.comparator != comparator) {
      this.comparator = comparator;
      updateTable();
    }
  }

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
  
  private void load() {
    // Sort first
    Collections.sort(listOfTracks, comparator);
    
    // Update the list of tracks
    int size = listOfTracks.size();
    while (table.getItemCount() > size)
      table.remove(table.getItemCount() - 1);
    while (table.getItemCount() < size)
      new TableItem(table, SWT.NONE);
      
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
    
    // Make sure the correct track is selected
    if (selected != null)
      select(selected);
  }
  
  public void updateTableItem(TableItem tableItem, Track track) {
    tableItem.setText(new String[] {
      track.getArtist(),
      track.getTitle(),
      track.getState(),
      String.valueOf(track.getNoOfTimesPlayed()),
      track.getLastPlayed().toString()
    });
  }
  
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

  public void addSelectionListener(SelectionListener selectionListener) {
    table.addSelectionListener(selectionListener);
  }
  
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

  private abstract class TrackComparator implements Comparator {
    public int compare(Object o0, Object o1) {
      return compareTrack((Track) o0, (Track) o1);
    }
  
    public abstract int compareTrack(Track track0, Track track1);  
  }

}

