package irate.swt;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class ThreeModeButton extends AlphaLabel implements Skinable {

  private boolean isPressed = false;

  private boolean isMouseOver = false;

  private boolean isActive = false;

  private boolean isEnabled = true;

  private int width;
  private int height;

  ThreeModeButton(Composite parent, int style) {
    super(parent, style);

    // Add a listener for when the mouse enters a button and exits
    addMouseTrackListener(new MouseTrackListener() {

      public void mouseEnter(MouseEvent e) {
        ThreeModeButton.this.mouseOverButton(e);
      }

      public void mouseExit(MouseEvent e) {
        ThreeModeButton.this.mouseLeaveButton(e);
      }

      public void mouseHover(MouseEvent e) {
      };
    });

    // Add a listener for when a button is clicked with the mouse
    addMouseListener(new MouseListener() {

      public void mouseDoubleClick(MouseEvent arg0) {
      }

      public void mouseDown(MouseEvent arg0) {
        ThreeModeButton.this.activateButton(arg0);
      }

      public void mouseUp(MouseEvent arg0) {
        ThreeModeButton.this.setSelection(false);
      }
    });

  }

  public void setSelection(boolean state) {
    if(isEnabled) {
      isActive = false;
      isPressed = state;
      updateState();
    }
  }

  // Call when a button is clicked on, and the mouse held down.
  // - switch the 'active' flag to true.
  protected void activateButton(MouseEvent arg0) {
    if(isEnabled) {
      isActive = true;
      updateState();
    }
  }

  // If the mouse stops hovering over a button, then switch the mouseOver flag and redraw()
  protected void mouseLeaveButton(MouseEvent e) {
    isMouseOver = false;
    updateState();
  }

  // If the mouse goes over a button, switch the mouseOver flag and redraw
  protected void mouseOverButton(MouseEvent e) {
    isMouseOver = true;
    updateState();
  }

  public void setEnabled(boolean flag) {
    isEnabled = flag;
    updateState();
  }

  // Calculate key depending on its current state.
  private void updateState() {
    // If the user has the mouse button held down over the button, display
    // the appropriate image.
    String key;
    if (isActive) {
      if (isPressed) {
        key = "pressed.active";
      }
      else {
        key = "active";
      }
    }
    else {
      // If the mouse is over the button
      if (isMouseOver) {
        // If the mouse is pressed, display the pressed Hot Image
        if (isPressed) {
          key = "pressed.hot";
        }
        // Otherwise, display the normal hot image
        else {
          key = "hot";
        }
      }
      // If the mouse isn't over the image ...
      else {
        // If the button is pressed, show the pressed image
        if (isPressed) {
          key = "pressed";
        }
        // Otherwise, just show the normal state
        else {
          key = "";
        }
      }
    }
    setKey(key);
  }

  public void setImageUpdate(boolean state) {
    if (state) {
      ImageData normal = getImage("");
      if (normal == null)
        return;
      
      defaultImage("hot", "");
      defaultImage("active", "");
      defaultImage("pressed", "active");
      defaultImage("pressed.hot", "pressed");
      defaultImage("pressed.active", "pressed");
    }
    super.setImageUpdate(state);
  }

/*  
  private Image getTransparentImage(String key) {
    ImageData imageData = (ImageData) imageDataHash.get(key);
    if (imageData == null) {
      if (key.equals("pressed.hot") || key.equals("pressed.active"))
        imageData = (ImageData) imageDataHash.get("pressed");
      if (imageData == null) {
        if (key.equals("pressed"))
          imageData = (ImageData) imageDataHash.get("active");          
        if (imageData == null)
          imageData = (ImageData) imageDataHash.get("");
      }
    }
    Rectangle bounds = getBounds();
    int x = (bounds.width - imageData.width) / 2;
    int y = (bounds.height - imageData.height) / 2;
    ImageData backgroundData = transparencyManager.getBackground(this);
    ImageData mergedImageData = imageMerger.merge(backgroundData, x, y, imageData);
    
    // Look up our ImageData in our cache.
    ImageHandle imageHandle = (ImageHandle) cache.get(mergedImageData);
    if (imageHandle == null) {
      imageHandle = new ImageHandle(new Image(this.getDisplay(), mergedImageData));
      cache.put(mergedImageData, imageHandle);
    }
    
    return imageHandle.getImage();
  }
*/
  
  public boolean isPressed() {
    return isPressed;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void addSelectionListener(final SelectionListener selectionListener) {
    addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent mouseEvent) {
        Event event = new Event();
        event.data = mouseEvent.data;
        event.display = mouseEvent.display;
        event.time = mouseEvent.time;
        event.widget = mouseEvent.widget;
        selectionListener.widgetSelected(new SelectionEvent(event));
      }
    });
  }

}

