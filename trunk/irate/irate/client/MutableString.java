/**
 * This class implements a mutable string.
 *
 * @author Creator: Robin Sheat
 */
public class MutableString {

  /**
   * The actual string is stored here.
   */
  private String s;

  /**
   * Constructor that sets the initial value to be an empty string.
   */
  public MutableString() {
    s = "";
  }

  /**
   * Constructor that sets the string to have an initial value.
   *
   * @param s the initial value of the string
   */
  public MutableString(String s) {
    this.s = s;
  }

  /**
   * Set the string value.
   *
   * @param s the new value for the string
   */
  public void set(String s) {
    this.s = s;
  }

  /**
   * Get the string value.
   *
   * @return the string contents
   */
  public String get() {
    return s;
  }

  /**
   * Auto-converts to a string.
   *
   * @return the string contents
   */
  public String toString() {
    return get();
  }

}

