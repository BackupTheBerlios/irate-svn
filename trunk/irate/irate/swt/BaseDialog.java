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
public class BaseDialog extends Shell {
  public Composite mainComposite;

  public BaseDialog(Display display, String title) {
    super(display);
    setLayout(new GridLayout(1, false));
    setText(title);
    setImage(display);
    mainComposite = new Composite(this, SWT.NONE);
    GridData data;
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    mainComposite.setLayoutData(data);
    buttonsComposite = new Composite(this, SWT.NONE);
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

  private Composite buttonsComposite;

  private void setImage(Display display) {
    try {
      InputStream stream = BaseResources.getResourceAsStream("icon.gif");
      ImageData imageData = new ImageData(stream); 
      setImage(new Image(display, imageData));
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
