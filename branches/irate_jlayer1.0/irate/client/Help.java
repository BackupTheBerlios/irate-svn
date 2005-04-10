package irate.client;

public class Help {

  /** @deprecated */
  
  public String get(String key) {
    if (key.endsWith(".txt"))
      key = key.substring(0, key.length() - 4);
    return Resources.getString(key.replace('/', '.'));
  }

}
