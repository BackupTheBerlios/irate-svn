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
#include "downloadcenter.h"
#include <kio/jobclasses.h>
#include <kio/scheduler.h>
#include <qfile.h>
//#include <stdio.h>
#include <qfileinfo.h>
#include <qdir.h>
#include <klocale.h>
#include <kmessagebox.h>
//#include "configuration.h"
#define DOWNLOAD_TIMEOUT 45
JobDownload::JobDownload(KIO::FileCopyJob * job, SafeListViewItem* it, /*const QString& dest,*/QObject* parent,const int& curRetry) :QObject(parent),i(it),retry(curRetry)/*,f(dest+".part"),mdest(dest)*/ {
	this->mJob=job;
	this->mBps=0;
	this->lastReceived=time(NULL);
	this->filesize=0;
	//connect(job,SIGNAL(data(KIO::Job *, const QByteArray &)),this,SLOT(data(KIO::Job*, const QByteArray& )));
	connect(job,SIGNAL(infoMessage(KIO::Job*, const QString& )),this,SLOT(infoMessage(KIO::Job*, const QString& )));
	connect(job,SIGNAL(percent(KIO::Job*, unsigned long )),this,SLOT(percent(KIO::Job*, unsigned long )));

	connect(job,SIGNAL(result(KIO::Job* )),this,SLOT(jobDone(KIO::Job* )));
	connect(job,SIGNAL(speed(KIO::Job*, unsigned long )),this,SLOT(speed(KIO::Job*, unsigned long )));
	connect(job,SIGNAL(processedSize(KIO::Job*,KIO::filesize_t)),this,SLOT(processedSize(KIO::Job*, KIO::filesize_t )));
}
void JobDownload::percent( KIO::Job *, unsigned long percent) {
	emit si_percent(percent,this->i);
}
void JobDownload::processedSize(KIO::Job *,KIO::filesize_t size) {
	if(size!=this->filesize) {
		this->filesize=size;
		this->lastReceived=time(NULL);
	}
}
void JobDownload::jobDone( KIO::Job *job) {
	/*if(f.isOpen())f.close();
	if(f.exists()){
		QDir dir;
		dir.rename(this->mdest+".part",this->mdest,true);
	}*/
	this->lastReceived=0;
	if(job->error()) {
		emit si_error(job->errorString(),this, job->error());
		return;
	}
	/*if(this->mJob->isErrorPage()){
		this->f.remove();
		emit si_error(i18n("File broken"),this->i,this);
		return;
	}*/
	emit si_finish(this);
	//i=0;
}
void JobDownload::speed(KIO::Job *, unsigned long bps) {
	this->mBps=bps;
	emit si_speed(bps,this->i);
}
void JobDownload::infoMessage(KIO::Job *, const QString &msg) {
	emit si_infoMessage(msg,this->i);
}
/*void JobDownload::data(KIO::Job *, const QByteArray &data){
	if(!f.isOpen()){
		f.open(IO_WriteOnly);
	}
	this->lastReceived=time(NULL);
	f.writeBlock(data);
	f.flush();
}*/
void JobDownload::abortJob() {
	this->mJob->kill();
	/*	if(f.isOpen()){
			f.close();
		}
		if(f.exists()){
			f.remove();
		}
		*/
}
DownloadCenter * DownloadCenter::mInstance= new DownloadCenter();
DownloadCenter::DownloadCenter()
		: QObject() {
	this->mListener=0;
	this->concurrentDownload=5;
	this->mTimeout=60;
	this->maxRetry=10;
	this->currentJobs.setAutoDelete(true);
	this->startTimer(1000);
}


DownloadCenter::~DownloadCenter() {
	this->clean(true);
}

void DownloadCenter::enqueue(SafeListViewItem * i, const KURL& url, const QString& localfile) {
	if(QFile::exists(localfile)) {
		if(this->mListener!=0) {
			QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Finished,i18n("Finished"),i));
		}
		return;
	}
	if(this->allowConnect && this->blockedServer.findIndex(url.host())==-1&&( this->concurrentDownload==0||this->currentJobs.count()<this->concurrentDownload)) {
		this->startJob(i,url,localfile);
	} else {
		QueuedItem q={i,url,localfile,0};
		this->queued.append(q);
	}

}
void DownloadCenter::startJob(SafeListViewItem * i, const KURL& url, const QString& localfile, const int& curRetry) {
	this->blockedServer.append(url.host());
	//jd->file.open(IO_WriteOnly);

	/*if(QFile::exists(localfile+".part")){
		QFile::remove(localfile+".part");
	}*/
	KIO::FileCopyJob* mJob=KIO::file_copy(url,KURL(localfile), -1,false,false,false);
	//KIO::get(url,false,false);

	JobDownload *jd= new JobDownload(mJob,i,/*localfile,*/this,curRetry);
	//connect(jd->mJob, SIGNAL(data(KIO::Job*, const QByteArray&)), SLOT(data(KIO::Job*, const QByteArray&)));
	connect(jd, SIGNAL(si_finish(JobDownload* )), this, SLOT(jobDone(JobDownload* )));
	connect(jd,SIGNAL(si_error(const QString&, JobDownload*,const int&)),this,SLOT(jobError(const QString&,  JobDownload*,const int&)));
	connect(jd, SIGNAL(si_percent(unsigned long, SafeListViewItem* )) ,this, SLOT(percent( unsigned long,SafeListViewItem* ) ));
	connect(jd, SIGNAL(si_infoMessage(const QString&, SafeListViewItem* )), this, SLOT(infoMessage(const QString&, SafeListViewItem* )));
	connect(jd, SIGNAL(si_speed(unsigned long, SafeListViewItem* )),this,SLOT(speed(unsigned long, SafeListViewItem* )));
	this->currentJobs.append(jd);
	printf("Started Job %s\n",url.url().latin1());
}
void DownloadCenter::clean(bool quietly) {
	JobDownload*jd= this->currentJobs.first();
	while(this->currentJobs.current()) {
		jd->abortJob();
		if(!quietly) {
			if(this->mListener!=0) {
				QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Error,i18n("Aborted"),jd->getItem()));
			}
		}
		this->currentJobs.remove();
		jd=this->currentJobs.current();
	}
	//	this->currentJobs.clear();
	this->queued.clear();
	this->blockedServer.clear();
}
DownloadCenter* DownloadCenter::instance() {
	return mInstance;
}
/*struct JobDownload{
	QFile file;
	SafeListViewItem* i;
	time_t lastData;
	KIO::Job *mJob;
};*/

#include "downloadcenter.moc"

void DownloadCenter::percent(unsigned long percent,SafeListViewItem* i) {
	if(this->mListener!=0) {
		QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Percent,i18n("%L1%").arg(percent),i));
	}
	//	this->mListener->downloaded(percent,i);
}
void DownloadCenter::jobDone(JobDownload* jd) {
	printf("Finished\n");
	this->blockedServer.remove(jd->getRemoteUrl().host());
	//jd->file.close();
	if(this->mListener!=0) {
		QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Finished,i18n("Finished"),jd->getItem()));
		//this->mListener->downloadFinished(i);
	}
	this->currentJobs.removeRef(jd);
	//delete jd;

}
void DownloadCenter::jobError( const QString& msg,JobDownload* jd,const int& error) {
	this->blockedServer.remove(jd->getRemoteUrl().host());
	//Error considered as unrecoverable
	if(error==KIO::ERR_DOES_NOT_EXIST||KIO::ERR_CYCLIC_LINK){
		if(this->mListener!=0) {
			QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Error,i18n("Unrecoverable Error : %3").arg(msg),jd->getItem()));
		}
		this->currentJobs.removeRef(jd);
		return;
	}
	//Here retry has to by check depending on error code
	//Hard documentation !
	//Actually always retry since iuma is really weak and errors are generated
	if(jd->getRetryCount()<this->maxRetry) {
		QueuedItem q={jd->getItem(),jd->getRemoteUrl(),jd->getLocalUrl().url(),jd->getRetryCount()+1};
		this->queued.append(q);
		//jd->abortJob();
		if(this->mListener!=0) {
			QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Info,i18n("Retry %1 of %2 after %3").arg(jd->getRetryCount()+1).arg(this->maxRetry).arg(msg),jd->getItem()));
		}

	}else{
		if(this->mListener!=0) {
			QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Error,i18n("Error after %1 retries. Error : %3").arg(this->maxRetry).arg(msg),jd->getItem()));
		}
	}
	this->currentJobs.removeRef(jd);
	//delete jd;

}
void DownloadCenter::speed(unsigned long bytes_per_second,SafeListViewItem* i) {
	if(this->mListener!=0) {
		//this->mListener->downloadSpeed(bytes_per_second,i);
		QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Speed,i18n("%1/s").arg(KIO::convertSize(bytes_per_second)),i));
	}
}
void DownloadCenter::infoMessage(const QString &msg,SafeListViewItem* i) {
	if(this->mListener!=0) {
		//this->mListener->downloadInfoMessage(msg,i);
		QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Info,msg,i));
	}

}

void DownloadCenter::timerEvent(QTimerEvent*) {
	JobDownload* jd=this->currentJobs.first();
	time_t cur= time(NULL);
	this->totalspeed=0;
	while(this->currentJobs.current()) {
		this->totalspeed+=jd->getSpeed();
		//printf("JDLast %d cur %d\n",jd->getLastReceived(),cur);
		if(
		this->mTimeout>0&& 
		jd->getLastReceived()!=0 && 
		(jd->getLastReceived()+this->mTimeout)<cur) {
			printf("Lost job %s in timer\n",jd->getItem()->property("title").latin1());
			this->blockedServer.remove(jd->getRemoteUrl().host());
			if(jd->getRetryCount()<this->maxRetry) {
				QueuedItem q={jd->getItem(),jd->getRemoteUrl(),jd->getLocalUrl().url(),jd->getRetryCount()+1};
				this->queued.append(q);
				jd->abortJob();
				if(this->mListener!=0) {
					QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Timeout,i18n("Retry %1 of %2").arg(jd->getRetryCount()+1).arg(this->maxRetry),jd->getItem()));
				}

			} else {
				jd->abortJob();
				if(this->mListener!=0) {
					//this->mListener->downloadTimeout(jd->getItem());
					QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::Error,i18n("Timed Out After %1").arg(this->maxRetry),jd->getItem()));
				}
			}
			this->currentJobs.remove();
			jd=this->currentJobs.current();
		} else jd=this->currentJobs.next();
	}
	if(this->mListener!=0) {
		QApplication::postEvent(this->mListener,new DownloadEvent(DownloadEvent::TotalDownloadSpeed,i18n("%1/s").arg(KIO::convertSize(this->totalspeed))));
		//this->mListener->totalDownloadSpeed(this->totalspeed);
	}
	if(!this->allowConnect)return;
	QValueList<QueuedItem>::iterator it=this->queued.begin();
	while( it!=this->queued.end() && (this->concurrentDownload==0 || this->currentJobs.count()<this->concurrentDownload)) {
		//QueuedItem q = queued.first();
		//queued.pop_front();
		if(this->blockedServer.findIndex((*it).src.host())==-1) {
			this->startJob((*it).i,(*it).src,(*it).dest,(*it).retry);
			it=this->queued.remove(it);
		} else ++it;
	}
	
}

