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
#include "trackdatabase.h"
#include <kmessagebox.h>
#include <kuser.h>
#include <klocale.h>
#include <kurlrequester.h>
#include <kstandarddirs.h>

NewAccountDialog::NewAccountDialog(TrackDatabase * td,QWidget* parent, const char* name)
		: NewAccountBase(parent,name) {
	this->passEdit->setText(KApplication::randomString(8));
	KUser user;
	if(user.fullName().isEmpty()){
		this->userEdit->setText(user.loginName());
	}else this->userEdit->setText(user.fullName());
	this->mtd=td;
	this->irateDirectory->setMode(KFile::Directory|KFile::LocalOnly);
	this->irateDirectory->setURL(user.homeDir()+"/irate/");
	connect(this->mtd,SIGNAL(connectionState(int )),this,SLOT(connectionState(int )));
	connect(this->mtd,SIGNAL(serverError(QString, QString )),this,SLOT(serverError(QString, QString )));
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
	irateDir=KStandardDirs::realPath(irateDir);
	if(!irateDir.endsWith("/")){
		irateDir+='/';
	}
	this->mtd->setIRateDir(irateDir);
	int port=this->portInput->value();
	QString host=this->hostEdit->text();
	QString pass=this->passEdit->text();
	QString user=this->userEdit->text();
	this->mtd->setNewAccountValue(host,port,user,pass);
	this->enableInput(false);
	this->read=0;
	this->mtd->connectToServer();
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
	QString lfile=locate("html","en/kirateradio/"+url);
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
	this->enableInput(true);
}
void NewAccountDialog::connectionState(int code){
	bool failed=false;
	switch(code){
		case 1:
			this->statusBox->insertItem(i18n("Connecting to server : %1...\n").arg(this->hostEdit->text()));
		break;
		case 2:
			this->statusBox->insertItem(i18n("Connected !\n"));
			
		break;
		case 3:
			this->statusBox->insertItem(i18n("Reading server Response...\n"));
		break;
		case 4:
			this->statusBox->insertItem(i18n("Finished Reading Server Response...\n"));
			read++;
		break;
		case 5:
			if(!this->error&&this->read>0){
				KMessageBox::information(this,i18n("Account succesfully created"),i18n("Account created"));
				QDialog::accept();
			}
			else{
			//	this->enableInput(true);
				this->statusBox->insertItem(i18n("Connection closed by server !!!\n"));
				failed=true;
			}
		break;
		case 6:
			this->statusBox->insertItem(i18n("Socket Error !!! (verify that's a valid address and that you are connected\n"));
			failed=true;
		break;
		
	}
	if(failed){
		this->enableInput(true);
	}
}


#include "newaccountdialog.moc"

