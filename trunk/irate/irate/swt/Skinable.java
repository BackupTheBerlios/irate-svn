/*
 * Created on Apr 16, 2004
 */
package irate.swt;

import org.eclipse.swt.graphics.ImageData;

/**
 * @author Anthony Jones
 */
public interface Skinable {
  public void setTransparencyManager(TransparencyManager tm);
  public void setText(String key, String text);
  public void setToolTipText(String key, String text);
  public void setImage(String key, ImageData imageData);
  public void redraw();      
}
