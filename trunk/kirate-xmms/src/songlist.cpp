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
#include <kpassivepopup.h>
#include <ktextbrowser.h>
#include "songlist.h"
#include "newaccountdialog.h"
#include <klocale.h>
#include <kiconloader.h>
//#include <qfileinfo.h>
//#include <qdir.h>
#include <kdebug.h>
#include <kstandarddirs.h>
#include <kdialogbase.h>
#include <kfiledialog.h>
#include <qheader.h>
#include <kuser.h>
//#include <qregexp.h>
#include "template.h"
#include <qt_irate.h>
#include <list>

SongList::SongList(View *parent): KListView(parent)/*,DownloadListener() */{
	/*KRootPixmap* krp=new KRootPixmap(this);
	kdDebug()<<"Transparency avail. "<<krp->isAvailable()<<" opacity "<<krp->opacity()<<endl;*/
	//Headers
	addColumn(i18n("Artist"));
	addColumn(i18n("Title"));
	addColumn(i18n("Rating"));
	addColumn(i18n("Played"));
	addColumn(i18n("Time"));
	addColumn(i18n("Last played"));
	addColumn(i18n("Copyright"));
	//addColumn(i18n("Prob."));
	addColumn(i18n("Download"));
	//this->setShowSortIndicator(true);
	this->setColumnAlignment(2,Qt::AlignCenter);
	this->setColumnAlignment(3,Qt::AlignCenter);
	this->setColumnAlignment(4,Qt::AlignCenter);
	this->setColumnAlignment(5,Qt::AlignCenter);
	this->setColumnAlignment(7,Qt::AlignCenter);
	SafeListViewItem::initMessage();
	KIconLoader *kil=KGlobal::instance()->iconLoader();
	//Popup menu
	this->mPopup=new KPopupMenu(this,"Rate Menu");
	this->popInfo= new KPopupTitle(this->mPopup,"popinfo");
	this->popInfo->setTitle("Info");
	this->mPopup->insertItem(this->popInfo,0,0);
	this->mPopup->insertItem(kil->loadIconSet("info",KIcon::Small,0,false),i18n("Info"),1,1);
	this->popRate= new KPopupTitle(this->mPopup,"poprate");
	this->popRate->setTitle(i18n("Rate"));
	this->mPopup->insertItem(this->popRate,2,2);
	this->mPopup->insertItem(kil->loadIconSet("edittrash",KIcon::Small,0,false),i18n("This sux"),3,3);
	this->mPopup->insertItem(kil->loadIconSet("rate2",KIcon::User,0,false),i18n("Yawn"),4,4);
	this->mPopup->insertItem(kil->loadIconSet("rate5",KIcon::User,0,false),i18n("Not bad"),5,5);
	this->mPopup->insertItem(kil->loadIconSet("rate7",KIcon::User,0,false),i18n("Cool"),6,6);
	this->mPopup->insertItem(kil->loadIconSet("rate10",KIcon::User,0,false),i18n("Love it"),7,7);
	this->popDown=new KPopupTitle(this->mPopup,"popdown");
	this->popDown->setTitle(i18n("File"));
	this->mPopup->insertItem(this->popDown,8,8);
	this->mPopup->insertItem(kil->loadIconSet("reload",KIcon::Small,0,false),i18n("Download Again"),9,9);
	//this->mPopup->insertItem(kil->loadIconSet("remove",KIcon::Small,0,false),i18n("Set broken"),10,10);
	this->mPopup->insertItem(kil->loadIconSet("fileexport",KIcon::Small,0,false),i18n("Save playlist..."),10,10);

	connect(this->mPopup,SIGNAL(activated(int)),this,SLOT(processPopup(int)));
	this->header()->installEventFilter(this);

	setSorting(-1);
	setSelectionMode(QListView::Extended);

	connect(this,SIGNAL(contextMenuRequested(QListViewItem*, const QPoint&, int )),this,SLOT(showMenu(QListViewItem*, const QPoint&, int )));
	this->setAllColumnsShowFocus(TRUE);
	this->setFullWidth(true);

}
void SongList::setFont(const QFont& f) {
	this->mPopup->setFont(f);
	this->popDown->setFont(f);
	this->popInfo->setFont(f);
	this->popRate->setFont(f);
	KListView::setFont(f);
}
SongList::~SongList() {
	KConfig* config=KGlobal::config();
	config->setGroup("irate_songlist");
	config->writeEntry("songlist_col_artist",this->columnWidth(IR_ARTIST_COL));
	config->writeEntry("songlist_col_title",this->columnWidth(IR_TITLE_COL));
	config->writeEntry("songlist_col_rating",this->columnWidth(IR_RATING_COL));
	config->writeEntry("songlist_col_played",this->columnWidth(IR_PLAYED_COL));
	config->writeEntry("songlist_col_time",this->columnWidth(IR_TIME_COL));
	config->writeEntry("songlist_col_last",this->columnWidth(IR_LAST_COL));
	config->writeEntry("songlist_col_copyright",this->columnWidth(IR_COPYRIGHT_COL));
//	config->writeEntry("songlist_col_probability",this->columnWidth(IR_PROBS_COL));
	config->writeEntry("songlist_col_download",this->columnWidth(IR_DOWNLOAD_COL));
	config->sync();
}
//The methods where too much things are TODO clean it
void SongList::init() {
	this->initColumnWidth();
	std::list<QTrack*>l=QT_IRate::instance()->availableTracks();
	std::list<QTrack*>::iterator it;
	QTrack* qtr=NULL;
	SafeListViewItem *i=NULL;
	for(it=l.begin(); it != l.end();++it) {
		qtr=*it;
		//kdDebug()<<"Adding "<<qtr->getName().latin1()<<endl;
		if(!qtr->isHidden()&&!this->transformer.contains(qtr)) {
			i= new SafeListViewItem(this,qtr);
			this->transformer[qtr]=i;
			i->setHeaders();
		}
	}
	isCreatingAccount=QT_IRate::instance()->needNewAccount();
	if(isCreatingAccount) {
		NewAccountDialog nad(this);
		nad.exec();
		isCreatingAccount=false;
	} else {
		QT_IRate::instance()->startDownloading();
	}



}
void SongList::initColumnWidth() {
	KConfig* config=KGlobal::config();
	config->setGroup("irate_songlist");
	this->setColumnWidthMode(IR_ARTIST_COL,Manual);
	this->setColumnWidth(IR_ARTIST_COL,config->readNumEntry("songlist_col_artist",columnWidth(IR_ARTIST_COL)));
	this->setColumnWidthMode(IR_TITLE_COL,Manual);
	this->setColumnWidth(IR_TITLE_COL,config->readNumEntry("songlist_col_title",this->columnWidth(IR_TITLE_COL)));
	this->setColumnWidthMode(IR_RATING_COL,Manual);
	this->setColumnWidth(IR_RATING_COL,config->readNumEntry("songlist_col_rating",this->columnWidth(IR_RATING_COL)));
	this->setColumnWidthMode(IR_PLAYED_COL,Manual);
	this->setColumnWidth(IR_PLAYED_COL,config->readNumEntry("songlist_col_played",this->columnWidth(IR_PLAYED_COL)));
	this->setColumnWidthMode(IR_TIME_COL,Manual);
	this->setColumnWidth(IR_TIME_COL,config->readNumEntry("songlist_col_time",this->columnWidth(IR_TIME_COL)));
	this->setColumnWidthMode(IR_LAST_COL,Manual);
	this->setColumnWidth(IR_LAST_COL,config->readNumEntry("songlist_col_last",this->columnWidth(IR_LAST_COL)));
	this->setColumnWidthMode(IR_COPYRIGHT_COL,Manual);
	this->setColumnWidth(IR_COPYRIGHT_COL,config->readNumEntry("songlist_col_copyright",this->columnWidth(IR_COPYRIGHT_COL)));
	//For probability default is hidden
	/*this->setColumnWidthMode(IR_PROBS_COL,Manual);
	this->setColumnWidth(IR_PROBS_COL,config->readNumEntry("songlist_col_probability",0));*/
	this->setColumnWidthMode(IR_DOWNLOAD_COL,Manual);
	this->setColumnWidth(IR_DOWNLOAD_COL,config->readNumEntry("songlist_col_download",this->columnWidth(IR_DOWNLOAD_COL)));
}
void SongList::unload() {
}
void SongList::keyPressEvent(QKeyEvent *e) {
	if (e->key()==Key_Enter || e->key()==Key_Return) {
		if (currentItem()) {
			emit KListView::executed(currentItem());
		}

		return;
	}

	KListView::keyPressEvent(e);
}
//Credit to amaroK for this
//Seems like it can become the next plugin to do since amarok is really cool (just missing plugin arch)
bool SongList::eventFilter(QObject *o, QEvent *e ) {
	if(o == header() && e->type() == QEvent::MouseButtonPress && static_cast<QMouseEvent*>(e)->button() == Qt::RightButton ) {
		KPopupMenu popup;
		//popup.setFont(this->font());
		popup.setCheckable(true);
		popup.insertTitle(i18n("Available Columns"));
		int colcount=columns();
		for( int i = 0; i < colcount; ++i ) //columns() references a property
		{
			popup.insertItem(columnText(i),i,i+1 );
			popup.setItemChecked(i,columnWidth(i)!=0);
		}

		int col = popup.exec( static_cast<QMouseEvent *>(e)->globalPos() );

		if( col != -1 ) {
			//TODO can result in massively wide column appearing!
			if( columnWidth( col ) == 0 ) {
				adjustColumn( col );
				header()->setResizeEnabled( true, col );
			} else hideColumn( col );
		}

		//determine first visible column again, since it has changed
		//eat event
		return TRUE;
	}
	return KListView::eventFilter(o,e);
}

void SongList::showMenu(QListViewItem *i,const QPoint &p,int) {
	if(i) {

		this->ensureItemVisible(i);
		int count=0;
		QListViewItem *it=this->firstChild();
		while(it&&count<2) {
			if(it->isSelected()) {
				count++;
			}
			it=it->nextSibling();
		}
		if(count<2) {
			if(!i->isEnabled()) {
				this->tmpItem=static_cast<SafeListViewItem*>(i);
				this->clearSelection();
				this->mPopup->setItemEnabled(1,true);
				this->mPopup->setItemEnabled(3,false);
				this->mPopup->setItemEnabled(4,false);
				this->mPopup->setItemEnabled(5,false);
				this->mPopup->setItemEnabled(6,false);
				this->mPopup->setItemEnabled(7,false);
				this->mPopup->setItemEnabled(9,false);
				this->mPopup->setItemEnabled(10,false);
			} else {

				this->tmpItem=static_cast<SafeListViewItem*>(i);
				this->clearSelection();
				this->mPopup->setItemEnabled(1,true);
				this->mPopup->setItemEnabled(3,true);
				this->mPopup->setItemEnabled(4,true);
				this->mPopup->setItemEnabled(5,true);
				this->mPopup->setItemEnabled(6,true);
				this->mPopup->setItemEnabled(7,true);
				this->mPopup->setItemEnabled(9,true);
				this->mPopup->setItemEnabled(10,false);
				this->setSelected(i,TRUE);
			}
			SafeListViewItem *j=static_cast<SafeListViewItem*>(i);
			this->popInfo->setTitle(j->property("artist")+" - "+j->property("title"));
		} else {
			//this->clearSelection();
			this->tmpItem=0;
			this->mPopup->setItemEnabled(1,false);
			this->mPopup->setItemEnabled(3,false);
			this->mPopup->setItemEnabled(4,false);
			this->mPopup->setItemEnabled(5,false);
			this->mPopup->setItemEnabled(6,false);
			this->mPopup->setItemEnabled(7,false);
			this->mPopup->setItemEnabled(9,false);
			this->mPopup->setItemEnabled(10,true);
			this->popInfo->setTitle(i18n("Grouped action"));
		}

		this->mPopup->popup(p);
	}
}
void SongList::handleError(QString code,QString url) {
	if(isCreatingAccount)return;
	QString lfile=locate("html","en/kirateradioxmms/"+url);
	if(lfile.isEmpty()) {
		KMessageBox::sorry(this,i18n("The communication with the server as generated the following error\n code %1\nurl %2\n\nMore detailled error description is not yet available").arg(code).arg(url),i18n("iRate Server Error"));
		return;
	}
	QFile f(lfile);
	f.open(IO_ReadOnly);
	KMessageBox::sorry(this,f.readAll(),i18n("iRate Server Error"),KMessageBox::AllowLink);
	f.close();
}

void SongList::processPopup(int id) {
	if(id<=0||id>10)return;
	//SafeListViewItem * i= this->tmpItem;
	if(id==1) {//Info
		if(this->tmpItem==0)return;
		QString title=this->tmpItem->property("title")+" - "+this->tmpItem->property("artist");
		this->getMainView()->getInfoDialog()->setText(Template::instance()->infoTemplate(this->tmpItem),title);
	} else if(id==9) {//Download again
		if(this->tmpItem==0)return;
		this->tmpItem->reDownload();
		return;
	} else if(id==10/*||id==12*/) {//For both action we need to get a list of selected items Actually id==12 removed
		QPtrList<SafeListViewItem> sel_list;
		SafeListViewItem* it=static_cast<SafeListViewItem*>(this->firstChild());
		while(it) {
			if(it->isSelected()) {
				sel_list.append(it);
			}
			it=static_cast<SafeListViewItem*>(it->nextSibling());
		}
		if(id==10) {//Playlist
			QString filename=KFileDialog::getSaveFileName(QString::null,"*.m3u|"+i18n("Playlist (*.m3u)"),this->getMainView());
			if(filename.isNull())return;
			QFile f(filename);
			f.open(IO_WriteOnly);
			QTextStream s(&f);
			if(KMessageBox::questionYesNo(this->getMainView(),i18n("Do you want to save the playlist using URL(good to share) or absolute filename ?"),i18n("Save as m3u..."), i18n("URL"),i18n("Absolute filename"))==KMessageBox::Yes) {//Url
				for(it=sel_list.first();it;it=sel_list.next()) {
					s<<it->getRealUrl()<<"\n";
				}
			} else {
				for(it=sel_list.first();it;it=sel_list.next()) {
					s<<it->property("file")<<"\n";
				}
			}
			f.flush();
			f.close();
			return;

		}
	}
	if(id<3||id>7||this->tmpItem==0)return;
	//Rating actions
	int rate=-1;
	switch(id) {
	case 3:
		rate=0;
		break;
	case 4:
		rate=2;
		break;
	case 5:
		rate=5;
		break;
	case 6:
		rate=7;
		break;
	case 7:
		rate=10;
		break;
	}
	this->tmpItem->getTrack()->setRating(rate);
	/*if(rate==0){
		delete this->tmpItem;
	}*/
	/*if(rate!=0 && View::singleInstance()->current()==this->tmpItem->getTrack()) {
		//emit trackChange(this->tmpItem);
	}*/
}
void SongList::setCurrentSong(QTrack* qtr) {
	SafeListViewItem *i=NULL;
	if(!this->transformer.contains(qtr)) {
		i=new SafeListViewItem(this,qtr);
		i->setHeaders();
		this->transformer[qtr]=i;
	} else {
		i=this->transformer[qtr];
	}
	if(this->itemIndex(i)!=-1) {
		this->clearSelection();
		this->setCurrentItem(i);
		if(this->mPopup->isHidden()) {
			this->setSelected(i,true);
			this->ensureItemVisible(i);
		}
		QRect currentRect= this->itemRect(i);
		this->viewport()->repaint(currentRect);
		i->setPixmap(0, KGlobal::iconLoader()->loadIcon("play",KIcon::User,0,KIcon::ActiveState,0,false));
		if(!qtr->isRated()&&this->showPassive) {
			KPassivePopup::message("[Unrated]",Template::instance()->osdTemplate(i)/*i18n("%1 - %2").arg(i->property("artist","")).arg(i->property("title",""))*/,this->getMainView());
		}

	}
//	emit trackChange(i);
}
void SongList::updateTrack(QTrack* qtr) {
	SafeListViewItem *i=NULL;
	if(!this->transformer.contains(qtr)) {
		if(qtr->isHidden())return;
		i=new SafeListViewItem(this,qtr);
		i->setHeaders();
		this->transformer[qtr]=i;
	} else {
		i=this->transformer[qtr];
		if(qtr->isHidden()) {
			/*if(XmmsWrapper::instance()->current()==i->getTrack()){
				XmmsWrapper::instance()->next();
			}*/
			this->transformer.remove(qtr);
			delete i;
			return;
		}
		i->updateDisplay();
	}
}
void SongList::downloadFinished(QTrack* qtr, bool success) {
	SafeListViewItem *i=NULL;
	if(!this->transformer.contains(qtr)) {
		if(!success) {
			return;
		}
		i=new SafeListViewItem(this,qtr);
		i->setHeaders();
		this->transformer[qtr]=i;
	} else {
		i=this->transformer[qtr];
		if(!success) {
			this->transformer.remove(qtr);
			delete i;
			return;
		}
		i->updateDisplay();
	}
	i->downloadFinished("Finished");
}
void SongList::downloadProgressed(QTrack* qtr, int p) {
	SafeListViewItem *i=NULL;
	if(!this->transformer.contains(qtr)) {
		i=new SafeListViewItem(this,qtr);
		i->setHeaders();
		this->transformer[qtr]=i;
	} else {
		i=this->transformer[qtr];
		//i->updateDisplay();
	}
	i->downloaded(QString::number(p));

}
void SongList::downloadStarted(QTrack* qtr) {
	SafeListViewItem *i=NULL;
	if(!this->transformer.contains(qtr)) {
		i=new SafeListViewItem(this,qtr);
		i->setHeaders();
		this->transformer[qtr]=i;
	} else {
		i=this->transformer[qtr];
		i->updateDisplay();
	}
}

