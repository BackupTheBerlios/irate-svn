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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import irate.common.Track;
import irate.plugin.PluginApplication;
import irate.swt.BaseDialog;

/**
 * @author Taras Glek
 *
 */
public class MarsyasResultDialog extends BaseDialog{
  private Table resultTable = null;
  Collection result;
  private MarsyasPlugin plugin;
  
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
    Composite mainComposite = getMainComposite();
    createResultTable(mainComposite);
    Shell shell = super.getShell();
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
    
    TableColumn col = new TableColumn(resultTable, SWT.LEFT);
    col.setWidth(200);
    col.setText(Resources.getString("track_name"));
    col = new TableColumn(resultTable, SWT.LEFT);
    col.setWidth(200);
    col.setText(Resources.getString("track_distance"));
    
    resultTable.setHeaderVisible(true);
    
    for (Iterator iter = result.iterator(); iter.hasNext();) {
      MarsyasSimilaritySearch.ComparableTrack ct = (MarsyasSimilaritySearch.ComparableTrack) iter.next();
      TableItem ti = new TableItem(resultTable, SWT.NONE);
      ti.setText(new String[]{ct.track.toString(), ct.distance.toString()});
      ti.setData(ct.track);
    }
    
    resultTable.pack();
    resultTable.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent se) {
        plugin.playTrack((Track)se.item.getData());       
      }
    });
  }
}
