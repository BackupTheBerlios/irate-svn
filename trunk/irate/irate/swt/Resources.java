/*
 * Created on Dec 4, 2003
 */
package irate.swt;

import java.io.IOException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
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

  public static ImageData getIconImageData() throws IOException {
    if (iconImageData == null)
      iconImageData =
        new ImageData(irate.resources.BaseResources.getResourceAsStream("icon.gif"));
    return iconImageData;
  }
  
  public static Image getIconImage(Display display) throws IOException {
    return new Image(display, getIconImageData());
  }

}
