/*
 * Created on Apr 27, 2004
 */
package irate.swt;

import java.util.Hashtable;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * @author Anthony Jones
 */
public class BasicSkinable implements Skinable {

  private Hashtable images = new Hashtable();
  
  public BasicSkinable(Display display) {
  }
  
  public ImageData getImageData(String key) {
    return (ImageData) images.get(key); 
  }

  public void setTransparencyManager(TransparencyManager tm) {
  }

  public void setText(String key, String text) {
  }

  public void setToolTipText(String key, String text) {
  }

  public void setImage(String key, ImageData imageData) {
    images.put(key, imageData);    
  }
  
  public void redraw() {
  }

}
