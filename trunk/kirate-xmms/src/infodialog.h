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
#ifndef INFODIALOG_H
#define INFODIALOG_H

#include <kdialogbase.h>
#include <qfont.h>
#include <ktextbrowser.h>
/**
@author Matthias Studer
*/

class InfoDialog : public KDialogBase {
		Q_OBJECT
	public:
		InfoDialog(QWidget *parent = 0);
		virtual ~InfoDialog();
		virtual void setFont(const QFont& f);
	public slots:
		void showHelp();
		void showAbout();
		void setText(const QString& text,const QString& title);
		//Reimplemented for later
/*		void mailClick(const QString &name,const QString &address);
		void urlClick(const QString &url);*/
		void slotOk();
	protected:
		virtual void closeEvent(QCloseEvent*e);
		KTextBrowser * textBrowser;
};

#endif
