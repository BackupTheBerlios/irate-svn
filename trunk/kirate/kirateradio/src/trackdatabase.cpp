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
#include "trackdatabase.h"
//#include <qapplication.h>
//#include <stdio.h>
#include <qfile.h>
#include <qregexp.h>
#include <kfilterdev.h>
#include <qbuffer.h>
#include <kdebug.h>
#include <kglobal.h>
#include <qdatetime.h>
#include <klocale.h>
/**Hack to ensure valid string in XML representations
*/
QString encodeForXML(QString str) {
	static QRegExp amp("&(?!amp;|lt;|gt;|quot;|apos;)");
	str=str.replace('<',"&lt;").replace('>',"&gt;").replace('"',"&quot;").replace('\'',"&apos;");
	return str.replace(amp,"&amp;");
}
QStringList TrackInfo::xmlAttributes;

TrackInfo::TrackInfo(const QXmlAttributes& atts) {
	for(int i=0; i<atts.length();i++) {
		this->mProperties.insert(atts.qName(i),atts.value(i),true);
	}
}
void TrackInfo::addPlayed(){
	this->setProperty("played",QString::number(this->getPlayed()+1));
	QDateTime dt=QDateTime::currentDateTime();
	this->setProperty("last",dt.toString("yyyyMMddhhmmss"));
	this->setProperty("playdate",KGlobal::locale()->formatDateTime(dt));
}
void TrackInfo::calcDate(){
	QString date = this->property("last","");
	if(date.isEmpty()){
		this->setProperty("playdate",i18n("Not played"));
		return;
	}
	int year=date.left(4).toInt();
	int month = date.mid(4,2).toInt();
	int day=date.mid(6,2).toInt();
	int hour=date.mid(8,2).toInt();
	int min=date.mid(10,2).toInt();
	int sec=date.mid(12,2).toInt();
	this->setProperty("playdate",KGlobal::locale()->formatDateTime(QDateTime(QDate(year,month,day),QTime(hour,min,sec,0))));
	
}
void TrackInfo::saveAsString(QTextStream &s) {
	s<<"<Track ";
	//printf("%s\n",xmlAttributes.join("/").latin1());
	for(QMap<QString,QString>::iterator it=this->mProperties.begin();it!=this->mProperties.end();++it) {
		//printf("%s => %s\n",it.key().latin1(),it.data().latin1());
		if(xmlAttributes.contains(it.key())!=0) {
			s<<it.key()<<"=\""<<encodeForXML(it.data())<<"\" ";
		}
	}
	s<<"/>\n";
}
TrackDatabase::TrackDatabase(const QString &irateDir,QObject *parent, const char *name):QObject(parent,name), mNotify(false), mIrateDir(irateDir),connectCount(0) {
	//printf("Sortie Constructeur\n%s\n",this->mIrateDir.latin1());
	this->mHashTrack.setAutoDelete(TRUE);
	this->mSerial=0;
	mAutoDown=5;
	mDownCount=0;
	mPlaylist=19;
	this->socket= new QSocket();
	connect(socket,SIGNAL(readyRead()),this,SLOT(readResponse()));
	connect(socket,SIGNAL(connected()),this,SLOT(connected()));
	connect(socket, SIGNAL(connectionClosed()),this,SLOT(connectionClosed()));
	connect(socket,SIGNAL(error(int)),this,SLOT(socketError(int )));
}

TrackDatabase::~TrackDatabase(){
	//this->cleanDownloadDir();
	delete socket;
}
QPtrList<TrackInfo> TrackDatabase::getPlayable(const float &minrating) {
	QPtrList<TrackInfo> list;
	QDictIterator<TrackInfo> it(this->mHashTrack);
	while(it.current()) {
		if(it.current()->isPlayable()&&it.current()->property("rating","10.0").toFloat()>minrating) {
			list.append(it.current());
		}
		++it;
	}
	return list;
}
QPtrList<TrackInfo> TrackDatabase::getDeleted(){
	QPtrList<TrackInfo> list;
	QDictIterator<TrackInfo> it(this->mHashTrack);
	while(it.current()) {
		if(it.current()->property("deleted")=="true") {
			list.append(it.current());
		}
		++it;
	}
	return list;
}

bool TrackDatabase::startElement(const QString &, const QString &,const QString &qName,const QXmlAttributes &atts) {

	if(qName=="Track") {
		QString url=atts.value("url");
		if(url!=QString::null) {
			TrackInfo* track;
			if((track=this->mHashTrack.find(url))==0) {
				TrackInfo* track=new TrackInfo(atts);
				this->mHashTrack.insert(url,track);
				if(this->mNotify) {
					emit trackAdded(track);
				}
			}
		}
	} else if(qName=="TrackDatabase") {
		if(atts.value("serial")!=QString::null) {
			this->mSerial=atts.value("serial").toInt();
		}
	} else if(qName=="User") {
		this->mHost=atts.value("host");
		this->mPort=atts.value("port").toInt();
		this->mUser=atts.value("name");
		this->mPassword=atts.value("password");
	} else if(qName=="AutoDownload") {
		this->mAutoDown=atts.value("setting").isNull()?5:atts.value("setting").toInt();
		this->mDownCount=atts.value("count").isNull()?0:atts.value("count").toInt();
	} else if(qName=="PlayList") {
		this->mPlaylist=atts.value("length").isNull()?19:atts.value("length").toInt();
	} else if(qName=="Error") {
		kdDebug()<<atts.value("code")<<" "<<atts.value("url")<<endl;
		emit serverError(atts.value("code"),atts.value("url"));
	}
	return true;
}
bool TrackDatabase::endElement(const QString &, const QString &,const QString &qName){
	//kdDebug()<<qName<<endl;
	if(qName=="TrackDatabase"){
		kdDebug()<<"Parse finished"<<endl;
	}	
	return true;
}
void TrackDatabase::saveAsString(QTextStream &s) {
	s<<"<?xml version=\"1.0\"?>\n<TrackDatabase serial=\""<<this->mSerial<<"\">\n";
	s<<"<User port=\""<<this->mPort<<"\" name=\""<<this->mUser<<"\" password=\""<<this->mPassword<<"\" host=\""<<this->mHost<<"\"/>";
	s<<"<AutoDownload setting=\""<<this->mAutoDown<<"\" count=\""<<this->mDownCount<<"\"/>";
	s<<"<PlayList length=\""<<this->mPlaylist<<"\"/>";
	QDictIterator<TrackInfo> it(this->mHashTrack);
	while(it.current()) {
		it.current()->saveAsString(s);
		++it;
	}
	s<<"</TrackDatabase>\n";
}
int TrackDatabase::saveAsSerialString(QTextStream &s) {
	s<<"<?xml version=\"1.0\"?>\n<TrackDatabase serial=\""<<this->mSerial<<"\">\n";
	s<<"<User port=\""<<this->mPort<<"\" name=\""<<this->mUser<<"\" password=\""<<this->mPassword<<"\" host=\""<<this->mHost<<"\"/>";
	s<<"<AutoDownload setting=\""<<this->mAutoDown<<"\" count=\""<<this->mDownCount<<"\"/>";
	s<<"<PlayList length=\""<<this->mPlaylist<<"\"/>";
	QDictIterator<TrackInfo> it(this->mHashTrack);
	int count=0;
	while(it.current()) {
		if(it.current()->getSerial()>this->mSerial) {
			it.current()->saveAsString(s);
			count++;
		}
		++it;
	}
	s<<"</TrackDatabase>\n";
	return count;
}


void TrackDatabase::processXML(QXmlInputSource * source) {
	//printf("starting XML process 1\n");
	QXmlSimpleReader reader;
	//printf("starting XML process 2\n");
	reader.setContentHandler(this);
	//printf("starting XML process 3\n");
	reader.parse(source);
	//printf("Finished XML process 4\n");
}
void TrackDatabase::setNewAccountValue(const QString& host, const int &port, const QString& user,const QString& password) {
	this->mHost=host;
	this->mPort=port;
	this->mUser=user;
	this->mPassword=password;
}
#include "trackdatabase.moc"
void TrackDatabase::cleanDownloadDir(){
	QDictIterator<TrackInfo> it(this->mHashTrack);
	while(it.current()) {
		QString f=it.current()->property("file","");
		//Long condition
		if(!f.isEmpty() &&(it.current()->property("rating","10.0").toFloat()==0.0f ||it.current()->property("state")=="broken"||it.current()->property("deleted")=="true")){
			if(QFile::exists(f)){
				QFile::remove(f);
			}
			it.current()->setProperty("file","");
			/*if(!it.current()->isProperty("state")){
				it.current()->setProperty("state","erased");
			}*/
		}
		++it;
	}
}
void TrackDatabase::saveFile() {
	//if(!this->mModified)return;

	QFile f(this->mIrateDir+"/trackdatabase.xml");
	f.open(IO_WriteOnly);
	QTextStream s(&f);
	this->saveAsString(s);
	f.flush();
	f.close();
}






/*!
    \fn TrackDatabase::connect()
 */
#include <qsocket.h>
void TrackDatabase::connectToServer() {
	if(this->socket->state()!=QSocket::Idle) {
		printf("Socket not available %d",socket->state());
		return;
	}
	this->socket->connectToHost(this->mHost,this->mPort);
	emit connectionState(1);
}
void TrackDatabase::connected() {
	//Here we send all our stuff
	QString xml;
	QTextStream stream(&xml,IO_WriteOnly);
	if(this->connectCount>0) {
		this->saveAsSerialString(stream);
	} else {
		this->saveAsString(stream);
	}
	QByteArray ba;
	QBuffer *buf=new QBuffer(ba);
	//QFile *f=new QFile("/home/mat/tmp.gz");
	//buf.open(IO_ReadWrite);
	QIODevice* dev=KFilterDev::device(buf,"application/x-gzip",false);
	if(dev==0) {
		kdDebug()<<"NULLLLLL"<<endl;
		//		delete td;
		return ;
	}
	dev->open(IO_WriteOnly);

	//kdDebug()<<QString(xml)<<endl;
	dev->writeBlock(xml.latin1(),xml.length());
	dev->flush();
	//f->flush();
	buf->flush();
	dev->close();
	//f->close();
	/*QFile f("/home/mat/tmp.gz");
	f.open(IO_WriteOnly);
	f.writeBlock(ba);
	f.close();
	//f->open(IO_ReadOnly);
	//QByteArray ba2=f->readAll();*/
	QString data("Content-Length: ");
	data.append(QString::number(xml.length()));
	data.append("\r\nContent-Encoding: gzip\r\n\r\n");
	//data.append(xml);
	QByteArray toSend;
	QBuffer buf2(toSend);
	buf2.open(IO_WriteOnly);
	buf2.writeBlock(data.latin1(),data.length());
	buf2.writeBlock(ba);
	buf2.flush();
	buf2.close();
	//data.append(ba);
	//printf("Data sent :\n%s\n",data.latin1());
	this->socket->writeBlock(toSend.data(),toSend.size());
	this->socket->flush();
	emit connectionState(2);
	/*kdDebug()<<"Decompressing buffer"<<endl;
	buf->setBuffer(ba);
	QIODevice* dev2=KFilterDev::device(buf,"application/x-gzip",false);
	dev2->open(IO_ReadOnly);
	kdDebug()<<QString(dev2->readAll())<<endl;
	dev2->close();
	delete dev2;
	delete dev;
	delete buf;
	*/
	//this->socket->flush();
}
void TrackDatabase::readResponse() {
	emit connectionState(3);

}
void TrackDatabase::connectionClosed() {
	//if byte available is 10 then it's the GZip header
	//It's always ten bytes (see RFC 1952)
	//So we don't want to read it now (keep for later)
	if(this->socket->bytesAvailable()==10) {
		kdDebug()<<"Connection lost but only GZip Header received. Expect error report : "<<endl;
	}
	//kdDebug()<<" open "<<this->socket->isOpen()<<endl;
	QIODevice* dev=KFilterDev::device(this->socket,"application/x-gzip",false);
	if(dev==0) {
		kdDebug()<<"NULLLLLL GZIP Device"<<endl;
		//		delete td;
		return ;
	}
	dev->open(IO_ReadOnly);
	//QByteArray ba2=dev->readAll();
	QXmlInputSource source(dev);
	//source.setData(ba2);
	this->processXML(&source);
	emit connectionState(4);
	dev->close();
	delete dev;
	this->connectCount++;
	this->mSerial++;
	this->socket->close();
	//kdDebug()<<"connection closed\n"<<endl;
	emit connectionState(5);
	//delete socket;
}
void TrackDatabase::socketError(int i) {
	//this->socket->clearPendingData();
	kdDebug()<<"Socket error number "<<i<<endl;
	socket->close();
	//this->socket->close();
	if(this->socket->state()==QSocket::Closing) {
		kdDebug()<<"DelayedClose used\n"<<endl;
	}
	//	this->connectCount++;
	emit networkError(i);
	emit connectionState(6);
	//delete socket;
}

/*!
    \fn TrackInfo::init()
 */
void TrackInfo::init() {
	xmlAttributes.append("rating");
	xmlAttributes.append("played");
	xmlAttributes.append("last");
	xmlAttributes.append("weight");
	xmlAttributes.append("volume");
	xmlAttributes.append("state");
	xmlAttributes.append("deleted");
	xmlAttributes.append("artist");
	xmlAttributes.append("title");
	xmlAttributes.append("url");
	xmlAttributes.append("file");
	xmlAttributes.append("copyright");
}
/*
if(this->socket->bytesAvailable()==10){
		kdDebug()<<"Connection lost but only GZip Header received. Expect error report : "<<endl;
	}
	
	QByteArray ba = this->socket->readAll();
	this->connectCount++;
	this->mSerial++;
	
	if(this->socket->state()==QSocket::Closing){
		printf("DelayedClose used\n");
	}
	QBuffer *buf=new QBuffer(ba);
	QIODevice* dev=KFilterDev::device(buf,"application/x-gzip",false);
	if(dev==0) {
		kdDebug()<<"NULLLLLL"<<endl;
		//		delete td;
		return ;
	}
	dev->open(IO_ReadOnly);
	QByteArray ba2=dev->readAll();
	dev->close();
	kdDebug()<<" Size "<<ba2.size()<<QString(ba2)<<endl;
	if(!ba2.isEmpty()){
		QXmlInputSource source;
		source.setData(ba2);
		this->processXML(&source);
		emit connectionState(4);
	}
	
	delete dev;
	delete buf;
	this->socket->close();
	kdDebug()<<"connection closed\n"<<endl;
	emit connectionState(5);
	*/
