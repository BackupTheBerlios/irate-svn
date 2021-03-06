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

#ifndef NEWACCOUNTDIALOG_H
#define NEWACCOUNTDIALOG_H
#include <klineedit.h>
#include <knuminput.h>
#include <qlabel.h>
#include <qpushbutton.h>
#include <qt_irate.h>

#include "newaccount.h"
//#include "trackdatabase.h"
class NewAccountDialog : public NewAccountBase {
		Q_OBJECT

	public:
		NewAccountDialog(QWidget* parent = 0, const char* name = 0);
		~NewAccountDialog();
		/*$PUBLIC_FUNCTIONS$*/
		void enableInput(bool);
		
	public slots:
		/*$PUBLIC_SLOTS$*/
		void connectionState(QString status, int state);
		void serverError(QString code,QString url);
	protected:
		/*$PROTECTED_FUNCTIONS$*/
		bool error;
		int read;

	protected slots:
		/*$PROTECTED_SLOTS$*/
		virtual void slotOk();

};

#endif

