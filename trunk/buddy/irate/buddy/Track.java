package irate.buddy;

import java.io.Serializable;

public class Track implements Serializable {
    static final long serialVersionUID = -1528741773893505258L;

    public Track(String url, String artist, String title, String www) {
        this.url = url;
        this.artist = artist;
        this.title = title;
        this.www = www;
    }

    public String url;

    public String artist;

    public String title;

    public String www;

    public void printDebug(Context context) {
        context.logger.finest("url=" + url);
        context.logger.finest("artist="+artist);
        context.logger.finest("title=" +title);
        context.logger.finest("www="+www);
    }
}
