/*
 * Created on Apr 13, 2004
 */
package irate.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Anthony Jones
 */
public class ImageMerger {
  
  private Cache cache = new Cache("ImageMerger");
  
  public ImageMerger() {
  }
  
  public ImageData merge(ImageData backgroundData, Point location, ImageData foregroundData) {
    return merge(backgroundData, location.x, location.y, foregroundData);
  }
  
  public ImageData merge(ImageData backgroundData, int x, int y, ImageData foregroundData) {    
    Merge merge = new Merge();
    merge.backgroundData = backgroundData;
    merge.x = x;
    merge.y = y;
    merge.foregroundData = foregroundData;
    
    ImageData imageData = (ImageData) cache.get(merge);        
    if (imageData == null)
      cache.put(merge, imageData = merge(merge));
    
    return imageData;
  }
  
  public ImageData merge(Color backgroundColour, ImageData foregroundData) {    
    ColourMerge merge = new ColourMerge();
    merge.backgroundColour = backgroundColour;
    merge.foregroundData = foregroundData;
    
    ImageData imageData = (ImageData) cache.get(merge);        
    if (imageData == null)
      cache.put(merge, imageData = merge(merge));
    
    return imageData;
  }  
  
  private ImageData merge(Merge merge) {        
    ImageData destData = new ImageData(merge.backgroundData.width, merge.backgroundData.height,
          merge.foregroundData.depth, merge.foregroundData.palette,
          merge.backgroundData.scanlinePad, merge.backgroundData.data);
    
    // Merge the foreground onto the background
    int[] foregroundPixels = new int[merge.foregroundData.width];
    int[] backgroundPixels = new int[merge.foregroundData.width];
    int[] destPixels = new int[merge.foregroundData.width];
    for (int i = 0; i < merge.foregroundData.height; i++) {
        merge.foregroundData.getPixels(0, i, foregroundPixels.length, foregroundPixels, 0);
        merge.backgroundData.getPixels(merge.x, merge.y + i, backgroundPixels.length, backgroundPixels, 0);
        
        for (int j = 0; j < destPixels.length; j++) {
          
          // Grab the alpha value of the pixel we're deal with
          int a = merge.foregroundData.getAlpha(j,i);
         
          // Calculate the merge value
          RGB frgb = merge.foregroundData.palette.getRGB(foregroundPixels[j]);
          RGB brgb = merge.backgroundData.palette.getRGB(backgroundPixels[j]);
          int r = (frgb.red * a + brgb.red * (255 - a)) / 255;
          int g = (frgb.green * a + brgb.green * (255 - a)) / 255;
          int b = (frgb.blue * a + brgb.blue * (255 - a)) / 255;
          RGB drgb = new RGB(r, g, b);
          destPixels[j] = getNearestPixel(destData.palette, drgb);
        }
        destData.setPixels(merge.x, merge.y + i, destPixels.length, destPixels, 0);
    }    
    return destData;
  }

  private ImageData merge(ColourMerge merge) {
    ImageData destData = new ImageData(merge.foregroundData.width, merge.foregroundData.height,
        merge.foregroundData.depth, merge.foregroundData.palette);
    RGB brgb = merge.backgroundColour.getRGB();
    int[] foregroundPixels = new int[merge.foregroundData.width];
    int[] destPixels = new int[merge.foregroundData.width];
    for (int i = 0; i < merge.foregroundData.height; i++) {
      merge.foregroundData.getPixels(0, i, foregroundPixels.length, foregroundPixels, 0);
      for (int j = 0; j < destPixels.length; j++) {
        
        // Grab the alpha value of the pixel we're deal with
        int a = merge.foregroundData.getAlpha(j,i);
       
        // Calculate the merge value
        RGB frgb = merge.foregroundData.palette.getRGB(foregroundPixels[j]);
        int r = (frgb.red * a + brgb.red * (255 - a)) / 255;
        int g = (frgb.green * a + brgb.green * (255 - a)) / 255;
        int b = (frgb.blue * a + brgb.blue * (255 - a)) / 255;
        RGB drgb = new RGB(r, g, b);
        destPixels[j] = getNearestPixel(destData.palette, drgb);
      }
      destData.setPixels(0, i, destPixels.length, destPixels, 0);
    }
    return destData;
  }
  
  private int getNearestPixel(PaletteData palette, RGB ideal) {
    int pixel = palette.getPixel(ideal);
    if (pixel != SWT.ERROR_INVALID_ARGUMENT)
      return pixel;
    System.out.println("Pixel match failure");
    RGB[] rgbs = palette.getRGBs();
    pixel = 0;
    int best = 1 << 31;
    for (int i = 0; i < rgbs.length; i++) {
      RGB rgb = rgbs[i];
      int dist = (rgb.red - ideal.red) ^ 2 
          + (rgb.green - ideal.green) ^ 2
          + (rgb.blue - ideal.blue) ^ 2;
      if (dist < best) {
        pixel = i;
        best = dist;
      }
    }
    
    return pixel;
  }

  private class Merge {
    ImageData backgroundData;
    int x;
    int y;
    ImageData foregroundData;
    
    public boolean equals(Object obj) {
      if (!(obj instanceof Merge))
        return false;
      
      Merge merge = (Merge) obj;
      return x == merge.x && y == merge.y 
          && foregroundData == merge.foregroundData 
          && backgroundData == merge.backgroundData; 
    }

    public int hashCode() {
      return x + (y  << 16) + backgroundData.hashCode() + foregroundData.hashCode();
    }
  }
  
  private class ColourMerge {
    Color backgroundColour;
    ImageData foregroundData;
    
    public boolean equals(Object obj) {
      if (!(obj instanceof ColourMerge))
        return false;
      
      ColourMerge merge = (ColourMerge) obj;
      return foregroundData == merge.foregroundData 
          && backgroundColour.equals(merge.backgroundColour); 
    }
    public int hashCode() {
      return backgroundColour.hashCode() + foregroundData.hashCode();
    }
  }
}
