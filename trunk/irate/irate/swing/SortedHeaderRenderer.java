/*
 * Created on Dec 1, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package irate.swing;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * @author Tony
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SortedHeaderRenderer
  implements TableCellRenderer {

  private JLabel renderer = new JLabel();
  private ImageIcon down = new ImageIcon(getClass().getResource("down.gif"));
  private ImageIcon up = new ImageIcon(getClass().getResource("up.gif"));

  public SortedHeaderRenderer(){
    renderer.setBorder(BorderFactory.createEtchedBorder());
    renderer.setHorizontalTextPosition(SwingConstants.LEFT);
  }
  /* (non-Javadoc)
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
    TableSorter ts = (TableSorter) arg0.getModel();
    renderer.setText(arg1.toString());
    renderer.setIcon(null);
    
    int tmp=arg0.convertColumnIndexToModel(arg5);
    if (arg0.getModel().getColumnClass(tmp) == Integer.class){
      renderer.setHorizontalTextPosition(SwingConstants.RIGHT);
      renderer.setHorizontalAlignment(SwingConstants.RIGHT);
    }
    else
    {renderer.setHorizontalTextPosition(SwingConstants.LEFT);
     renderer.setHorizontalAlignment(SwingConstants.LEFT);
    }
    if (ts.sortedColumn==tmp){
      if (ts.ascending) renderer.setIcon(down);
      else renderer.setIcon(up);
    }
      
    return renderer;
  }
}
