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
#include "exportdialog.h"
#include <qvbox.h>
#include <qhbox.h>
#include <kfiledialog.h>
#include <qfile.h>
#include <qtextstream.h>
#include <klocale.h>
#include <qlabel.h>
#include <kglobal.h>
#include <kiconloader.h>
#include <kmessagebox.h>
ExportDialog::ExportDialog(QWidget* parent, SongList * list)
		: KDialogBase(parent,"exportdlg",true,i18n("Export playlist"),KDialogBase::Ok|KDialogBase::Cancel) {
	this->mList=list;
	QVBox *box= new QVBox(this);
	//box->resize(400,400);
	QHBox *tmp= new QHBox(box);
	//tmp->setBaseSize(400,200);

	new QLabel(i18n("Minimum rating : "),tmp);
	this->minRating = new QComboBox(FALSE,tmp);

	KIconLoader *kil=KGlobal::instance()->iconLoader();
	this->minRating->insertItem(kil->loadIcon("rate2",KIcon::User,0,0,false),i18n("Yawn (All)"),0);
	this->minRating->insertItem(kil->loadIcon("rate5",KIcon::User,0,0,false),i18n("Not bad"),1);
	this->minRating->insertItem(kil->loadIcon("rate7",KIcon::User,0,0,false),i18n("Cool"),2);
	this->minRating->insertItem(kil->loadIcon("rate10",KIcon::User,0,0,false),i18n("Love it"),3);
	tmp= new QHBox(box);
	//tmp->setBaseSize(400,200);
	new QLabel(i18n("Format : "),tmp);
	this->format = new QComboBox(FALSE,tmp);
	this->format->insertItem(i18n("URL (good for sharing)"),0);
	this->format->insertItem(i18n("Absolute file"),1);
	this->includeUnrated = new QCheckBox(i18n("Include unrated tracks"),box);
	this->includeUnrated->setChecked(false);
	this->setMainWidget(box);
	//tmp->adjustSize();
	//box->adjustSize();
	//top->adjustSize();

}


ExportDialog::~ExportDialog() {}

void ExportDialog::slotOk() {
	QString fname=KFileDialog::getSaveFileName(QString::null,"*.m3u|"+i18n("Playlist"),this,i18n("Save as..."));
	if(fname.isEmpty()) {
		return;
	}
	QFile f(fname);
	if(!f.open(IO_WriteOnly)) {
		KMessageBox::sorry(this,i18n("Failed to open (Write acces) %1").arg(f.name()),i18n("File error"));
		return;
	}
	SafeListViewItem * i = static_cast<SafeListViewItem*>(this->mList->firstChild());
	QTextStream s(&f);
	float rating=2.0f;
	if(this->minRating->currentItem()==1) {
		rating=5.0f;
	} else if(this->minRating->currentItem()==2) {
		rating=7.0f;
	} else if(this->minRating->currentItem()==3) {
		rating=10.0f;
	}
	QString unrated;
	if(this->includeUnrated->isChecked()) unrated="13.0";
	else unrated="0.0";
	bool url = this->format->currentItem()==0;
	while(i) {
		if(i->property("rating",unrated).toFloat()>=rating) {
			if(url) {
				s<<i->getRealUrl()<<"\n";
			} else {
				s<<i->property("file")<<"\n";
			}
		}

		i=static_cast<SafeListViewItem*>(i->nextSibling());
	}
	f.flush();
	f.close();
	KMessageBox::information(this,i18n("File successfully created"),"Export");
	KDialogBase::slotOk();
}

