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


// Abandon All Hope, Ye Who Enter Here

#include <qheader.h>
#include <kdebug.h>
#include <klocale.h>
#include <kmenubar.h>

#include <kstandarddirs.h>
#include <kaction.h>
#include <kedittoolbar.h>
#include <kmessagebox.h>
#include <qfile.h>
#include <kapplication.h>

#include <kuser.h>
#include <qdir.h>
#include <kstatusbar.h>

#include <kconfig.h>
#include <kinstance.h>
#include <kiconloader.h>
#include "view.h"
#include "xmmswrapper.h"
#include "configurationcenter.h"
#include <ktextbrowser.h>
#include "exportdialog.h"
#include "template.h"
View * View::_minstance=NULL;
View * View::singleInstance() {if(!_minstance)_minstance=new View();return _minstance;}

View::View(): KMainWindow(0,0) {
	xmms=new XmmsWrapper();
	list=new SongList(this);
	setCentralWidget(list);
	//kdDebug()<<"ACtion collection"<<this->actionCollection()->count()<<endl;
	// connect the click on the header with sorting
	connect(list->header(),SIGNAL(clicked(int)),this,SLOT(headerClicked(int)) );
	connect(list, SIGNAL(executed(QListViewItem*)), SLOT(listItemSelected(QListViewItem*)));
	this->mConf= new KAction(i18n("Configure iRate"), KGlobal::iconLoader()->loadIcon("configure",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(showPref()),actionCollection(),"prefs");
	this->mConf->plug(this->toolBar("irate"));
	this->mAbout = new KAction(i18n("About iRate Radio"), KGlobal::iconLoader()->loadIcon("irate",KIcon::User,0,KIcon::ActiveState,0,false),0,this,SLOT(showAboutApplication()),actionCollection(),"about_irate");
	this->mAbout->plug(this->toolBar("irate"));
	this->mExport = new KAction(i18n("Export as m3u..."), KGlobal::iconLoader()->loadIcon("fileexport",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(showExport()),actionCollection(),"export");
	this->mViewCurrent= new KAction(i18n("View current"),KGlobal::iconLoader()->loadIcon("goto",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(viewCurrent()),actionCollection(),"view_current");
	//Player action
	this->mPrevious= new KAction(i18n("Previous track"), KGlobal::iconLoader()->loadIcon("player_rew",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(previous()),actionCollection(),"prefs");
	this->mPrevious->plug(this->toolBar("player"));
	this->mPlay= new KAction(i18n("Play"), KGlobal::iconLoader()->loadIcon("player_play",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(play()),actionCollection(),"prefs");
	this->mPlay->plug(this->toolBar("player"));
	this->mPause= new KAction(i18n("Pause"), KGlobal::iconLoader()->loadIcon("player_pause",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(pause()),actionCollection(),"prefs");
	this->mPause->plug(this->toolBar("player"));
	this->mNext= new KAction(i18n("Next track"), KGlobal::iconLoader()->loadIcon("player_fwd",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(forward()),actionCollection(),"prefs");
	this->mNext->plug(this->toolBar("player"));
	/*this->noa_back=NoatunStdAction::back(this,"noa_back");
	this->noa_back->plug(this->toolBar("noatun"));
	this->noa_playp=NoatunStdAction::playpause(this,"noa_play_pause");
	this->noa_playp->plug(this->toolBar("noatun"));
	this->noa_for=NoatunStdAction::forward(this,"noa_forward");
	this->noa_for->plug(this->toolBar("noatun"));
	this->noa_stop=NoatunStdAction::stop(this,"noa_stop");
	this->noa_stop->plug(this->toolBar("noatun"));*/
	this->mExport->plug(this->toolBar("irate"));
	this->mViewCurrent->plug(this->toolBar("irate"));
	this->mFile= new KPopupMenu(this);
	this->mExport->plug(mFile);
	this->menuBar()->insertItem(i18n("&File"),this->mFile);
	this->mSetting= new KPopupMenu(this);
	this->mConf->plug(this->mSetting);
	this->mAbout->plug(this->mSetting);
	this->menuBar()->insertItem(i18n("&Settings"),this->mSetting);
	this->statusBar()->insertItem(i18n("Welcome on iRate Radio"),0);
	this->setCaption(i18n("iRate Radio"));
	this->ktb= new InfoDialog(0);
	this->ktb->setIcon(KGlobal::iconLoader()->loadIcon("irate",KIcon::User,0,KIcon::ActiveState,0,false));

	applyMainWindowSettings(KGlobal::config(), "iRateRadio Window");
	list->setFocus();
	this->m_osd= new OSDWidget(QString::null,0,"osdshow");
	this->osdMode=1;
	connect(this->m_osd,SIGNAL(linkActivated(const QString&)),this,SLOT(OSDLinkClicked(const QString&)));
	connect(this->xmms,SIGNAL(trackChanged()),this,SLOT(trackChanged()));
	connect(this->xmms,SIGNAL(connectionLost()),this,SLOT(connectionLost()));
}


View::~View() {
	exiting=true;
	saveMainWindowSettings(KGlobal::config(), "iRateRadio Window");
	hide();
	delete mConf;
	delete mAbout;
	delete mExport;
	delete ktb;
	delete list;
	delete m_osd;
	delete this->mViewCurrent;
}
//Mainly load all config stuff
void View::init() {
	this->setIcon(KGlobal::iconLoader()->loadIcon("irate",KIcon::User,0,KIcon::ActiveState,0,false));
	exiting=false;
	KUser user;
	KConfig * config= KGlobal::config();
	this->_mirsign=initQTIRateEngine();

	connect(this->_mirsign,SIGNAL(irateError(QString, QString )),this->list,SLOT(handleError(QString, QString )));
	connect(this->_mirsign,SIGNAL(trackDownloadFinished(QTrack*, bool )),this->list ,SLOT(downloadFinished(QTrack*, bool )));
	connect(this->_mirsign,SIGNAL(trackDownloadProgressed(QTrack*, int )),this->list,SLOT(downloadProgressed(QTrack*, int )));
	connect(this->_mirsign,SIGNAL(trackDownloadStarted(QTrack* )),this->list,SLOT(downloadStarted(QTrack* )));
	connect(this->_mirsign,SIGNAL(trackUpdated(QTrack* )),this->list,SLOT(updateTrack(QTrack* )));
	/*connect(view->listView(), SIGNAL(executed(QListViewItem*)), SLOT(listItemSelected(QListViewItem*)));
	connect(view, SIGNAL(shown()), SIGNAL(listShown()));
	connect(view, SIGNAL(hidden()), SIGNAL(listHidden()));*/

	config->setGroup("irate");
	this->list->setShowPassivePopup(config->readBoolEntry("show_passive",true));
	QFont ft=config->readFontEntry("display_font");
	this->list->setFont(ft);
	this->list->init();//user.homeDir()+"/irate/");
	xmms->init();
	//Needed since list->init change it
	config->setGroup("irate");
	Template::instance()->init(user.homeDir()+"/irate/templates/",KGlobal::instance()->dirs()->findResourceDir("data","kirateradioxmms/pics/irate.png")+"kirateradioxmms/pics/");
	Template::instance()->loadOSDTemplate(config->readEntry("osd_templ_file","default.osd"));
	Template::instance()->loadInfoTemplate(config->readEntry("info_templ_file","default.tmpl"));
	//kdDebug()<<Template::instance()->getCurrentOSDTemplate()<<" info : "<<Template::instance()->getCurrentInfoTemplate()<<endl;
	this->ktb->setFont(ft);
	this->osdMode=config->readNumEntry("osd_mode",1);
	ft=config->readFontEntry("osd_font");
	this->m_osd->setFont(ft);
	this->m_osd->setDuration(config->readNumEntry("osd_duration",5000));

	/*	KPopupMenu* kpm=napp->pluginMenu();
		kpm->insertItem(i18n("This Sux"),this,SLOT(rateSux()));
		kpm->insertItem(i18n("Yawn"),this,SLOT(rateYawn()));
		kpm->insertItem(i18n("Not Bad"),this,SLOT(rateNotBad()));
		kpm->insertItem(i18n("Cool"),this,SLOT(rateCool()));
		kpm->insertItem(i18n("Love It"),this,SLOT(rateLove()));*/
	KIconLoader *kil=KGlobal::instance()->iconLoader();
	this->tray=new KSystemTray(this,"IRateSysTray");
	this->tray->setPixmap(kil->loadIcon("iratetray",KIcon::User,0,KIcon::ActiveState,0,false));
	KPopupMenu* mPopup=tray->contextMenu();
	mPopup->insertTitle(i18n("Rate current track"),0,0);
	mPopup->insertItem(kil->loadIconSet("info",KIcon::Small,0,false),i18n("Info"),1,1);
	mPopup->insertItem(kil->loadIconSet("edittrash",KIcon::Small,0,false),i18n("This sux"),2,2);
	mPopup->insertItem(kil->loadIconSet("rate2",KIcon::User,0,false),i18n("Yawn"),3,3);
	mPopup->insertItem(kil->loadIconSet("rate5",KIcon::User,0,false),i18n("Not bad"),4,4);
	mPopup->insertItem(kil->loadIconSet("rate7",KIcon::User,0,false),i18n("Cool"),5,5);
	mPopup->insertItem(kil->loadIconSet("rate10",KIcon::User,0,false),i18n("Love it"),6,6);
	mPopup->insertTitle(i18n("Player"),7,7);
	mPopup->insertItem(kil->loadIconSet("player_fwd",KIcon::Small,0,false),i18n("Next"),8,8);
	mPopup->insertItem(kil->loadIconSet("player_pause",KIcon::Small,0,false),i18n("Pause"),9,9);
	mPopup->insertItem(kil->loadIconSet("player_play",KIcon::Small,0,false),i18n("Play"),10,10);
	mPopup->insertItem(kil->loadIconSet("player_rew",KIcon::Small,0,false),i18n("Previous"),11,11);
	/*mPopup->insertTitle(i18n("Other"),12,12);
	mPopup->insertItem(kil->loadIconSet("exit",KIcon::Small,0,false),i18n("Quit"),13,13);*/
	connect(mPopup,SIGNAL(activated(int)),this,SLOT(processMenu(int )));
	connect(this->tray,SIGNAL(quitSelected()),this,SLOT(exit()));
	this->tray->show();
	this->play();
}
void View::exit() {
	KApplication::exit(0);
}
void View::processMenu(int id) {
	QString title;
	SafeListViewItem *tmpItem;
	kdDebug()<<"activated id "<<id<<endl;
	switch(id) {
	case 1:
		tmpItem=this->list->getSafeListViewItem(xmms->current());
		if(!tmpItem)return;
		title=tmpItem->property("title")+" - "+tmpItem->property("artist");
		this->getInfoDialog()->setText(Template::instance()->infoTemplate(tmpItem),title);
		break;
	case 2:
		this->rateSux();
		break;
	case 3:
		this->rateYawn();
		break;
	case 4:
		this->rateNotBad();
		break;
	case 5:
		this->rateCool();
		break;

	case 6:
		this->rateLove();
		break;
	case 8:
		this->forward();
		break;
	case 9:
		this->pause();
		break;
	case 10:
		this->play();
		break;
	case 11:
		this->previous();
		break;

	}
}

void View::closeEvent(QCloseEvent* ) {
	hide();
	//KMainWindow::closeEvent(e);
}

void View::showEvent(QShowEvent *e) {
	KMainWindow::showEvent(e);
	//emit shown();
}

void View::hideEvent(QHideEvent *e) {
	KMainWindow::hideEvent(e);
	//emit hidden();
}


// turns the sorting on or off
void View::setSorting(bool on, int column) {
	if (on) {
		list->setSorting(column,true);
		list->setShowSortIndicator(true);
	} else {
		list->setShowSortIndicator(false);
		list->setSorting(-1);
	}
}

void View::headerClicked(int column) {
	// this is to avoid that if we already have it sorted,
	// we sort it again ascendingly this way, clicking on
	// the header a second time will correctly toggle
	// ascending/descending sort
	if (list->showSortIndicator()) {
		return;
	} else {
		setSorting(true,column);
	}
}
void View::trackChanged() {
	SafeListViewItem *i=this->list->getSafeListViewItem(xmms->current());
	if(!i)return;
	QString str(i->property("artist","")+" - "+i->property("title",""));
	QString song="["+(i->getStringRating())+"] - "+str;
	this->setCaption(song+" / iRate Radio");
	this->statusBar()->changeItem(i18n("Now playing : %1").arg(song),0);
	if(this->osdMode==1||(this->osdMode==2&&i->getTrack()->isRated())) {
		//this->m_osd->setLinkData(i);
		//if(i!=IratePlugin::SPL()->current().data()){
		this->m_osd->showOSD(Template::instance()->osdTemplate(i),true);
		//}

	}
	this->setCurrentTrack(xmms->current(),false);
	//this->ct->setText(song);
}
void View::setCurrentTrack(QTrack *t,bool notify) {
	if(exiting)return;
	SafeListViewItem *now;
	if(notify)now=this->list->getSafeListViewItem(xmms->current());
	else now=now=this->list->getSafeListViewItem(xmms->previousTrack());
	if (now) {
		now->setPixmap(0,QPixmap());
		QRect rect(this->listView()->itemRect(now));
		rect.setWidth(this->listView()->viewport()->width());
		this->listView()->viewport()->repaint(rect,true);
	}
	if(notify)xmms->setCurrent(t);
	now=this->list->getSafeListViewItem(t);
	if(now) {
		this->list->setCurrentSong(t);
	}

}
void View::safeRemoveTrack(QTrack*t) {
	if(!exiting&&xmms->current()==t) {
		xmms->next();
	}
}
void View::showPref() {
	ConfigurationCenter cc(this);
	//cc->show();
	cc.exec();
	//	delete cc;
	//Two times are needed (I know ugly hack)
}
void View::showAboutApplication() {
	this->ktb->showAbout();
}
void View::showExport() {
	ExportDialog * ed= new ExportDialog(this,this->list);
	ed->exec();
	delete ed;
}
void View::rateSux() {
	QTrack* t=xmms->current();
	if(!t)return;
	t->setRating(0);
	//SafeListViewItem *i = this->li
}
void View::rateYawn() {
	QTrack* t=xmms->current();
	if(!t)return;
	t->setRating(2);
}
void View::rateNotBad() {
	QTrack* t=xmms->current();
	if(!t)return;
	t->setRating(5);
}
void View::rateCool() {
	QTrack* t=xmms->current();
	if(!t)return;
	t->setRating(7);
}
void View::rateLove() {
	QTrack* t=xmms->current();
	if(!t)return;
	t->setRating(10);
	//	this->list->updateRating();
}
void View::rate(int id) {
	switch(id) {
	case 0:
		this->rateSux();
		break;
	case 1:
		this->rateYawn();
		break;
	case 2:
		this->rateNotBad();
		break;
	case 3:
		this->rateCool();
		break;
	case 4:
		this->rateLove();
		break;

	}
}
void View::OSDLinkClicked(const QString& url) {
	if(url=="info") {
		SafeListViewItem *i =this->list->getSafeListViewItem(xmms->current());
		if(!i)return;
		this->ktb->setText(Template::instance()->infoTemplate(i),i18n("[%1] - %2").arg(i->property("artist")).arg(i->property("title")));
		this->ktb->show();
	} else if(url=="next") {
		this->forward();
	} else if(url=="back") {
		this->previous();
	}

}
void View::viewCurrent() {
	SafeListViewItem* i=this->list->getSafeListViewItem(xmms->current());
	if(i) {
		this->list->clearSelection();
		this->list->setSelected(i,TRUE);
		this->list->ensureItemVisible(i);
	}
}
void View::listItemSelected(QListViewItem *i) {
	SafeListViewItem * it=static_cast<SafeListViewItem*>(i);
	if(it)
		this->setCurrentTrack(it->getTrack(), true);
}
//Player actions
void View::forward() {
	xmms->next();
}
void View::playpause() {
	if(xmms->isPlaying())this->pause();
	else this->play();
}
void View::stop() {
	xmms->stop();
}
void View::play() {
	xmms->play();
}
void View::previous() {
	xmms->previous();
}
void View::pause() {
	xmms->pause();
}
void View::connectionLost() {
	KMessageBox::sorry(this,i18n("Can't find a running Xmms program, you should start Xmms before KiRateRadio-Xmms. Good by and have a nice day!"),i18n("Can't find Xmms"));
	this->exit();

}
#include "view.moc"

