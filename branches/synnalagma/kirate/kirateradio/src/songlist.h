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
#ifndef SONGLIST_H
#define SONGLIST_H

#include <klistview.h>
#include <qevent.h>
#include <kmessagebox.h>

//#include <qptrlist.h>
#include <qptrstack.h>
#include <qvaluestack.h>
#include <qfont.h>
#include <kpopupmenu.h>
//#include <kdebug.h>
#include "songselection.h"
#include "trackdatabase.h"
#include "view.h"
//forward declaration
class View;

/**
@author Matthias Studer
*/


class SongList : public KListView/*, public DownloadListener*/{
		Q_OBJECT
		friend class View;
	public:
		SongList(View *parent);
		virtual ~SongList();
		void init(const QString &irateDir);
		PlaylistItem next();
		PlaylistItem previous();
		/*void addToRemoveList(SafeListViewItem* i){
			this->removeList.push(i);
		}*/
		View * getMainView(){
			return static_cast<View*>(this->parent());
		}
		TrackDatabase * getTD(){return this->td;}
		void unload();
		void setCurrentSong(SafeListViewItem* i);
		
		void setMinUnrated(const int& newMinUnrated){
			this->minUnrated=newMinUnrated;
		}
		void setShowPassivePopup(const bool &show){
			this->showPassive=show;
		}
		int getMinUnrated(){
			return this->minUnrated;
		}
		bool getShowPassivePopup(){
			return this->showPassive;
		}
		BasicSelection * getSelector(){
			return this->selector;
		}
		void setSelector(BasicSelection *select);
		void updateRating();
		bool getAllowConnect(){
			return this->allowConnect;
		}
		void setAllowConnect(const bool &ac){
			this->allowConnect=ac;
		}
		int getPlaySongMode(){
			return this->playSoundMode;
		}
		void setPlaySoundMode(const int& mode){
			this->playSoundMode=mode;
		}
		QString getSoundPath(){
			return this->soundPath;
		}
		void setSoundPath(const QString& sound){
			this->soundPath=sound;
		}
		void setFont(const QFont& f);
	protected:
		virtual void customEvent(QCustomEvent * event);
		void keyPressEvent(QKeyEvent *e);
		void timerEvent(QTimerEvent* t);
		bool eventFilter( QObject *o, QEvent *e );
		//Members 
		KPopupMenu *mPopup;
		KPopupTitle * popInfo,*popRate,*popDown;
		TrackDatabase* td;
		int totalRating;
		int noUnrated;
		int minUnrated;
		int playSoundMode;
		QString soundPath;
		bool showPassive;
		bool allowConnect;
		QValueStack<PlaylistItem> previousSong;
		//Unused actually
		//QPtrStack<SafeListViewItem> removeList;
		SafeListViewItem * nextToRemove;
		SafeListViewItem* tmpItem;
		BasicSelection* selector;
		int timerid;
		int songtimerid;
		int playsoundID;
		
		
		
	public slots:
		void addTrack(TrackInfo* newTrack);
		void serverError(QString code,QString url);
		
	protected slots:
		
		void showMenu(QListViewItem *i,const QPoint &p,int);
		void processPopup(int id);
		
	
	signals:
		void trackChange(SafeListViewItem *i);
		void downloadMessage(const QString& message);
		void totalDownloadSpeed(const QString& speed);

};

#endif
