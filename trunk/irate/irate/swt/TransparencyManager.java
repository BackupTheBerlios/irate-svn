package irate.swt;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TransparencyManager {

  private static final String key = "TransparencyManager.background";
  
  private Cache cache = new Cache();
  
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
    if (imageData != null) {
      Scale scale = new Scale();
      scale.width = bounds.width;
      scale.height = bounds.height;
      ImageData scaledImageData = (ImageData) cache.get(scale);
      if (scaledImageData == null) {
         scaledImageData = imageData.scaledTo(bounds.width, bounds.height);
         cache.put(scale, scaledImageData);
      }
      return scaledImageData;
    }
    
    // If there's no parent composite then we don't have a parent image.
    Composite parent = control.getParent();    
    if (parent == null)
      return null;

    // If the parent has no image then we bail.
    ImageData parentImageData = getBackground(parent);
    if (parentImageData == null)
      return null;
    
    Area area = new Area();
    area.parentImageData = parentImageData;
    area.bounds = bounds;
    imageData = (ImageData) cache.get(area);
    if (imageData == null) {
      imageData = getArea(area);
      cache.put(area, imageData);
    }
    return imageData;
  }
  
  private ImageData getArea(Area area) {
    // System.err.println("Image size = " + bounds.x + "x" + bounds.y);
    ImageData imageData = new ImageData(area.bounds.width, area.bounds.height, 
        area.parentImageData.depth, area.parentImageData.palette);
    int[] data = new int[area.bounds.width];
    for (int i = 0; i < area.bounds.height; i++) {
      if (area.bounds.y + i < area.parentImageData.height) {
        area.parentImageData.getPixels(area.bounds.x, area.bounds.y + i, data.length, data, 0);
        imageData.setPixels(0, i, data.length, data, 0);
      }
    }
    return imageData;
  }
  
  private class Area {
    ImageData parentImageData;
    Rectangle bounds;
    
    public boolean equals(Object obj) {
      if (!(obj instanceof Area))
        return false;
      
      Area area = (Area) obj;
      return parentImageData == area.parentImageData && bounds.equals(area.bounds);
    }
  }
  
  private class Scale {
    int width;
    int height;    
    ImageData imageData;

    public boolean equals(Object obj) {
      if (!(obj instanceof Scale))
        return false;
      
      Scale scale = (Scale) obj;
      return width == scale.width && height == scale.height && imageData == scale.imageData;
    }
}
  
}
