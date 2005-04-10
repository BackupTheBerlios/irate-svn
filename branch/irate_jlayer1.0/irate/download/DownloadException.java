/*
 * Created on 9/12/2004
 */
package irate.download;


/**
 * @author Anthony Jones
 */
public class DownloadException extends Exception {
  
  private String code;
  private String urlString;
  
  /** @deprecated */
  public DownloadException() {
  }
  
  public DownloadException(String code, String urlString) {
    this.code = code;
    this.urlString = urlString;
  }
  
  public String getCode() {
    return code;
  }
  
  public String getURLString() {
    return urlString;
  }
  
}
