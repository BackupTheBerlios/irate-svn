/*
 * Created on Aug 29, 2004
 *
 */
package irate.plugin.marsyas;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import irate.common.Track;
import irate.swt.BaseDialog;

/**
 * @author Taras Glek
 *
 */
public class MarsyasResultDialog extends BaseDialog{
  private Table resultTable = null;
  Collection result;
  private MarsyasPlugin plugin;
  private Button btnPlay, btnDownload;
  /**
   * 
   * @param plugin
   * @param query track that the results are for
   * @param result array of results sorted by their closeness to the query
   */
  public MarsyasResultDialog(MarsyasPlugin plugin, Track query, Collection result) {
    super(Display.findDisplay(Thread.currentThread()), Resources.getString("similar_music")+query.getName());
    this.result = result;
    this.plugin = plugin;
    createUI();
  }

  
  /**
   * 
   */
  private void createUI() {
    Composite mainComposite = getMainComposite();
    createResultTable(mainComposite);
    Shell shell = getShell();
    btnPlay = addButton(Resources.getString("play_track"));
    btnPlay.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent arg0) {
        int i = resultTable.getSelectionIndex();
        if(i < 0)
          return;
        ((MarsyasSimilaritySearch.ServerTrack)resultTable.getItem(i).getData()).play();
      }      
    });
    btnDownload = addButton(Resources.getString("download_track"));
    btnPlay.setEnabled(false);
    btnDownload.setEnabled(false);
    addButton(Resources.getString("close")).addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent arg0) {
        getShell().close();
        dispose();
      }
    });
    shell.pack();
    shell.open();        
  }


  /** Setup the result table 
   * @param mainComposite
   */
  private void createResultTable(Composite parent) {
    resultTable = new Table(parent, SWT.NONE);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.verticalAlignment = GridData.FILL;
    gridData.grabExcessVerticalSpace = true;
    resultTable.setLayoutData(gridData);
    
    TableColumn cols[] = null;
    for (Iterator iter = result.iterator(); iter.hasNext();) {
      MarsyasSimilaritySearch.ServerTrack t=(MarsyasSimilaritySearch.ServerTrack) iter.next();
      //setup the headers
      if(!resultTable.getHeaderVisible()) {
        String s[] = t.getColumns();
        cols = new TableColumn[s.length];
        for (int i = 0; i < s.length; i++) {
          cols[i] = new TableColumn(resultTable, SWT.LEFT);
          cols[i].setText(s[i]);   
        }        
        resultTable.setHeaderVisible(true);
      }
      
      TableItem ti = new TableItem(resultTable, SWT.NONE);
      ti.setText(t.getData());
      ti.setData(t);
    }
    
    for (int i = 0; i < cols.length; i++) {
      cols[i].pack();
    }
    
    resultTable.pack();
    resultTable.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent se) {
        MarsyasSimilaritySearch.ServerTrack st  = (MarsyasSimilaritySearch.ServerTrack)se.item.getData();       
        btnDownload.setEnabled(!st.isLocal());
        btnPlay.setEnabled(st.isLocal());
      }
    });
  }
}
