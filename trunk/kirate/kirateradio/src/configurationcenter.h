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

#ifndef CONFIGURATIONCENTER_H
#define CONFIGURATIONCENTER_H

#include <kdialogbase.h>
#include "trackdatabase.h"
#include "downloadcenter.h"
#include <knuminput.h>
#include <qcheckbox.h>
#include <qcombobox.h>
#include "songlist.h"
#include <kfontrequester.h>
#include <kpushbutton.h>
//class SongList;
class ConfigurationCenter : public KDialogBase {
		Q_OBJECT

	public:
		ConfigurationCenter(SongList* l,TrackDatabase * mtd,View* parent );
		~ConfigurationCenter();
		/*$PUBLIC_FUNCTIONS$*/

	public slots:
		/*$PUBLIC_SLOTS$*/

	protected:
		/*$PROTECTED_FUNCTIONS$*/
		TrackDatabase *td;
		SongList* sl;
		View * view;
		KIntNumInput *maxSimDown,*mTimeout,*minUnrated,*autoDown,*playSize,*maxRetry,*unratedWeight,*osdDuration;
		QCheckBox *allowConnection,*showPassive,*usePlayed,*useExp,*useSqrtPlay;
		QComboBox* minRating,*osdSetting,*osdTemplate,*infoTemplate;
		KPushButton * osdTest;
		KFontRequester* pl_f;
		

	protected slots:
		virtual void slotApply();
		virtual void slotOk();
		void slotTestOSD();
		/*$PROTECTED_SLOTS$*/

};

#endif

