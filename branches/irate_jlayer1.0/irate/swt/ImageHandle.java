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

  /*
   * This finalize() method was causing the user interface to hang frequently.
   * Now it probably leaks memory, but since Anthony is planning to revisit the
   * whole image-caching issue, I didn't think it was worth going to trouble
   * over this. -- Stephen Blackheath
  protected void finalize() throws Throwable {
    image.dispose();
  }
  */
}
