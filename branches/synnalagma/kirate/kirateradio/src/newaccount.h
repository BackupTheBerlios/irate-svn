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

#ifndef NEWACCOUNTBASE_H
#define NEWACCOUNTBASE_H

#include <qvariant.h>
#include <qdialog.h>
#include <kdialogbase.h>
class QVBoxLayout;
class QHBoxLayout;
class QGridLayout;
class QSpacerItem;
class KLineEdit;
class KIntNumInput;
class QLabel;
class KURLRequester;
class QListBox;
class QListBoxItem;
class QPushButton;

class NewAccountBase : public KDialogBase
{
  //  Q_OBJECT

public:
    NewAccountBase( QWidget* parent = 0, const char* name = 0);
    virtual ~NewAccountBase();

    KLineEdit* userEdit;
    KLineEdit* passEdit;
    KIntNumInput* portInput;
    KLineEdit* hostEdit;
    QLabel* textLabel1;
    QLabel* textLabel2;
    QLabel* textLabel4;
    QLabel* textLabel3;
    QLabel* textLabel1_2;
    KURLRequester* irateDirectory;
    QLabel* textLabel2_2;
    QListBox* statusBox;
    //QPushButton* buttonOk;
    //QPushButton* buttonCancel;

protected:
    QVBoxLayout* layout8;
    QGridLayout* layout3;
    QVBoxLayout* layout7;
    QVBoxLayout* layout6;
    QHBoxLayout* Layout1;
    //QSpacerItem* Horizontal_Spacing2;

/*protected slots:
    virtual void languageChange();
*/
};

#endif // NEWACCOUNTBASE_H
