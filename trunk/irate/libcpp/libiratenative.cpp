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
#include <libirateinterface.h>
#include <irate/common/Track.h>
#include <jiratelib/LibIRateNative.h>
#include <gcj/cni.h>
#ifndef NULL
#define NULL ((void *)0)
#endif
LibIRateListener* globalListener=NULL;
jiratelib::LibIRateNative* irateLib=NULL;
namespace iratelib {
	void initEngine(LibIRateListener *l) {
		if(irateLib!=NULL)return;
		JvCreateJavaVM(NULL);
		JvAttachCurrentThread(NULL, NULL);
		globalListener=l;
		irateLib= new jiratelib::LibIRateNative();
	}
	void closeAndQuitEngine() {
		irateLib->quit();
		//delete irateLib;
		irateLib=NULL;
		globalListener=NULL;
		JvDetachCurrentThread();
	}
	track_t next(bool lastFinished) {
		return (track_t)irateLib->next(lastFinished);
	}
	track_t previous() {
		return (track_t)irateLib->previous();
	}
	void setRating (const track_t &trackID, const int& rating) {
		irateLib->setRating((irate::common::Track *)trackID,(jint)rating);
	}
	void undoLastRating () {
		irateLib->undoLastRating();
	}
	//JArray< ::irate::common::Track *> *getAvailableTracks ();
	void createNewAccount (const char* user, const char* password, const char* host, int port,const char* requestDir) {
		irateLib->createNewAccount(JvNewStringLatin1(user), JvNewStringLatin1(password), JvNewStringLatin1(host), (jint) port,JvNewStringLatin1(requestDir));
	}
	/*bool createNewAccount (jstring user, jstring password, jstring host, int port,jstring requestDir){
		return (bool)irateLib->createNewAccount(user, password, host, (jint) port,requestDir);
	}*/
	track_t * availableTracks(int *length) {
		track_t* ret;
		JArray< ::irate::common::Track *> *trackArray=irateLib->getAvailableTracks();
		if(trackArray==NULL||trackArray->length==0){
			*length=0;
			return NULL;
		}
		::irate::common::Track ** trackToAdd=elements(trackArray);
		ret= new track_t[trackArray->length];
		
		(*length)=trackArray->length;
		
		for(int i=0;i<trackArray->length;i++) {
			ret[i]=(track_t)trackToAdd[i];
		}
		return ret;
	}
	/**Start to download and connecting to the server
	 */
	void startDownloading(){
		irateLib->startDownloading();
	}
	
	/**Check if there's something to undo
	 * 
	 * @return true if it's possible to undo the last rating
	 */
	bool canUndoLastRating(){
		return (bool)irateLib->canUndoLastRating();
	}
	void setTrackPlayed(track_t t){
		irateLib->setTrackPlayed((::irate::common::Track *)t);
	}
	void setPlayUnrated(bool play){
		irateLib->setPlayUnrated((jboolean)play);
	}
	bool needNewAccount(){
		return (bool)irateLib->needNewAccount();
	}
};
//extern "Java"{
namespace jiratelib {

		void LibIRateNative::updateTrack (::irate::common::Track *t) {
			if(globalListener!=NULL)
				globalListener->updateTrack((track_t)t);
		}
		void LibIRateNative::handleError (::java::lang::String *url, ::java::lang::String *message) {
			//handleErrorCallback(url,message);
			if(globalListener!=NULL)
			globalListener->handleError((string_jt)url,(string_jt)message);
		}
		void LibIRateNative::downloadFinished (::irate::common::Track *t, jboolean success) {
			//downloadFinishedCallback(t,success);
			if(globalListener!=NULL)
			globalListener->downloadFinished((track_t)t,(bool)success);
		}
		void LibIRateNative::downloadProgressed (::irate::common::Track *t, jint perc, ::java::lang::String *) {
			//downloadProgressedCallback(t,perc,state);
			if(globalListener!=NULL)
			globalListener->downloadProgressed((track_t)t,(int)perc);
		}
		void LibIRateNative::downloadStarted (::irate::common::Track *t) {
			//downloadStartedCallback(t);
			if(globalListener!=NULL)
			globalListener->downloadStarted((track_t)t);
		}
		void LibIRateNative::newAccountCreationMessage(::java::lang::String * statut,jint s){
			if(globalListener!=NULL){
				globalListener->newAccountCreationMessage((string_jt)statut,(int)s);
			}
		}
	};
//};
