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
#include "safelistviewitem.h"
//#include "downloadcenter.h"
#include <klocale.h>
#include "songlist.h"
#include <kurl.h>
//#include <noatun/player.h>
//#include <noatun/app.h>
#include <qfile.h>
#include <kdebug.h>
#include <kiconloader.h>
#include <kglobal.h>
#include "view.h"
QMap<QString,QPixmap> *SafeListViewItem::IconMessage=NULL;
QMap<QString,QString> SafeListViewItem::RateMessage;
void SafeListViewItem::initMessage() {
	KIconLoader *kil=KGlobal::instance()->iconLoader();
	IconMessage = new QMap<QString,QPixmap>();
	IconMessage->insert("0rated",kil->loadIcon("edittrash",KIcon::Small,0,0,false));
	IconMessage->insert("2rated",kil->loadIcon("rate2",KIcon::User,0,0,false));
	IconMessage->insert("5rated",kil->loadIcon("rate5",KIcon::User,0,0,false));
	IconMessage->insert("7rated",kil->loadIcon("rate7",KIcon::User,0,0,false));
	IconMessage->insert("10rated",kil->loadIcon("rate10",KIcon::User,0,0,false));
	IconMessage->insert("unrated",kil->loadIcon("unrated",KIcon::User,0,0,false));
	IconMessage->insert("ccicon",kil->loadIcon("cc",KIcon::User,0,0,false));
	RateMessage.insert("0rated",i18n("This Sux"));
	RateMessage.insert("2rated",i18n("Yawn"));
	RateMessage.insert("5rated",i18n("Not Bad"));
	RateMessage.insert("7rated",i18n("Cool"));
	RateMessage.insert("10rated",i18n("Love it"));
	RateMessage.insert("unrated",i18n("Unrated"));

}
SafeListViewItem::SafeListViewItem(QListView *parent, QTrack* mtrack)
		: KListViewItem(parent), track(mtrack), removed(false) {
//	addRef();
	
	//Check if download needed
	//This has been checked before <We can safely ignore this check now
	//track->property("state",QString::null).isEmpty()&&(track->property("deleted")=="false"||!track->isProperty("deleted"))&&
	//if file field is empty we assume it hasn't been downloaded, to conform to orgininal iRATE
	if(track->isNotDownloaded()/*||!QFile::exists(track->getFile())*/) {
		KURL url(this->track->getURL());
		this->setEnabled(false);
		//this->setProperty("enabled","false");
		this->setText(IR_DOWNLOAD_COL,i18n("Queued"));
	} else if(track->exists()) {
		
		//This isn't the best way to do it. But when we have unrecognised unicode chars it doesn't find 
		//the file so we must recheck it
		//Seems like to much have to found another way
		/*if(!QFile::exists(track->getFile())){
			KURL url(this->track->getURL());
			QString localfilename=irateDir+"download/"+url.fileName(true);
			if(QFile::exists(localfilename)){
				track->setProperty("file",localfilename);
			}
		}*/
		KURL tmpURL(track->getFile());
		this->mUrl=tmpURL.url();
		
		//setOn(true);
		this->setEnabled(true);
		//this->setProperty("enabled","true");
		//this->setUrl(this->track->getFile());
	}
	int rate=(int)this->track->getRating();
	switch(rate) {
	case 0:
		this->setProperty("rateid","0rated",false);
		break;
	case 2:
		this->setProperty("rateid","2rated",false);
		break;
	case 5:
		this->setProperty("rateid","5rated",false);
		break;
	case 7:
		this->setProperty("rateid","7rated",false);
		break;
	case 10:
		this->setProperty("rateid","10rated",false);
		break;
	default:
		this->setProperty("rateid","unrated",false);

	}
	this->setProperty("played",QString::number(this->track->getNoOfTimesPlayed()));
	//printf("added\n");
	//this->updateProbs();

	//printf("Quitting\n");
}
QString SafeListViewItem::property(const QString &key, const QString & def) const {
	//Ugly Hack here
	if(key=="url") {
		return this->mUrl;
	}
	//kdDebug()<<"Property called "<<key<<" def="<<def<<" value "<<this->track->property(key,def)<<endl;
	if(!track)return def;
	return this->track->property(key,def);
}
void SafeListViewItem::setProperty(const QString &key, const QString &value,const bool& notify) {
	//Ugly Hack here
	if(key=="url") this->mUrl=value;
	//Update title only if we don't have one (often tags have less info) 
	//and IRate already look if tag is better
	else if(key=="title")return;
	else if(!track)return;
	else this->track->setProperty(key,value);
	if(notify)this->modified(key);
}
void SafeListViewItem::clearProperty(const QString &key) {
	//Ugly Hack here
	//kdDebug()<<"Property cleared "<<key<<endl;
	if(key=="url")this->mUrl="";
	else if(key=="title")return;
	else if(!track) return;
	else this->track->setProperty(key,"");
	this->modified(key);
}
QStringList SafeListViewItem::properties() const {
	if(!track) return QStringList("url");
	return QStringList(this->track->properties());
}
bool SafeListViewItem::isProperty(const QString &key) const {
	//kdDebug()<<"Property test "<<key<<endl;
	if(!track)return true;
	return this->track->isProperty(key);
}

SafeListViewItem::~SafeListViewItem() {
	if(this->removed) {
		kdDebug()<<"Item destroyed "<<endl;
	} else remove();
}
bool SafeListViewItem::operator==(const  SafeListViewItem &d)const {
	QString f1=this->property("file","");
	QString f2=d.property("file","");
	if(!f1.isEmpty())return f1==f2;
	else if(!f2.isEmpty()) {
		return false;
	} else {
		return this->track->getKey()==d.property("key");
	}
}

int SafeListViewItem::compare(QListViewItem * i, int col, bool) const {
	SafeListViewItem *j=static_cast<SafeListViewItem*>(i);
	switch(col) {
	case IR_RATING_COL:
		return (int) (j->track->getRating()-this->track->getRating());

	case IR_PLAYED_COL:
		return j->track->getNoOfTimesPlayed()-this->track->getNoOfTimesPlayed();

	case IR_TIME_COL:
		return j->track->getPlayingTime()-this->track->getPlayingTime();


	case IR_LAST_COL:
		return this->property("lastplayed","0").localeAwareCompare(j->property("lastplayed","0"));

	case IR_COPYRIGHT_COL:
		return this->track->getCopyrightInfo().localeAwareCompare(j->track->getCopyrightInfo());

	default:
		return text(col).localeAwareCompare(i->text(col));
	}
}

void SafeListViewItem::reDownload() {
	KURL url(this->track->getURL());
	QFile::remove(this->track->getFile());
	//this->track->clearProperty("file");
	this->setText(IR_DOWNLOAD_COL,i18n("Queued"));
	this->setEnabled(false);
//	DownloadCenter::instance()->enqueue(this, url, localfilename);
}
/*void SafeListViewItem::setBroken() {
	this->track->setProperty("state","broken");
	this->track->setProperty("file","");
	this->remove();
	this->removeRef();
}*/

void SafeListViewItem::downloadInfoMessage(const QString &) {
	//Before this was disturbing display but now (since column width are Manualy set) it should disturb 
	//anymore
	//this->setText(IR_DOWNLOAD_COL,msg);
	//this->modified();
}
void SafeListViewItem::downloaded(const QString& percent) {
	this->setProperty("percdone",percent,false);
	setText(IR_DOWNLOAD_COL, i18n("%1% downloaded").arg(percent));
	//this->modified();
}
/*void SafeListViewItem::setDownloadMessage(const QString& msg){
	this->setText(IR_DOWNLOAD_COL,msg);
	this->modified();
}*/

void SafeListViewItem::downloadFinished(const QString& msg) {
	//this->track->setProperty("file",this->file());
	if(!track) {
		kdDebug()<<msg<<" track deleted";
		return;
	}
	KURL tmpURL(track->getFile());
	printf("LocalFilename in finished is %s\n",track->getFile().latin1());
	this->mUrl=tmpURL.url();
	this->setEnabled(true);
	this->setText(IR_DOWNLOAD_COL,msg);
	//this->setDownloadMessage(msg);
	//this->modified();

	//setText(2, "Unrated");
}

void SafeListViewItem::modified(const int& col) {
	if(!track) return;
	if(col==IR_ARTIST_COL||col==-1) {
		this->setText(IR_ARTIST_COL,this->track->getArtist());
	}
	if (col==IR_TITLE_COL||col==-1) {
		setText(IR_TITLE_COL, this->track->getTitle());
	}
	if (col==IR_TIME_COL||col==-1) {
		setText(IR_TIME_COL, this->track->getPlayingTimeString());
	}
	if(col==IR_RATING_COL||col==-1) {
		this->setPixmap(IR_RATING_COL,(*IconMessage)[this->track->property("rateid","unrated")]);
	}
	if(col==IR_PLAYED_COL||col==-1)
		this->setText(IR_PLAYED_COL,QString::number(this->track->getNoOfTimesPlayed()));
	if(col==IR_LAST_COL||col==-1)
		this->setText(IR_LAST_COL,this->track->getLastPlayed());
	//this->setText(6,QString::number(this->getProbs()));
}
void SafeListViewItem::modified(const QString& key) {
	if(!track) return;
	if(key=="artist") {
		this->setText(IR_ARTIST_COL,this->track->getArtist());
	}
	if(key=="title") {
		setText(IR_TITLE_COL, this->track->getTitle());
	}
	if(key=="length") {
		setText(IR_TIME_COL, this->track->getPlayingTimeString());
	}
	if(key=="rating") {
		this->setPixmap(IR_RATING_COL,(*IconMessage)[this->track->property("rateid","unrated")]);
	}
	if(key=="played")
		this->setText(IR_PLAYED_COL,QString::number(this->track->getNoOfTimesPlayed()));
	if(key=="lastplayed")
		this->setText(IR_LAST_COL,this->track->getLastPlayed());
	//this->setText(6,QString::number(this->getProbs()));
}

void SafeListViewItem::updateDisplay() {
	if(!track) return;
	switch((int)this->track->getRating()) {
	case 0:
		this->setProperty("rateid","0rated",false);
		break;
	case 2:
		this->setProperty("rateid","2rated",false);
		break;
	case 5:
		this->setProperty("rateid","5rated",false);
		break;
	case 7:
		this->setProperty("rateid","7rated",false);
		break;
	case 10:
		this->setProperty("rateid","10rated",false);
		break;

	}
	this->track->setProperty("played",QString::number(this->track->getNoOfTimesPlayed()));
	this->setProperty("rating",QString::number(this->track->getRating()),false);
	this->setText(IR_ARTIST_COL,this->track->getArtist());
	setText(IR_TITLE_COL, this->track->getTitle());
	setText(IR_TIME_COL, this->track->getPlayingTimeString());
	this->setPixmap(IR_RATING_COL,(*IconMessage)[this->track->property("rateid","unrated")]);
	this->setText(IR_PLAYED_COL,this->track->property("played","0"));
	this->setText(IR_LAST_COL,this->track->getLastPlayed());
	//this->setText(IR_PROBS_COL,this->track->property("probs"));
	//this->setText(6,QString::number(this->getProbs()));
}

void SafeListViewItem::paintCell(QPainter *p, const QColorGroup &cg, int column, int width, int align) {
	KListViewItem::paintCell(p, cg, column, width, align);

	if (View::singleInstance()->current() == this->track) {
		p->save();
		p->setRasterOp(XorROP);
		p->fillRect(0, 0, width, height(), QColor(255,255,255));
		p->restore();
	}
}

void SafeListViewItem::remove() {
	removed=true;
	// 	if (napp->player()->current()==this && !itemAbove() && !itemBelow()) {
	// 		napp->player()->stop();
	// 		SPL->setCurrent(0);
	// 		napp->player()->playCurrent();
	// 	} else
	View::singleInstance()->safeRemoveTrack(this->track);

/*	if (listView()) {
		if (IratePlugin::SPL()->currentItem==this) // just optimizing for least unreadably
			IratePlugin::SPL()->setCurrent(static_cast<SafeListViewItem*>(itemBelow()));

		listView()->takeItem(this);
	} else if (IratePlugin::SPL()->currentItem==this) {
		IratePlugin::SPL()->setCurrent(0);
	}*/

}
void SafeListViewItem::setHeaders() {
	this->setText(IR_ARTIST_COL,this->track->getArtist());
	this->setText(IR_TITLE_COL,this->track->getTitle());
	//float r=this->property("rating","-1").toFloat();
	this->setPixmap(IR_RATING_COL,(*IconMessage)[this->property("rateid","unrated")]);
	this->setText(IR_PLAYED_COL,QString::number(this->track->getNoOfTimesPlayed()));
	this->setText(IR_TIME_COL,this->track->getPlayingTimeString());
	this->setText(IR_LAST_COL,this->track->getLastPlayed());
	if(this->track->getCopyrightInfo().find("creativecommons",0,FALSE)!=-1) {
		this->setPixmap(IR_COPYRIGHT_COL,(*IconMessage)["ccicon"]);
	}
	//this->setText(IR_PROBS_COL,this->property("probs"));
}

//#include "safelistviewitem.moc"
