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
#include <qt_irate.h>
#include "songlist.h"
#include <qfile.h>


ConfigurationCenter::ConfigurationCenter(View* parent): 
	KDialogBase(KDialogBase::IconList,WStyle_DialogBorder,parent,"ConfigurationDialog",i18n("Configuration center")), view(parent){
	//Irate
	QVBox *box1 = this->addVBoxPage(i18n("iRate Options"),QString::null,KGlobal::iconLoader()->loadIcon("irate",KIcon::User,KIcon::SizeMedium));
	playUnrated=new  QCheckBox(i18n("Play unrated tracks"),box1);
	playUnrated->setChecked(QT_IRate::instance()->isPlayingUnrated());
	box1 = this->addVBoxPage(i18n("Display"),QString::null, KGlobal::iconLoader()->loadIcon("tv",KIcon::Panel,KIcon::SizeMedium));
	new QLabel(i18n("Playlist font"),box1);
	this->pl_f= new KFontRequester(box1,"playlistFont",false);
	this->pl_f->setFont(this->view->listView()->font());
	QHBox *tmp = new QHBox(box1);
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
	this->showPassive->setChecked(view->listView()->getShowPassivePopup());
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
	QT_IRate::instance()->setPlayUnrated(this->playUnrated->isChecked());
	//Download center
	
	this->view->listView()->setShowPassivePopup(this->showPassive->isChecked());
	config->writeEntry("display_font",this->pl_f->font());
	config->writeEntry("osd_mode",this->osdSetting->currentItem());
	this->view->listView()->setFont(this->pl_f->font());
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
	this->view->getOSD()->showOSD(Template::instance()->fastTemplate(QString(f.readAll()),static_cast<SafeListViewItem*>(this->view->listView()->currentItem())),true);
	f.close();
	
}


#include "configurationcenter.moc"

