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
#include "template.h"
#include <qregexp.h>
#include <kglobal.h>
#include <klocale.h>
#include <kstandarddirs.h>
#include <kmacroexpander.h>
#include <qdir.h>
#include <kdebug.h>
#include <qt_irate.h>
#include <kiconloader.h>
Template * Template::m_instance= new Template();
Template::Template()
{
	/*this->keywords["LOC_"]=i18n("");
	this->keywords["LOC_"]=i18n("");
	this->keywords["LOC_"]=i18n("");*/
	
}
void Template::init(const QString& tmplDir,const QString& iconDir){
	this->keywords["IR_ICON_DIR"]=iconDir;
	//KGlobal::instance()->dirs()->findResourceDir("data","noatun/pics/irate.png");
	this->keywords["IR_ICON"]=iconDir+"irate.png";
	this->keywords["IR_BIG_ICON"]=iconDir+"irateicon.png";
	this->keywords["10rated"]=iconDir+"rate10.png";
	this->keywords["7rated"]=iconDir+"rate7.png";
	this->keywords["5rated"]=iconDir+"rate5.png";
	this->keywords["2rated"]=iconDir+"rate2.png";
	this->keywords["0rated"]=iconDir+"rate0.png";
	this->keywords["unrated"]=iconDir+"unrated.png";
	this->keywords["IR_TEMPLATE_DIR"]=tmplDir;
	this->keywords["IR_NEXT_BUTTON"]="<a href=\"next\"><img src=\""+KGlobal::iconLoader()->iconPath("player_fwd",KIcon::Small)+"\"></a>";
	this->keywords["IR_BACK_BUTTON"]="<a href=\"back\"><img src=\""+KGlobal::iconLoader()->iconPath("player_rew",KIcon::Small)+"\"></a>";
	this->keywords["IR_INFO_BUTTON"]="<a href=\"info\"><img src=\""+KGlobal::iconLoader()->iconPath("info",KIcon::Small)+"\"></a>";
	//iconDir=KGlobal::dirs()->findResourceDir("data","noatun/pics/irate.png");
	this->keywords["LOC_SONG_PART"]=i18n("Song information");
	this->keywords["LOC_FILE_PART"]=i18n("File information");
	this->keywords["LOC_TITLE"]=i18n("Title");
	this->keywords["LOC_ARTIST"]=i18n("Artist");
	this->keywords["LOC_BITRATE"]=i18n("Bitrate");
	this->keywords["LOC_LENGTH"]=i18n("Time");
	this->keywords["LOC_RATING"]=i18n("Rating");
	this->keywords["LOC_LAST"]=i18n("Last played");
	this->keywords["LOC_FILE"]=i18n("File");
	this->keywords["LOC_URL"]=i18n("File url");
	this->keywords["LOC_COMMENT"]=i18n("Comment");
	this->keywords["LOC_GENRE"]=i18n("Genre");
	this->keywords["LOC_DATE"]=i18n("Date");
	this->keywords["LOC_COPYRIGHT"]=i18n("Copyright");
	this->keywords["LOC_PLAYED"]=i18n("Played");
	this->keywords["LOC_SAMPLERATE"]=i18n("Sample rate in kHz");
	this->keywords["LOC_CHANNELS"]=i18n("Channels");
	this->templateDir=tmplDir;

}
void Template::loadOSDTemplate(const QString &file){
	QFile f(this->templateDir+file);
	//kdDebug()<<(this->templateDir+file)<<" loading "<<f.exists()<<endl;
	if(f.exists()){
		f.open(IO_ReadOnly);
		this->osdTmpl=KMacroExpander::expandMacros(f.readAll(),this->keywords,'%');
		f.close();
		this->osdTmplFile=file;
	}
	else if(this->osdTmpl.isEmpty()){
		QFile def(locate("html","en/kirateradioxmms/default.osd"));
		this->osdTmplFile="default.osd";
		if(!def.exists())return;
		def.open(IO_ReadOnly);
		this->osdTmpl=KMacroExpander::expandMacros(def.readAll(),this->keywords,'%');
		def.close();
	}
	
}
void Template::loadInfoTemplate(const QString &file){
	QFile f(this->templateDir+file);
	//kdDebug()<<(this->templateDir+file)<<" loading "<<f.exists()<<endl;
	if(f.exists()){
		f.open(IO_ReadOnly);
		this->infoTmpl=KMacroExpander::expandMacros(f.readAll(),this->keywords,'%');
		f.close();
		this->infoTmplFile=file;
	}else if(this->infoTmpl.isEmpty()){
		QFile def(locate("html","en/kirateradioxmms/default.tmpl"));
		this->infoTmplFile="default.tmpl";
		if(!def.exists())return;
		def.open(IO_ReadOnly);
		this->infoTmpl=KMacroExpander::expandMacros(def.readAll(),this->keywords,'%');
		def.close();
	}
}
QStringList Template::getAvailableInfoTemplate(){
	QDir dir(this->templateDir);
	return dir.entryList("*.tmpl");
}
QStringList Template::getAvailableOSDTemplate(){
	QDir dir(this->templateDir);
	if(!dir.exists())
	{
		kdDebug()<<"Dir not exists "<<dir.path()<<endl;
	}
	//else kdDebug()<<dir.entryList()<<endl;
	return dir.entryList("*.osd");
}
Template::~Template()
{
}
Template * Template::instance(){
	return m_instance;
}
QString Template::decodeText(const QString &text, SafeListViewItem* i){
	static QString unknow=i18n("Unknow");
	QTrack* ti=i->getTrack();
	ti->cacheAll();
	//Maybe this can be faster
	QString ret=text;
	ret=ret.replace("%SONG_RATE_ICON",this->keywords[ti->property("rateid","unrated")]).replace("%SONG_RATE_STRING",i->getStringRating()).replace("%SONG_LENGTH",i->getTrack()->getPlayingTimeString());
	static QRegExp rx("\\%SONG_([A-Z]+)");
	//int count = 0;
	int pos = 0;
	while((pos = rx.search(ret, pos)) != -1){
		//count++;
		ret.replace(pos,rx.matchedLength(),ti->property(rx.cap(1).lower(),unknow));
		//pos+=rx.matchedLength();
	}
	return ret;

/*	QMap<QString,QString> song;
	
	QStringList keys=ti->properties();
	
	QStringList::iterator it=keys.begin();
	while(it!=keys.end()){
		song["SONG_"+(*it).upper()]=ti->property((*it),unknow);
		++it;
	}
	song["SONG_RATE_ICON"]=this->keywords[ti->property("rateid","unrated")];
	song["SONG_RATE_STRING"]=i->getStringRating();
	return KMacroExpander::expandMacros(text,song,'%');
	*/
}




