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
#include "viewdeleteddialog.h"
#include <klocale.h>
#include <qvbox.h>
#include <qlabel.h>

ViewDeletedDialog::ViewDeletedDialog(QWidget* parent, SongList * list)
		: KDialogBase(parent,"deleteddlg",true,i18n("View deleted tracks"),KDialogBase::Ok|KDialogBase::Cancel) {
	this->mList=list;
	QVBox *box= new QVBox(this);
	new QLabel(i18n("This are the deleted tracks select and choose Ok to reimport this tracks : "),box);
	this->lbox= new QListBox(box);
	this->lbox->setSelectionMode(QListBox::Extended);
	this->deletedList=list->getTD()->getDeleted();
	for(TrackInfo* ti=this->deletedList.first();ti;ti=this->deletedList.next()) {
		this->lbox->insertItem("["+ti->property("artist")+"] - "+ti->property("title"),this->deletedList.at());
	}
	this->setMainWidget(box);
}


ViewDeletedDialog::~ViewDeletedDialog() {}

void ViewDeletedDialog::slotOk() {
	for(int i=0;i<this->lbox->count();i++) {
		if(this->lbox->isSelected(i)) {
			TrackInfo* ti=this->deletedList.at(i);
			if(ti) {
				ti->setProperty("deleted","false");
				this->mList->addTrack(ti);
			}
		}
	}
	KDialogBase::slotOk();
}

