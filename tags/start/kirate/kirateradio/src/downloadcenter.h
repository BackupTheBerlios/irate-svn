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
#ifndef DOWNLOADCENTER_H
#define DOWNLOADCENTER_H

#include <qobject.h>
#include <qdict.h>
#include <kio/job.h>
#include <kio/jobclasses.h>
#include "safelistviewitem.h"
#include <qfile.h>
#include <time.h>
#include <qstring.h>
#include <qptrlist.h>
#include <qvaluelist.h>
#include <qevent.h>

/**Download message
 Actually should rewrite to have three level
 Info
 Warning
 Error
@author Matthias Studer
*/
class DownloadEvent:public QCustomEvent{
	public:
	enum MessageType{
		Percent=1102,
		Speed=1103,
		Finished=1104,
		TotalDownloadSpeed=1106,
		Info=1107,
		Timeout=1108,
		Error=1109
	};
	DownloadEvent(const MessageType &type,const QString& _msg):QCustomEvent(type),msg(_msg){}
	DownloadEvent(const MessageType &type,const QString& _msg, SafeListViewItem *it):QCustomEvent(type),msg(_msg),i(it){}
	virtual ~DownloadEvent(){}
	QString getMessage(){return this->msg;}
	SafeListViewItem * getItem(){return this->i;}
	bool isPercentMessage(){
		return this->type()==1102;
	}
	bool isSpeedMessage(){
		return this->type()==1103;
	}
	bool isFinishedMessage(){
		return this->type()==1104;
	}
	bool isTotalDownloadSpeedMessage(){
		return this->type()==1106;
	}
	bool isInfoMessage(){
		return this->type()==1107;
	}
	bool isTimeoutMessage(){
		return this->type()==1108;
	}
	bool isErrorMessage(){
		return this->type()==1109;
	}
	protected:
		QString msg;
		SafeListViewItem *i;
};


/** Class that handle a TransferJob and give simplified signals
*/
class JobDownload : public QObject{
	Q_OBJECT
public:
	JobDownload(KIO::FileCopyJob* job, SafeListViewItem* it, /*const QString& dest,*/QObject* parent,const int& curRetry);
	~JobDownload(){}
	unsigned long getSpeed(){return mBps;}
	/*bool dataReceived(){
		return this->f.isOpen();
	}*/
	void abortJob();
	SafeListViewItem* getItem(){return i;}
	time_t getLastReceived(){
		return this->lastReceived;
	}
	KURL getLocalUrl(){
		return this->mJob->destURL();
	}
	KURL getRemoteUrl(){
		return this->mJob->srcURL();
	}
	int getRetryCount(){return this->retry;}
public slots:
	void percent( KIO::Job *job, unsigned long percent);
	void jobDone( KIO::Job *job);
	void speed(KIO::Job *job, unsigned long bytes_per_second);
	void infoMessage(KIO::Job *job, const QString &msg);
	void processedSize(KIO::Job *job,KIO::filesize_t size);
	//void data( KIO::Job *job, const QByteArray &data);
signals:
	void si_percent(unsigned long percent,SafeListViewItem *it);
	void si_finish(JobDownload*);
	void si_speed(unsigned long bytes_per_second, SafeListViewItem *it);
	void si_infoMessage(const QString &msg, SafeListViewItem *it);
	void si_error(const QString& msg, JobDownload*,const int& error);
	
private:
	SafeListViewItem* i;
	//QFile f;
	//QString mdest;
	unsigned long mBps;
	KIO::filesize_t filesize;
	int retry;
	time_t lastReceived;
	KIO::FileCopyJob* mJob;
};
/** Download center is a singleton that is able to queue items
*   The number of concurrent downloads can be set throught the setConcurrentDownload methods
*   Timeout can be given
*/
class DownloadCenter : public QObject {
		Q_OBJECT
	public:
		DownloadCenter();

		~DownloadCenter();
		void enqueue(SafeListViewItem * i, const KURL& url, const QString& localfile);
		void setDownloadListener(QObject* listener){
			this->mListener=listener;
		}
		void clean(bool quietly=true);
		static DownloadCenter* instance();
		void setConcurrentDownload(const int& newConcurrentDownload){
			this->concurrentDownload=newConcurrentDownload;
		}
		void setTimeout(const unsigned int& newTimeout){
			this->mTimeout=newTimeout;
		}
		time_t getTimeout(){
			return this->mTimeout;
		}
		int getConcurrentDownload(){
			return this->concurrentDownload;
		}
		int getMaxRetry(){
			return this->maxRetry;
		}
		void setMaxRetry(const int& newMax){
			this->maxRetry=newMax;
		}
		bool getAllowConnect(){
			return this->allowConnect;
		}
		void setAllowConnect(const bool & ac){
			this->allowConnect=ac;
		}
	public slots:
		//void data( KIO::Job *job, const QByteArray &data);
		void percent(unsigned long percent,SafeListViewItem*);
		void jobDone(JobDownload*);
		void jobError(const QString& msg, JobDownload*,const int& error);
		void speed( unsigned long bytes_per_second,SafeListViewItem*);
		void infoMessage(const QString &msg,SafeListViewItem*);
		
	protected:
		//void nextJob();
		void timerEvent(QTimerEvent*);
		struct QueuedItem{
			SafeListViewItem* i;
			KURL src;
			QString dest;
			int retry;
		};
		void startJob(SafeListViewItem * i, const KURL& url, const QString& localfile,const int& curRetry=0);
		static DownloadCenter* mInstance;
		QObject* mListener;
		unsigned int concurrentDownload;
		time_t mTimeout;
		//int mTimeout;
		int maxRetry;
		unsigned long totalspeed;
		bool allowConnect;
		QPtrList<JobDownload> currentJobs;
		QValueList<QueuedItem> queued;
		QStringList blockedServer;
};

#endif
