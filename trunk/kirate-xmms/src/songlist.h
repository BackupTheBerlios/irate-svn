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
#include <qt_irate.h>
#include "view.h"
//forward declaration
class View;

/**
@author Matthias Studer
*/


class SongList : public KListView{
		Q_OBJECT
		friend class View;
	public:
		SongList(View *parent);
		virtual ~SongList();
		void init();
		QTrack* next();
		QTrack* previous();
		/*void addToRemoveList(SafeListViewItem* i){
			this->removeList.push(i);
		}*/
		View * getMainView(){
			return static_cast<View*>(this->parent());
		}
	
		//TrackDatabase * getTD(){return this->td;}
		void unload();
		void setCurrentSong(QTrack* qtr);
		
		void setShowPassivePopup(const bool &show){
			this->showPassive=show;
		}
		
		bool getShowPassivePopup(){
			return this->showPassive;
		}
		void setFont(const QFont& f);
		SafeListViewItem *getSafeListViewItem(QTrack *t){
			if(this->transformer.contains(t)){
				return this->transformer[t];
			}
			else{
				if(t->isHidden()){
					return NULL;
				}
				SafeListViewItem* i=new SafeListViewItem(this,t);
				i->setHeaders();
				this->transformer[t]=i;
				return i;
			}
		}
	public slots:
		virtual void updateTrack(QTrack*);
		virtual void handleError(QString ,QString );
		virtual void downloadFinished(QTrack* , bool);
		virtual void downloadProgressed(QTrack*, int);
		virtual void downloadStarted(QTrack*);
	protected:
		void keyPressEvent(QKeyEvent *e);
		bool eventFilter( QObject *o, QEvent *e );
		void initColumnWidth();
		//Members 
		KPopupMenu *mPopup;
		KPopupTitle * popInfo,*popRate,*popDown;
//		TrackDatabase* td;
		bool showPassive;
		//Unused actually
		//QPtrStack<SafeListViewItem> removeList;
		QMap<QTrack*,SafeListViewItem*> transformer;
		SafeListViewItem* tmpItem;
		bool isCreatingAccount;
		
		
	protected slots:
		
		void showMenu(QListViewItem *i,const QPoint &p,int);
		void processPopup(int id);
};

#endif
