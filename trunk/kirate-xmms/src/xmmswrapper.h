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
#ifndef XMMSWRAPPER_H
#define XMMSWRAPPER_H

#include <qobject.h>
#include <qptrstack.h>
#include <qt_irate.h>
/**
@author Matthias Studer
*/
class XmmsWrapper : public QObject {
		Q_OBJECT
	public:
		XmmsWrapper(QObject *parent = 0, const char *name = 0);
		~XmmsWrapper();
		QTrack* current(){return tcur;}
		inline bool hasPrevious(){return !this->prevStack.isEmpty();}
		inline bool exiting(){return _mExiting;}
		inline void setExiting(bool exit){
			this->_mExiting=exit;
		}
		inline QTrack* previousTrack(){return this->prevStack.top();}
		void setCurrent(QTrack* t);
		bool init();
		bool isPlaying();
	public slots:
		void next();
		void previous();
		void pause();
		void play();
		void stop();
	signals:
		void trackChanged();
		void connectionLost();
	protected:
		void timerEvent(QTimerEvent*);

		QPtrStack<QTrack> prevStack;
		QTrack* tcur;
		QTrack* tnext;
		//bool hasPrevious;
		bool _mExiting;
		int checktimer;
		int songTimer;
};

#endif
