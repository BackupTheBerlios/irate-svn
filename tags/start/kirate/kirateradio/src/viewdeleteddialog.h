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
#ifndef VIEWDELETEDDIALOG_H
#define VIEWDELETEDDIALOG_H

#include <kdialogbase.h>
#include <qlistbox.h>
#include "trackdatabase.h"
#include "songlist.h"
/**
@author Matthias Studer
*/
class ViewDeletedDialog : public KDialogBase {
	Q_OBJECT
	public:
		ViewDeletedDialog(QWidget* parent, SongList * list);

		virtual ~ViewDeletedDialog();
	protected slots:
		void slotOk();

	protected:
		SongList* mList;
		QListBox * lbox;
		QPtrList<TrackInfo> deletedList;
};

#endif
