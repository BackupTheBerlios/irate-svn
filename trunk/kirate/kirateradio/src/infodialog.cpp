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
#include "infodialog.h"
#include <kapplication.h>
#include <kstandarddirs.h>
#include <qfile.h>
#include <klocale.h>
#include <kurl.h>
#include <noatun/app.h>
#include "template.h"

InfoDialog::InfoDialog(QWidget *parent)
 : KDialogBase(parent,"Info",false,"info",KDialogBase::Ok)
{
	this->textBrowser=new KTextBrowser(this,"infobrowser",false);
	this->setMainWidget(this->textBrowser);
	this->setInitialSize(this->configDialogSize("irateinfo"));
	/*connect(this->textBrowser,SIGNAL(mailClick(const QString&, const QString& )),this,SLOT(mailClick(const QString&, const QString& )));
	connect(this->textBrowser,SIGNAL(urlClick(const QString& )),this,SLOT(urlClick(const QString& )));*/
}


InfoDialog::~InfoDialog()
{
	this->saveDialogSize("irateinfo");
}
void InfoDialog::showHelp(){
	
}
void InfoDialog::showAbout(){
	this->setCaption(i18n("About iRate Radio"));
	QString lfile=locate("html","en/kirateradio/irateabout.html");
	QFile f(lfile);
	f.open(IO_ReadOnly);
	//kdDebug()<<"LFile "<<lfile<<endl;
	this->textBrowser->setText(f.readAll());
	f.close();
	if(!this->isShown())this->show();
}
void InfoDialog::setText(const QString& text,const QString& title){
	this->textBrowser->setText(text);
	this->setCaption(title);
	if(!this->isShown())this->show();
}
void InfoDialog::slotOk(){
	this->hide();
}
//Reimplemented for later
void InfoDialog::mailClick(const QString &,const QString &address){
	napp->invokeMailer(KURL(address));
}
void InfoDialog::urlClick(const QString &url){
	napp->invokeBrowser(url);
}
void InfoDialog::closeEvent(QCloseEvent*){
	this->hide();
}
void InfoDialog::setFont(const QFont& f){
	this->textBrowser->setFont(f);
}
#include "infodialog.moc"
