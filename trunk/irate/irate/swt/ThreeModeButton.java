package irate.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;


public class ThreeModeButton extends Canvas implements ISkinableButton {

    TransparencyManager transparencyManager;
    Image normalImage, hotImage, pressedImage, pressedHotImage, activeNormalImage, activePressedImage;
    String normalText, pressedText;
    Image textBackground;
    boolean isPressed = false;
    boolean isMouseOver = false;
    boolean isActive = false;
    GC gc = null;
    int width, height;

    
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
         public void mouseHover(MouseEvent e) {};
      });
      
      // Add a listener for when a button is clicked with the mouse
      addMouseListener(new MouseListener() {
        public void mouseDoubleClick(MouseEvent arg0) {}

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
    protected void pressButton(MouseEvent arg0) {
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
    
    private int getStringLength(String text) {
      int len = 0;
      for(int i=0; i<text.length(); i++) {
       len += gc.getAdvanceWidth(text.charAt(i));
     }
      return len;
    }
    
    // Paint the button depending on its current state.
    void paintControl(PaintEvent e) {
      gc = e.gc;    
      
      // Text Mode!
      if(normalImage == null) {
        int textHeight = gc.getFontMetrics().getHeight();
        
        // If we have some kind of background, we need to deal with printing the
        // text button on top with some kind of transparency.  Which is what
        // this code does.
        if(textBackground != null) { 
            Image tempImage = new Image(this.getDisplay(), textBackground.getImageData());
            GC tempGC = new GC(tempImage);
            
            if(isMouseOver) {
              tempGC.drawRectangle(0,0,textBackground.getImageData().width-2,textBackground.getImageData().height-2);
            }
            
            if(isPressed) {
              int length = getStringLength(pressedText);
              int x = (textBackground.getImageData().width - length) / 2;
              int y = (textBackground.getImageData().height - textHeight) / 2;
              tempGC.drawText(pressedText,x,y,true);
            }
            else {
              int length = getStringLength(normalText);
              int x = (textBackground.getImageData().width - length) / 2;
              int y = (textBackground.getImageData().height - textHeight) / 2;
              tempGC.drawText(normalText,x,y,true);
            }
            gc.drawImage(tempImage,0,0);
            tempGC.dispose();
            tempImage.dispose();
        }
        // If we have no background, then we can just draw right onto the GC, but
        // we need to cut out anything that was previously there.
        else {
          
          if(isMouseOver) {
            gc.drawRectangle(0,0,this.getBounds().width-2,this.getBounds().height-2);   
         }
          
          if(isPressed) {
            int length = getStringLength(pressedText);
           
            int x = (this.getBounds().width  - length) / 2;
            int y = (this.getBounds().height - textHeight) / 2;
            gc.drawText(pressedText,x,y,true);
          }
          else {
            int length = getStringLength(normalText);
            
            int x = (this.getBounds().width - length) / 2;
            int y = (this.getBounds().height - textHeight) / 2;
            gc.drawText(normalText,x,y,true);
          }
        }
      }
      else {

      // Graphic mode!
        
      // If graphic types havn't been set, then we shoudln't blow up, 
      // just do the best we can.
      if(hotImage == null) { hotImage = normalImage; }
      if(pressedHotImage == null) { pressedHotImage = pressedImage; }
      if(activeNormalImage == null) { activeNormalImage = normalImage; }
      if(activePressedImage == null) { activePressedImage = pressedImage; }
      
      // If the user has the mouse button held down over the button, display
      // the appropriate image.
      if(isActive) {
        if(isPressed) {
          gc.drawImage(activePressedImage, 0, 0);
        }
        else {
          gc.drawImage(activeNormalImage, 0, 0);
        }
      }
      else {
        // If the mouse is over the button
        if(isMouseOver) {
          // If the mouse is pressed, display the pressed Hot Image
          if(isPressed) {
            gc.drawImage(pressedHotImage, 0, 0);
          }
          // Otherwise, display the normal hot image
          else {
            gc.drawImage(hotImage, 0, 0);
          }
      }
      // If the mouse isn't over the image ...
      else {
        // If the button is pressed, show the pressed image
        if(isPressed) {
          gc.drawImage(pressedImage, 0, 0);
        }
        // Otherwise, just show the normal state
        else {
          gc.drawImage(normalImage, 0, 0);
        }
      }    
     }
      }
    }
    
   
    // Base the size of the button on the normal image.  All images should be the
    // same size -- if this isn't the case, it will look pretty bad.
    public Point computeSize(int wHint, int hHint, boolean changed) {

      if (normalImage != null) {
        Rectangle bounds = normalImage.getBounds();
        width = bounds.width;
        height = bounds.height;
      }
      return new Point(width, height);    
    }
    
    private Image createTransparentImage(ImageData image) {
      
      ImageData imgData = transparencyManager.getBackground(this);
      
      // Ok.  Go through the image and find every white pixel.  Replace the
      // white pixel with the corresponding pixel from the background image.
      for (int i = 0; i < image.height; i++) { 
        for(int j = 0; j < image.width; j++) {
          if(image.getPixel(j,i) == 16777215) {
            image.setPixel(j,i, imgData.getPixel(j,i));
          }
        }
      }
      
      return new Image(this.getDisplay(), image);
      
    }
    
    

    public boolean isPressed() {
      return isPressed;
    }
  
    public void setTransparencyManager(TransparencyManager tm) {
      this.transparencyManager = tm;
    }


    public void setNormalImage(ImageData image) {
      this.setBounds(this.getLocation().x, this.getLocation().y, image.width, image.height);
      this.normalImage = createTransparentImage(image);
      this.redraw();
    }

    public void setPressedImage(ImageData image) {
      this.pressedImage = createTransparentImage(image);
      this.redraw();
    }

    public void setNormalHotImage(ImageData image) {
      // TODO Auto-generated method stub
      
    }

    /* (non-Javadoc)
     * @see irate.swt.ISkinableButton#setPressedHotImage(org.eclipse.swt.graphics.ImageData)
     */
    public void setPressedHotImage(ImageData image) {
      // TODO Auto-generated method stub
      
    }

    /* (non-Javadoc)
     * @see irate.swt.ISkinableButton#setActiveNormalImage(org.eclipse.swt.graphics.ImageData)
     */
    public void setActiveNormalImage(ImageData image) {
      // TODO Auto-generated method stub
      
    }

    /* (non-Javadoc)
     * @see irate.swt.ISkinableButton#setActivePressedImage(org.eclipse.swt.graphics.ImageData)
     */
    public void setActivePressedImage(ImageData image) {
      // TODO Auto-generated method stub
      
    }

    public void setNormalText(String text) {
      if(transparencyManager != null) {
        ImageData imgData = transparencyManager.getBackground(this);
        
        if(imgData != null) {
          textBackground = new Image(this.getDisplay(), imgData);
        }
      }
      normalText = text;
      this.redraw();
    }

    public void setPressedText(String text) {
      if(transparencyManager != null) {
        ImageData imgData = transparencyManager.getBackground(this);
        
        if(imgData != null) {
          textBackground = new Image(this.getDisplay(), imgData);
        }
      }
      pressedText = text;
      this.redraw();
    }

  }
 

  

