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
 /**
 * This file is based on splitplaylist (but heavily modified)
 **/

#ifndef SAFELISTVIEWITEM_H
#define SAFELISTVIEWITEM_H

#include <klistview.h>
#include <noatun/playlist.h>
#include "trackdatabase.h"
#include <qpainter.h>
#include <qmap.h>
#include <noatun/app.h>
#include <qpixmap.h>
#include <qrect.h>
#define IR_ARTIST_COL 0
#define IR_TITLE_COL 1
#define IR_RATING_COL 2
#define IR_PLAYED_COL 3
#define IR_TIME_COL 4
#define IR_LAST_COL 5
#define IR_COPYRIGHT_COL 6
#define IR_PROBS_COL 7
#define IR_DOWNLOAD_COL 8
/**
@author Matthias Studer
*/
class SafeListViewItem: public KListViewItem, public PlaylistItemData{
	public:
		SafeListViewItem(QListView *parent, TrackInfo* mtrack, const QString& irateDir);
//		SafeListViewItem(SafeListViewItem *i);
		virtual ~SafeListViewItem();
		
		//Property stuff
		virtual QString property(const QString &key, const QString & def= 0) const;
		virtual void setProperty(const QString &key, const QString &value,const bool& notify);
		virtual void setProperty(const QString &key, const QString &value){
			this->setProperty(key,value,true);
		}
		virtual void clearProperty(const QString &key);
		virtual QStringList properties() const;
		virtual bool isProperty(const QString &key) const;
		
		
		//Download
		virtual void downloaded(const QString &);
		virtual void downloadSpeed(const QString &);
		virtual void downloadInfoMessage(const QString &msg);
		virtual void downloadFinished(const QString& msg);
		//virtual void setDownloadMessage(const QString& msg);
		virtual void reDownload(const QString& irateDir);
		//virtual QString file() const;
		int compare(QListViewItem * i, int col, bool ascending) const;
		virtual void remove();
		int getProbs()const{
			return probs;
		}
		void setProbs(const int& prob,const int &total);
		void addPlayed(){
			this->track->addPlayed();
			this->modified();
		}
		virtual void setHeaders();
		
		virtual bool operator==(const PlaylistItemData &d)const;
		virtual void setBroken();

		virtual QString getRealUrl()const{
			return this->track->property("url",QString::null);
		}
		TrackInfo * getTrack(){
			return this->track;
		}
		void setRating(const int& rate);
		QString getStringRating(){
			return RateMessage[this->track->property("rateid","unrated")];
		}
		QPixmap getPixmapRating(){
			return IconMessage[this->track->property("rateid","unrated")];
		}
		int getDistance(){
			return this->distance;
		}
		void addDistance(){
			this->distance++;
		}
		void resetDistance(){
			this->distance=0;
		}
		static void initMessage();
	protected:
		static QMap<QString,QPixmap> IconMessage;
		static QMap<QString,QString> RateMessage;
		//We need some opt. here 
		virtual void modified(const int& col=-1);
		virtual void modified(const QString &prop);
//		virtual void stateChange(bool s);
		/*SongList * getList(){
			return (SongList*)(this->listView());
		}*/
		void paintCell(QPainter *p, const QColorGroup &cg, int column, int width, int align);
	private:
		TrackInfo* track;
		bool removed;
		int probs;
		int distance;
		//Warning ugly hack here
		QString mUrl;
		

};

#endif
