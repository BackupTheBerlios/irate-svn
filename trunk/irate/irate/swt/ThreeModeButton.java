package irate.swt;

import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;


public class ThreeModeButton extends Canvas {

    Image normalImage, hotImage, pressedImage, pressedHotImage;
    boolean isPressed = false;
    boolean isMouseOver = false;
    GC gc = null;

    /**
     * @param parent
     * @param normal The image showing the normal look of the button, unpressed, not hot -- the size will be based on this
     * @param pressed The image showing the button when it is pressed
     * @param hot The image showing the button when it is unpressed but the mouse is over it
     * @param pressedHot The image showing the button when it is pressed and the mouse is over it 
     * @param style
     */
    ThreeModeButton(Composite parent, Image normal, Image pressed, Image hot, Image pressedHot, int style) {
      super(parent, style);
      
      normalImage = normal;
      pressedImage = pressed;
      hotImage = hot;
      pressedHotImage = pressedHot;
      
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
         public void mouseHover(MouseEvent e) {};
      });
      
      // Add a listener for when a button is clicked with the mouse
      addMouseListener(new MouseListener() {
        public void mouseDoubleClick(MouseEvent arg0) {}

        public void mouseDown(MouseEvent arg0) {
            ThreeModeButton.this.pressButton(arg0);
        }
        public void mouseUp(MouseEvent arg0) {}    
      });
      
    }
    
    // If a button is pressed, switch the pressed flag and redraw
    protected void pressButton(MouseEvent arg0) {
      isPressed = !isPressed;
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

    // Be sure to dispose of all the images when the buttons are disposed.
    public void widgetDisposed(DisposeEvent e) {
      if(normalImage != null) 
        normalImage.dispose();
      if(pressedImage != null)
        pressedImage.dispose();
      if(hotImage != null)
        hotImage.dispose();
      if(pressedHotImage != null)
        pressedHotImage.dispose();
      if(gc != null) 
         gc.dispose();
    }
    
    // Paint the button depending on its current state.
    void paintControl(PaintEvent e) {
      gc = e.gc;
      int x = 1;
     
      if(hotImage == null) { hotImage = normalImage; }
      if(pressedHotImage == null) { pressedHotImage = pressedImage; }

      // If the mouse is over the button
      if(isMouseOver) {
        // If the mouse is pressed, display the pressed Hot Image
        if(isPressed) {
          gc.drawImage(pressedHotImage, 1, 1);
        }
        // Otherwise, display the normal hot image
        else {
          gc.drawImage(hotImage, 1, 1);
        }
      }
      // If the mouse isn't over the image ...
      else {
        // If the button is pressed, show the pressed image
        if(isPressed) {
          gc.drawImage(pressedImage, 1, 1);
        }
        // Otherwise, just show the normal state
        else {
          gc.drawImage(normalImage, 1, 1);
        }
      }    
    }
    
    
    // Base the size of the button on the normal image.  All images should be the
    // same size -- if this isn't the case, it will look pretty bad.
    public Point computeSize(int wHint, int hHint, boolean changed) {
      int width = 0, height = 0;
      if (normalImage != null) {
        Rectangle bounds = normalImage.getBounds();
        width = bounds.width;
        height = bounds.height;
      }
      return new Point(width, height+1);    
    }
    
    public Image getHotImage() {
      return hotImage;
    }

    public void setHotImage(Image hotImage) {
      this.hotImage = hotImage;
      redraw();
    }

    public Image getNormalImage() {
      return normalImage;
    }

    public void setNormalImage(Image normalImage) {
      this.normalImage = normalImage;
      redraw();
    }

    public Image getPressedImage() {
      return pressedImage;
    }

    public void setPressedImage(Image pressedImage) {
      this.pressedImage = pressedImage;
      redraw();
    }

    public boolean isPressed() {
      return isPressed;
    }


    public void setPressed(boolean isPressed) {
      this.isPressed = isPressed;
      redraw();
    }

    public Image getPressedHotImage() {
      return pressedHotImage;
    }

    public void setPressedHotImage(Image pressedHotImage) {
      this.pressedHotImage = pressedHotImage;
      redraw();
    }

  }
 

  

