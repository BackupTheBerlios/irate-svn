/*
 * Created on May 27, 2004
 */
package irate.client;

import java.io.FileNotFoundException;


/**
 * @author Anthony Jones
 */
public class MadplayEsdPlayer extends MadplayPlayer {

  public MadplayEsdPlayer() throws FileNotFoundException {
  }
  
  public String[] formatOtherArguments() {
    String[] superArgs = super.formatOtherArguments();
    String[] args = new String[superArgs.length + 2];
    args[0] = "-o";
    args[1] = "esd:";
    System.arraycopy(superArgs, 0, args, 2, superArgs.length);    
    return args;
  }

  public String getName() {
    return "madplay esd";
  }

}
