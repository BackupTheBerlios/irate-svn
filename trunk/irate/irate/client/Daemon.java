// Copyright (C) 2004 Len Trigg.
// Created 2004-09-14
package irate.client;

import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.Utils;
import irate.download.DownloadThread;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * <code>Daemon</code> provides network access to several iRate
 * functions. The intention is that music playing applications can
 * query and edit track ratings. To use this, start from the command
 * line with the name of the track database file:<p>
 *
 * <pre><code>
 * java irate.client.Daemon ~/irate/trackdatabase.xml
 * </pre></code><p>
 *
 * One simple way to send commands is to use the netcat program. Here
 * is how to ask the Daemon what commands it understands:<p>
 *
 * <pre><code>
 * echo "help" | nc localhost 12543
 * </pre></code><p>
 *
 * The daemon is singlethreaded, so only one connection can be
 * processed at a time.
 *
 * @author <a href="mailto:lenbok@gmail.com">Len Trigg</a>
 * @version $Revision: 1.2 $
 */
public class Daemon {

  /** Default port to listen on. */
  public static final int DEFAULT_PORT = 12543;

  /** The track database */
  private final TrackDatabase mTrackDatabase;

  /** Our contact with the server and new track provider */
  private final DownloadThread mDownloader;
  
  /** Holds the commands we know how to execute. */
  private final Map mCommands = new TreeMap();

  private boolean mKeepRunning = true;


  /**
   * Base for <code>Command</code>s accepted by the Daemon.
   */
  private abstract class Command {

    private final String mName;
    
    /**
     * Creates a new <code>Command</code>.
     *
     * @param name the name by which the command can be invoked.
     */
    public Command(String name) {
      mName = name;
    }

    /** Gets the name of the command. */
    public String name() {
      return mName;
    }

    /** Returns a description of the command. */
    public abstract String description();

    /**
     * Process the command.
     *
     * @param input the remaining command input
     * @param out a <code>PrintWriter</code> value to which results
     * should be output. Within reason, responses should be
     * single-line.
     * @exception IOException if an error occurs.
     */
    public abstract void process(String input, PrintWriter out) throws IOException;

  }


  private final Command mHelpCommand = new Command("help") {
      public void process(String input, PrintWriter out) throws IOException {
        Iterator it = mCommands.keySet().iterator();
        while (it.hasNext()) {
          String name = (String) it.next();
          Command c = (Command) mCommands.get(name);
          out.println(name + ": " + c.description());
        }
      }
      public String description() {
        return "Lists the descriptions of all known commands.";
      }
    };

  private final Command mQueryCommand = new Command("query") {
      public void process(String input, PrintWriter out) throws IOException {
        Track t = getTrack(input);
        if (t != null) {
          out.print(t.toString());
          if (t.isRated()) {
            out.print(" " + Integer.toString((int) t.getRating()));
          }
          out.println();
        } else {
          out.println("ERROR: No track named \"" + input + "\"");
        }
      }
      public String description() {
        return "Returns information about a specific track. Give the name of the track to query.";
      }
    };

  private final Command mPlayedCommand = new Command("played") {
      public void process(String input, PrintWriter out) throws IOException {
        Track t = getTrack(input);
        if (t != null) {
          t.updateTimeStamp();
          t.incNoOfTimesPlayed();
          out.println(t.toString() + " " + (t.isRated() ? Integer.toString((int) t.getRating()) : "unrated" ));
        } else {
          out.println("ERROR: No track named \"" + input + "\"");
        }
      }
      public String description() {
        return "Increments playcount and timestamp information for the specified. Give the name of the track to query.";
      }
    };

  private final Command mOnlineCommand = new Command("online") {
      public void process(String input, PrintWriter out) throws IOException {
        if (!mDownloader.isAlive()) {
          out.println("Going online.");
          System.err.println("Going online.");
          mDownloader.start();
        }
      }
      public String description() {
        return "Syncronizes with the iRate server and initiates any needed downloads.";
      }
    };

  private final Command mQuitCommand = new Command("quit") {
      public void process(String input, PrintWriter out) throws IOException {
        mTrackDatabase.save();
        out.println("Quitting.");
        System.err.println("Quitting.");
        stop(); // Will stop after the current connection closes.
      }
      public String description() {
        return "Saves the track database and terminates the daemon.";
      }
    };

  private final Command mSummaryCommand = new Command("summary") {
      public void process(String input, PrintWriter out) throws IOException {
        // XXX just shows the state of whichever download thread last updated the state
        String downloaderState = mDownloader.isAlive() 
          ? (mDownloader.getDownloadState() == null ? "inactive" : mDownloader.getDownloadState())
          : "offline";

        out.println("Unrated:" + mTrackDatabase.getNoOfUnrated()
                    + " Rated:" + mTrackDatabase.getNoOfRated()
                    + " Total:" + mTrackDatabase.getNoOfTracks() 
                    + " Downloader:" + downloaderState);
      }
      public String description() {
        return "Outputs summary information about the track database.";
      }
    };

  private final Command mPurgeCommand = new Command("purge") {
      public void process(String input, PrintWriter out) throws IOException {
        mTrackDatabase.purge();
        out.println("Purged.");
        System.err.println("Purged.");
      }
      public String description() {
        return "Purges (deletes) any tracks rated 0.";
      }
    };

  private final Command mRateCommand = new Command("rate") {
      public void process(String input, PrintWriter out) throws IOException {
        final String msgNum = "ERROR: Expected numeric track rating, 0-10";
        int spPos = input.indexOf(' ');
        if (spPos == -1) {
          out.println(msgNum);
        } else {
          float rating = -1.0f;
          try {
            rating = Float.parseFloat(input.substring(0, spPos));
          } catch (NumberFormatException nfe) {
            // Don't care
          }
          if (rating < 0 || rating > 10) {
            out.println(msgNum);
          } else {
            String trackName = input.substring(spPos + 1);
            Track t = getTrack(trackName);
            if (t != null) {

              t.setRating(rating);
              out.print(t.toString());
              if (t.isRated()) {
                out.print(" " + Integer.toString((int) t.getRating()));
              }
              System.err.println("Rating set to " + t.getRating() + " for track " + t.toString());

              mTrackDatabase.save();

              if (mDownloader.isAlive()) {
                mDownloader.checkAutoDownload();
              }

            } else {
              out.println("ERROR: No track named \"" + trackName + "\"");
            }
          }              
        }
      }
      public String description() {
        return "Sets the rating for a track. Give the numeric rating (0.0 - 10.0), followed by the name of the track.";
      }
    };

  private final Command mPlaylistCommand = new Command("playlist") {
      public void process(String input, PrintWriter out) throws IOException {
        final String msgArgs = "ERROR: Expected numUnrated and numRated parameters.";
        String[] args = Utils.split(input, ' ');
        if (args.length > 2) {
          out.println(msgArgs);
        } else {
          int numUnrated = -1;
          int numRated = -1;
          if (args.length > 0) {
            try {
              numUnrated = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
            }
            if (args.length > 1) {
              try {
                numRated = Integer.parseInt(args[1]);
              } catch (NumberFormatException nfe) {
              }
            }
          }
          List playlist = makePlayList(numUnrated, numRated);
          Utils.scramble(playlist);
          System.err.println(playlist);
          for (int i = 0; i < playlist.size(); i++) {
            Track track = (Track) playlist.get(i);
            out.println("# " + track.toString());
            out.println(track.getFile());
          }
        }
      }


      private List makePlayList(int numUnrated, int numRated) {
        Track[] tracks = mTrackDatabase.getTracks();
        List unrated = new ArrayList();
        List rated = new ArrayList();
        float totalRating = 0;
        // Sort into unrated and rated
        for (int i = 0; i < tracks.length; i++) {
          Track track = tracks[i];
          if (track.isActive()) {
            if (track.isRated()) {
              rated.add(track);
              totalRating += track.getRating();
            } else {
              unrated.add(track);
            }
          }
        }
        if (numUnrated < 0 || numUnrated > unrated.size()) {
          System.err.println("Using " + unrated.size() + " unrated tracks.");
          numUnrated = unrated.size();
        }
        if (numRated < 0 || numRated > rated.size()) {
          System.err.println("Using " + rated.size() + " rated tracks.");
          numRated = rated.size();
        }

        List playlist = new ArrayList();
        Random r = new Random();

        // Add numUnrated tracks from unrated to playlist
        for (int i = 0; i < unrated.size(); i++) {
          Track track = (Track) unrated.get(i);
          if (r.nextDouble() <= ((double) numUnrated / (unrated.size() - i))) {
            playlist.add(track);
            numUnrated--;
          }
        }
        
        // Add numRated tracks from rated to playlist, weighted by rating
        for (int i = 0; i < rated.size(); i++) {
          Track track = (Track) rated.get(i);
          if (r.nextDouble() <= ((double) numRated * track.getRating() / totalRating)) {
            playlist.add(track);
            numRated--;
          }
          totalRating -= track.getRating();
        }

        return playlist;
      }

      public String description() {
        return "Creates a playlist. Optionally give number of unrated tracks and number of rated tracks desired.";
      }
    };


  /**
   * Creates a <code>Daemon</code>.
   *
   * @param dbFile <code>File</code> containing the track database xml
   * @exception IOException if an error occurs.
   */
  public Daemon(File dbFile) throws IOException {
    mTrackDatabase = new TrackDatabase(dbFile);
    mDownloader = new DownloadThread(mTrackDatabase);

    addCommand(mHelpCommand);
    addCommand(mSummaryCommand);
    addCommand(mQuitCommand);

    // Track db / downloader commands
    addCommand(mOnlineCommand);
    addCommand(mPurgeCommand);

    // Single track commands
    addCommand(mQueryCommand);
    addCommand(mPlayedCommand);
    addCommand(mRateCommand);

    // Others...
    addCommand(mPlaylistCommand);
  }


  private void addCommand(Command command) {
    mCommands.put(command.name(), command);
  }


  private Track getTrack(String localOrRemote) {
    Track track = mTrackDatabase.getTrack(localOrRemote);
    if (track == null) {
      // Maybe local filename was provided
      Track[] tracks = mTrackDatabase.getTracks();
      for (int i = 0; i < tracks.length; i++) {
        File file = tracks[i].getFile();
        if ((file != null) && localOrRemote.equals(file.toString())) {
          track = tracks[i];
          break;
        }
      }
    }
    return track;
  }


  /**
   * Starts the daemon waiting for connections and initiates
   * processing when connections are made.
   *
   * @param port the port number to accept connections on.
   * @exception IOException if an error occurs.
   */
  public void listen(int port) throws IOException {
    ServerSocket ss = new ServerSocket(port);

    System.out.println("Waiting for connections...");
    while (mKeepRunning) {
      process(ss.accept());
    }
  }


  /**
   * Tells the daemon to stop accepting new connecions. It will quit
   * once the current connection is closed.
   */
  public void stop() {
    mKeepRunning = false;
  }


  // Handle a client connection in socket mode
  private void process(Socket client) {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
      PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));

      process(in, out);

      in.close();
      out.close();
      client.close();
    } catch (Exception e) {
      System.err.println("Daemon got a client exception: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // Performs the actual work of processing the request
  private void process(BufferedReader in, PrintWriter out) {
    try {
      String line;
      while ((line = in.readLine()) != null) {
        if (line.length() != 0) {
          System.err.println(">>" + line);
          int spPos = line.indexOf(' ');
          if (spPos == -1) {
            spPos = line.length();
          }
          Command command = (Command) mCommands.get(line.substring(0, spPos));
          if (command != null) {
            command.process(line.substring(spPos).trim(), out);
          } else {
            out.println("ERROR: Unknown command \"" + line + "\"");
          }
          out.flush();
        }
      }
    } catch (Exception e) {
      System.err.println("Daemon got a client exception: " + e.getMessage());
      e.printStackTrace();
    } finally {
      out.flush();
    }
  }



  /**
   * Starts the Daemon. Give the track database file as argument.
   *
   * @param args a <code>String[]</code> value
   * @exception Exception if an error occurs.
   */
  public static void main(String [] args) throws Exception {
    if (args.length != 1) {
      throw new Exception("USAGE: Daemon trackdatabase.xml");
    }
    Daemon server = new Daemon(new File(args[0]));
    server.listen(DEFAULT_PORT);
  }
  
}
