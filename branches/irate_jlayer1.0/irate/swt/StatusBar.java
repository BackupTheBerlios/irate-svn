package irate.swt;
/**
 * <p>This handles the SWT-specific parts of dealing with writing to
 * the status bar. All the hard work is done in
 * <code>AbstractStatus</code>.</p>
 *
 * @author Creator: Robin Sheat
 *
 * $Id: StatusBar.java,v 1.1 2004/08/28 07:31:33 eythian Exp $
 */
import irate.client.AbstractStatus;
import org.eclipse.swt.widgets.Label;

public class StatusBar extends AbstractStatus {

  /**
   * The status bar label
   */
  Label label;

  /**
   * Constructor that accepts a label that is the status bar.
   *
   * @param l the swt widget that gets updated when the status changes
   */
  public StatusBar(Label l) {
    label = l;
  }

  /**
   * Sets the status bar text.
   *
   * @param s the text to display
   */
  protected void updateStatus(String s) {
    label.setText(s);
  }

}
