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
 //This allow include two time one for wchar_t and one for char
#ifdef _CPPIR_USE_CHAR
	#ifndef _CHAR_IRATETRACK_H
		#undef IRATETRACK_H
		#define _CHAR_IRATETRACK_H
	#endif //_CHAR_IRATETRACK_H
		
#elif defined(_CPPIR_USE_WCHAR_T)
	#ifndef WCHAR_IRATETRACK_H
		#undef IRATETRACK_H
		#define WCHAR_IRATETRACK_H
	#endif
#else
	#define IRATETRACK_H
	#error You must define en char type before including iratetrack.h, define _CPPIR_USE_CHAR for char or\
	_CPPIR_USE_WCHAR_T for wchar_t
#endif

#ifndef IRATETRACK_H
#define IRATETRACK_H

#include <iratedef.h>
//#include <baseiratetrack.h>
#include <map>
#include <c_irate.h>
#include <libirateinterface.h>
/**
@author Matthias Studer
*/
#ifdef _CPPIR_USE_CHAR
	#define _CPPIR_MODE 
#elif defined(_CPPIR_USE_WCHAR_T)
	#define _CPPIR_MODE L
#endif
class BaseIRateTrack {
	public:
		BaseIRateTrack(track_t t):_mt(t) {}
		virtual ~BaseIRateTrack(){}
		virtual track_t getTrackHanlde() {return this->_mt;}
		virtual bool isRated();
		virtual float getRating();
		virtual int getNoOfTimesPlayed();
		virtual void setVolume(int v);
		virtual int getVolume();
		virtual void unSetVolume();
		virtual bool isBroken();
		virtual bool isMissing();
		virtual bool isDeleted();
		virtual bool isActive();
		virtual bool isNotDownloaded();
		virtual bool isHidden();
		virtual bool isPendingPurge();
		virtual bool isOnPlayList();
		virtual int getProbability();
		virtual bool exists ();
		virtual long getPlayingTime();
		virtual int getDownloadAttempts();
		//virtual void update()=0;
		//virtual string_jt getTrackProperty(char *key);
	protected:
		track_t _mt;
};

template<typename _IR_String> class IRateTrack
	:public BaseIRateTrack{
	public:
		IRateTrack(track_t t):BaseIRateTrack(t){}

		virtual ~IRateTrack() {}
		virtual _IR_String getName(){
			if(isProperty(_CPPIR_MODE"name"))return property(_CPPIR_MODE"name");
			setProperty(_CPPIR_MODE"name",decodeString(ir_getName(this->_mt)));
			return property(_CPPIR_MODE"name");
		}
		virtual _IR_String getLastPlayed(){
			if(isProperty(_CPPIR_MODE"lastplayed"))return property(_CPPIR_MODE"lastplayed");
			setProperty(_CPPIR_MODE"lastplayed",decodeString(ir_getLastPlayed(this->_mt)));
			return property(_CPPIR_MODE"lastplayed");
		}
		virtual _IR_String getArtist (){
			if(isProperty(_CPPIR_MODE"artist"))return property(_CPPIR_MODE"artist");
			setProperty(_CPPIR_MODE"artist",decodeString(ir_getArtist(this->_mt)));
			return property(_CPPIR_MODE"artist");
		}
		virtual _IR_String getTitle (){
			if(isProperty(_CPPIR_MODE"title"))return property(_CPPIR_MODE"title");
			setProperty(_CPPIR_MODE"title",decodeString(ir_getTitle(this->_mt)));
			return property(_CPPIR_MODE"title");
		}
		virtual _IR_String getURL (){
			if(isProperty(_CPPIR_MODE"url"))return property(_CPPIR_MODE"url");
			setProperty(_CPPIR_MODE"url",decodeString(ir_getURL(this->_mt)));
			return property(_CPPIR_MODE"url");
		}
		virtual _IR_String getKey (){
			if(isProperty(_CPPIR_MODE"key"))return property(_CPPIR_MODE"key");
			setProperty(_CPPIR_MODE"key",decodeString(ir_getKey(this->_mt)));
			return property(_CPPIR_MODE"key");
		}
		virtual _IR_String getFile (){
			if(isProperty(_CPPIR_MODE"file"))return property(_CPPIR_MODE"file");
			setProperty(_CPPIR_MODE"file",decodeString(ir_getFile(this->_mt)));
			return property(_CPPIR_MODE"file");
		}
		virtual _IR_String getState(){
			return decodeString(ir_getState(this->_mt));
		}

		virtual _IR_String getWebSite (){
			if(isProperty(_CPPIR_MODE"www"))return property(_CPPIR_MODE"www");
			setProperty(_CPPIR_MODE"www",decodeString(ir_getWebSite(this->_mt)));
			return property(_CPPIR_MODE"www");
		}
		virtual _IR_String getLicense (){
			if(isProperty(_CPPIR_MODE"license"))return property(_CPPIR_MODE"license");
			setProperty(_CPPIR_MODE"license",decodeString(ir_getLicense(this->_mt)));
			return property(_CPPIR_MODE"license");
		}
		virtual _IR_String getAlbum (){
			if(isProperty(_CPPIR_MODE"album"))return property(_CPPIR_MODE"album");
			setProperty(_CPPIR_MODE"album",decodeString(ir_getAlbum(this->_mt)));
			return property(_CPPIR_MODE"album");
		}

		virtual _IR_String getComment (){
			if(isProperty(_CPPIR_MODE"comment"))return property(_CPPIR_MODE"comment");
			setProperty(_CPPIR_MODE"comment",decodeString(ir_getComment(this->_mt)));
			return property(_CPPIR_MODE"comment");
		}
		virtual _IR_String getCopyrightInfo(){
			if(isProperty(_CPPIR_MODE"copyrightinfo"))return property(_CPPIR_MODE"copyrightinfo");
			setProperty(_CPPIR_MODE"copyrightinfo",decodeString(ir_getCopyrightInfo(this->_mt)));
			return property(_CPPIR_MODE"copyrightinfo");
		}
		virtual _IR_String getGenre (){
			if(isProperty(_CPPIR_MODE"genre"))return property(_CPPIR_MODE"genre");
			setProperty(_CPPIR_MODE"genre",decodeString(ir_getGenre(this->_mt)));
			return property(_CPPIR_MODE"genre");
		}
		virtual _IR_String getPlayingTimeString (){
			if(isProperty(_CPPIR_MODE"playtime"))return property(_CPPIR_MODE"playtime");
			setProperty(_CPPIR_MODE"playtime",decodeString(ir_getPlayingTimeString(this->_mt)));
			return property(_CPPIR_MODE"playtime");
		}
		virtual _IR_String getYear (){
			if(isProperty(_CPPIR_MODE"playtime"))return property(_CPPIR_MODE"playtime");
			setProperty(_CPPIR_MODE"playtime",decodeString(ir_getYear(this->_mt)));
			return property(_CPPIR_MODE"playtime");
		}
//		virtual bool isNullOrEmpty(const _IR_String& str)=0;
		virtual inline bool isProperty(const _IR_String& key){
			return _mcache.find(key)!=_mcache.end();
		}
		virtual inline _IR_String property(const _IR_String& key,const _IR_String& def=_CPPIR_MODE""){
			if(isProperty(key)){
				return _mcache[key];
			}return def;
		}
		virtual inline void setProperty(const _IR_String& key, const _IR_String& value){
			if(isNullOrEmpty(value))return;
			_mcache[key]=value;
		}
		virtual inline void clearProperty(const _IR_String& key){
			_mcache.erase(key);
		}
		virtual inline void clearCache(){
			this->_mcache.clear();
		}
		virtual void update(){
			this->clearProperty(_CPPIR_MODE"lastplayed");
		}
		virtual inline void setTrackPlayed(){
			iratelib::setTrackPlayed(this->_mt);
		}
		virtual inline void setRating(const int& rating){
			iratelib::setRating(this->_mt,rating);
		}
		static void initFunc(bool(*nullempty)(const _IR_String&),_IR_String(*decoder)(string_jt));
	protected:
		static _IR_String(*decodeString)(string_jt);
		static bool(*isNullOrEmpty)(const _IR_String&);
		std::map<_IR_String,_IR_String> _mcache;
		//track_t _mt;

};
template <typename _IR_String> _IR_String(*IRateTrack<_IR_String>::decodeString)(string_jt)=NULL;
template <typename _IR_String> bool(*IRateTrack<_IR_String>::isNullOrEmpty)(const _IR_String&)=NULL;
template <typename _IR_String>void IRateTrack<_IR_String>::initFunc(bool(*nullempty)(const _IR_String&),_IR_String(*decoder)(string_jt)){
	decodeString=decoder;
	isNullOrEmpty=nullempty;
}
#endif
