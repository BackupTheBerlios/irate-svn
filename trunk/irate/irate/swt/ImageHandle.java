/*
 * Created on Apr 22, 2004
 */
package irate.swt;

import org.eclipse.swt.graphics.Image;

/**
 * @author Anthony Jones
 */
public class ImageHandle {
  
  private Image image;
  
  public ImageHandle(Image image) {
    this.image = image;
  }
  
  public Image getImage() {
    return image;
  }

  protected void finalize() throws Throwable {
    image.dispose();
  }
}
