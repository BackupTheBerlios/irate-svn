/*
 * Created on Dec 4, 2003
 */
package irate.swt;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * @author Anthony Jones
 */
public class Resources {

  private static ImageData iconImageData;
  
  /** Get a resource string from the properties file associated with this 
   * package.
   */
  public static String getString(String key) {
    return irate.resources.BaseResources.getString("irate.swt.locale", key);
  }

  public static ImageData getIconImageData() {
    if (iconImageData == null)
      try {
        iconImageData =
          new ImageData(irate.resources.BaseResources.getResourceAsStream("icon.gif"));
        int whitePixel = iconImageData.palette.getPixel(new RGB(255, 255, 255));
        iconImageData.transparentPixel = whitePixel;
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    return iconImageData;
  }
  
  public static Image getIconImage(Display display) {
    return new Image(display, getIconImageData());
  }

}
