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
	#error You must define a char type before including iratetrack.h, define _CPPIR_USE_CHAR for char or\
	_CPPIR_USE_WCHAR_T for wchar_t
#endif

#ifndef IRATETRACK_H
#define IRATETRACK_H

#include <iratedef.h>
//#include <baseiratetrack.h>
#include <map>
#include <list>
#include <libirateinterface.h>
/**
@author Matthias Studer
*/
#ifdef _CPPIR_USE_CHAR
	#define _CPPIR_MODE 
#elif defined(_CPPIR_USE_WCHAR_T)
	#define _CPPIR_MODE L
#endif
/** This is a base class provided if you want to build your own IRate wrapper
* public methods are all methods that doesn't use the string 
*
*/
class BaseIRateTrack {
	public:
		BaseIRateTrack(track_t t):_mt(t) {}
		virtual ~BaseIRateTrack(){}
		/**Get the track handle associated with these class
		* Normally you shouldn't use it
		* @return track handle
		*/
		track_t getTrackHanlde() {return this->_mt;}
		/**To know if a track is rated
		* @return true if the track is rated
		*/
		bool isRated();
		/**Return the rating of the track as float, if the track isn't rated return is unknow
		* @return rating associated with this track
		*/
		float getRating();
		/**return the number of time this track has been played
		* @return int
		*/
		int getNoOfTimesPlayed();
		/**Set the volume attribute of this track
		* @param v new volume
		*/
		void setVolume(int v);
		/**Get the volume associated with this track usually you should change the volume according to this
		* @return volume
		*/
		int getVolume();
		/**Set the volume to default
		*/
		void unSetVolume();
		/**Test if track is broken (broken download)
		* @return true if broken
		*/
		bool isBroken();
		/**Test if track is missing (not downloaded and it should have been)
		* @return true if missing
		*/
		bool isMissing();
		/**True if the track has been deleted
		* @return bool
		*/
		bool isDeleted();
		
		bool isActive();
		/**True if the track hasn't been downloaded
		* @return 
		*/
		bool isNotDownloaded();
		/**True if track is hidden (broken or rated 0)
		* @return 
		*/
		bool isHidden();
		/**True if this track will be erased at the end of this session (when we will call quit)
		* @return 
		*/
		bool isPendingPurge();
		/**True if the track is on the internal playlist
		* @return 
		*/
		bool isOnPlayList();
		/**Return a probability number associated with this track. This is not exaclty a probability but a number that allow to know a probability (getProbability/total)
		* @return 
		*/
		int getProbability();
		/**true if the file exists
		* @return 
		*/
		bool exists ();
		/**Return ID3 playing time
		* @return 
		*/
		long getPlayingTime();
		/**Return the number of attempts to download the track
		* @return 
		*/
		int getDownloadAttempts();
	protected:
		/**Internal, not for external use*/
		string_jt _getName();
		/**Internal, not for external use*/
		string_jt _getLastPlayed();
		/**Internal, not for external use*/
		string_jt _getArtist ();
		/**Internal, not for external use*/
		string_jt _getTitle ();
		/**Internal, not for external use*/
		string_jt _getURL ();
		/**Internal, not for external use*/
		string_jt _getKey ();
		/**Internal, not for external use*/
		string_jt _getFile ();
		/**Internal, not for external use*/
		string_jt _getState ();
		/**Internal, not for external use*/
		string_jt _getWebSite ();
		/**Internal, not for external use*/
		string_jt _getLicense ();
		/**Internal, not for external use*/
		string_jt _getAlbum ();
		/**Internal, not for external use*/
		string_jt _getComment ();
		/**Internal, not for external use*/
		string_jt _getCopyrightInfo();
		/**Internal, not for external use*/
		string_jt _getGenre ();
		/**Internal, not for external use*/
		string_jt _getPlayingTimeString ();
		/**Internal, not for external use*/
		string_jt _getYear ();

		//void update()=0;
		//string_jt getTrackProperty(char *key);
	protected:
		track_t _mt;
};
/**This template class provide a fully usable system in conjonction with IRateCenter.
* String transformation are cached in order to not overload the system with string conversion
*@see IRateCenter
*/
template<typename _IR_String> class IRateTrack
	:public BaseIRateTrack{
	public:
		typedef typename std::list<_IR_String> PropertyList;
		IRateTrack(track_t t):BaseIRateTrack(t){}

		~IRateTrack() {}
		/**Return a formatted name
		* @return _IR_String
		*/
		_IR_String getName(){
			if(isProperty(_CPPIR_MODE"name"))return property(_CPPIR_MODE"name");
			setProperty(_CPPIR_MODE"name",decodeString(this->_getName()));
			return property(_CPPIR_MODE"name");
		}
		/**A formatted date to show the last time we play this track
		* @return _IR_String
		*/
		_IR_String getLastPlayed(){
			if(isProperty(_CPPIR_MODE"lastplayed"))return property(_CPPIR_MODE"lastplayed");
			setProperty(_CPPIR_MODE"lastplayed",decodeString(this->_getLastPlayed()));
			return property(_CPPIR_MODE"lastplayed");
		}
		/**The artist of this track. It may differ from ID3 tag and is often better (a comparison is made between the both).
		* @return _IR_String
		*/
		_IR_String getArtist (){
			if(isProperty(_CPPIR_MODE"artist"))return property(_CPPIR_MODE"artist");
			setProperty(_CPPIR_MODE"artist",decodeString(this->_getArtist()));
			return property(_CPPIR_MODE"artist");
		}
		/**Return the title of this track. It may differ from ID3 tag and is often better (a comparison is made between the both).
		* @return _IR_String
		*/
		_IR_String getTitle (){
			if(isProperty(_CPPIR_MODE"title"))return property(_CPPIR_MODE"title");
			setProperty(_CPPIR_MODE"title",decodeString(this->_getTitle()));
			return property(_CPPIR_MODE"title");
		}
		/**Get the url from where we are downloading the track
		* @return _IR_String
		*/
		_IR_String getURL (){
			if(isProperty(_CPPIR_MODE"url"))return property(_CPPIR_MODE"url");
			setProperty(_CPPIR_MODE"url",decodeString(this->_getURL()));
			return property(_CPPIR_MODE"url");
		}
		/**Return an unique string for this track
		* @return _IR_String
		*/
		_IR_String getKey (){
			if(isProperty(_CPPIR_MODE"key"))return property(_CPPIR_MODE"key");
			setProperty(_CPPIR_MODE"key",decodeString(this->_getKey()));
			return property(_CPPIR_MODE"key");
		}
		/**Return the filename of the track
		* @return _IR_String
		*/
		_IR_String getFile (){
			if(isProperty(_CPPIR_MODE"file"))return property(_CPPIR_MODE"file");
			setProperty(_CPPIR_MODE"file",decodeString(this->_getFile()));
			return property(_CPPIR_MODE"file");
		}
		/**Return a string showing the state of the track this can change (downloaded percent, broken,rating)
		* @return _IR_String
		*/
		_IR_String getState(){
			return decodeString(this->_getState());
		}
		/**Return the website associated with this track often this is empty
		* @return _IR_String
		*/
		_IR_String getWebSite (){
			if(isProperty(_CPPIR_MODE"www"))return property(_CPPIR_MODE"www");
			setProperty(_CPPIR_MODE"www",decodeString(this->_getWebSite()));
			return property(_CPPIR_MODE"www");
		}
		/**Return the license of this track
		* @return _IR_String
		*/
		_IR_String getLicense (){
			if(isProperty(_CPPIR_MODE"license"))return property(_CPPIR_MODE"license");
			setProperty(_CPPIR_MODE"license",decodeString(this->_getLicense()));
			return property(_CPPIR_MODE"license");
		}
		/**Return album ID3 attribute of the track
		* @return _IR_String
		*/
		_IR_String getAlbum (){
			if(isProperty(_CPPIR_MODE"album"))return property(_CPPIR_MODE"album");
			setProperty(_CPPIR_MODE"album",decodeString(this->_getAlbum()));
			return property(_CPPIR_MODE"album");
		}
		/**return comment ID3 attribute of the track 
		* @return _IR_String
		*/
		_IR_String getComment (){
			if(isProperty(_CPPIR_MODE"comment"))return property(_CPPIR_MODE"comment");
			setProperty(_CPPIR_MODE"comment",decodeString(this->_getComment()));
			return property(_CPPIR_MODE"comment");
		}
		/**Return copyright information for this track 
		* @return _IR_String
		*/
		_IR_String getCopyrightInfo(){
			if(isProperty(_CPPIR_MODE"copyrightinfo"))return property(_CPPIR_MODE"copyrightinfo");
			setProperty(_CPPIR_MODE"copyrightinfo",decodeString(this->_getCopyrightInfo()));
			return property(_CPPIR_MODE"copyrightinfo");
		}
		/**get Genre ID3 attribute for this track 
		* @return _IR_String
		*/
		_IR_String getGenre (){
			if(isProperty(_CPPIR_MODE"genre"))return property(_CPPIR_MODE"genre");
			setProperty(_CPPIR_MODE"genre",decodeString(this->_getGenre()));
			return property(_CPPIR_MODE"genre");
		}
		/**Return ID3 attribute about playing time
		* @return _IR_String
		*/
		_IR_String getPlayingTimeString (){
			if(isProperty(_CPPIR_MODE"playtime"))return property(_CPPIR_MODE"playtime");
			setProperty(_CPPIR_MODE"playtime",decodeString(this->_getPlayingTimeString()));
			return property(_CPPIR_MODE"playtime");
		}
		/**return ID3 year attribute
		* @return _IR_String
		*/
		_IR_String getYear (){
			if(isProperty(_CPPIR_MODE"year"))return property(_CPPIR_MODE"year");
			setProperty(_CPPIR_MODE"year",decodeString(this->_getYear()));
			return property(_CPPIR_MODE"year");
		}
		/**Return a list of all cached properties (name)
		* @return PropertyList is list\<_IR_String\>
		*/
		PropertyList properties(){
			PropertyList proplist;
			typename std::map<_IR_String,_IR_String>::iterator it;
			for(it=_mcache.begin();it!=_mcache.end();++it){
				proplist.push_back((*it).first);
			}
			return proplist;
		}
		/**Test if a property is in cache
		* @param key property name
		* @return true if the property is in cache and the associated value isn't empty or null
		*/
		inline bool isProperty(const _IR_String& key){
			return _mcache.find(key)!=_mcache.end();
		}
		/**Get a property from the cache
		* @param key property key
		* @param def return value if key not in cache
		* @return the property
		*/
		inline _IR_String property(const _IR_String& key,const _IR_String& def=_CPPIR_MODE""){
			if(isProperty(key)){
				return _mcache[key];
			}return def;
		}
		/**Set the value of property key if value isn't null or empty
		* you can safely use this to add your own property like<br>
		* <code>
		* mytrack->setProperty("formattedname","["+mytrack->getArtist()+"] - "+mytrack->getTitle());<br>
		*</code>
		* @param key property name
		* @param value value associated with this key
		*/
		inline void setProperty(const _IR_String& key, const _IR_String& value){
			if(isNullOrEmpty(value))return;
			_mcache[key]=value;
		}
		/**Remove a property from the cache, this as two differents effects
		* if the key is used by IRate it will check directly the value in IRate and put it in cache again
		* @param key property name
		* @return true if the property is in cache and the associated value isn't empty or null
		*/
		inline void clearProperty(const _IR_String& key){
			_mcache.erase(key);
		}
		/**Erase all value in cache
		*/
		inline void clearCache(){
			this->_mcache.clear();
		}
		/**Put all value from IRate in cache. However null or empty value will not be in cache
		*/
		void cacheAll();
		/**Update the track actually just remove value that might change from cache
		*/
		void update(){
			this->clearProperty(_CPPIR_MODE"lastplayed");
		}
		/**Set the track as played (this will change some info like number of time played and last played date
		*/
		inline void setTrackPlayed(){
			iratelib::setTrackPlayed(this->_mt);
		}
		/**Set the rating associated with this track <br>
		* "This Sux"=\>0<br>
		* "Yawn"=\>2<br>
		* "Not Bad"=\>5<br>
		* "Cool" =\>7<br>
		* "Love it"=\>10<br>
		*/
		inline void setRating(const int& rating){
			iratelib::setRating(this->_mt,rating);
		}
		/**This function is called by IRateCenter to initialize decoding functions
		* It's not really intended to be used by you
		*/
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
template <typename _IR_String> void IRateTrack<_IR_String>::cacheAll(){
	setProperty(_CPPIR_MODE"name",decodeString(this->_getName()));
	setProperty(_CPPIR_MODE"lastplayed",decodeString(this->_getLastPlayed()));
	setProperty(_CPPIR_MODE"artist",decodeString(this->_getArtist()));
	setProperty(_CPPIR_MODE"title",decodeString(this->_getTitle()));
	setProperty(_CPPIR_MODE"url",decodeString(this->_getURL()));
	setProperty(_CPPIR_MODE"key",decodeString(this->_getKey()));
	setProperty(_CPPIR_MODE"file",decodeString(this->_getFile()));
	setProperty(_CPPIR_MODE"www",decodeString(this->_getWebSite()));
	setProperty(_CPPIR_MODE"license",decodeString(this->_getLicense()));
	setProperty(_CPPIR_MODE"album",decodeString(this->_getAlbum()));
	setProperty(_CPPIR_MODE"comment",decodeString(this->_getComment()));
	setProperty(_CPPIR_MODE"copyrightinfo",decodeString(this->_getCopyrightInfo()));
	setProperty(_CPPIR_MODE"genre",decodeString(this->_getGenre()));
	setProperty(_CPPIR_MODE"playtime",decodeString(this->_getPlayingTimeString()));
	setProperty(_CPPIR_MODE"playtime",decodeString(this->_getYear()));
}
#endif
