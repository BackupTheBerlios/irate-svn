/*
 * LibIRate.java
 *
 * Created on November 10, 2004, 1:08 PM
 */

package jiratelib;
import irate.common.DiskControl;
import irate.common.Preferences;
import irate.common.Track;
import irate.common.TrackDatabase;
import irate.common.UpdateListener;
import irate.download.DownloadListener;
import irate.download.DownloadThread;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

/**
 *
 * @author  mat
 */
public class LibIRateNative implements irate.download.DownloadListener {
	protected TrackDatabase trackDatabase;
	protected PlayListManager playListManager;
	protected DownloadThread downloadThread;
	protected Stack lastTracks;
	private Track lastRatedTrack;
	private int lastTrackPreviousRank;
	//private Stack lastRatedTrack;
	//private Stack lastTrackPreviousRank;
	
	
	protected Preferences userPreferences;
	/** Creates a new instance of LibIRate */
	public LibIRateNative() {
		
		userPreferences = new Preferences();
		this.lastTracks= new Stack();
		lastRatedTrack = null;
		lastTrackPreviousRank = -1;
		
		File home = new File(System.getProperties().getProperty("user.home"));
		File dir = null;
		File file = null;
		File downloadDir = null;
		
		// Check to see if the iRATE directory exists in the user's home.
		// This needs to be there.
		dir = Preferences.getPrefsDirectory();
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		// Check to see if the user has a track database directory set in the
		// irate.xml file.  If so, this directory should point to the trackdatabase.xml
		// file.
		String preference = Preferences.getUserDownloadDirectoryPreference();
		if (preference != null) {
			file = new File(preference);
			downloadDir = new File(file.getParentFile(), "download/");
			
		}
		else {
			// If they don't have one set, fall back on the home directory.
			// If it doesn't exist in either location, then the user will need to fill in the
			// registration information.
			downloadDir = new File(dir, "download/");
			file = new File(dir, "trackdatabase.xml");
		}
		/*try{
		System.err.close();//=new PrintStream(new FileOutputStream(new File("/home/mat/logtestlibirate.txt")));
		}catch (Exception e) {
			e.printStackTrace();
		}*/
		try {
			trackDatabase = new TrackDatabase(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		playListManager = new PlayListManager(trackDatabase);
		
		
		downloadThread = new DownloadThread(trackDatabase) {
			public void process() throws IOException {
				super.process();
				// perhapsDisableAccount();
			}
			
			public void handleError(String code, String urlString) {
				LibIRateNative.this.handleError(code, urlString);
			}
		};
		
		downloadThread.addDownloadListener(this);
		
		
		System.out.println("Number of tracks "+trackDatabase.getNoOfTracks() );
		
		// Deal with some file cleanup here -- if the user has the maximum disk amount
		// set then we need to mark some files to be deleted.
		String downloadLimit = Preferences.getUserPreference("downloadLimit");
		if (downloadLimit != null) {
			DiskControl dc = new DiskControl(downloadDir, trackDatabase);
			dc.clearDiskSpace(new Integer(downloadLimit).intValue());
		}
		
	}
	public void startDownloading(){
		// If a track database couldn't be loaded from the file system, then we
		// need to create a new account.
		//Changed here to avoid some problems of NULL pointer in c calls
		this.downloadThread.start();
	}
	public void setPlayUnrated(boolean playunrated){
		if(playunrated){
			trackDatabase.setUnratedPlayListRatio(20);
		}else{
			trackDatabase.setUnratedPlayListRatio(0);
		}
	}
	
	public Track next(boolean lastFinished){
		if(lastFinished&&!this.lastTracks.empty()){
			Track tr=(Track)this.lastTracks.peek();
			if(tr!=null){
				tr.incNoOfTimesPlayed();
				tr.updateTimeStamp();
				updateTrack(tr);
			}
			
		}
		Track tra=this.playListManager.chooseTrack();
		this.lastTracks.push(tra);
		return tra;
	}
	public void setTrackPlayed(Track tr){
		tr.incNoOfTimesPlayed();
		tr.updateTimeStamp();
		this.lastTracks.push(tr);
		updateTrack(tr);
	}
	public Track previous(){
		if(this.lastTracks.empty())return null;
		else return(Track)this.lastTracks.pop();
	}
	public void setRating(final Track track, int rating){
		lastRatedTrack = track;
		lastTrackPreviousRank = -1;
		if (track.isRated())
			lastTrackPreviousRank = (int) track.getRating();
		
		// Update the Track Rating
		track.setRating(rating);
		
		// Save the database with the updated rating
		try {
			trackDatabase.save();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		updateTrack(track);
		this.downloadThread.checkAutoDownload();
	}
	public void undoLastRating() {
		
		if (lastRatedTrack == null) {
			return;
		}
		
		if (lastTrackPreviousRank != -1) {
			lastRatedTrack.setRating(lastTrackPreviousRank);
		}
		else {
			lastRatedTrack.unSetRating();
		}
		
		//  Save the database with the updated rating
		try {
			trackDatabase.save();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		updateTrack(lastRatedTrack);
		
		// Disable the undo option
		lastRatedTrack = null;
	}
	public boolean canUndoLastRating(){
		return lastRatedTrack!=null;
	}
	public native void updateTrack(Track track);
	public Track[] getAvailableTracks(){
		return trackDatabase.getTracks();
		/*Track[] tr=trackDatabase.getTracks();
		Vector v = new Vector();
		for(int i=0; i<tr.length;i++){
			if(!tr[i].isHidden()){
				v.add(tr[i]);
			}
		}
		tr =new Track[v.size()];
		return (Track[])v.toArray(tr);*/
	}
	public native void newAccountCreationMessage(String statut,int state);
	public boolean needNewAccount(){
		return trackDatabase.getNoOfTracks()==0;
	}
	public void createNewAccount(String user, String password, String host, int port,String requestDir){
		trackDatabase.setUserName(user);
		trackDatabase.setPassword(password);
		trackDatabase.setHost(host);
		trackDatabase.setPort(port);
		final String txtDirText = requestDir;
		 new Thread(new Runnable(){public void run() {
			// Check the current directory for an existing trackdatabase.xml for
			// compatibility reasons only.
			File dir = new File(".");  
			File file = new File(dir, "trackdatabase.xml");  
			if (!file.exists()) {
				dir = new File("/irate");  
				file = new File(dir, "trackdatabase.xml");  
				dir = new File(txtDirText, "irate");  
				
				if (!dir.exists())
				dir.mkdir();
				
				file = new File(dir, "trackdatabase.xml");  
				dir = new File(dir, "download");  
				
				if(!dir.exists())
				dir.mkdir();
			}
			try {
				Preferences.savePreferenceToFile("downloadDir", file.toString());  
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			trackDatabase.setFile(file);
			trackDatabase.setDownloadDir(dir);
			
			UpdateListener ul = new UpdateListener() {
				public void actionPerformed() {
					LibIRateNative.this.newAccountCreationMessage(downloadThread.getDownloadState(),1);
				}
				public void newTrackStarted(Track track) { }
			};//ul
			downloadThread.addUpdateListener(ul);
			
			downloadThread.contactServer(trackDatabase);
			downloadThread.removeUpdateListener(ul);
			//System.out.println("grrr");
			if (trackDatabase.getNoOfTracks() != 0) {
				LibIRateNative.this.newAccountCreationMessage("success",2);
			}
			else{
				LibIRateNative.this.newAccountCreationMessage("failed",3);
			}
		}}).start();
	}
	public native void handleError(String code, String urlString);
	public void downloadData(irate.common.Track track, byte[] buffer, int offset, int length) {
	}
	
	public native void downloadFinished(irate.common.Track track, boolean succeeded);
	
	public native void downloadProgressed(irate.common.Track track, int percentComplete, String state);
	
	public native void downloadStarted(irate.common.Track track);
	public void quit() {
		trackDatabase.purge();
		try {
			trackDatabase.save();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	  }
	
}
