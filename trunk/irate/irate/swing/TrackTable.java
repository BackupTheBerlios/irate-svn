package irate.swing;

import irate.common.Track;
import irate.common.TrackDatabase;
import irate.client.PlayListManager;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class TrackTable implements TableModel {

  private final String[] columnName = new String[] { "Artist", "Track", "Rating", "Plays", "Last" };
  private Track[] tracks;
  private Vector listeners;
  private PlayListManager playListManager;
  
  public TrackTable(PlayListManager playListManager) {
    this.playListManager = playListManager;
    listeners = new Vector();
    tracks = playListManager.getPlayList().getTracks();
  }

  public void addTableModelListener(TableModelListener l) {
    listeners.add(l);
  }

  public void removeTableModelListener(TableModelListener l) {
    listeners.remove(l);
  }
  
  public synchronized void notifyListeners() {
    TrackDatabase td = playListManager.getPlayList();
    td.sort();
    tracks = td.getTracks();
    for (int i = 0; i < listeners.size(); i++) 
      ((TableModelListener) listeners.elementAt(i)).tableChanged(new TableModelEvent(this));
  }

  public Class getColumnClass(int columnIndex) {
    return new String().getClass();
  }

  public int getColumnCount() {
    return columnName.length;
  }

  public String getColumnName(int columnIndex) {
    return columnName[columnIndex];
  }

  public int getRowCount() {
    return tracks.length;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    Track track = tracks[rowIndex];
    switch (columnIndex) {
      case 0: return track.getArtist();
      case 1: return track.getTitle();
      case 2: return track.getState();
      case 3: return Integer.toString(track.getNoOfTimesPlayed());
      case 4: return track.getLastPlayed();
    }
    return "?";
  }
  
  public void setValueAt(Object obj, int rowIndex, int columnIndex) {
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  public Track getTrack(int index) {
    return tracks[index];
  }
}
