package irate.swt;

import irate.resources.BaseResources;
import java.io.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/** Base class for dialogs.
    Sets the icon to the iRATE icon and creates a main composite
    to be filled by the user and a button composite where buttons
    can be added with the addButton function.
*/
public class BaseDialog {

  public BaseDialog(Display display, String title) {
    shell = new Shell(display);
    shell.setLayout(new GridLayout(1, false));
    shell.setText(title);
    setImage(display);
    mainComposite = new Composite(shell, SWT.NONE);
    GridData data;
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    mainComposite.setLayoutData(data);
    buttonsComposite = new Composite(shell, SWT.NONE);
    buttonsComposite.setLayout(new FillLayout());
    data = new GridData();
    data.horizontalAlignment = GridData.END;
    buttonsComposite.setLayoutData(data);
  }

  public Button addButton(String text) {
    Button button = new Button(buttonsComposite, SWT.NONE);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    button.setLayoutData(data);
    button.setText(text);
    return button;
  }

  /** Center this shell on a parent shell or on display if parent == null. */
  public void centerOn(Shell parent) {
    Rectangle displaySize = shell.getDisplay().getClientArea();
    Point size = shell.getSize();
    int x, y;
    if (parent == null) {
      x = displaySize.x + (displaySize.width - size.x) / 2;
      y = displaySize.y + (displaySize.height - size.y) / 2;
    }
    else {
      Point parentLocation = parent.getLocation();
      Point parentSize = parent.getSize();
      x = parentLocation.x + (parentSize.x - size.x) / 2;
      y = parentLocation.y + (parentSize.y - size.y) / 2;    
    }
    int xMax = displaySize.x + displaySize.width - size.x;
    if (x < displaySize.x)
      x = displaySize.x;
    else if (x > xMax)
      x = xMax;
    int yMax = displaySize.y + displaySize.height - size.y;
    if (y < displaySize.y)
      y = displaySize.y;
    else if (y > yMax)
      y = yMax;
    shell.setLocation(x, y);
  }

  public void dispose() {
    shell.dispose();
    shell = null;
    if (image != null) {
      image.dispose();
      image = null;
    }
  }

  public Shell getShell() {
    return shell;
  }

  public Composite getMainComposite() {
    return mainComposite;
  }

  private Composite buttonsComposite;

  private Composite mainComposite;

  private Image image;

  private Shell shell;

  private void setImage(Display display) {
    try {
      InputStream stream = BaseResources.getResourceAsStream("icon.gif");
      ImageData imageData = new ImageData(stream); 
      image = new Image(display, imageData);
      shell.setImage(image);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

// Local Variables:
// c-file-style:"gnu"
// c-basic-offset:2
// indent-tabs-mode:nil
// tab-width:4
// End:
