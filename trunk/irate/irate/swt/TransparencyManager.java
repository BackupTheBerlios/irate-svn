package irate.swt;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TransparencyManager {

  private static final String key = "TransparencyManager.background";
  
  public TransparencyManager() {
  }

  public void associate(Control control, ImageData imageData) {
    control.setData(key, imageData);
  }
  
  public ImageData getBackground(Control control) {
    // Give up if one of the bounds is 0.
    Rectangle bounds = control.getBounds();
    if (bounds.width == 0 || bounds.height == 0)
      return null;
    
    // See if this image exists.
    ImageData imageData = (ImageData) control.getData(key);
    if (imageData != null)
      return imageData.scaledTo(bounds.width, bounds.height);
    
    // If there's no parent composite then we don't have a parent image.
    Composite parent = control.getParent();
    if (parent == null)
      return null;

    // If the parent has no image then we bail.
    ImageData parentImageData = getBackground(parent);
    if (parentImageData == null)
      return null;

    System.out.println("Image size = " + bounds.x + "x" + bounds.y);
    imageData = new ImageData(bounds.width, bounds.height, parentImageData.depth, parentImageData.palette);
    int[] data = new int[bounds.width];
    for (int i = 0; i < bounds.height; i++) {
      parentImageData.getPixels(bounds.x, bounds.y + i, data.length, data, 0);
      imageData.setPixels(0, i, data.length, data, 0);
    }
    return imageData;
  }
  
}
