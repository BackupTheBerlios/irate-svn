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
#ifndef _C_IRATE_H
#define _C_IRATE_H
#include <iratedef.h>
/**To know if a track is rated
 * @param t the track
 * @return true if already rated
 */
short ir_isRated (track_t t);
/**Get the rating of the track
*if not rated returning is undetermined
  * @param t the track
 * @return rating or unknow value if unrated
 */
  float ir_getRating (track_t t);
/**get the number of time the track has been played
 * 
* @param t the track
 * @return number of time played
 */
  int ir_getNoOfTimesPlayed (track_t t);
/**Set the volume attribute of this track
 * 
 * @param v the volume value
* @param t the track
 */
  void ir_setVolume (int v,track_t t);
/**get the volume value for this track
 * 
 * @param t the track
 * @return volum
 */
  int ir_getVolume (track_t t);
/**Reset the volume to default
 * 
 * @param t the track
 */
  void ir_unSetVolume (track_t t);
/**Check if download of this track is broken
 * 
 * @param t the track
 * @return true if broken
 */
  short ir_isBroken (track_t t);
/**Check if the file is missing
 * 
 * @param t the track
 * @return true if missing
 */
  short ir_isMissing (track_t t);
/**Check if the track has been deleted
 * 
 * @param t the track
 * @return true if deleted
 */
  short ir_isDeleted (track_t t);
  short ir_isActive (track_t t);
/**Check if the track hasn't been already downloaded
 * 
 * @param t the track
 * @return true if not downloaded
 */
  short ir_isNotDownloaded(track_t t);
/**Check if the track must be hidden(Download broken or rated 0)
 * 
 * @param t the track
 * @return true if hidden
 */
  short ir_isHidden (track_t t);
  /**Check if the track must be deleted when closing IRate when rated "This Sux" for exemple
 * 
 * @param t the track
 * @return true if on list for deletion
 */
short ir_isPendingPurge(track_t t);
/**Check if track is on the current playlist
 * 
 * @param t the track
 * @return true if on playlist
 */
  short ir_isOnPlayList (track_t t);
/**Get the probability of this track to be played
 * 
 * @param t the track
 * @return a value
 */
  int ir_getProbability (track_t t);
/**Check if track exist
 * 
 * @param t the track
 * @return true if it exists
 */
  short ir_exists (track_t t);
/**Get the playing time of this track
 * 
 * @param t the track
 * @return playing time
 */
  long ir_getPlayingTime(track_t t);
/**Return the number of download attempts
 * 
 * @param t the track
 * @return number of download attempts
 */
  int ir_getDownloadAttempts(track_t t);

/**Return formated name with title an artist
 * 
 * @param t the track
 * @return name can be NULL
 */
  string_jt ir_getName(track_t t);
/**Get formatted string of last time (date) this track was played
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getLastPlayed(track_t t);
/**get the artist of this track
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getArtist (track_t t);
/**return title of this track
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getTitle (track_t t);
/**Return the url from where we download the track
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getURL (track_t t);
/**Return an unique string for this track
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getKey (track_t t);
/**Return the filename of this track
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getFile (track_t t);

/**Return a string showing the state of this track (broken...)
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getState (track_t t);

/**Return the website of this track
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getWebSite (track_t t);
/**Get license of this track (an url)
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getLicense (track_t t);
/**Get the album name for this track
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getAlbum (track_t t);

/**Get comment ID3 on this track
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getComment (track_t t);
/**Get copyright information for this track
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getCopyrightInfo(track_t t);
/**Get genre information for this track
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getGenre (track_t t);
/**Get playing time as a formatted string
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getPlayingTimeString (track_t t);
/**get ID3 year attribute
 * 
 * @param t the track
 * @return string
 */
  string_jt ir_getYear (track_t t);

/**Return the size of the specified string
 * 
 * @param s the string
 * @return size of the string
 */
  int ir_getStringSize(string_jt s);

/**Return chars for this string
 * Chars are unicode character of 16 bits
 * @param s the string
 * @return pointer to array of 16 bits chars
 */
  jt_char* ir_getStringChars(string_jt s);


/**This function set the callback that will be called by IRate engine and init the engine
 * A callback can be set to NULL, callback cannot be change after initialization
 * See doc of LibIRateListener for better documentation about parameters
 * 
 * @param (* handleErrorCallback)( string_jt , string_jt ) Called when an error occured 
 * @param (* downloadFinishedCallback)( track_t , short ) Download of track trackHandle is finished (with success or not)
 * @param (* downloadProgressedCallback)( track_t , int ) Dowload of the specified track has progressed
 * @param (* downloadStartedCallback)( track_t ) Download of the specified track has started
 * @param (* updateTrackCallback)( track_t ) Some parameters of this track have been updated
 */
  void ir_initEngineWithCallback(
    void (*handleErrorCallback)(string_jt, string_jt),
    void (*downloadFinishedCallback)(track_t, short),
    void (*downloadProgressedCallback)(track_t, int),
    void (*downloadStartedCallback)(track_t),
    void (*updateTrackCallback)(track_t)
    
);
/**Close and clean the engine. You must call it when your program quit if you have used initEngineWithCallback
 */
  void ir_closeAndQuitEngineWithCallback();
/**Return the next track that should be played
 * 
 * @param lastFinished if true we set some parameters to last track (Lastplaying time and so on)
 * @return handle to the track
 */
  track_t ir_next(short lastFinished);
/**Return an handle to the last track
 * There are no guarantie that this track can be played (for exemple if last track was rated 0)
 * @return trackHandle
 */
  track_t ir_previous();
/**Rate the track Specified by trackHandle to rating
 * 
 * @param trackHandle the track to rate
 * @param rating the rating to give (0-10)
 */
  void ir_setRating (track_t trackHandle, int rating);

/**Undo the last rating done not yet well implemented (does nothing if no LastRating to undo)
 */
  void ir_undoLastRating();
/**Try to create a new account with the specified parameters return true if successfull
 * 
 * @param user username to use
 * @param password password for this account
 * @param host hostname (usually server.irateradio.com)
 * @param port port number usually 2278
 * @param requestDir directory path to use to store downloaded songs and irate files should be writeable by the user
 * @return true if account creation is successfull, false otherwise
 */
  void ir_createNewAccount (const char* user,const  char* password, const char* host, int port,const char* requestDir,void(*newAccountCreationMessageCallback)(string_jt,int));
/**
 * @return array
 */
/**get a list of not hidden tracks (ie download not brocken and rated>0)
 * The array returned must be deleted
  * @param length this pointer will hold the array length
 * @return an array of trackHanldes
 */
  track_t* ir_availableTracks(int *length);

/**Start to download and connecting to the server
 */
  void ir_startDownloading();

/**Check if there's something to undo
 * 
 * @return true if it's possible to undo the last rating
 */
  short ir_canUndoLastRating();

/**Tell that this track has been played.
 * This will some track parameter (so updateTrack will be called)
 * The track will also added to the previous stack
 * @param t the track that has been updated
 */
  void ir_setTrackPlayed(track_t t);
  
  /**Set wheter we should play unrated tracks or not
  * @param play true to play unrated tracks
  */
  void ir_setPlayUnrated(short play);
  /**To know if we need a new account of not
  * @return true if a new account need to be created
  */
  short ir_needNewAccount();
  short ir_isPlayingUnrated();
#ifdef _WIN32_PLATFORM
#include <Winnls.h>
/**Convert internal unicode string to Window native*/
  int ir_unicodeToNative(string_jt str, LPSTR buf, int buflen);
#endif

#endif /*//#ifndef _C_IRATE_H*/
