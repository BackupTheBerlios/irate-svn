/**
 * Copyright (C) 2002 by Mark Stier, Germany
 */

package MAD;

import java.util.*;
import java.io.*;

public class MadEvent {
    public static final String ARTIST = "Artist:";
    public static final String TITLE = "Title:";
    public static final String ALBUM = "Album:";
    public static final String TRACK = "Track:";
    public static final String GENRE = "Genre:";

    private long when;
    private String msg;

    MadEvent(String msg) {
	this.msg = msg;
	this.when = System.currentTimeMillis();
    }

    public long getWhen() { return when; }
    public String getMsg() { return msg; }

    public boolean hasStopped() {
	int i = msg.indexOf(' ');
	if(i == -1) return false;
	if(msg.length() <= i+("frames decoded".length())) return false;
	if(msg.substring(i+1).startsWith("frames decoded"))
	    return true;
	return false;
    }

    public String getTime() {
	if(msg.length() >= 9
	   &&Character.isWhitespace(msg.charAt(0))
	   && Character.isDigit(msg.charAt(1))
	   && Character.isDigit(msg.charAt(2))
	   && msg.charAt(3) == ':'
	   && Character.isDigit(msg.charAt(4))
	   && Character.isDigit(msg.charAt(5))
	   && msg.charAt(6) == ':'
	   && Character.isDigit(msg.charAt(7))
	   && Character.isDigit(msg.charAt(8)))
	    return msg.substring(1, 9);
	else
	    return null;
    }
    
    public String getArtist() {
	String r = msg.trim();
	if(MadPlayer.startsWithIgnoreCase(r, ARTIST))
	    return r.substring(ARTIST.length()).trim();
	else
	    return null;
    }
    
    public String getTitle() {
	String r = msg.trim();
	if(MadPlayer.startsWithIgnoreCase(r, TITLE))
	    return r.substring(TITLE.length()).trim();
	else
	    return null;
    }
    
    public String getAlbum() {
	String r = msg.trim();
	if(MadPlayer.startsWithIgnoreCase(r, ALBUM))
	    return r.substring(ALBUM.length()).trim();
	else
	    return null;
    }
    
    public String getTrack() {
	String r = msg.trim();
	if(MadPlayer.startsWithIgnoreCase(r, TRACK))
	    return r.substring(TRACK.length()).trim();
	else
	    return null;
    }
    
    public String getGenre() {
	String r = msg.trim();
	if(MadPlayer.startsWithIgnoreCase(r, GENRE))
	    return r.substring(GENRE.length()).trim();
	else
	    return null;
    }
}
