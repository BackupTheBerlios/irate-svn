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
#ifndef BASEIRATETRACK_H
#define BASEIRATETRACK_H
#include <libirateinterface.h>
#include <list>
#include <iratetrack.h>

/**To be informed about things happening in IRate
* As IRate is multithreaded you certainly should Post an event (or something like this before you can anything) in order to be in the GUI thread (The QT implementation does it)
*/
template <typename _IR_String>
class IRateCenterListener{
	public:
	/**Called when a track has been updated
	*@param track the track that was updated
	*/
	virtual void updateTrack (IRateTrack<_IR_String>* track)=0;
	/**Called when an Error has occured
	* @param code the code for this error
	*@param url the url to find the message containing explaination about error (see the help files)
	*/
	virtual void handleError (_IR_String code, _IR_String url)=0;
	/**Download of a track has finished
	* You should enable it in your GUI if success is set to true
	* @param track the track that was downloaded
	* @param success true if download was successfull
	*/
	virtual void downloadFinished (IRateTrack<_IR_String>* track, bool success)=0;
	/**Download of a track has progressed
	*@param track the track that we are downloading
	*@param percent percent accomplished
	*/
	virtual void downloadProgressed (IRateTrack<_IR_String>* track, const int& percent)=0;
	/**Download of this track has started
	* @param track track started
	*/
	virtual void downloadStarted (IRateTrack<_IR_String>* track)=0;
	/**When we start creating a new account this show current progression of the creation
	* Typically you would like to start the download if state==2 with IRateCenter\<_IR_String\>::instance()->startDownloading();
	*@param statut a string describing the statut
	* @param state if 1 then creation is in progress if 2 then account was successfully created if 3 account creation hase failed
	*/
	virtual void newAccountCreationMessage(_IR_String statut,int state)=0;
};
/**This class control IRate it also cache IRateTrack instance.
* Cached IRateTrack are automatically deleted at the end of the program
*@see IRateTrack a class to get information about IRate Track 
*@see IRateCenterListener To get notified about IRate events
*/
template <typename _IR_String>
class IRateCenter:public LibIRateListener{
	public:
		/**The class is a singleton (only one instance available and you can access it from here
		* before the initialization it will always return NULL
		*/
		static IRateCenter<_IR_String>* instance();
		/**Initialize the engine of IRate
		*@param nullempty a pointer to a function that test if a string (Of your type) is null or empty
		* typically<code> bool mynullempty(MyString s){return s.length==0;}</code>
		*@param decode a pointer to function that translate strings from string_jt to your type
		*/
		static void initEngine(bool(*nullempty)(const _IR_String&),_IR_String(*decoder)(string_jt));
		/**Close and quit the IRate engine, free used memory
		*/
		static void closeAndQuitEngine();
		//LibIRateListener methods
		/**For internal use*/
		virtual void updateTrack (const track_t& trackHandle);
		/**For internal use*/
		virtual void handleError (string_jt code, string_jt url);
		/**For internal use*/
		virtual void downloadFinished (const track_t& trackHandle, bool success);
		/**For internal use*/
		virtual void downloadProgressed (const track_t& trackHandle, const int& percent);
		/**For internal use*/
		virtual void downloadStarted (const track_t& trackHandle);
		/**For internal use*/
		virtual void newAccountCreationMessage(string_jt statut,int state);
		//Methods for controlling IRate
		/**Call it to know wich track to play next
		* can return a null value if no track in database
		*@param lastFinished if true we change some parameter on last track (like time played and so on)
		*@return the next track to play
		*/
		IRateTrack<_IR_String>* next(bool lastFinished){
			track_t t= iratelib::next(lastFinished);
			if(t!=NULL){
				return doCache(t,false);
			}return NULL;
		}
		/**Return the previous played song or NULL if no song was played before
		*/
		IRateTrack<_IR_String>* previous(){
			track_t t= iratelib::previous();
			if(t!=NULL){
				return doCache(t,false);
			}return NULL;
		}
		/**start the creation of a new account
		* it's an async operation
		*/
		inline void createNewAccount (const char* user, const char* password, const char* host, int port,const char* requestDir){
			iratelib::createNewAccount(user,password,host,port,requestDir);
		}
		/**Return a list of track playable or not*/
		std::list<IRateTrack<_IR_String>*> availableTracks();
		/**Start to download if we haven't do it before, you need to be sure to call it only once.
		* But you may want not to start downloading before having an account on the server
		* So check if needNewAccount 
		*/
		inline void startDownloading(){
			if(!_mdownloading){
				iratelib::startDownloading();
				_mdownloading=true;
			}
		}
		/**true if it's possible to undo last rating*/
		inline bool canUndoLastRating(){
			return iratelib::canUndoLastRating();
		}
		/**Undo the last rating done*/
		inline void undoLastRating(){
			iratelib::undoLastRating();
		}
		/**Set whether we play unrated tracks*/
		inline void setPlayUnrated(bool playunrated){
			iratelib::setPlayUnrated(playunrated);
		}
		inline bool isPlayingUnrated(){
			return iratelib::isPlayingUnrated();
		}
		/**True if a new account need to be created*/
		inline bool needNewAccount(){
			return iratelib::needNewAccount();
		}
		/**Add a listener to IRate events
		* if you add a same listener multiple time you will be notified multiple times
		*/
		void addListener(IRateCenterListener<_IR_String> *listener){
			_mlistener.push_front(listener);
		}
		/**remove a previously added listener all instance found are removed
		*/
		void removeListener(IRateCenterListener<_IR_String>* listener){
			ListenerIterator it;
			for(it=_mlistener.begin(); it != _mlistener.end();++it){
				if((*it)==listener){
					_mlistener.erase(it);
				}
			}
		}
	protected:
		typedef typename std::list<IRateCenterListener<_IR_String>*>::iterator ListenerIterator;
		
		IRateTrack<_IR_String>* doCache(const track_t& trackHandle,const bool& update){
			IRateTrack<_IR_String>* track=NULL;
			if(_mcache.find(trackHandle)!=_mcache.end()){
				track=_mcache[trackHandle];
				if(update)track->update();
			}else{
				track= new IRateTrack<_IR_String>(trackHandle);
				_mcache[trackHandle]=track;
			}
			return track;
		}
		inline IRateTrack<_IR_String>* getTrack(const track_t& trackHandle){
			if(_mcache.find(trackHandle)!=_mcache.end()){
				return _mcache[trackHandle];
			}
			return NULL;
		}
		IRateCenter():LibIRateListener(),_mdownloading(false){}
		virtual ~IRateCenter();
		std::map<track_t,IRateTrack<_IR_String>*> _mcache;
		static _IR_String(*decodeString)(string_jt);
		static bool(*isNullOrEmpty)(const _IR_String&);
		static IRateCenter<_IR_String> * _minstance;
		bool _mdownloading;
		std::list<IRateCenterListener<_IR_String>*> _mlistener;
};
template <typename _IR_String>_IR_String (*IRateCenter<_IR_String>::decodeString)(string_jt)=NULL;
template <typename _IR_String>bool (*IRateCenter<_IR_String>::isNullOrEmpty)(const _IR_String&)=NULL;
template <typename _IR_String>IRateCenter<_IR_String>* IRateCenter<_IR_String>::_minstance=NULL;


template <typename _IR_String>IRateCenter<_IR_String>* IRateCenter<_IR_String>::instance(){
	return _minstance;
}
template <typename _IR_String>
void IRateCenter<_IR_String>::initEngine(bool(*nullempty)(const _IR_String&), _IR_String(*decoder)(string_jt)){
	isNullOrEmpty=nullempty;
	decodeString=decoder;
	IRateTrack<_IR_String>::initFunc(nullempty,decoder);
	_minstance=new IRateCenter<_IR_String>();
	iratelib::initEngine(_minstance);
	int length=0;
	track_t *list=iratelib::availableTracks(&length);
	if(length>0){
		for(int i=0;i<length;i++){
			_minstance->doCache(list[i],false);
		}
		delete list;
	}
	
}
template <typename _IR_String>
void IRateCenter<_IR_String>::closeAndQuitEngine(){
	iratelib::closeAndQuitEngine();
	delete _minstance;
	_minstance=NULL;
}
template <typename _IR_String>IRateCenter<_IR_String>::~IRateCenter(){
	std::list<IRateTrack<_IR_String>* > lt=this->availableTracks();
	typename std::list<IRateTrack<_IR_String>* >::iterator it;
	IRateTrack<_IR_String>* tr;
	for(it=lt.begin(); it != lt.end();++it){
		tr=*it;
		if(tr!=NULL)
			delete tr;
	}
	this->_mcache.clear();
}
template <typename _IR_String>
void IRateCenter<_IR_String>::updateTrack (const track_t& trackHandle){
	IRateTrack<_IR_String>* tr= this->doCache(trackHandle,true);
	ListenerIterator it;
	for(it=_mlistener.begin(); it != _mlistener.end();++it){
		(*it)->updateTrack(tr);
	}
}
template <typename _IR_String>
void IRateCenter<_IR_String>::handleError (string_jt code, string_jt url){
	_IR_String mycode=decodeString(code);
	_IR_String myurl=decodeString(url);
	ListenerIterator it;
	for(it=_mlistener.begin(); it != _mlistener.end();++it){
		(*it)->handleError(mycode,myurl);
	}
}
template <typename _IR_String>
void IRateCenter<_IR_String>::downloadFinished (const track_t& trackHandle, bool success){
	IRateTrack<_IR_String>* tr= this->doCache(trackHandle,true);
	ListenerIterator it;
	for(it=_mlistener.begin(); it != _mlistener.end();++it){
		(*it)->downloadFinished(tr,success);
	}

}
template <typename _IR_String>
void IRateCenter<_IR_String>::downloadProgressed (const track_t& trackHandle, const int& percent){
	IRateTrack<_IR_String>* tr= this->doCache(trackHandle,true);
	ListenerIterator it;
	for(it=_mlistener.begin(); it != _mlistener.end();++it){
		(*it)->downloadProgressed(tr,percent);
	}
}
template <typename _IR_String>
void IRateCenter<_IR_String>::downloadStarted (const track_t& trackHandle){
	IRateTrack<_IR_String>* tr= this->doCache(trackHandle,true);
	ListenerIterator it;
	for(it=_mlistener.begin(); it != _mlistener.end();++it){
		(*it)->downloadStarted(tr);
	}
}
template <typename _IR_String>
void IRateCenter<_IR_String>::newAccountCreationMessage(string_jt statut,int state){
	_IR_String str=decodeString(statut);
	ListenerIterator it;
	for(it=_mlistener.begin(); it != _mlistener.end();++it){
		(*it)->newAccountCreationMessage(str,state);
	}
}
template <typename _IR_String>
std::list<IRateTrack<_IR_String>*> IRateCenter<_IR_String>::availableTracks(){
	typename std::map<track_t,IRateTrack<_IR_String>*>::iterator it;
	std::list<IRateTrack<_IR_String>*> l;
	for(it=this->_mcache.begin();it!=this->_mcache.end();++it){
		l.push_front((*it).second);
	}
	return l;
}
#endif
