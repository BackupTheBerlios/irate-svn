package irate.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Point;

/**
 * A container and manager for the pop-up menu that belongs to the track table.
 *
 * @author Stephen Blackheath
 */
public class TrackTableMenu
{
  private Menu menu;
  private PluginApplication app;
  private Track selectedTrack;

  /**
   * @param app Gives the menu the ability to rate the track.
   */
  public TrackTableMenu(final Shell shell, SkinManager skinManager, final Client app, RatingFunction[] ratingFunctions)
  {
    this.app = app;
    menu = new Menu(shell, SWT.POP_UP);
    /*
    MenuItem info = new MenuItem(menu, SWT.NONE);
    info.addArmListener(new ToolTipArmListener(app, Resources.getString("button.info.tooltip")));
    info.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          if (selectedTrack != null) {
            TrackInfoDialog trackInfoDialog = 
              new TrackInfoDialog(shell.getDisplay(), shell);
            trackInfoDialog.displayTrackInfo(track, client);
          }
        }
      });
    skinManager.addItem(info, "button.info");
    */

    for (int i = 0; i < ratingFunctions.length; i++) {
      RatingFunction rf = ratingFunctions[i];
      MenuItem item = new MenuItem(menu, SWT.NONE);
      item.addArmListener(new ToolTipArmListener(app, Resources.getString(rf.getName() + ".tooltip")));
      final int value = rf.getValue();
      item.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          if (selectedTrack != null)
            app.setRating(selectedTrack, value);
        }
      });
      skinManager.addItem(item, rf.getName());
    }
  }
  
  public void addMenuListener(MenuListener listener)
  {
    menu.addMenuListener(listener);
  }
  
  public void removeMenuListener(MenuListener listener)
  {
    menu.removeMenuListener(listener);
  }

  public void popUp(Track track, Point point)
  {
    selectedTrack = track;
    menu.setVisible(false);
    menu.setLocation(point.x, point.y);
    menu.setVisible(true);
  }
}

