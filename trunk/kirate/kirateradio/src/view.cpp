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

//#include <qdragobject.h>
#include <qheader.h>
//#include <qlayout.h>
//#include <qregexp.h>
//#include <qtextstream.h>
//#include <qpainter.h>

//#include <kaction.h>
#include <kdebug.h>
//#include <kfiledialog.h>
//#include <kfileitem.h>
//#include <kio/job.h>
//#include <kio/netaccess.h>
#include <klocale.h>
#include <kmenubar.h>
//#include <ksimpleconfig.h>
#include <kstandarddirs.h>
//#include <kstdaction.h>
#include <kaction.h>
#include <kedittoolbar.h>
//#include <kurldrag.h>
#include <kmessagebox.h>
//#include <qfileinfo.h>
#include <qfile.h>
#include <noatun/app.h>
#include <noatun/player.h>
#include <noatun/stdaction.h>
//#include <noatun/playlistsaver.h>
//#include <kuser.h>
#include <qdir.h>
#include <kstatusbar.h>

#include <kconfig.h>
#include <kinstance.h>
#include <kiconloader.h>
//#include "playlist.h"
#include "view.h"

//#include "find.h"
#include "downloadcenter.h"
#include "plugin_kirateradio_impl.h"
#include "configurationcenter.h"
//#include <kaboutdialog.h>

//#include <krootpixmap.h>
#include <ktextbrowser.h>
#include "exportdialog.h"
#include "template.h"
//#include <kde>

//class DownloadCenter;

View::View(IratePlugin *): KMainWindow(0,0) {

	list=new SongList(this);
	setCentralWidget(list);
	//kdDebug()<<"ACtion collection"<<this->actionCollection()->count()<<endl;
	// connect the click on the header with sorting
	connect(list->header(),SIGNAL(clicked(int)),this,SLOT(headerClicked(int)) );
	//connect(list,SIGNAL(contextMenu(KListView *l,QListViewItem *i,const QPoint &p)),this,SLOT(showMenu(KListView *l,QListViewItem *i,const QPoint &p)));
	connect(list,SIGNAL(trackChange(SafeListViewItem* )),this,SLOT(trackChanged(SafeListViewItem* )));
	connect(list,SIGNAL(downloadMessage(const QString& )),this,SLOT(downloadMessage(const QString&)));
	connect(list,SIGNAL(totalDownloadSpeed(const QString& )),this,SLOT(totalDownloadSpeed(const QString&)));
	//applyMainWindowSettings(KGlobal::config(), "SPL Window");
	this->mConf= new KAction(i18n("Configure iRate"), KGlobal::iconLoader()->loadIcon("configure",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(showPref()),actionCollection(),"prefs");
	this->mConf->plug(this->toolBar("irate"));
	this->mAbout = new KAction(i18n("About iRate Radio"), KGlobal::iconLoader()->loadIcon("irate",KIcon::User,0,KIcon::ActiveState,0,false),0,this,SLOT(showAboutApplication()),actionCollection(),"about_irate");
	this->mAbout->plug(this->toolBar("irate"));
	this->mExport = new KAction(i18n("Export as m3u..."),KGlobal::iconLoader()->loadIcon("fileexport",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(showExport()),actionCollection(),"export");
	//this->mViewDeleted= new KAction(i18n("View deleted tracks..."),KGlobal::iconLoader()->loadIcon("fileimport",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(showDeleted()),this->actionCollection(),"view_deleted");
	this->mViewCurrent= new KAction(i18n("View current"),KGlobal::iconLoader()->loadIcon("goto",KIcon::Small,0,KIcon::ActiveState,0,false),0,this,SLOT(viewCurrent()),actionCollection(),"view_current");
	//kdDebug()<<"ACtion collection"<<this->actionCollection()->count()<<endl;
	//NoatunStdAction::play(this,"noa_play");
	this->noa_back=NoatunStdAction::back(this,"noa_back");
	this->noa_back->plug(this->toolBar("noatun"));
	this->noa_playp=NoatunStdAction::playpause(this,"noa_play_pause");
	this->noa_playp->plug(this->toolBar("noatun"));
	this->noa_for=NoatunStdAction::forward(this,"noa_forward");
	this->noa_for->plug(this->toolBar("noatun"));
	this->noa_stop=NoatunStdAction::stop(this,"noa_stop");
	this->noa_stop->plug(this->toolBar("noatun"));
	//NoatunStdAction::pause(this,"noa_pause");
	//this->noa_effect=NoatunStdAction::effects(this,"noa_effect");
	//this->noa_effect->plug(this->toolBar("noatun"));
	//this->noa_eq=NoatunStdAction::equalizer(this,"noa_equalizer");
	//this->noa_eq->plug(this->toolBar("noatun"));
	
	this->mExport->plug(this->toolBar("irate"));
	//this->mViewDeleted->plug(this->toolBar("irate"));
	this->mViewCurrent->plug(this->toolBar("irate"));
	this->mFile= new KPopupMenu(this);
//	this->mViewDeleted->plug(mFile);
	this->mExport->plug(mFile);
	this->menuBar()->insertItem(i18n("&File"),this->mFile);
	this->mSetting= new KPopupMenu(this);
	this->mConf->plug(this->mSetting);
	this->mAbout->plug(this->mSetting);
	this->menuBar()->insertItem(i18n("&Settings"),this->mSetting);
	/*this->ct= new CyclicText("Welcome on iRate Radio"," ******* ",this->toolBar("song"),15,"cyclictext");
	this->toolBar("song")->insertWidget(0,this->ct->minimumWidth(),this->ct);*/

	//(void) KStdAction::configureToolbars(this, SLOT(configureToolBars()), actionCollection());
	/*KAction * about =KStdAction::aboutApp(this,SLOT(showAboutApplication()),actionCollection(),"aboutappirate");
	about->plug(this->toolBar());*/
	//list->setFocus();
	/*this->statusBar()->insertItem("Welcome",0);
	this->statusBar()->insertItem("      ",1);*/
	/*this->toolBar("rate")->insertButton(SafeListViewItem::IconMessage["rate0"],0,true,i18n("This Sux"));
	this->toolBar("rate")->insertButton(SafeListViewItem::IconMessage["rate2"],1,true,i18n("Yawn"));
	this->toolBar("rate")->insertButton(SafeListViewItem::IconMessage["rate5"],2,true,i18n("Not Bad"));
	this->toolBar("rate")->insertButton(SafeListViewItem::IconMessage["rate7"],3,true,i18n("Cool"));
	this->toolBar("rate")->insertButton(SafeListViewItem::IconMessage["rate10"],4,true,i18n("Love it"));
	connect(this->toolBar("rate"),SIGNAL(released(int )),this,SLOT(rate(int )));*/
	this->statusBar()->insertItem(i18n("Welcome on iRate Radio"),0);
	
	this->statusBar()->insertItem(i18n("0 Ko/s"),1,0,true);
	//this->statusBar()->setItemAlignment(2,Qt::AlignRight);
	this->setCaption(i18n("iRate Radio"));
	this->setIcon(KGlobal::iconLoader()->loadIcon("irate",KIcon::User,0,KIcon::ActiveState,0,false));
	//createGUI("irateradioui.rc");
	this->ktb= new InfoDialog(0);
	this->ktb->setIcon(KGlobal::iconLoader()->loadIcon("irate",KIcon::User,0,KIcon::ActiveState,0,false));
	
	applyMainWindowSettings(KGlobal::config(), "iRateRadio Window");
	//this->statusBar()->setItemFixed(0,this->statusBar()->fontMetrics().width(i18n("Welcome on iRate Radio"))*2);
	list->setFocus();
	this->m_osd= new OSDWidget(QString::null,0,"osdshow");
	//this->m_osd->setDuration(0);
	//this->m_osd->setIcon(KGlobal::iconLoader()->loadIcon("irate",KIcon::User,0,KIcon::ActiveState,0,false));
	this->osdMode=1;
	//connect(this->m_osd,SIGNAL(linkActivated()),this,SLOT(OSDLinkClicked()));
}


View::~View() {
	saveMainWindowSettings(KGlobal::config(), "iRateRadio Window");
	napp->player()->stop();
	hide();
	DownloadCenter::instance()->clean(true);
	delete mConf;
	delete mAbout;
	delete mExport;
	delete ktb;
	delete list;
	delete m_osd;
	delete this->mViewCurrent;
	//delete this->mViewDeleted;
}
//Mainly load all config stuff
void View::init() {
//	KUser user;
	KConfig * config= KGlobal::config();
	config->setGroup("irate");
	DownloadCenter::instance()->setConcurrentDownload(config->readNumEntry("concurrent_download",3));
	DownloadCenter::instance()->setTimeout(config->readNumEntry("download_timeout",60));
	DownloadCenter::instance()->setMaxRetry(config->readNumEntry("max_retry",10));
	DownloadCenter::instance()->setAllowConnect(config->readBoolEntry("allow_connect",true));
	this->list->setMinUnrated(config->readNumEntry("min_unrated",5));
	BasicSelection* select= this->list->getSelector();
	/*
	select->setCarePlayed(config->readBoolEntry("select_care_played",true));
	select->setMinRating(config->readNumEntry("select_min_rating",0));
	select->setUnratedWeight(config->readNumEntry("select_unrated_weight",15));
	select->setUseExp(config->readBoolEntry("select_use_exp",false));
	select->setUseSqrtPlayed(config->readBoolEntry("select_use_sqrt_play",false));
	*/
	select->setCarePlayed(true);
	select->setMinRating(0);
	select->setUnratedWeight(15);
	select->setUseExp(false);
	select->setUseSqrtPlayed(false);
	this->list->setShowPassivePopup(config->readBoolEntry("show_passive",true));
	this->list->setAllowConnect(config->readBoolEntry("allow_connect",true));
	QFont ft=config->readFontEntry("display_font");
	this->list->setFont(ft);
	this->list->init(config->readPathEntry("irate_directory",QString::null));//user.homeDir()+"/irate/");
	//Needed since list->init change it
	config->setGroup("irate");
	Template::instance()->init(list->getTD()->getIRateDir()+"templates/",KGlobal::instance()->dirs()->findResourceDir("data","noatun/pics/irate.png")+"noatun/pics/");
	Template::instance()->loadOSDTemplate(config->readEntry("osd_templ_file","default.osd"));
	Template::instance()->loadInfoTemplate(config->readEntry("info_templ_file","default.tmpl"));
	//kdDebug()<<Template::instance()->getCurrentOSDTemplate()<<" info : "<<Template::instance()->getCurrentInfoTemplate()<<endl;
	this->ktb->setFont(ft);
	this->osdMode=config->readNumEntry("osd_mode",1);
	ft=config->readFontEntry("osd_font");
	this->m_osd->setFont(ft);
	this->m_osd->setDuration(config->readNumEntry("osd_duration",5000));
	
	KPopupMenu* kpm=napp->pluginMenu();
	kpm->insertItem(i18n("This Sux"),this,SLOT(rateSux()));
	kpm->insertItem(i18n("Yawn"),this,SLOT(rateYawn()));
	kpm->insertItem(i18n("Not Bad"),this,SLOT(rateNotBad()));
	kpm->insertItem(i18n("Cool"),this,SLOT(rateCool()));
	kpm->insertItem(i18n("Love It"),this,SLOT(rateLove()));
}


void View::closeEvent(QCloseEvent*) {
	hide();
}

void View::showEvent(QShowEvent *) {
	emit shown();
}

void View::hideEvent(QHideEvent *) {
	emit hidden();
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
void View::trackChanged(SafeListViewItem* i) {
	QString str(i->property("artist","")+" - "+i->property("title",""));

	
	QString song="["+(i->getStringRating())+"] - "+str;
	this->setCaption(song+" / iRate Radio");
	this->statusBar()->changeItem(i18n("Now playing : %1").arg(song),0);
	if(this->osdMode==1||(this->osdMode==2&&i->isProperty("rating"))){
		//this->m_osd->setLinkData(i);
		//if(i!=IratePlugin::SPL()->current().data()){
			this->m_osd->showOSD(Template::instance()->osdTemplate(i),true);
		//}
		
	}
	//this->ct->setText(song);
}
void View::showPref() {
	ConfigurationCenter cc(this->list,this->list->td,this);
	//cc->show();
	cc.exec();
	//delete cc;
	//Two times are needed (I know ugly hack)
	this->list->updateRating();
	this->list->updateRating();
}
void View::showAboutApplication() {
	this->ktb->showAbout();
}
void View::showExport() {
	ExportDialog * ed= new ExportDialog(this,this->list);
	ed->exec();
	delete ed;
}
void View::configureToolBars() {
/*	saveMainWindowSettings(KGlobal::config(), "iRateRadio Window");
	KEditToolbar dlg(actionCollection(), "irateradioui.rc");
	connect(&dlg, SIGNAL(newToolbarConfig()), SLOT(newToolBarConfig()));
	dlg.exec();
	*/
}

void View::newToolBarConfig() {
/*	createGUI("irateradioui.rc");
	applyMainWindowSettings(KGlobal::config(), "iRateRadio Window");
	*/
}
void View::rateSux() {
	PlaylistItem pl=napp->player()->current();
	if(!pl)return;
	SafeListViewItem *i=static_cast<SafeListViewItem*> (pl.data());
	if(i) {
		i->setProperty("rating","0.0");
		i->remove();
		i->removeRef();
		this->list->updateRating();
	}
}
void View::rateYawn() {
	PlaylistItem pl=napp->player()->current();
	pl.setProperty("rating","2.0");
	this->list->updateRating();
}
void View::rateNotBad() {
	PlaylistItem pl=napp->player()->current();
	if(!pl)return;
	pl.setProperty("rating","5.0");
	this->list->updateRating();
}
void View::rateCool() {
	PlaylistItem pl=napp->player()->current();
	if(!pl)return;
	pl.setProperty("rating","7.0");
	this->list->updateRating();
}
void View::rateLove() {
	PlaylistItem pl=napp->player()->current();
	if(!pl)return;
	pl.setProperty("rating","10.0");
	this->list->updateRating();
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
void View::downloadMessage(const QString& /*message*/){
	//this->statusBar()->changeItem(message.left(60),0);
}
void View::totalDownloadSpeed(const QString& speed){
	this->statusBar()->changeItem(i18n("Total speed %1").arg(speed),1);
}
void View::OSDLinkClicked(){
	SafeListViewItem *i = static_cast<SafeListViewItem*>(IratePlugin::SPL()->current().data());//this->m_osd->linkData();
	this->ktb->setText(Template::instance()->infoTemplate(i),i18n("[%1] - %2").arg(i->property("artist")).arg(i->property("title")));
	this->ktb->show();
}
#include "viewdeleteddialog.h"
void View::showDeleted(){
	ViewDeletedDialog vdd(this,this->list);
	vdd.exec();
}
void View::viewCurrent(){
	SafeListViewItem* i=static_cast<SafeListViewItem*>(napp->player()->current().data());
	if(i){
		this->list->clearSelection();
		this->list->setSelected(i,TRUE);
		this->list->ensureItemVisible(i);
	}
}
#include "view.moc"

