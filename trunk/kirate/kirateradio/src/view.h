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
#include "plugin_kirateradio_impl.h"
#include "safelistviewitem.h"
#include "infodialog.h"
//#include "cyclictext.h"
#include "osd.h"

//#include "downloadcenter.h"

//class Finder;


//class DownloadListener;
//Forward declaration
class SongList;
class IratePlugin;
//class DownloadCenter;
//class ConfigurationCenter;


//class KToggleAction;
//class KToolBar;

class View : public KMainWindow {
		Q_OBJECT
	public:
		View(IratePlugin *mother);
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

	public slots:

		void setSorting(bool on, int column = 0);
		void setNoSorting() { setSorting(false); }
		void headerClicked(int column);
		void trackChanged(SafeListViewItem* i);
		void showPref();
		void showAboutApplication();
		void showExport();
		void showDeleted();
		void viewCurrent();
		void configureToolBars();
		void newToolBarConfig();
		void rate(int id);
		void rateSux();
		void rateYawn();
		void rateNotBad();
		void rateCool();
		void rateLove();
		void downloadMessage(const QString& message);
		void totalDownloadSpeed(const QString& speed);
		void OSDLinkClicked(const QString& url);
	signals:
		void hidden();
		void shown();	

	protected:

		void setModified(bool);
		virtual void closeEvent(QCloseEvent*e);
		virtual void showEvent(QShowEvent *);
		virtual void hideEvent(QHideEvent *);

	

	private:
		SongList *list;
		KAction *mAbout,*mConf,*mExport,*noa_playp,*noa_for,*noa_back,*noa_stop/*,*mViewDeleted*/,*mViewCurrent;
		//Doesn't work 
		//KAction * rSux,*rYawn,*rNotBad,*rCool,*rLove;
		KPopupMenu *mFile,*mSetting;
		InfoDialog * ktb;
		OSDWidget * m_osd;
		/*0 => never show
		1 => Always show
		2 => unrated only
		*/
		int osdMode;
		
};

#endif
