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
#include "xmmswrapper.h"
#include <xmms/xmmsctrl.h>
#include <kurl.h>
#include <kdebug.h>
/*#include <kmessagebox.h>
#include "view.h"
#include <klocale.h>*/
XmmsWrapper::XmmsWrapper(QObject *parent, const char *name)
		: QObject(parent, name) {
	_mExiting=false;

}

/*void insertPlaylist(QTrack *t,int pos){
	QString url=KURL::fromPathOrURL(t->getFile()).url();
	gchar *buff=new gchar[url->length()+1];
	const char * b=url.latin1();
	for(int i=0;i
	
}*/
XmmsWrapper::~XmmsWrapper() {}
void XmmsWrapper::setCurrent(QTrack *t) {
	kdDebug()<<"Set current<<"<<endl;
	this->prevStack.push(this->tcur);
	this->tcur=t;
	xmms_remote_playlist_delete(0,0);
	xmms_remote_playlist_ins_url_string(0,(gchar*)KURL::fromPathOrURL(this->tcur->getFile()).url().latin1(),1);
	if(!this->hasPrevious())
	xmms_remote_playlist_ins_url_string(0,(gchar*)KURL::fromPathOrURL(this->tnext->getFile()).url().latin1(),2);
	xmms_remote_set_playlist_pos(0, 1);
	if (!xmms_remote_is_playing(0))
		xmms_remote_play(0);
}
void XmmsWrapper::next() {
	xmms_remote_playlist_next(0);
	this->timerEvent(NULL);
}
void XmmsWrapper::previous() {
	xmms_remote_playlist_prev(0);
	this->timerEvent(NULL);
}
void XmmsWrapper::pause() {
	xmms_remote_pause(0);
}
void XmmsWrapper::play() {
	xmms_remote_play(0);
}
void XmmsWrapper::stop() {
	xmms_remote_stop(0);
}
bool XmmsWrapper::isPlaying(){
	return xmms_remote_is_playing(0);
}
void XmmsWrapper::timerEvent(QTimerEvent* e) {
	if(!xmms_remote_is_running(0)){
		this->killTimers();
		emit connectionLost();
	}
	if(e==NULL||e->timerId()==this->checktimer) {
		int pos=xmms_remote_get_playlist_pos(0);
		bool modified=false;
		/*kdDebug()<<"Current is "<<tcur->getName()<<endl;
		kdDebug()<<"Next is "<<tnext->getName()<<endl;*/
		if(this->hasPrevious()) {
			if(pos==0) {//previous was called we don't know how but...
				//this->prevStack->push(this->tcur);
				this->tcur=this->prevStack.pop();
				this->tnext=QT_IRate::instance()->next(false);
				xmms_remote_playlist_delete(0,2);
				xmms_remote_playlist_delete(0,1);
				QString nextURL=KURL::fromPathOrURL(tnext->getFile()).url();
				if(this->prevStack.isEmpty()) {
					xmms_remote_playlist_ins_url_string(0,(gchar*)nextURL.latin1(),1);
				} else {
					xmms_remote_playlist_ins_url_string(0,(gchar*)KURL::fromPathOrURL(this->prevStack.top()->getFile()).url().latin1(),0);
					xmms_remote_playlist_ins_url_string(0,(gchar*)nextURL.latin1(),2);
				}
				modified=true;
			} else if(pos==2) {//next was called
				
				this->prevStack.push(this->tcur);
				this->tcur=this->tnext;
				this->tnext=QT_IRate::instance()->next(false);
				xmms_remote_playlist_delete(0,0);
				QString nextURL=KURL::fromPathOrURL(tnext->getFile()).url();
				xmms_remote_playlist_ins_url_string(0,(gchar*)nextURL.latin1(),2);
				modified=true;
				
			}
		} else {
			if(pos==1) {//Next was called
				this->prevStack.push(this->tcur);
				this->tcur=this->tnext;
				this->tnext=QT_IRate::instance()->next(false);
				xmms_remote_playlist_ins_url_string(0,(gchar*)KURL::fromPathOrURL(tnext->getFile()).url().latin1(),2);
				modified=true;
				
			}
		}
		if(modified){
			int time=xmms_remote_get_playlist_time(0,1);
			kdDebug()<<"Timer is "<<time<<" second "<<(time/1000) <<endl;
			this->killTimer(this->songTimer);
			this->songTimer=this->startTimer(time-1500);
			emit trackChanged();
		}
	} else if(e->timerId()==this->songTimer){
		this->current()->setTrackPlayed();
		kdDebug()<<"Current track has been played"<<endl;
		this->killTimer(this->songTimer);
	}

}
bool XmmsWrapper::init() {
	/*if(!xmms_remote_is_running(0)){
		emit connectionLost();
		return false;
	}*/
	if(xmms_remote_is_shuffle(0)){
		xmms_remote_toggle_shuffle(0);
	}
	xmms_remote_playlist_clear(0);
	this->tcur=QT_IRate::instance()->next(false);
	xmms_remote_playlist_ins_url_string(0,(gchar*)KURL::fromPathOrURL(this->tcur->getFile()).url().latin1(),0);
	this->tnext=QT_IRate::instance()->next(false);
	xmms_remote_playlist_ins_url_string(0,(gchar*)KURL::fromPathOrURL(tnext->getFile()).url().latin1(),1);
	kdDebug()<<"Current is "<<tcur->getName()<<endl;
	kdDebug()<<"Next is "<<tnext->getName()<<endl;
	this->checktimer=this->startTimer(500);
	return true;
}
#include "xmmswrapper.moc"
