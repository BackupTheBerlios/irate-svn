// Copyright 2003 Stephen Blackheath

package irate.plugin.lircremote;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import nanoxml.*;

public abstract class Function
{
  public Function()
  {
  }
  
  public abstract String getID();

  public abstract String getName();

  public abstract void perform();

  public List buttons = new Vector();

  public void clearConfig()
  {
    buttons.clear();
  }

  public void parseXML(XMLElement elt)
  {
    buttons.clear();
    Enumeration enum = elt.enumerateChildren();
    while (enum.hasMoreElements()) {
      XMLElement child = (XMLElement) enum.nextElement();
      if (child.getName().equals("button"))
        buttons.add(new Button(child));
    }
  }

  public XMLElement formatXML()
  {
    XMLElement elt = new XMLElement(new Hashtable(), false, false);
    elt.setName("function");
    elt.setAttribute("id", getID());
    for (int i = 0; i < buttons.size(); i++)
      elt.addChild(((Button)buttons.get(i)).formatXML());
    return elt;
  }

  /**
   * Default repeat policy is: Single button presses only.
   * Functions such as volume up/down will use Button.REPEAT;
   */
  public int getRepeatPolicy() {return Button.SINGLE;}
}

