package irate.swt;

import org.eclipse.swt.graphics.ImageData;


public interface ISkinableButton {

  public void setTransparencyManager(TransparencyManager tm);
  public void setNormalText(String text);
  public void setPressedText(String text);
  public void setNormalImage(ImageData image);
  public void setPressedImage(ImageData image);
  public void setNormalHotImage(ImageData image);
  public void setPressedHotImage(ImageData image);
  public void setActiveNormalImage(ImageData image);
  public void setActivePressedImage(ImageData image);
  public void redraw();
  
}
