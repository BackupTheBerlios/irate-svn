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
#ifndef VIEW_H
#define VIEW_H

#include <kmainwindow.h>
#include <kpopupmenu.h>
#include <qfont.h>
#include <kaction.h>
#include "safelistviewitem.h"
#include "infodialog.h"
#include "osd.h"
#include <qt_irate.h>
#include "xmmswrapper.h"
#include <ksystemtray.h>



//Forward declaration
class SongList;

class View : public KMainWindow {
		Q_OBJECT
	public:
		static View* singleInstance();
		View();
		// load the SM playlist
		void init();
		virtual ~View();
		SongList *listView() const { return list; }
		InfoDialog * getInfoDialog(){
			return this->ktb;
		}
		OSDWidget* getOSD(){
			return this->m_osd;
		}
		int getOSDMode(){
			return this->osdMode;
		}
		void setOSDMode(const int&mode){
			this->osdMode=mode;
		}
		QTIRateSignaler * getIRateSignaler(){return this->_mirsign;}
		void setCurrentTrack(QTrack *t,bool notify);
		void safeRemoveTrack(QTrack*t);
		QTrack * current(){return xmms->current();}
		bool isExiting(){return this->exiting;}
	public slots:
		void setSorting(bool on, int column = 0);
		void setNoSorting() { setSorting(false); }
		void headerClicked(int column);
		void trackChanged();
		void showPref();
		void showAboutApplication();
		void showExport();
		void viewCurrent();
		void rate(int id);
		void rateSux();
		void rateYawn();
		void rateNotBad();
		void rateCool();
		void rateLove();
		void forward();
		void playpause();
		void stop();
		void play();
		void pause();
		void previous();
		void exit();
		void OSDLinkClicked(const QString& url);
		void listItemSelected(QListViewItem*);
		void processMenu(int);
		void connectionLost();
	signals:
		void hidden();
		void shown();	

	protected:

		virtual void closeEvent(QCloseEvent*e);
		virtual void showEvent(QShowEvent *);
		virtual void hideEvent(QHideEvent *);
	private:
		SongList *list;
		KAction *mAbout,*mConf,*mExport,*mViewCurrent,*mNext,*mPrevious,*mPlay,*mPause;
		//Doesn't work 
		KPopupMenu *mFile,*mSetting;
		InfoDialog * ktb;
		OSDWidget * m_osd;
		/*0 => never show
		1 => Always show
		2 => unrated only
		*/
		int osdMode;
		static View* _minstance;
		bool exiting;
		QTIRateSignaler *_mirsign;
		XmmsWrapper *xmms;
		KSystemTray * tray;
		
};

#endif
