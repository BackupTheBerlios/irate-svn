/*
 * Created on Apr 24, 2004
 */
package irate.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Anthony Jones
 */
public class AlphaLabel extends Canvas implements Skinable {

  private TransparencyManager transparencyManager;

  private ImageData imageData;

  private String text = "";

  private Cache cache = new Cache();

  private ImageMerger imageMerger;

  public AlphaLabel(Composite parent, int style) {
    super(parent, style);

    addPaintListener(new PaintListener() {

      public void paintControl(PaintEvent e) {
        paint(e.gc);
      }
    });
  }

  public void setTransparencyManager(TransparencyManager transparencyManager) {
    this.transparencyManager = transparencyManager;
    imageMerger = new ImageMerger(transparencyManager, this);
  }

  public void paint(GC gc) {
    Rectangle bounds = getBounds();
    if (imageData == null) {
      /* Get the background image. */
      Image image = new Image(getDisplay(), transparencyManager.getBackground(this));
      
      /* Draw the text onto it. */
      GC imageGc = new GC(image);
      Point size = gc.stringExtent(text);
      int x = (bounds.width - size.x) / 2;
      int y = (bounds.height - size.y) / 2;
      imageGc.drawText(text, x, y, true);
      imageGc.dispose();
      
      /* Draw the image. */
      gc.drawImage(image, 0, 0);
      image.dispose();
    }
    else {
      int x = (bounds.width - imageData.width) / 2;
      int y = (bounds.height - imageData.height) / 2;
      ImageData mergedImage = imageMerger.merge(x, y, imageData);

      /* Look up our ImageData in our cache. */
      ImageHandle imageHandle = (ImageHandle) cache.get(mergedImage);
      if (imageHandle == null) {
        imageHandle = new ImageHandle(new Image(this.getDisplay(), mergedImage));
        cache.put(mergedImage, imageHandle);
      }
      gc.drawImage(imageHandle.getImage(), 0, 0);
    }
    gc.dispose();
  }

  public void setImage(String key, ImageData imageData) {
    if (key.equals("")) { this.imageData = imageData; redraw(); }
  }

  public void setText(String key, String text) {
    if (key.equals("")) { this.text = text; redraw(); }
  }

  public void setToolTipText(String key, String text) {
    if (key.equals("")) setToolTipText(text);
  }

  public Point computeSize(int wHint, int hHint, boolean changed) {
    Point size = new Point(0, 0);
    if (imageData == null) {
      GC gc = new GC(this);
      size = gc.stringExtent(text);
      gc.dispose();
    }
    else {
      size.x = imageData.width;
      size.y = imageData.height;
    }

    if (wHint > size.x) size.x = wHint;

    if (hHint > size.y) size.y = hHint;

    return size;
  }
  
  public void setText(String text) {
    setText("", text);
  }

}