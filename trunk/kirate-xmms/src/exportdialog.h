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
#ifndef EXPORTDIALOG_H
#define EXPORTDIALOG_H

#include <kdialogbase.h>
#include "songlist.h"
#include <qcheckbox.h>
#include <qcombobox.h>

/**Class for exporting in m3u format maybe other to come
 
@author Matthias Studer
*/
class ExportDialog : public KDialogBase {
	Q_OBJECT
	public:
		ExportDialog(QWidget* parent,SongList* list);

		virtual ~ExportDialog();
	protected:
		QComboBox *minRating,*format;
		SongList* mList;
		QCheckBox * includeUnrated;
	protected slots:
		void slotOk();
		
};

#endif
