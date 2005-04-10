package irate.swt;

/**
 * Encapsulates a rating function.
 */
public class RatingFunction {
  
  private int value;
  private String name;
  private ThreeModeButton item;
  
  public RatingFunction(int value, String name) {
    this.value = value;
    this.name = name;
  }
  
  public int getValue() { return value; }
  public String getName() { return name; }
  
  public void setItem(ThreeModeButton item) { this.item = item; }
  public ThreeModeButton getItem() { return item; }    
}

