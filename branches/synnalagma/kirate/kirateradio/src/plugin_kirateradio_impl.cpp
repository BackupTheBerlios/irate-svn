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

#include "plugin_kirateradio_impl.h"
//#include "playlist.h"
#include "view.h"
#include <noatun/player.h>
#include <kdebug.h>

#include <kapplication.h>
#include <krandomsequence.h>
#include <kdebug.h>
#include <klocale.h>
#include <kiconloader.h>
#include "trackdatabase.h"
#include "songlist.h"
#include <qrect.h>

IratePlugin *IratePlugin::Self=0;

IratePlugin::IratePlugin()
		: Playlist(0, "IratePlugin"), Plugin(), mExiting(false) {
	Self=this;
	//kdDebug()<<"Constructor called"<<endl;
}

void IratePlugin::init() {
	//Init stuff in TrackInfo
	KGlobal::locale()->insertCatalogue("kirateradio");
	TrackInfo::init();
	//Building TrackDatabase
	//TrackDatabase::init();
	//kdDebug()<<"Starting init"<<endl;
	view=new View(this); // 195
	//kdDebug()<<"View construite"<<endl;
	connect(view->listView(), SIGNAL(executed(QListViewItem*)), SLOT(listItemSelected(QListViewItem*)));
	connect(view, SIGNAL(shown()), SIGNAL(listShown()));
	connect(view, SIGNAL(hidden()), SIGNAL(listHidden()));

	view->init(); // 1000
	//kdDebug()<<"View inited"<<endl;
}
bool IratePlugin::unload(){
	kdDebug()<<"unload called"<<endl;
	//this->view->listView()->unload();
	//this->setCurrent(0,true);
	return true;
}
IratePlugin::~IratePlugin(){
	kdDebug()<<"Destructor called"<<endl;
	this->setCurrent(0,true);
	mExiting=true;
	delete view;
}

void IratePlugin::reset() {
	//Nothing to do since Playlist can not be reseted
	kdDebug()<<"Reset called"<<endl;
	/*SafeListViewItem *i;
	setCurrent(i=static_cast<SafeListViewItem*>(view->listView()->firstChild()), false);
	if (i && !i->isOn())
		next(false);*/
}

PlaylistItem IratePlugin::next() {
	//kdDebug()<<"Next called"<<endl;
	return next(true);
}

PlaylistItem IratePlugin::next(bool play) {
	
	PlaylistItem nextItem=view->listView()->next();

	
	/*static int krach=0;
	if(krach>=10)return 0;
	krach++;*/
	// Ignore all this order stuff and select a random item
	
	
	if (!nextItem){ // don't set a null-item as current item
		//kdDebug()<<"!nextItem"<<endl;
		return 0;
	}
	
	PlaylistItem oldCurrent = currentItem;
	setCurrent(nextItem, play);

	// Hack for back button on randomized play
	if (oldCurrent)
		randomPrevious = oldCurrent;

	if (currentItem){
		if (!static_cast<SafeListViewItem*>(currentItem.data())->isEnabled()){
			kdDebug()<<"Recursive!!!!!"<<endl;
			return next(play);
		}
	}
	//kdDebug()<<"Next mime is "<<currentItem.mimetype()<<" file "<<currentItem.file()<<endl;
	return currentItem;
}

PlaylistItem IratePlugin::current() {
	return currentItem;
}

PlaylistItem IratePlugin::previous() {
	setCurrent(this->view->listView()->previous());
	return currentItem;

	
	// there's a possibility that I will fall out to here
	// from the above branch

	/*PlaylistItem nextItem;
	if(!current()) {
		nextItem = static_cast<SafeListViewItem*>(static_cast<SafeListViewItem*>(getFirst().data()));
	} else {
		nextItem = static_cast<SafeListViewItem*>(
		               static_cast<SafeListViewItem*>(current().data())->itemAbove());
	}
	if (!nextItem) // don't set a null-item as current item
		return 0;

	setCurrent(nextItem);

	if (currentItem)
		if (!static_cast<SafeListViewItem*>(currentItem.data())->isEnabled())
			return previous();

	return currentItem;*/
}

PlaylistItem IratePlugin::getFirst() const {
	//kdDebug()<<"Get First Called"<<endl;
	return static_cast<SafeListViewItem*>(view->listView()->firstChild());
}

PlaylistItem IratePlugin::getAfter(const PlaylistItem &item) const {
	//kdDebug()<<"GetAfter called"<<endl;
	if (item)
		return static_cast<SafeListViewItem*>(static_cast<const SafeListViewItem*>(item.data())->nextSibling());
		
	return 0;
}

bool IratePlugin::listVisible() const {
	return view->isVisible();
}

void IratePlugin::showList() {
	view->show();
}

void IratePlugin::hideList() {
	view->hide();
}

void IratePlugin::clear() {
	kdDebug()<<"Clear called"<<endl;
//	view->listView()->clear();
}

void IratePlugin::addFile(const KURL &file, bool) {
	kdDebug()<<"add file called "<<file<<endl;
	//view->addFile(file, play);
}

void IratePlugin::setCurrent(const PlaylistItem &i) {
	setCurrent(i, true);
}

void IratePlugin::setCurrent(const PlaylistItem &i, bool emitC) {
	randomPrevious = PlaylistItem();
	emitC = emitC && currentItem;
	if (!i) {
		//kdDebug()<<"current is null!!!!!!"<<i.file()<<endl;
		currentItem=0;
	} else {
		// remove the old icon
		SafeListViewItem *now=static_cast<SafeListViewItem*>(current().data());
		if (now)
			now->setPixmap(0, QPixmap());

		QRect rect(view->listView()->itemRect(static_cast<SafeListViewItem*>(current().data())));
		rect.setWidth(view->listView()->viewport()->width());
		currentItem=i;
		view->listView()->viewport()->repaint(rect,true);

		view->listView()->ensureItemVisible(static_cast<SafeListViewItem*>(current().data()));
		QRect currentRect= view->listView()->itemRect(static_cast<SafeListViewItem*>(current().data()));
		view->listView()->viewport()->repaint(currentRect);

		now=static_cast<SafeListViewItem*>(current().data());
		if(now)
			now->setPixmap(0, ::SmallIcon("noatunplay"));
	}

	if (emitC && !exiting())
		emit playCurrent();
}

void IratePlugin::remove(const PlaylistItem &) {
	//	delete i;
}

void IratePlugin::listItemSelected(QListViewItem *i) {
	setCurrent(PlaylistItem(static_cast<SafeListViewItem*>(i)), false);
	this->view->listView()->setCurrentSong(static_cast<SafeListViewItem*>(i));
	emit playCurrent();
}

/*void IratePlugin::randomize() {
	// turning off sorting is necessary
	// otherwise, the list will get randomized and promptly sorted again
	view->setSorting(false);
	List *lview = view->listView();
	// eeeeevil :)
	QPtrList<void> list;
	QPtrList<QListViewItem> items;
	for(int i = 0; i < lview->childCount(); i++) {
		list.append( (void*) i );
		items.append( lview->itemAtIndex( i ) );
	}

	KRandomSequence seq;
	seq.randomize( &list );

	for(int i = 0; i < lview->childCount(); i++) {
		items.take()->moveItem(lview->itemAtIndex((int) list.take()));
	}

	setCurrent(currentItem, false);
}*/

void IratePlugin::sort() {
	view->setSorting(true);
	setCurrent(currentItem, false);
}

#include "plugin_kirateradio_impl.moc"
