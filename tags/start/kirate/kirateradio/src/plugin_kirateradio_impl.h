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

#ifndef IRATEPLUGIN_IMPL_H
#define IRATEPLUGIN_IMPL_H

#include <noatun/playlist.h>
#include <noatun/plugin.h>
//#define SPL IratePlugin::SPL()
/*
class PlaylistItem
{
	PlaylistItem(const KURL &u=0);
	virtual ~PlaylistItem();
 
	QString title() const;
	virtual void setTitle(const QString &t);
 
	KURL url() const;
	virtual void setUrl(const KURL &u);
 
	int length() const;
	virtual void setLength(int l);
};
*/
class SafeListViewItem;
class View;
class SongList;
class QListViewItem;

class IratePlugin : public Playlist, public Plugin {
		Q_OBJECT
		friend class SafeListViewItem;
		friend class List;
	public:
		IratePlugin();
		~IratePlugin();

		/**
		 * go to the front
		 **/
		virtual void reset();

		virtual void clear();
		virtual void addFile(const KURL&, bool play=false);
		/**
		 * Cycle everthing through forward
		 **/
		virtual PlaylistItem next();
		PlaylistItem next(bool play);
		/**
		 * return the one that might/should be playing now
		 **/
		virtual PlaylistItem current();
		/**
		 * Cycle through backwards
		 **/
		virtual PlaylistItem previous();

		virtual PlaylistItem getFirst() const;
		virtual PlaylistItem getAfter(const PlaylistItem &item) const;

		virtual bool listVisible() const;
		virtual void init();

		virtual Playlist *playlist() { return this; }
		//virtual 

		static IratePlugin *SPL() { return Self; }
		inline bool exiting() const { return mExiting; }
		virtual bool unload();
	public slots:
		virtual void showList();
		virtual void hideList();
		virtual void remove(const PlaylistItem&);
		virtual void sort();


	public slots:
		void setCurrent(const PlaylistItem &, bool emitC);
		void setCurrent(const PlaylistItem &);

		void listItemSelected(QListViewItem*);

		//void randomize();

	private:
		PlaylistItem currentItem, randomPrevious;

	signals:
		void play(PlaylistItem*);

	private:
		bool mExiting; // HACK HACK HACK HACK!!!
		View *view;
		//	QRect currentRect;
		static IratePlugin *Self;
};


#endif
