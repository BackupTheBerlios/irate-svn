/***************************************************************************
 *   Copyright (C) 2004 by Matthias Studer                                 *
 *   matthias.studer@ezwww.ch                                              *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
#ifndef _LIB_IRATE_INTERFACE_H
 #define _LIB_IRATE_INTERFACE_H
#include <iratedef.h>
class LibIRateListener {
	public:
		LibIRateListener() {}
		/**Called when the track trackHandle has been updated
		 * 
		 * @param trackHandle track to be updated
		 */
		virtual void updateTrack (const track_t& /*trackHandle*/) {}
		/**There are no valid account. request for creating a new one. You can create it using createAccount
		 */
		//virtual void newAccountRequested () {}
		/**Called when an error occured
		 * 
		 * @param url this is a pointer to a jstring with the url(basically a unique string)
		 * @param message this is a pointer to a jstring with the messae error
		 */
		virtual void handleError (string_jt /*url*/, string_jt /*message*/) {}
		/**Download of track trackHandle is finished (with success or not)
		 * 
		 * @param trackHandle track
		 * @param success true if download is successfull
		 */
		virtual void downloadFinished (const track_t& /*trackHandle*/, bool /*success*/) {}
		/**Dowload of the specified track has progressed
		 * 
		 * @param trackHandle track
		 * @param percent percent of download completed
		 */
		virtual void downloadProgressed (const track_t& /*trackHandle*/, const int& /*percent*/) {}
		/**Download of the specified track has started
		 * 
		 * @param trackHandle 
		 */
		virtual void downloadStarted (const track_t& /*trackHandle*/) {}
		/**Be notified of new account creation progress 
		* Account creation is async
		* @param statut the statut of account creation or "success" when account creation is successfull
		* @param state 1 creation in process, 2=>success, 3=>failed
		*/
		virtual void newAccountCreationMessage(string_jt /*statut*/,int /*state*/){}
};
namespace iratelib {
	/**Call to init the engine
	 * this can only be called once. Next call will be ignored
	 * @param l the listener to be notified by irate
	 */
	void initEngine(LibIRateListener *l);
	/**Close and clean the engine. You must call it when your program quit
	 */
	void closeAndQuitEngine();
	/**Return the next track that should be played
	 * 
	 * @param lastFinished if true we set some parameters to last track (Lastplaying time and so on)
	 * @return handle to the track
	 */
	track_t next(bool lastFinished);
	/**Return an handle to the last track
	 * There are no guarantie that this track can be played (for exemple if last track was rated 0)
	 * @return trackHandle
	 */
	track_t previous();
	/**Rate the track Specified by trackHandle to rating
	 * 
	 * @param trackHandle the track to rate
	 * @param rating the rating to give (0-10)
	 */
	void setRating (const track_t &trackHandle, const int& rating);

	/**Undo the last rating done not yet well implemented (does nothing if no LastRating to undo)
	 */
	void undoLastRating();
	//JArray< ::irate::common::Track *> *getAvailableTracks ();
	/**Try to create a new account with the specified parameters return true if successfull
	 * 
	 * @param user username to use
	 * @param password password for this account
	 * @param host hostname (usually server.irateradio.com)
	 * @param port port number usually 2278
	 * @param requestDir directory path to use to store downloaded songs and irate files should be writeable by the user
	 * @return true if account creation is successfull, false otherwise
	 */
	void createNewAccount (const char* user, const char* password, const char* host, int port,const char* requestDir);
	//bool createNewAccount (jstring user, jstring password, jstring host, int port,jstring requestDir);
	/**
	 * @return array
	 */
	/**get a list of not hidden tracks (ie download not brocken and rated>0)
	 * The array returned must be deleted
	  * @param length this pointer will hold the array length
	 * @return an array of trackHanldes
	 */
	track_t* availableTracks(int *length);

	/**Start to download and connecting to the server
	 */
	void startDownloading();

	/**Check if there's something to undo
	 * 
	 * @return true if it's possible to undo the last rating
	 */
	bool canUndoLastRating();

	/**Tell that this track has been played.
	 * This will some track parameter (so updateTrack will be called)
	 * The track will also added to the previous stack
	 * @param t the track that has been updated
	 */
	void setTrackPlayed(track_t t);
	
	void setPlayUnrated(bool playunrated);
	
	bool needNewAccount();

};
#endif
