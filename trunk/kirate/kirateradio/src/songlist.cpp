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
#include <noatun/player.h>
#include <kiconloader.h>
#include <qfileinfo.h>
#include <qdir.h>
#include <kdebug.h>
#include "downloadcenter.h"
#include <kstandarddirs.h>
#include <kdialogbase.h>
#include <kfiledialog.h>
#include <qheader.h>
#include <kuser.h>
#include <qregexp.h>
#include "template.h"

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
	addColumn(i18n("Prob."));
	addColumn(i18n("Download"));
	//addColumn(i18n("Probs"));
	this->nextToRemove=0;
	this->noUnrated=0;
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
	//Function removed to conform original iRATE
	//this->mPopup->insertItem(kil->loadIconSet("editdelete",KIcon::Small,0,false),i18n("Delete files..."),12,12);
	connect(this->mPopup,SIGNAL(activated(int)),this,SLOT(processPopup(int)));
	this->header()->installEventFilter(this);

	//setAcceptDrops(true);
	setSorting(-1);
	//setDropVisualizer(true);
	//setDragEnabled(true);
	//setItemsMovable(true);
	setSelectionMode(QListView::Extended);

	//connect(this, SIGNAL(dropped(QDropEvent*, QListViewItem*)), SLOT(dropEvent(QDropEvent*, QListViewItem*)));
	//connect(this, SIGNAL(moved()), SLOT(move()));
	//connect(this, SIGNAL(aboutToMove()), parent, SLOT(setNoSorting()));
	connect(this,SIGNAL(contextMenuRequested(QListViewItem*, const QPoint&, int )),this,SLOT(showMenu(QListViewItem*, const QPoint&, int )));
	//connect(this, SIGNAL(deleteCurrentItem()), parent, SLOT(deleteSelected()));
	this->selector=new BasicSelection();
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
	//this->clear();
	//this->unload();
	//KMessageBox::information(0,"Destroying","Start",QString::null,KMessageBox::AllowLink);
	KConfig* config=KGlobal::config();
	config->setGroup("irate_songlist");
	config->writeEntry("songlist_col_artist",this->columnWidth(IR_ARTIST_COL));
	config->writeEntry("songlist_col_title",this->columnWidth(IR_TITLE_COL));
	config->writeEntry("songlist_col_rating",this->columnWidth(IR_RATING_COL));
	config->writeEntry("songlist_col_played",this->columnWidth(IR_PLAYED_COL));
	config->writeEntry("songlist_col_time",this->columnWidth(IR_TIME_COL));
	config->writeEntry("songlist_col_last",this->columnWidth(IR_LAST_COL));
	config->writeEntry("songlist_col_copyright",this->columnWidth(IR_COPYRIGHT_COL));
	config->writeEntry("songlist_col_probability",this->columnWidth(IR_PROBS_COL));
	config->writeEntry("songlist_col_download",this->columnWidth(IR_DOWNLOAD_COL));
	config->sync();
	//KMessageBox::information(0,"Destroyed DownloadCenter","Start",QString::null,KMessageBox::AllowLink);
	//Must do it before saving
	this->td->cleanDownloadDir();
	this->td->saveFile();
	//this->td->cleanDownloadDir();
	//KMessageBox::information(0,"File Saved","Start",QString::null,KMessageBox::AllowLink);
	delete td;
	delete selector;
	//KMessageBox::information(0,"TrackDb deleted","Start",QString::null,KMessageBox::AllowLink);
}
//The methods where too much things are TODO clean it
void SongList::init() {
	/*QString irateDir=startDir;
	this->noUnrated=0;
	//QFileInfo ifile(irateDir+"trackdatabase.xml");
	if(!irateDir.isEmpty()&&!irateDir.endsWith("/")) {
		irateDir+='/';
	}
	td= new TrackDatabase(irateDir,this,"TrackDatabase");
	connect(td,SIGNAL(trackAdded(TrackInfo* )),this,SLOT(addTrack(TrackInfo* )));
	connect(td,SIGNAL(serverError(QString, QString )),this,SLOT(serverError(QString, QString )));
	bool initplaylist=false;
	if(!startDir.isEmpty()&&QFile::exists(irateDir+"trackdatabase.xml")) {

		QFile tdFile(irateDir+"trackdatabase.xml");
		//printf("File inited %d\n",tdFile.exists());
		//tdFile.open(IO_ReadOnly);
		QXmlInputSource source(&tdFile);
		//printf("Input source inited \n");
		td->processXML(&source);
		//tdFile.close();
		//delete source;
		//printf("Finished constructor \n");
		initplaylist=true;

	} else {*/
		
		bool initplaylist=false;
		KUser user;
		bool irateDirFounded=false;
		QString irateDir;
		//Check for an existing irate dir
		
		td= new TrackDatabase(irateDir,this,"TrackDatabase");
		connect(td,SIGNAL(trackAdded(TrackInfo* )),this,SLOT(addTrack(TrackInfo* )));
		connect(td,SIGNAL(serverError(QString, QString )),this,SLOT(serverError(QString, QString )));
		
		if(QFile::exists(user.homeDir()+"/irate/irate.xml")){
			QFile firate(user.homeDir()+"/irate/irate.xml");
			if(firate.open(IO_ReadOnly)){
				QRegExp irExp("<preference\\s+id=\"downloadDir\">([^<]*)</preference>");
				QString irContent(firate.readAll());
				kdDebug()<<irContent<<endl;
				irExp.search(irContent,0);
				if(irExp.numCaptures()>0){
					kdDebug()<<irExp.cap(0)<<irExp.cap(1)<<endl;
					if(QFile::exists(irExp.cap(1))){
						irateDirFounded=true;
						//newIrateDir= irExp.cap(1);
						QFileInfo tdFileInfo(KStandardDirs::realPath(irExp.cap(1)));
						/*kdDebug()<<"Irate directory "<<KStandardDirs::realPath(tdFileInfo.dirPath(TRUE))<<" Selected "<<newIrateDir<<endl;*/
						irateDir=tdFileInfo.dirPath(TRUE);
						if(!irateDir.endsWith("/")) {
							irateDir+='/';
						}
						td->setIRateDir(irateDir);
						kdDebug()<<"Irate directory "<<td->getIRateDir()<<" Selected "<<irateDir<<endl;
						/*KConfig* config=KGlobal::config();
						config->setGroup("irate");
						config->writePathEntry("irate_directory",td->getIRateDir());*/
						//irateDir=td->getIRateDir();
						QFile tdFile(irateDir+"trackdatabase.xml");
						//kdDebug()<<"File "<<tdFile.name()<<" "<<tdFile.exists()<<endl;
						QXmlInputSource source(&tdFile);
						td->processXML(&source);
						initplaylist=true;
					}
				}
			}
		}
		
		/*int ret=KMessageBox::questionYesNo(this->getMainView(),i18n("Hi!\n\n Seems like it's the first time your running this plugin. So welcome and enjoy!\n\nIn order to use iRate Radio you need to have an account on the server.\n\nHave you already an account ?"),i18n("Create Account"), i18n("I have an account"),i18n("Create a new account"));
		if(ret ==KMessageBox::Yes) {
			newIrateDir=KFileDialog::getOpenFileName(QString::null,"trackdatabase.xml|iRate account (trackdatabase.xml)",this->getMainView());
			if(!newIrateDir.isEmpty()) {
				QFileInfo tdFileInfo(KStandardDirs::realPath(newIrateDir));
				kdDebug()<<"Irate directory "<<KStandardDirs::realPath(tdFileInfo.dirPath(TRUE))<<" Selected "<<newIrateDir<<endl;
				irateDir=tdFileInfo.dirPath(TRUE);
				if(!irateDir.endsWith("/")) {
					irateDir+='/';
				}
				td->setIRateDir(irateDir);
				kdDebug()<<"Irate directory "<<td->getIRateDir()<<" Selected "<<newIrateDir<<endl;
				KConfig* config=KGlobal::config();
				config->setGroup("irate");
				config->writePathEntry("irate_directory",td->getIRateDir());
				//irateDir=td->getIRateDir();
				QFile tdFile(irateDir+"trackdatabase.xml");
				//kdDebug()<<"File "<<tdFile.name()<<" "<<tdFile.exists()<<endl;
				QXmlInputSource source(&tdFile);
				td->processXML(&source);
				initplaylist=true;
			} else {
				ret=KMessageBox::No;
			}
		}*/
		//No else since last could abort
		if(!irateDirFounded) {
			kdDebug()<<"Building dialog"<<endl;

			NewAccountDialog* nad=new NewAccountDialog(td,this);

			kdDebug()<<"Done Building dialog"<<endl;
			if(nad->exec()==QDialog::Accepted) {
				irateDir=td->getIRateDir();
				/*KConfig* config=KGlobal::config();
				config->setGroup("irate");
				config->writePathEntry("irate_directory",td->getIRateDir());*/
				initplaylist=true;
				td->saveFile();
			} else {
				KMessageBox::sorry(this->getMainView(),i18n("Without having any account, you wont be able to do anything with this plugin, restart Noatun to create one"),i18n("Warning"));
			}
			delete nad;
		}
	//}
	DownloadCenter::instance()->setDownloadListener(this);
	if(initplaylist) {

		td->setNotify(true);
		QPtrList<TrackInfo> l=td->getPlayable();
		TrackInfo* track;
		this->totalRating=0;
		SafeListViewItem* it;
		for(track=l.first();track;track=l.next()) {
			//printf("%s\n",track->property("artist").latin1());
			track->calcDate();
			it=new SafeListViewItem(this,track, td->getIRateDir());
			it->setProbs(this->selector->setSongWeight(it),1);
			this->totalRating+=it->getProbs();
			//printf("%s\n",track->property("artist").latin1());
		}
		it=static_cast<SafeListViewItem*>(this->firstChild());
		while(it) {
			it->setProbs(it->getProbs(),this->totalRating);
			it->setHeaders();
			it=static_cast<SafeListViewItem*>(it->nextSibling());
		}

		this->selector->setTotal(this->totalRating);
		this->initColumnWidth();

	}
	this->timerid=this->startTimer(60000);
	this->songtimerid=-1;

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
	this->setColumnWidthMode(IR_PROBS_COL,Manual);
	this->setColumnWidth(IR_PROBS_COL,config->readNumEntry("songlist_col_probability",0));
	this->setColumnWidthMode(IR_DOWNLOAD_COL,Manual);
	this->setColumnWidth(IR_DOWNLOAD_COL,config->readNumEntry("songlist_col_download",this->columnWidth(IR_DOWNLOAD_COL)));
}
void SongList::unload() {
	DownloadCenter::instance()->clean(true);
	this->td->saveFile();
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
//Here we suppose Track hasn't been played before
//Supposition removed since with deleted tag a song can have been played before
void SongList::addTrack(TrackInfo* newTrack) {
	newTrack->calcDate();
	SafeListViewItem *it=new SafeListViewItem(this,newTrack, td->getIRateDir());
	int w=this->selector->setSongWeight(it);
	this->totalRating+=w;
	this->selector->setTotal(this->totalRating);

	if(it->property("rating","").isEmpty())
		this->noUnrated++;
	//printf("%s\n",track->property("artist").latin1());
	it->setHeaders();
	this->updateRating();
}

void SongList::customEvent(QCustomEvent * event) {
	/*if(event->type()<1102&&event->type()>1109) {
		QObject::customEvent(event);
		return;
	}*/
	DownloadEvent *de= dynamic_cast<DownloadEvent*>(event);
	if(!de)return;
	if(de->isPercentMessage()) {
		de->getItem()->downloaded(de->getMessage());
		emit downloadMessage(i18n("Downloading : [%1] %2 %3 ").arg(de->getItem()->property("artist")).arg(de->getItem()->property("title")).arg(de->getItem()->property("percdone")));
	} else if(de->isSpeedMessage()) {
		de->getItem()->downloadSpeed(de->getMessage());

	} else if(de->isFinishedMessage()) {
		//de->getItem()->setProbs(weight,this->totalRating);
		de->getItem()->downloadFinished(de->getMessage());
		int weight=this->selector->setSongWeight(de->getItem());
		this->totalRating+=weight;
		this->selector->setTotal(this->totalRating);
		this->updateRating();
	} else if(de->isTotalDownloadSpeedMessage()) {
		emit totalDownloadSpeed(de->getMessage());
	} else if(de->isInfoMessage()) {
		//de->getItem()->downloadInfoMessage(de->getMessage());
		emit downloadMessage(de->getMessage());
	} else if(de->isTimeoutMessage()) {
		//de->getItem()->setDownloadMessage(de->getMessage());
		kdDebug()<<"Download Timeout : "<<de->getMessage()<<endl;
		emit emit downloadMessage(i18n("[%1] %2 Timeout : %3 ").arg(de->getItem()->property("artist")).arg(de->getItem()->property("title")).arg(de->getMessage()));
		//this->getMainView()->statusBar()->changeItem(de->getMessage(),0);
	} else if(de->isErrorMessage()) {
		if(de->getItem()) {
			emit downloadMessage(i18n("Downloading : [%1] %2 Error : %3 ").arg(de->getItem()->property("artist")).arg(de->getItem()->property("title")).arg(de->getMessage()));
			de->getItem()->setProperty("state","broken",false);
			de->getItem()->setProperty("file","",false);
			//Ugly hack imported from iRATE
			de->getItem()->setProperty("rating","0");
			de->getItem()->remove();
			de->getItem()->removeRef();
		}

		kdDebug()<<"Download error : "<<de->getMessage()<<endl;
	}

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
				//this->mPopup->setItemEnabled(11,false);
				//	this->mPopup->setItemEnabled(12,false);
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
				//this->mPopup->setItemEnabled(11,false);
				//	this->mPopup->setItemEnabled(12,true);
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
			//this->mPopup->setItemEnabled(11,true);
			//this->mPopup->setItemEnabled(12,true);
			this->popInfo->setTitle(i18n("Grouped action"));
		}

		this->mPopup->popup(p);
	} /*else {
			this->popInfo->setTitle("Header");
			this->mPopup->popup(p);
		}*/
}
void SongList::serverError(QString code,QString url) {
	QString lfile=locate("html","en/kirateradio/"+url);
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
		this->tmpItem->reDownload(this->td->getIRateDir());
		return;
	} /*else if(id==10) {//Set broken
		if(this->tmpItem==0)return;
		this->tmpItem->setBroken();
		return;

	} */else if(id==10/*||id==12*/) {//For both action we need to get a list of selected items Actually id==12 removed
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
		//Function removed to conform iRATE
		/*else if(id==12){//Delete
			QStringList itemList;
			for(it=sel_list.first();it;it=sel_list.next()){
				itemList.append("["+it->property("artist")+"] - "+it->property("title"));
			}
			if(KMessageBox::warningYesNoList(this->getMainView(),i18n("Are you sure that you want to delete this tracks ?"),itemList,i18n("Delete tracks"))==KMessageBox::Yes){
				for(it=sel_list.first();it;it=sel_list.next()){
					it->setProperty("deleted","true");
					//it->setProperty("file","");
					it->remove();
					it->removeRef();
				}
			}
			return;
		}*/
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
	if(!this->tmpItem->isProperty("rating")) {
		this->noUnrated--;
	}
	//i->setProperty("rating",QString::number(rate));
	this->tmpItem->setRating(rate);
	this->tmpItem->setProperty("serial",QString::number(td->getSerial()+1),false);
	if(rate!=0 && napp->player()->current()==this->tmpItem) {
		emit trackChange(this->tmpItem);
	}
	this->totalRating-=this->tmpItem->getProbs();
	int weight=this->selector->setSongWeight(this->tmpItem);
	this->totalRating+=weight;
	//i->setProbs(weight,this->totalRating);
	this->selector->setTotal(this->totalRating);
	this->updateRating();
	if(rate==0) {
		//delete i;
		if(this->nextToRemove!=0&&this->nextToRemove!=this->tmpItem) {
			this->nextToRemove->remove();
			this->nextToRemove->removeRef();
			this->nextToRemove=0;
		}
		this->nextToRemove=this->tmpItem;
	}//This is actually an undo
	else if(this->nextToRemove==this->tmpItem) {
		this->nextToRemove=0;
	}
	if(this->allowConnect && this->noUnrated<this->minUnrated) {
		kdDebug()<<"Connecting from rating"<< this->noUnrated<<" sur "<<td->getAutoDownload()<<endl;
		td->connectToServer();
	}

	static int rateCount=0;
	rateCount++;
	//Check how to this better
	if(rateCount%5==0) {
		td->saveFile();
	}
}
void SongList::timerEvent(QTimerEvent* e) {
	if(e->timerId()==this->timerid) {
		this->noUnrated=0;
		SafeListViewItem *i=static_cast<SafeListViewItem*>(this->firstChild());
		while(i) {
			if(!i->isProperty("rating")) {
				this->noUnrated++;
			}
			//if(i->text(5)=="Download finished")i->setText(6,"");
			i=static_cast<SafeListViewItem*>(i->nextSibling());
		}

		//this->td->saveFile();
		if(this->allowConnect&&this->noUnrated<this->minUnrated) {
			kdDebug()<<"Connecting from timer "<<this->noUnrated<<" sur "<<this->minUnrated<<endl;
			td->connectToServer();
		}
	} else if(e->timerId()==this->songtimerid) {
		this->songtimerid=-1;
		if(!this->previousSong.isEmpty()) {
			SafeListViewItem * last = static_cast<SafeListViewItem*>(this->previousSong.top().data());
			last->addPlayed();
			this->totalRating-=last->getProbs();
			int weight=this->selector->setSongWeight(last);
			this->totalRating+=weight;
			this->selector->setTotal(this->totalRating);
			//last->setProbs(weight,this->totalRating);
			this->updateRating();
			kdDebug()<<"End of song about to be reached"<<last->property("title")<<endl;
		}
	}
}
void SongList::setSelector(BasicSelection *select) {
	this->selector=select;
	this->updateRating();
}
void SongList::updateRating() {
	SafeListViewItem * i = static_cast<SafeListViewItem*>(this->firstChild());
	int oldTot = this->totalRating;
	this->totalRating=0;
	while(i) {
		i->setProbs(this->selector->setSongWeight(i),oldTot);
		this->totalRating+=i->getProbs();
		/*if(rand<=curTot&& i->isEnabled()){
			break;
		}*/
		i=static_cast<SafeListViewItem*>(i->nextSibling());
	}
	this->selector->setTotal(this->totalRating);
}
PlaylistItem SongList::next() {
	if(!this->childCount())return 0;

	//this->updateRating();
	int rand=this->selector->getRandom();
	int curTot=0;
	SafeListViewItem * i = static_cast<SafeListViewItem*>(this->firstChild());
	while(i) {
		curTot+=i->getProbs();
		if(rand<=curTot&& i->isEnabled()) {
			break;
		}
		i=static_cast<SafeListViewItem*>(i->nextSibling());
	}
	kdDebug()<<"Next called : "<<this->totalRating<<" random "<<rand<<" curTot "<<curTot<<endl;
	if(!i) {
		kdDebug()<<"No item found total : "<<this->totalRating<<" random "<<rand<<" curTot "<<curTot<<endl;
		static bool recursiveCheck=false;
		if(!recursiveCheck) {
			this->updateRating();
			recursiveCheck=true;
			PlaylistItem pl= this->next();
			recursiveCheck=false;
			return pl;
		}
		i=static_cast<SafeListViewItem*>(this->lastChild());
	}
	this->setCurrentSong(i);
	//emit trackChange(i);
	return i;

}
void SongList::setCurrentSong(SafeListViewItem* i) {
	if(this->itemIndex(i)!=-1) {
		this->clearSelection();
		this->previousSong.push(i);
		this->setCurrentItem(i);
		if(this->mPopup->isHidden()) {
			this->setSelected(i,true);
			this->ensureItemVisible(i);
		}
		if(!i->isProperty("rating")&&this->showPassive) {
			KPassivePopup::message("[Unrated]",Template::instance()->osdTemplate(i)/*i18n("%1 - %2").arg(i->property("artist","")).arg(i->property("title",""))*/,this->getMainView());
		}
		if(this->songtimerid!=-1) {
			this->killTimer(this->songtimerid);
			this->songtimerid=-1;
			kdDebug()<<"song Not finished"<<endl;
		}
		int length=i->length();
		//Should always pass
		if(length>10000) {
			//here we would like to ensure we don't miss the song
			this->songtimerid=this->startTimer(length-10000);
		}

	}
	emit trackChange(i);
}
PlaylistItem SongList::previous() {
	if(this->previousSong.isEmpty())return 0;
	//First song on stack is the current one we want to go one back!
	this->previousSong.pop();
	if(this->previousSong.isEmpty())return 0;
	SafeListViewItem* i=0;
	PlaylistItem pl;
	while(!i&& !this->previousSong.isEmpty()) {
		pl=this->previousSong.pop();
		i=static_cast<SafeListViewItem*>(pl.data());
	}
	if(!i)return 0;
	this->setCurrentSong(i);
	//this->previousSong.pop();
	return pl;
}


