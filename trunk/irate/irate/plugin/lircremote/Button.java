// Copyright 2003 Stephen Blackheath

package irate.plugin.lircremote;

import irate.plugin.*;
import nanoxml.*;
import java.util.Hashtable;

public class Button
{
  private String id;
  private int repeatCount;
  public static final int SINGLE = 0;
  public static final int REPEAT = -1;

  public Button(String id, int repeatCount)
  {
    this.id = id;
    this.repeatCount = repeatCount;
  }

  public Button(XMLElement elt)
  {
    this.id = elt.getStringAttribute("id");
    String rcStr = elt.getStringAttribute("repeat");
    if (rcStr.equals("REPEAT"))
      this.repeatCount = REPEAT;
    else {
      try {
        this.repeatCount = Integer.parseInt(rcStr);
      }
      catch (NumberFormatException e) {
        this.repeatCount = 0;
      }
    }
  }

  public XMLElement formatXML()
  {
    XMLElement elt = new XMLElement(new Hashtable(), false, false);
    elt.setName("button");
    elt.setAttribute("id", id);
    elt.setAttribute("repeat", repeatCount == REPEAT ? "REPEAT" : Integer.toString(repeatCount));
    return elt;
  }

  /**
   * The string that identifies this button.
   */
  public String getID() {return id;}
  /**
   * Repeat count, which is zero when the button is first pressed.
   */
  public int getRepeatCount() {return repeatCount;}
  
  public String toString()
  {
    return id+"("+repeatCount+")";
  }

  public boolean equals(Object other_)
  {
    if (other_ instanceof Button) {
      Button other = (Button)other_;
      if (!id.equals(other.id))
        return false;
      if (repeatCount == REPEAT || other.repeatCount == REPEAT)
        return true;
      return repeatCount == other.repeatCount;
    }
    else
      return false;
  }
}

