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


#include "configurationcenter.h"
//#include "configuration.h"
#include <qvbox.h>
#include <qhbox.h>
#include <qlabel.h>
#include <klocale.h>
#include <kglobal.h>
#include <kconfig.h>
#include <kiconloader.h>
#include "template.h"
#include <qstringlist.h>
#include <kdebug.h>


ConfigurationCenter::ConfigurationCenter(SongList* l,TrackDatabase * mtd,View* parent): 
	KDialogBase(KDialogBase::IconList,WStyle_DialogBorder,parent,"ConfigurationDialog",i18n("Configuration center")), td(mtd),sl(l),view(parent){
	//Network
	QVBox * box1 =this->addVBoxPage(i18n("Network Options"),QString::null,KGlobal::iconLoader()->loadIcon("network",KIcon::Panel,KIcon::SizeMedium));
	QHBox * tmp = new QHBox(box1);
	new QLabel(i18n("Max. Simultaneous Downloads"),tmp);
	this->maxSimDown = new KIntNumInput(DownloadCenter::instance()->getConcurrentDownload(),tmp);
	this->maxSimDown->setMinValue(0);
	this->maxSimDown->setMaxValue(30);
	tmp = new QHBox(box1);
	new QLabel(i18n("Timeout (in sec.)"),tmp);
	this->mTimeout = new KIntNumInput(DownloadCenter::instance()->getTimeout(),tmp);
	this->mTimeout->setMinValue(0);
	tmp = new QHBox(box1);
	new QLabel(i18n("Max. number of retry"),tmp);
	this->maxRetry = new KIntNumInput(DownloadCenter::instance()->getMaxRetry(),tmp);
	this->maxRetry->setMinValue(0);
	this->allowConnection= new QCheckBox(i18n("Enable internet connexion"),box1);
	this->allowConnection->setChecked(sl->getAllowConnect());
	new QLabel(i18n("A value of 0 disable the corresponding function"),box1);
	//Irate
	box1 = this->addVBoxPage(i18n("iRate Options"),QString::null,KGlobal::iconLoader()->loadIcon("irate",KIcon::User,KIcon::SizeMedium));
	tmp = new QHBox(box1);
	new QLabel(i18n("Min. Number of Unrated"),tmp);
	this->minUnrated = new KIntNumInput(sl->getMinUnrated(),tmp);
	this->minUnrated->setMinValue(0);
	this->minUnrated->setMaxValue(20);
	tmp = new QHBox(box1);
	new QLabel(i18n("Auto Download"),tmp);
	this->autoDown = new KIntNumInput(this->td->getAutoDownload(),tmp);
	this->autoDown->setMaxValue(20);
	this->autoDown->setMinValue(1);
	tmp = new QHBox(box1);
	new QLabel(i18n("Playlist Size"),tmp);
	this->playSize = new KIntNumInput(this->td->getPlaylistSize(),tmp);
	
	//Playlist selection
	box1 = this->addVBoxPage(i18n("Playlist"),QString::null,KGlobal::iconLoader()->loadIcon("folder_sound",KIcon::Panel,KIcon::SizeMedium));
	tmp = new QHBox(box1);
	new QLabel(i18n("Unrated weight"),tmp);
	this->unratedWeight = new KIntNumInput(this->sl->getSelector()->getUnratedWeight(),tmp);
	this->unratedWeight->setMaxValue(30);
	this->unratedWeight->setMinValue(1);
	tmp = new QHBox(box1);
	new QLabel(i18n("Play song with rating higher or equal : "),tmp);
	this->minRating = new QComboBox(FALSE,tmp);
	KIconLoader *kil=KGlobal::instance()->iconLoader();
	this->minRating->insertItem(kil->loadIcon("rate2",KIcon::User,0,0,false),i18n("Yawn (All)"),0);
	this->minRating->insertItem(kil->loadIcon("rate5",KIcon::User,0,0,false),i18n("Not bad"),1);
	this->minRating->insertItem(kil->loadIcon("rate7",KIcon::User,0,0,false),i18n("Cool"),2);
	this->minRating->insertItem(kil->loadIcon("rate10",KIcon::User,0,0,false),i18n("Love it"),3);
	int mr=this->sl->getSelector()->getMinRating();
	if(mr==0)this->minRating->setCurrentItem(0);
	else if(mr==2)this->minRating->setCurrentItem(0);
	else if(mr==5)this->minRating->setCurrentItem(1);
	else if(mr==7)this->minRating->setCurrentItem(2);
	else if(mr==10)this->minRating->setCurrentItem(3);
	this->usePlayed = new QCheckBox(i18n("Care about played parameter"),box1);
	this->usePlayed->setChecked(this->sl->getSelector()->getCarePlayed());
	this->useExp = new QCheckBox(i18n("Use squared rating (select more often high rating)"),box1);
	this->useExp->setChecked(this->sl->getSelector()->getUseExp());
	this->useSqrtPlay = new QCheckBox(i18n("Use square root of played (played less important)"),box1);
	this->useSqrtPlay->setChecked(this->sl->getSelector()->getUseSqrtPlayed());
	box1 = this->addVBoxPage(i18n("Display"),QString::null,KGlobal::iconLoader()->loadIcon("tv",KIcon::Panel,KIcon::SizeMedium));
	new QLabel(i18n("Playlist font"),box1);
	this->pl_f= new KFontRequester(box1,"playlistFont",false);
	this->pl_f->setFont(this->sl->font());
	tmp = new QHBox(box1);
	new QLabel(i18n("Info Template"),tmp);
	this->infoTemplate=new QComboBox(FALSE,tmp);
	QStringList strlist=Template::instance()->getAvailableInfoTemplate();
	//kdDebug()<<strlist<<endl;
	this->infoTemplate->insertStringList(strlist);
	this->infoTemplate->setCurrentItem(strlist.findIndex(Template::instance()->getCurrentInfoTemplate()));
	if(strlist.isEmpty()){
		this->infoTemplate->setCurrentText(i18n("You don't have any template available"));
		this->infoTemplate->setEnabled(false);
	}
	this->showPassive = new QCheckBox(i18n("Show a passive window for unrated"),box1);
	this->showPassive->setChecked(sl->getShowPassivePopup());
	tmp = new QHBox(box1);
	new QLabel(i18n("OSD settings"),tmp);
	this->osdSetting= new QComboBox(FALSE,tmp);
	this->osdSetting->insertItem(i18n("Never"),0);
	this->osdSetting->insertItem(i18n("Always"),1);
	this->osdSetting->insertItem(i18n("Only for unrated"),2);
	this->osdSetting->setCurrentItem(this->view->getOSDMode());
	tmp = new QHBox(box1);
	new QLabel(i18n("OSD duration in msec."),tmp);
	this->osdDuration=new KIntNumInput(this->view->getOSD()->duration(),tmp);
	this->osdDuration->setMinValue(0);
	tmp = new QHBox(box1);
	new QLabel(i18n("OSD Templates"),tmp);
	this->osdTemplate=new QComboBox(FALSE,tmp);
	strlist=Template::instance()->getAvailableOSDTemplate();
	//kdDebug()<<strlist<<endl;
	this->osdTemplate->insertStringList(strlist);
	this->osdTemplate->setCurrentItem(strlist.findIndex(Template::instance()->getCurrentOSDTemplate()));
	this->osdTest= new KPushButton(i18n("Test OSD"),box1);
	if(strlist.isEmpty()){
		this->osdTemplate->setCurrentText(i18n("You don't have any template available"));
		this->osdTemplate->setEnabled(false);
		this->osdTest->setEnabled(false);
	}
	
	
	connect(this->osdTest,SIGNAL(pressed()),this,SLOT(slotTestOSD()));
	//this->
}

ConfigurationCenter::~ConfigurationCenter() {}

/*$SPECIALIZATION$*/
void ConfigurationCenter::slotApply(){
	KConfig * config=KGlobal::config();
	config->setGroup("irate");
	config->writeEntry("show_passive",this->showPassive->isChecked());
	config->writeEntry("concurrent_download",this->maxSimDown->value());
	config->writeEntry("download_timeout",this->mTimeout->value());
	config->writeEntry("min_unrated",this->minUnrated->value());
	config->writeEntry("max_retry",this->maxRetry->value());
	config->writeEntry("allow_connect",this->allowConnection->isChecked());
	//Download center
	
	DownloadCenter::instance()->setConcurrentDownload(this->maxSimDown->value());
	DownloadCenter::instance()->setTimeout(this->mTimeout->value());
	DownloadCenter::instance()->setMaxRetry(this->maxRetry->value());
	DownloadCenter::instance()->setAllowConnect(this->allowConnection->isChecked());
	//Irate option
	this->td->setAutoDownload(this->autoDown->value());
	this->td->setPlaylistSize(this->playSize->value());
	this->sl->setShowPassivePopup(this->showPassive->isChecked());
	this->sl->setMinUnrated(this->minUnrated->value());
	this->sl->setAllowConnect(this->allowConnection->isChecked());
	//Playlist option
	BasicSelection* select=this->sl->getSelector();
	select->setCarePlayed(this->usePlayed->isChecked());
	select->setUseExp(this->useExp->isChecked());
	select->setUnratedWeight(this->unratedWeight->value());
	select->setUseSqrtPlayed(this->useSqrtPlay->isChecked());
	int mr=this->minRating->currentItem();
	int val=0;
	if(mr==0)val=2;
	else if(mr==1)val=5;
	else if(mr==2)val=7;
	else if(mr==3)val=10;
	select->setMinRating(val);
	config->writeEntry("select_min_rating",val);
	config->writeEntry("select_care_played",this->usePlayed->isChecked());
	config->writeEntry("select_use_exp",this->useExp->isChecked());
	config->writeEntry("select_unrated_weight",this->unratedWeight->value());
	config->writeEntry("select_use_sqrt_play",this->useSqrtPlay->isChecked());
	config->writeEntry("display_font",this->pl_f->font());
	config->writeEntry("osd_mode",this->osdSetting->currentItem());
	this->sl->setFont(this->pl_f->font());
	config->writeEntry("osd_duration",this->osdDuration->value());
	if(this->osdTemplate->isEnabled()){
		config->writeEntry("osd_templ_file",this->osdTemplate->currentText());
		Template::instance()->loadOSDTemplate(this->osdTemplate->currentText());
	}
	if(this->infoTemplate->isEnabled()){
		config->writeEntry("info_templ_file",this->infoTemplate->currentText());
		Template::instance()->loadInfoTemplate(this->infoTemplate->currentText());
	}
	config->sync();
}
void ConfigurationCenter::slotOk(){
	this->slotApply();
	KDialogBase::slotOk();
}
void ConfigurationCenter::slotTestOSD(){
	QFile f(Template::instance()->getTemplateDir()+this->osdTemplate->currentText());
	f.open(IO_ReadOnly);
	this->view->getOSD()->showOSD(Template::instance()->fastTemplate(QString(f.readAll()),static_cast<SafeListViewItem*>(sl->currentItem())),true);
	f.close();
	
}


#include "configurationcenter.moc"

