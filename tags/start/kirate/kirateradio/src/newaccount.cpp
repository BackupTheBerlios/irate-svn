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

#include <klocale.h>
#include "newaccount.h"

#include <qvariant.h>
#include <qpushbutton.h>
#include <klineedit.h>
#include <knuminput.h>
#include <qlabel.h>
#include <kurlrequester.h>
#include <qlistbox.h>
#include <qlayout.h>
#include <qtooltip.h>
#include <qwhatsthis.h>

/*
 *  Constructs a MyDialog1 as a child of 'parent', with the
 *  name 'name' and widget flags set to 'f'.
 *
 *  The dialog will by default be modeless, unless you set 'modal' to
 *  TRUE to construct a modal dialog.
 */
NewAccountBase::NewAccountBase( QWidget* parent, const char* name)
    : KDialogBase(parent,name,true,i18n( "New Account"),KDialogBase::Ok|KDialogBase::Cancel)
{
   // setSizeGripEnabled( TRUE );

    QWidget* privateLayoutWidget = new QWidget( this, "layout8" );
    privateLayoutWidget->setGeometry( QRect( 20, 20, 490, 450 ) );
    layout8 = new QVBoxLayout( privateLayoutWidget, 11, 6, "layout8"); 

    layout3 = new QGridLayout( 0, 1, 1, 0, 6, "layout3"); 

    userEdit = new KLineEdit( privateLayoutWidget, "userEdit" );

    layout3->addWidget( userEdit, 2, 1 );

    passEdit = new KLineEdit( privateLayoutWidget, "passEdit" );

    layout3->addWidget( passEdit, 3, 1 );

    portInput = new KIntNumInput( privateLayoutWidget, "portInput" );
    portInput->setValue( 2278 );
    portInput->setMinValue( 0 );

    layout3->addWidget( portInput, 1, 1 );

    hostEdit = new KLineEdit( privateLayoutWidget, "hostEdit" );
    hostEdit->setText("server.irateradio.org");

    layout3->addWidget( hostEdit, 0, 1 );

    textLabel1 = new QLabel( privateLayoutWidget, "textLabel1" );

    layout3->addWidget( textLabel1, 0, 0 );

    textLabel2 = new QLabel( privateLayoutWidget, "textLabel2" );

    layout3->addWidget( textLabel2, 1, 0 );

    textLabel4 = new QLabel( privateLayoutWidget, "textLabel4" );

    layout3->addWidget( textLabel4, 3, 0 );

    textLabel3 = new QLabel( privateLayoutWidget, "textLabel3" );

    layout3->addWidget( textLabel3, 2, 0 );
    layout8->addLayout( layout3 );

    layout7 = new QVBoxLayout( 0, 0, 6, "layout7"); 

    textLabel1_2 = new QLabel( privateLayoutWidget, "textLabel1_2" );
    textLabel1_2->setAlignment( int( QLabel::AlignVCenter ) );
    layout7->addWidget( textLabel1_2 );

    irateDirectory = new KURLRequester( privateLayoutWidget, "irateDirectory" );
    layout7->addWidget( irateDirectory );
    layout8->addLayout( layout7 );

    layout6 = new QVBoxLayout( 0, 0, 6, "layout6"); 

    textLabel2_2 = new QLabel( privateLayoutWidget, "textLabel2_2" );
    layout6->addWidget( textLabel2_2 );

    statusBox = new QListBox( privateLayoutWidget, "statusBox" );
    layout6->addWidget( statusBox );
    layout8->addLayout( layout6 );

   /* Layout1 = new QHBoxLayout( 0, 0, 6, "Layout1"); 
    Horizontal_Spacing2 = new QSpacerItem( 20, 20, QSizePolicy::Expanding, QSizePolicy::Minimum );
    Layout1->addItem( Horizontal_Spacing2 );

    buttonOk = new QPushButton( privateLayoutWidget, "buttonOk" );
    buttonOk->setAutoDefault( TRUE );
    buttonOk->setDefault( TRUE );
    Layout1->addWidget( buttonOk );

    buttonCancel = new QPushButton( privateLayoutWidget, "buttonCancel" );
    buttonCancel->setAutoDefault( TRUE );
    Layout1->addWidget( buttonCancel );
    layout8->addLayout( Layout1 );*/
    //setCaption(i18n( "New Account"));
    //languageChange();
    textLabel1->setText(i18n("Host :" ));
    textLabel2->setText(i18n("Port :" ));
    textLabel4->setText(i18n("Password :" ));
    textLabel3->setText(i18n("User (login) :"));
    textLabel1_2->setText(i18n( "Choose a directory :\n"
"We strongly recommand \n"
"choosing an non-existant directory."));
    textLabel2_2->setText( tr2i18n("Server Communication"));
    //resize( QSize(528, 491).expandedTo(minimumSizeHint()) );
    clearWState( WState_Polished );
    this->setMainWidget(privateLayoutWidget);

    // signals and slots connections
    /*connect( buttonOk, SIGNAL( clicked() ), this, SLOT( accept() ) );
    connect( buttonCancel, SIGNAL( clicked() ), this, SLOT( reject() ) );*/
}

/*
 *  Destroys the object and frees any allocated resources
 */
NewAccountBase::~NewAccountBase()
{
    // no need to delete child widgets, Qt does it all for us
}

/*
 *  Sets the strings of the subwidgets using the current
 *  language.
 */
/*void MyDialog1::languageChange()
{
    
    QToolTip::add( userEdit, tr2i18n( "Your username (you don't need to remember it)" ) );
    QToolTip::add( passEdit, tr2i18n( "Your password (you don't need to remember it)" ) );
    QToolTip::add( portInput, tr2i18n( "Server port (leave default)" ) );
    
    QToolTip::add( hostEdit, tr2i18n( "Server address (leave default)" ) );
    
    buttonOk->setText( tr2i18n( "&OK" ) );
    buttonOk->setAccel( QKeySequence( tr2i18n( "Alt+O" ) ) );
    buttonCancel->setText( tr2i18n( "&Cancel" ) );
    buttonCancel->setAccel( QKeySequence( QString::null ) );
}*/

//#include "newaccount.moc"
