/*
 * Created on Apr 24, 2004
 */
package irate.swt;

import java.util.Hashtable;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Anthony Jones
 */
public class AlphaLabel extends Canvas implements Skinable {

  private TransparencyManager transparencyManager;

  private Image image;

  private Hashtable imageHash = new Hashtable();
  
  private Hashtable textHash = new Hashtable();
  
  private Hashtable toolTipHash = new Hashtable();

  private ImageMerger imageMerger;
  
  private boolean imageUpdate;
  
  private Label label;
  
  private String key = "";
  
  private Object lock = new Object();
  
  public AlphaLabel(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());
    label = new Label(this, style);
    addControlListener(new ControlListener() {
      public void controlMoved(ControlEvent e) {
        if (isVisible())
          updateImage();
      }
      public void controlResized(ControlEvent e) {
        if (isVisible())
          updateImage();
      }
    });
    
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        synchronized (lock) {
          label.setImage(null);
          if (image != null)
            image.dispose();
          image = null;
        }
      }
    });
  }

  public void setTransparencyManager(TransparencyManager transparencyManager) {
    this.transparencyManager = transparencyManager;
    imageMerger = new ImageMerger();
  }
  
  public void setImageUpdate(boolean state) {
    imageUpdate = state;
    updateImage();
  }

  public void updateImage() {
    if (!imageUpdate)
      return;
    
    Image newImage;

    ImageData backgroundData = transparencyManager.getBackground(label);
    ImageData imageData = (ImageData) imageHash.get(key);
    if (imageData == null) {
      String text = (String) textHash.get(key);
      if (text == null)
        return;
      
      /* Get the background image. */
      GC gc;
      if (backgroundData == null)
      {
        Rectangle bounds = getBounds();
        newImage = new Image(label.getDisplay(), bounds.width, bounds.height);
        gc = new GC(newImage);
        gc.setBackground(getBackground());
        gc.fillRectangle(0, 0, bounds.width, bounds.height);
      }
      else {
        newImage = new Image(label.getDisplay(), backgroundData);
        gc = new GC(newImage);
      }
      
      /* Draw the text onto it. */
      gc.drawText(text, 0, 0, true);
      gc.dispose();
      System.out.println("Text: " + text);
    }
    else {
      ImageData mergedImageData;
      if (backgroundData == null) {
        Rectangle bounds = getBounds();
        int x = (bounds.width - imageData.width) / 2;
        int y = (bounds.height - imageData.height) / 2;
        mergedImageData = imageMerger.merge(getBackground(), imageData); 
      }
      else {
        int x = (backgroundData.width - imageData.width) / 2;
        int y = (backgroundData.height - imageData.height) / 2;
        mergedImageData = imageMerger.merge(backgroundData, x, y, imageData);
      }
      newImage = new Image(label.getDisplay(), mergedImageData);
    }
    
    synchronized (lock) {
      label.setImage(newImage);
      if (image != null)
        image.dispose();
      image = newImage;
    }
  }

  public Point computeSize(int wHint, int hHint, boolean changed) {
    Point size = new Point(0, 0);
    ImageData imageData = (ImageData) imageHash.get(key);
    if (imageData == null) {
      String text = (String) textHash.get(key);
      if (text != null) {
        GC gc = new GC(this);
        size = gc.stringExtent(text);
        gc.dispose();
      }
    }
    else {
      size.x = imageData.width;
      size.y = imageData.height;
    }

    if (wHint > size.x) size.x = wHint;

    if (hHint > size.y) size.y = hHint;

    return size;
  }
  
  public void setKey(String key) {
    if (!key.equals(this.key)) {
      this.key = key;
      System.out.println("Key = " + key);
      updateImage();
      String toolTip = (String) toolTipHash.get(key);
      label.setToolTipText(toolTip == null ? "" : toolTip);
    }
  }
        
  public void setImage(String key, ImageData imageData) {
    imageHash.put(key, imageData);
    if (key.equals(this.key)) updateImage();
  }

  public void setText(String key, String text) {
    textHash.put(key, text);
    if (key.equals(this.key)) updateImage();
  }

  public void setToolTipText(String key, String text) {
    toolTipHash.put(key, text);
    if (key.equals(this.key)) label.setToolTipText(text);
  }

  public void setText(String text) {
    setText("", text);
  }
  
  public void addMouseListener(MouseListener listener) {
    label.addMouseListener(listener);
  }
  
  public void addMouseMoveListener(MouseMoveListener listener) {
    label.addMouseMoveListener(listener);
  }

  public void addMouseTrackListener(MouseTrackListener listener) {
    label.addMouseTrackListener(listener);
  }
}
