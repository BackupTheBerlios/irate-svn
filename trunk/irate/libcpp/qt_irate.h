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
#ifndef QT_IRATE_H
#define QT_IRATE_H
#include <qobject.h>
#include <qapplication.h>
#include <qevent.h>
#include <iratecenter.h>
/**
@author Matthias Studer
*/

#include <qobject.h>
#include <qstring.h>
#define _IR_ERROR_EVENT 0
#define _IR_NEWACC_EVENT 1
#define _IR_UPDATE_EVENT 2
#define _IR_DOWFINI_EVENT 3
#define _IR_DOWPRO_EVENT 4
#define _IR_DOWSTAR_EVENT 5

typedef IRateCenter<QString> QT_IRate;
typedef IRateTrack<QString> QTrack;
class QTIRateEventError:public QCustomEvent {
	public:
		QTIRateEventError(const QString& mcode,const QString& murl):
		QCustomEvent(QEvent::User+2307),url(murl),code(mcode) {}
		QString getUrl() {return this->url;}
		QString getCode() {return this->code;}
	protected:
		QString url;
		QString code;
};
class QTIRateEvent:public QCustomEvent {
	public:
		QTIRateEvent(const int& messageType,QTrack* t, const int& other):
		QCustomEvent(QEvent::User+2307+messageType),_mt(t),_mother(other) {}
		int getOtherValue() {return this->_mother;}
		QTrack* getQTrack() {return this->_mt;}
	protected:
		QTrack* _mt;
		int _mother;

};
class QTIRateEventAccountCreation:public QCustomEvent{
	public:
		QTIRateEventAccountCreation(const QString& mstatut,const int& mstate):
		QCustomEvent(QEvent::User+2307+_IR_NEWACC_EVENT),statut(mstatut),state(mstate) {}
		QString getStatut() {return this->statut;}
		int getState() {return this->state;}
	protected:
		QString statut;
		int state;
};
class QTIRateSignaler:public QObject, public IRateCenterListener<QString>{
	Q_OBJECT
	public:
		QTIRateSignaler(QObject * parent=0, const char * name=0):QObject(parent,name){}

		virtual ~QTIRateSignaler(){}
		virtual void updateTrack(IRateTrack<QString>* t){ 
			QApplication::postEvent(this,new QTIRateEvent(_IR_UPDATE_EVENT,t,0));
		}
		virtual void handleError(QString code,QString url){ 
			QApplication::postEvent(this,new QTIRateEventError(code,url));
		}
		virtual void downloadFinished(IRateTrack<QString>* t, bool s) { 
			QApplication::postEvent(this,new QTIRateEvent(_IR_DOWFINI_EVENT,t,s));
		}
		virtual void downloadProgressed(IRateTrack<QString>* t, const int& p) { 
			QApplication::postEvent(this,new QTIRateEvent(_IR_DOWPRO_EVENT,t,p));
		}
		virtual void downloadStarted(IRateTrack<QString>*t) { 
			QApplication::postEvent(this,new QTIRateEvent(_IR_DOWSTAR_EVENT,t,0));
		}
		virtual void newAccountCreationMessage(QString statut,int state){
			QApplication::postEvent(this,new QTIRateEventAccountCreation(statut,state));
		}
	signals:
		void irateError(QString,QString);
		void trackUpdated(QTrack *);
		void trackDownloadFinished(QTrack *,bool);
		void trackDownloadProgressed(QTrack *,int);
		void trackDownloadStarted(QTrack *);
		void accountCreationMessage(QString ,int);
	protected:
		virtual void customEvent(QCustomEvent * e);


};
QString qt_decodeAsQString(string_jt str);
bool qt_isNullOrEmpty(const QString& s);

QTIRateSignaler * initQTIRateEngine();

#endif
