package irate.client;

import java.util.Hashtable;

public class Help {

  private Hashtable help = new Hashtable();

  public String get(String key) {
    return (String) help.get(key);
  }

  private void add(String key, String data) {
    help.put(key, data);
  }
  
	public Help()
  {
    add("help/about.txt", "iRATE radio 0.3 http://irate.sourceforge.net\n"
        + "\n"
        + "Original concept by Anthony Jones <ajones@clear.net.nz>. Written by Anthony\n"
        + "Jones and Taras Glek. Thanks to Stephen Blackheath, Robin Sheat, Chris Manley,\n"
        + "Eric Dalquist, Markus Enzenberger, Todd Pytel, Michael Crawford, Judd Cohen and\n"
        + "Sebastian Spaeth. Thanks also to those who contributed patches or bug fixes,\n"
        + "filed bug reports or submitted music web sites.\n"
        + "\n"
        + "This program is free software; you can redistribute it and/or modify it under\n"
        + "the terms of the GNU General Public License as published by the Free Software\n"
        + "Foundation; either version 2 of the License, or (at your option) any later\n"
        + "version.");
    add("help/connectionfailed.txt", "Couldn't connect to the server.");
    add("help/connectionrefused.txt", "The connection to the server was refused. This probably means that the server\n" 
        + "is not running.");
    add("help/connectiontimeout.txt", "The connection to the server timed out. This probably means that the server is\n" 
        + "down  or the machine you're accessing doesn't run the server.");
    add("help/continuousfailed.txt", "A download failed so the continuous download function has been stopped.");
    add("help/empty.txt", "Sorry but you have already got every single song on the server. Or more likely there's\n"
        + "something strange going on with the server.");
    add("help/getstuffed.txt", "Sorry but the server doesn't have any more tracks that it thinks you will like\n" 
        + "at the moment.");
/*    add("help/gettingstarted.txt", "Getting started\n" 
        + "\n"
        + "Start by configuring your account. It will default to my server\n" 
        + "takahe.blacksapphire.com port 2278. If you don't have an account on my server\n"
        + "it will automatically create one for you.\n"
        + "\n"
        + "Once you have configured an account click on Download. It will automatically\n"
        + "choose some tracks for you to hear. The ratings you give to these tracks will\n"
        + "affect the probability of these tracks being played and will also affect what\n"
        + "tracks you are given in the future.\n"
        + "\n"
        + "As you get bored with your existing tracks you can download more and more\n" 
        + "songs. Remember to give each a rating to help your (and other peoples) future\n"
        + "downloads.\n"
        + "\n"
        + "Note that it keeps track of the number of times a track has been played. The\n"
        + "more times it has been played the less likely it is to be played in the future.\n"
        + "This is to prevent you from getting sick of the old songs."); */
    add("help/hostnotfound.txt", "Host not found. You have either entered in a non-existent host name or your DNS\n"
        + "(Domain Name Server) service is not functioning correctly.");
    add("help/malformedurl.txt", "The server has responded with a malformed URL.");
    add("help/errorerror.txt", "The requested page could not be displayed.");
    add("help/missingplayer.txt", "Can't find a suitable mp3 player such as madplay.");
    add("help/missingswt.txt", "Unable to initialize SWT.\r\n.You might have to set LD_LIBRARY_PATH=lib or equivalent. If problems persist try switching between gtk2/motif versions");
    add("help/password.txt", "The password you have entered is incorrect. If this is a new account, try using a different account name.");
    add("help/user.txt", "You have tried to create a user account with an invalid user name or password.\n" 
        + "Perhaps either the user name or password is blank.");
    add("help/notenoughratings.txt", "You have not rated enough tracks for the server to recognise your tastes.");
  }
}
