package irate.swt;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class ThreeModeButton extends Canvas implements Skinable {

  private TransparencyManager transparencyManager;

  private ImageMerger imageMerger;

  private Hashtable imageDataHash = new Hashtable();

  private String normalText, pressedText;

  private Image textBackground;

  private boolean isPressed = false;

  private boolean isMouseOver = false;

  private boolean isActive = false;

  private boolean isEnabled = true;

  private GC gc = null;

  private int width;
  private int height;

  ThreeModeButton(Composite parent, int width, int height, int style) {
    super(parent, style);
    this.width = width;
    this.height = height;

    addDisposeListener(new DisposeListener() {

      public void widgetDisposed(DisposeEvent e) {
        ThreeModeButton.this.widgetDisposed(e);
      }
    });

    addPaintListener(new PaintListener() {

      public void paintControl(PaintEvent e) {
        ThreeModeButton.this.paintControl(e);
      }
    });

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
        ThreeModeButton.this.pressButton(arg0);
      }
    });

  }

  // Call when the button is pressed -- i.e when a user has pressed down and
  // released the mouse button.
  // - switch the 'active' flag to false (long longer held down)
  // - switch the 'pressed' flag to whatever it wasn't
  public void pressButton(MouseEvent arg0) {
    isActive = false;
    isPressed = !isPressed;
    redraw();
  }

  // Call when a button is clicked on, and the mouse held down.
  // - switch the 'active' flag to true.
  protected void activateButton(MouseEvent arg0) {
    isActive = true;
    redraw();
  }

  // If the mouse stops hovering over a button, then switch the mouseOver flag and redraw()
  protected void mouseLeaveButton(MouseEvent e) {
    isMouseOver = false;
    redraw();
  }

  // If the mouse goes over a button, switch the mouseOver flag and redraw
  protected void mouseOverButton(MouseEvent e) {
    isMouseOver = true;
    redraw();
  }

  public void setEnabled(boolean flag) {
    isEnabled = flag;
    redraw();
  }

  // Be sure to dispose of all the images when the buttons are disposed.
  public void widgetDisposed(DisposeEvent e) {
    if (gc != null) gc.dispose();
  }

  private int getStringLength(String text) {
    int len = 0;
    for (int i = 0; i < text.length(); i++) {
      len += gc.getAdvanceWidth(text.charAt(i));
    }
    return len;
  }

  // Paint the button depending on its current state.
  void paintControl(PaintEvent e) {
    gc = e.gc;

    // Text Mode!

    if (imageDataHash.get("") == null) {
      int textHeight = gc.getFontMetrics().getHeight();

      // If we have some kind of background, we need to deal with printing the
      // text button on top with some kind of transparency.  Which is what
      // this code does.
      if (textBackground != null) {
        Image tempImage = new Image(this.getDisplay(), textBackground
            .getImageData());
        GC tempGC = new GC(tempImage);

        if (isEnabled) {
          if (isMouseOver) {
            tempGC.drawRectangle(0, 0, textBackground.getImageData().width - 2,
                textBackground.getImageData().height - 2);
          }
        }
        else {
          tempGC.setForeground(this.getDisplay().getSystemColor(
              SWT.COLOR_DARK_GRAY));
        }

        if (isPressed) {
          int length = getStringLength(pressedText);
          int x = (textBackground.getImageData().width - length) / 2;
          int y = (textBackground.getImageData().height - textHeight) / 2;
          tempGC.drawText(pressedText, x, y, true);
        }
        else {
          int length = getStringLength(normalText);
          int x = (textBackground.getImageData().width - length) / 2;
          int y = (textBackground.getImageData().height - textHeight) / 2;
          tempGC.drawText(normalText, x, y, true);
        }
        gc.drawImage(tempImage, 0, 0);
        tempGC.dispose();
        tempImage.dispose();
      }
      // If we have no background, then we can just draw right onto the GC, but
      // we need to cut out anything that was previously there.
      else {

        if (isEnabled) {
          if (isMouseOver) {
            gc.drawRectangle(0, 0, this.getBounds().width - 2,
                this.getBounds().height - 2);
          }
        }
        else {
          gc.setForeground(this.getDisplay()
              .getSystemColor(SWT.COLOR_DARK_GRAY));
        }

        if (isPressed) {
          int length = getStringLength(pressedText);

          int x = (this.getBounds().width - length) / 2;
          int y = (this.getBounds().height - textHeight) / 2;
          gc.drawText(pressedText, x, y, true);
        }
        else {
          int length = getStringLength(normalText);

          int x = (this.getBounds().width - length) / 2;
          int y = (this.getBounds().height - textHeight) / 2;
          gc.drawText(normalText, x, y, true);
        }
      }
    }
    else {
      // Graphic mode!

      // If the user has the mouse button held down over the button, display
      // the appropriate image.
      String key;
      if (isActive) {
        if (isPressed) {
          key = "activePressed";
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
            key = "hotPressed";
            if(!imageDataHash.containsKey(key)) {
                key = "pressed";
            }
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
      try {
        Image image = createTransparentImage(key);
        gc.drawImage(image, 0, 0);
        image.dispose();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  // Base the size of the button on the normal image.  All images should be the
  // same size -- if this isn't the case, it will look pretty bad.
  public Point computeSize(int wHint, int hHint, boolean changed) {
    ImageData imageData = (ImageData) imageDataHash.get("");

    if (imageData != null) {
      width = imageData.width;
      height = imageData.height;
    }
    return new Point(width, height);
  }

  private Image createTransparentImage(String key) {
    ImageData imageData = (ImageData) imageDataHash.get(key);
    if (imageData == null) imageData = (ImageData) imageDataHash.get("");
    ImageData mergedImage = imageMerger.merge(0, 0, imageData);
    return new Image(this.getDisplay(), mergedImage);
  }

  public boolean isPressed() {
    return isPressed;
  }

  public void setTransparencyManager(TransparencyManager tm) {
    this.transparencyManager = tm;
    this.imageMerger = new ImageMerger(this.transparencyManager, this);
  }

  public void setText(String text) {
    if (transparencyManager != null) {
      ImageData imgData = transparencyManager.getBackground(this);

      if (imgData != null) {
        textBackground = new Image(this.getDisplay(), imgData);
      }
    }
    normalText = text;
    computeSize(0, 0, false);
  }

  public void setPressedText(String text) {
    if (transparencyManager != null) {
      ImageData imgData = transparencyManager.getBackground(this);

      if (imgData != null) {
        textBackground = new Image(this.getDisplay(), imgData);
      }
    }
    pressedText = text;
    computeSize(0, 0, false);
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  /* (non-Javadoc)
   * @see irate.swt.Skinable#setImage(java.lang.String, org.eclipse.swt.graphics.ImageData)
   */
  public void setImage(String key, ImageData imageData) {
    imageDataHash.put(key, imageData);
    computeSize(0, 0, false);
  }

}

