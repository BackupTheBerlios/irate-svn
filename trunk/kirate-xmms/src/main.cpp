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


#include "view.h"
#include <kuniqueapplication.h>
#include <kaboutdata.h>
#include <kcmdlineargs.h>
#include <klocale.h>
#include <kdebug.h>
#include <qt_irate.h>

static const char description[] =
    I18N_NOOP("A KDE KPart Application");

static const char version[] = "0.1";

static KCmdLineOptions options[] =
{
//    { "+[URL]", I18N_NOOP( "Document to open" ), 0 },
    KCmdLineLastOption
};

int main(int argc, char **argv)
{
    KAboutData about("kirateradioxmms", I18N_NOOP("kirateradioxmms"), version, description,
		     KAboutData::License_GPL, "(C) %{YEAR} Matthias Studer", 0, 0, "matthias.studer@ezwww.ch");
    about.addAuthor( "Matthias Studer", 0, "matthias.studer@ezwww.ch" );
    KCmdLineArgs::init(argc, argv, &about);
    KCmdLineArgs::addCmdLineOptions( options );
    KUniqueApplication app;
    //View *mainWin = 0;
	
    if (app.isRestored())
    {
        RESTORE(View);
    }
    else
    {
    	kdDebug()<<"entering not restored"<<endl;
        // no session.. just start up normally
        KCmdLineArgs *args = KCmdLineArgs::parsedArgs();

        /// @todo do something with the command line args here
        //mainWin = new View();
        app.setMainWidget(View::singleInstance());
//	XmmsWrapper::init();
	View::singleInstance()->init();
	View::singleInstance()->show();
	//mainWin->init();
        //mainWin->show();

        args->clear();
	kdDebug()<<"finished not restored"<<endl;
    }
	
    // mainWin has WDestructiveClose flag by default, so it will delete itself.
    int ret=app.exec();
    //delete View::singleInstance();
    QT_IRate::instance()->closeAndQuitEngine();
    kdDebug()<<"cleaned"<<endl;
    return ret;
}

