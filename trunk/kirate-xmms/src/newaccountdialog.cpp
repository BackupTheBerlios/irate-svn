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


#include "newaccountdialog.h"
#include <kapplication.h>
//#include "trackdatabase.h"
#include <kmessagebox.h>
#include <kuser.h>
#include <klocale.h>
#include <kurlrequester.h>
#include <kstandarddirs.h>
#include "view.h"
//#include "plugin_kirateradio_impl.h"

NewAccountDialog::NewAccountDialog(QWidget* parent, const char* name)
		: NewAccountBase(parent,name) {
	this->passEdit->setText(KApplication::randomString(8));
	KUser user;
	if(user.fullName().isEmpty()){
		this->userEdit->setText(user.loginName());
	}else this->userEdit->setText(user.fullName());
	this->irateDirectory->setMode(KFile::Directory|KFile::LocalOnly);
	this->irateDirectory->setURL(user.homeDir()+"/irate/");
	connect(View::singleInstance()->getIRateSignaler(),SIGNAL(irateError(QString, QString )),this,SLOT(serverError(QString, QString )));
	connect(View::singleInstance()->getIRateSignaler(),SIGNAL(accountCreationMessage(QString, int )),this,SLOT(connectionState(QString, int )));
}

NewAccountDialog::~NewAccountDialog() {}


void NewAccountDialog::slotOk() {
	error=false;
	KURL irateURL(this->irateDirectory->url());
	if(!irateURL.isValid()||!irateURL.isLocalFile()||irateURL.directory().isEmpty()){
		KMessageBox::sorry(this,i18n("Sorry, %1 directory is not a correct entry.").arg(irateURL.url()),i18n("Can't create directory"));
		return;
	}
	QString irateDir=irateURL.path(1);
	irateDir=KStandardDirs::realPath(irateDir);
	/*QString irateDir=irateURL.path(1);
	irateDir=KStandardDirs::realPath(irateDir);
	QDir ird(irateDir);
	if(!ird.exists()) {
		if(!ird.mkdir(irateDir)){
			KMessageBox::sorry(this,i18n("Sorry, unable to create %1 directory, make sure you have the rights to create it or select another directory.").arg(irateDir),i18n("Can't create directory"));
			return;
		}
	}
	if(!ird.exists(irateDir+"download/")) {
		if(!ird.mkdir(irateDir+"download/")){
			KMessageBox::sorry(this,i18n("Sorry, unable to create %1 directory, make sure you have the rights to create it or select another directory.\nThis directory is required by iRate Radio. It will contain all the song downloaded.").arg(irateDir+"download/"), i18n("Can't create directory"));
			return;
		}
	}
	if(!ird.exists(irateDir+"templates/")) {
		if(!ird.mkdir(irateDir+"templates/")){
			KMessageBox::sorry(this,i18n("Sorry, unable to create %1 directory.\nThis directory isn't absolutely necessary. Create it manually if you want to use custom templates.").arg(irateDir+"templates/"), i18n("Can't create directory"));
		}
	}
	KUser userAcc;
	ird.mkdir(userAcc.homeDir()+"/irate", TRUE);
	irateDir=KStandardDirs::realPath(irateDir);
	if(!irateDir.endsWith("/")){
		irateDir+='/';
	}
	QFile firate(userAcc.homeDir()+"/irate/irate.xml");
	firate.open(IO_WriteOnly);
	QString ir="<?xml version=\"1.0\"?>\n<irate><preference id=\"downloadDir\">"+irateDir+"trackdatabase.xml</preference><preference id=\"browser\">"+KStandardDirs::findExe("konqueror")+" %u</preference><plugin attached=\"false\" id=\"auto-normalize\"/><plugin localhostOnly=\"true\" simConns=\"20\" port=\"12473\" password=\"\" attached=\"false\" requirePassword=\"false\" id=\"external-control\"/><plugin attached=\"false\" id=\"lirc-remote\"><connect port=\"8765\" host=\"localhost\"/><function id=\"this-sux\"/><function id=\"yawn\"/><function id=\"not-bad\"/><function id=\"cool\"/><function id=\"love-it\"/><function id=\"pause/resume\"/><function id=\"skip\"/><function id=\"back\"/></plugin><plugin attached=\"true\" unratedNotificationMode=\"3\" id=\"unrated-notifier\"/></irate>\n";
	firate.writeBlock(ir.latin1(),ir.length());
	firate.close();
	this->mtd->setIRateDir(irateDir);
	*/
	
	int port=this->portInput->value();
	QString host=this->hostEdit->text();
	QString pass=this->passEdit->text();
	QString user=this->userEdit->text();
	QT_IRate::instance()->createNewAccount(user.latin1(),pass.latin1(),host.latin1(),port,irateDir.latin1());
	//this->mtd->setNewAccountValue(host,port,user,pass);
	this->enableInput(false);
	this->read=0;
	//this->mtd->connectToServer();
	//QDialog::accept();

}
void NewAccountDialog::enableInput(bool enable){
	this->hostEdit->setEnabled(enable);
	this->passEdit->setEnabled(enable);
	this->portInput->setEnabled(enable);
	this->userEdit->setEnabled(enable);
//	this->buttonOk->setEnabled(enable);
//	this->buttonCancel->setEnabled(enable);
	this->enableButtonOK(enable);
	this->enableButtonCancel(enable);
	this->irateDirectory->setEnabled(enable);
}
void NewAccountDialog::serverError(QString code,QString url){
	error=true;
	QString lfile=locate("html","en/kirateradioxmms/"+url);
	if(lfile.isEmpty()) {
		KMessageBox::sorry(this,i18n("The communication with the server as generated the following error\n code %1\nurl %2\n\nMore detailled error description is not yet available").arg(code).arg(url),i18n("iRate Server Error"));
		return;
	}
	QFile f(lfile);
	f.open(IO_ReadOnly);
	KMessageBox::sorry(this,f.readAll(),i18n("iRate Server Error"),KMessageBox::AllowLink);
	f.close();
	/*if(code=="user"){
		KMessageBox::information(this,i18n("You must change your username, it's already in use"),i18n("Username Error"));
	}*/
	//this->enableInput(true);
}
void NewAccountDialog::connectionState(QString status,int state){
	switch(state){
		case 1:
			this->statusBox->insertItem(status);
		break;
		case 2:
			this->statusBox->insertItem(i18n("Account succesfully created"));
			KMessageBox::information(this,i18n("Account succesfully created"),i18n("Account created"));
			QT_IRate::instance()->startDownloading();
			QDialog::accept();
			
		break;
		case 3:
			this->statusBox->insertItem(status);
			this->statusBox->insertItem(i18n("Account creation failed!!!"));
			this->enableInput(true);
			
		break;
	}
}


#include "newaccountdialog.moc"

