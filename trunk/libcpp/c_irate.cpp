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
#include <c_irate.h>
 #include <irate/common/Track.h>
 #include <irate/common/Date.h>
 #include <gcj/cni.h>
 #include <libirateinterface.h>
short ir_isRated (track_t t) {
	return (short)((irate::common::Track*)t)->isRated();
}
float ir_getRating (track_t t) {
	return (float)((irate::common::Track*)t)->getRating();
}
int ir_getNoOfTimesPlayed (track_t t) {
	return (int)((irate::common::Track*)t)->getNoOfTimesPlayed();
}
void ir_setVolume (int v,track_t t) {
	((irate::common::Track*)t)->setVolume(v);
}
int ir_getVolume (track_t t) {
	return (int) ((irate::common::Track*)t)->getVolume();
}
void ir_unSetVolume (track_t t) {
	((irate::common::Track*)t)->unSetVolume();
}
short ir_isBroken (track_t t) {
	return (short)((irate::common::Track*)t)->isBroken();
}
short ir_isMissing (track_t t) {
	return (short)((irate::common::Track*)t)->isMissing();
}
short ir_isDeleted (track_t t) {
	return (short)((irate::common::Track*)t)->isDeleted();
}
short ir_isActive (track_t t) {
	return (short)((irate::common::Track*)t)->isActive();
}
short ir_isNotDownloaded(track_t t) {
	return (short)((irate::common::Track*)t)->isNotDownloaded();
}
short ir_isHidden (track_t t) {
	return (short)((irate::common::Track*)t)->isHidden();
}
short ir_isPendingPurge(track_t t){
	return (short)((irate::common::Track*)t)->isPendingPurge();
}
short ir_isOnPlayList (track_t t) {
	return (short)((irate::common::Track*)t)->isOnPlayList();
}
int ir_getProbability (track_t t) {
	return (int)((irate::common::Track*)t)->getProbability();
}
short ir_exists (track_t t) {
	return (short)((irate::common::Track*)t)->exists();
}
long ir_getPlayingTime(track_t t) {
	if(((irate::common::Track*)t)->exists())return (long)((irate::common::Track*)t)->getPlayingTime();
	else return 0L;
}
int ir_getDownloadAttempts(track_t t) {
	return (int)((irate::common::Track*)t)->getDownloadAttempts();
}

string_jt ir_getName(track_t t) {
	return (string_jt)((irate::common::Track*)t)->getName();
}

string_jt ir_getLastPlayed(track_t t) {
	return (string_jt)((irate::common::Track*)t)->getLastPlayed();
}
string_jt ir_getArtist (track_t t) {
	return (string_jt)((irate::common::Track*)t)->getArtist();
}
string_jt ir_getTitle (track_t t) {
	return (string_jt)((irate::common::Track*)t)->getTitle();
}

string_jt ir_getURL (track_t t) {
	return (string_jt)((irate::common::Track*)t)->getProperty(JvNewStringLatin1("url"));
}

string_jt ir_getKey (track_t t) {
	return (string_jt)((irate::common::Track*)t)->getKey();
}

string_jt ir_getFile (track_t t) {
	return (string_jt)((irate::common::Track*)t)->getProperty(JvNewStringLatin1("file"));
}


string_jt ir_getState (track_t t) {
	return (string_jt)((irate::common::Track*)t)->getState();
}


string_jt ir_getWebSite (track_t t) {
	return (string_jt)((irate::common::Track*)t)->getProperty(JvNewStringLatin1("www"));
}

string_jt ir_getLicense (track_t t) {
	return (string_jt)((irate::common::Track*)t)->getProperty(JvNewStringLatin1("license"));
}
string_jt ir_getAlbum (track_t t) {
	if(((irate::common::Track*)t)->exists())
		return (string_jt)((irate::common::Track*)t)->getAlbum();
	else return NULL;
}

string_jt ir_getComment (track_t t) {
	if(((irate::common::Track*)t)->exists())
		return (string_jt)((irate::common::Track*)t)->getComment();
	else return NULL;
}
string_jt ir_getCopyrightInfo(track_t t) {
	if(((irate::common::Track*)t)->exists())return (string_jt)((irate::common::Track*)t)->getCopyrightInfo();
	else return NULL;
}

string_jt ir_getGenre (track_t t) {
	if(((irate::common::Track*)t)->exists())return (string_jt)((irate::common::Track*)t)->getGenre();
	else return NULL;
}
string_jt ir_getPlayingTimeString (track_t t) {
	if(((irate::common::Track*)t)->exists())return (string_jt)((irate::common::Track*)t)->getPlayingTimeString();
	else return NULL;
}
string_jt ir_getYear (track_t t) {
	if(((irate::common::Track*)t)->exists())return (string_jt)((irate::common::Track*)t)->getYear();
	else return NULL;
}

int ir_getStringSize(string_jt s) {
	return (int)((jstring)s)->length();
}

jt_char* ir_getStringChars(string_jt s) {
	return (jt_char*)JvGetStringChars((jstring)s);
}



class Priv_internalCallbackListener:public LibIRateListener {
	public:
		Priv_internalCallbackListener(
		                              void (*handleErrorCallback)(string_jt, string_jt),
		                              void (*downloadFinishedCallback)(track_t, short),
		                              void (*downloadProgressedCallback)(track_t, int),
		                              void (*downloadStartedCallback)(track_t),
		                              void (*updateTrackCallback)(track_t)):LibIRateListener(),
		he(handleErrorCallback),df(downloadFinishedCallback),dp(downloadProgressedCallback),ds(downloadStartedCallback),ut(updateTrackCallback) {}
		virtual ~Priv_internalCallbackListener(){}
		virtual void updateTrack (const track_t& t) {
			if(ut!=NULL)ut(t);
		}
		
		virtual void handleError (string_jt url, string_jt message) {
			if(he!=NULL)he(url,message);
		}
		virtual void downloadFinished (const track_t& t, short s) {
			if(df!=NULL)df(t,s);
		}
		virtual void downloadProgressed (const track_t& t, const int& p) {
			if(dp!=NULL)dp(t,p);
		}
		virtual void downloadStarted (const track_t& t) {
			if(ds!=NULL)ds(t);
		}
		virtual void newAccountCreationMessage(string_jt statut, int s){
			if(nacm!=NULL)nacm(statut,s);
		}
		void setNewAccountCreationCallback(void(*newAccountCreationMessageCallback)(string_jt,int)){
			this->nacm=newAccountCreationMessageCallback;
		}
	private:
		void (*he)(string_jt, string_jt);
		void (*df)(track_t, short);
		void (*dp)(track_t, int);
		void (*ds)(track_t);
		void (*ut)(track_t);
		void (*nacm)(string_jt,int);
};
Priv_internalCallbackListener *_priv_internal_libh=NULL;
void ir_initEngineWithCallback(
      void (*handleErrorCallback)(string_jt, string_jt),
    void (*downloadFinishedCallback)(track_t, short),
    void (*downloadProgressedCallback)(track_t, int),
    void (*downloadStartedCallback)(track_t),
    void (*updateTrackCallback)(track_t)
) {
	_priv_internal_libh= new Priv_internalCallbackListener(
	                     handleErrorCallback,
	                     downloadFinishedCallback,
	                     downloadProgressedCallback,
	                     downloadStartedCallback,
	                     updateTrackCallback);
	iratelib::initEngine(_priv_internal_libh);
}

void ir_closeAndQuitEngineWithCallback() {
	iratelib::closeAndQuitEngine();
	delete _priv_internal_libh;
	_priv_internal_libh=NULL;
}

track_t ir_next(short lastFinished){
	return iratelib::next(lastFinished);
}

track_t ir_previous(){
	return iratelib::previous();
}
void ir_setRating (track_t trackHandle, int rating){
	iratelib::setRating(trackHandle,rating);
}

void ir_undoLastRating(){
	iratelib::undoLastRating();
}
short ir_needNewAccount(){
	return (short)iratelib::needNewAccount();
}
void ir_createNewAccount (const char* user, const char* password, const char* host, int port,const char* requestDir, void(*newAccountCreationMessageCallback)(string_jt,int)){
	_priv_internal_libh->setNewAccountCreationCallback(newAccountCreationMessageCallback);
	iratelib::createNewAccount(user,password,host,port,requestDir);
}
track_t* ir_availableTracks(int *length){
	return iratelib::availableTracks(length);
}

void ir_startDownloading(){
	iratelib::startDownloading();
}

short ir_canUndoLastRating(){
	return iratelib::canUndoLastRating();
}

void ir_setTrackPlayed(track_t t){
	iratelib::setTrackPlayed(t);
}
void ir_setPlayUnrated(short play){
	iratelib::setPlayUnrated(play);
}
  
short ir_isPlayingUnrated(){
	return iratelib::isPlayingUnrated();
}
#ifdef _WIN32_PLATFORM
int ir_unicodeToNative(string_jt str, LPSTR buf, int buflen)
{
  return ::WideCharToMultiByte(GetACP(), 0, (LPWSTR) JvGetStringChars((jstring)str),
    ((jstring)str)->length(), buf, buflen, NULL, NULL);
}
#endif
