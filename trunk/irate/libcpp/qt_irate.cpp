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
#include <qt_irate.h>
#include <gcj/cni.h>
 

void QTIRateSignaler::customEvent(QCustomEvent * e) {
	int type=e->type()-QEvent::User-2307;
	if(type<0)return;
	void irateError(QString,QString);
		void trackUpdated(QTrack *);
		void trackDownloadFinished(QTrack *,bool);
		void trackDownloadProgressed(QTrack *,int);
		void trackDownloadStarted(QTrack *);
		void accountCreationMessage(QString ,int);
	if(type==_IR_ERROR_EVENT){
		QTIRateEventError* ev2=(QTIRateEventError*)e;
		emit irateError(ev2->getCode(),ev2->getUrl());
	}
	else if(type==_IR_NEWACC_EVENT){
		QTIRateEventAccountCreation *ev=(QTIRateEventAccountCreation*)e;
		emit accountCreationMessage(ev->getStatut(),ev->getState());
	}
	else if(type==_IR_UPDATE_EVENT){
		QTIRateEvent* ev=(QTIRateEvent*)e;
		emit trackUpdated(ev->getQTrack());
	}
	else if(type==_IR_DOWFINI_EVENT){
		QTIRateEvent* ev=(QTIRateEvent*)e;
		emit trackDownloadFinished(ev->getQTrack(),ev->getOtherValue()!=0);
	}
	else if(type==_IR_DOWPRO_EVENT){
		QTIRateEvent* ev=(QTIRateEvent*)e;
		emit trackDownloadProgressed(ev->getQTrack(),ev->getOtherValue());
	}
	else if(type==_IR_DOWSTAR_EVENT){
		QTIRateEvent* ev=(QTIRateEvent*)e;
		emit trackDownloadStarted(ev->getQTrack());
	}
}
QString qt_decodeAsQString(string_jt str){
	QString s;
	s.setUnicodeCodes((ushort*)JvGetStringChars((jstring)str),((jstring)str)->length());
	return s;
}
bool qt_isNullOrEmpty(const QString& s){return s.isEmpty();}

QTIRateSignaler * initQTIRateEngine(){
	QT_IRate::initEngine(qt_isNullOrEmpty,qt_decodeAsQString);
	QTIRateSignaler * signaler = new QTIRateSignaler(0,"IRateSignaler");
	QT_IRate::instance()->addListener(signaler);
	return signaler;
}
