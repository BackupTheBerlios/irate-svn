package irate.swt;

import org.eclipse.swt.graphics.*;
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
    if (parent == null) {
      Image image = new Image(control.getDisplay(), bounds.width, bounds.height);
      GC gc = new GC(image);
      gc.setBackground(control.getBackground());
      gc.fillRectangle(0, 0, bounds.width, bounds.height);
      imageData = image.getImageData();
      image.dispose();
      control.setData(key, imageData);
      return imageData;
    }

    // Get the image data from the parent.
    ImageData parentImageData = getBackground(parent);
    
    // If the parent has no image then we bail. This can happen if the image is 0 x 0
    if (parentImageData == null)
      return null;
    
//    System.err.println("Image size = " + bounds.width + "x" + bounds.height);
    
    imageData = new ImageData(bounds.width, bounds.height, 
        parentImageData.depth, parentImageData.palette);

    int[] data = new int[bounds.width];
    for (int i = 0; i < bounds.height; i++) {
      if (bounds.y + i < parentImageData.height) {
        parentImageData.getPixels(bounds.x, bounds.y + i, data.length, data, 0);
        imageData.setPixels(0, i, data.length, data, 0);
      }
    }
    return imageData;
  }
  
}
