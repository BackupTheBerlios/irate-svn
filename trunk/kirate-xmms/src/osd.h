/*
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
*/

/*
  osd.h   -  Provides an interface to a plain QWidget, which is independent of KDE (bypassed to X11)
  begin:     Fre Sep 26 2003
  copyright: (C) 2003 by Christian Muehlhaeuser
  email:     muesli@chareit.net
  
  Modified by Matthias Studer 
  email: matthias.studer@ezwww.ch
  
*/

#ifndef AMAROK_OSD_H
#define AMAROK_OSD_H

#include <qpixmap.h> //stack allocated
#include <qtimer.h>  //stack allocated
#include <qwidget.h> //baseclass
#include <qfont.h>
#include <qstringlist.h>
#include <qtimer.h>
#include <qevent.h>
#include <qsimplerichtext.h>
//class MetaBundle;





class OSDWidget : public QWidget {
		Q_OBJECT
	public:
		enum Alignment { Left, Middle, Center, Right };

		OSDWidget(const QString &context=QString::null,QWidget *parent = 0, const char *name = "osd");
		virtual ~OSDWidget(){
			if(this->m_srt)delete m_srt;
		}
		void setDuration(int ms);
//		void setFont(QFont newfont);
		//void setShadow(bool shadow);
		void setOffset( int x, int y );
		void setAlignment(Alignment);
		void setScreen(uint screen);
		void setText(const QString &text);
		//Warning doesn't refresh you must recall setText() or showOSD()
		//Just because in most case you'll call this before showing
		//context the context to look for image in <img src="tmp.png"> if null full path must be
		// specified /home/mklds/tmp.png
		void setContext(const QString& context) {
			this->m_context=context;
		}

		int screen()    { return m_screen; }
		int alignment() { return m_alignment; }
		int y()         { return m_y; }
		int duration(){return m_duration;}

	signals:
		/**Emitted when a link is clicked*/
		void linkActivated(const QString &url);
		void positionChanged();
	public slots:
		//TODO rename show, scrap removeOSD, just use hide() <- easier to learn
		void showOSD(const QString&, bool preemptive=false );
		void removeOSD() { hide(); } //inlined as is convenience function

	protected slots:
		void minReached();

	protected:
		/* render text into osdBuffer */
		void renderOSDText(const QString &text);
		//void mousePressEvent( QMouseEvent* );
		bool event(QEvent*);

		void show();

		/* call to reposition a new OSD text or when position attributes change */
		void reposition( QSize newSize = QSize() );

		/* called after most set*() calls to update the OSD */
		void refresh();
		//From OSDPreviewWidget added link signal
		void mousePressEvent(QMouseEvent*);
		void mouseReleaseEvent(QMouseEvent*);
		void mouseMoveEvent(QMouseEvent*);

		static const int MARGIN = 15;

		int         m_duration;
		QTimer      timer;
		QTimer      timerMin;
		QPixmap     osdBuffer;
		//bool        m_shadow;
		QStringList   textBuffer;
		bool m_linking;
		QString m_context;
		QString m_currentText;
		Alignment   m_alignment;
		int         m_screen;
		uint        m_y;
		//Hold geometry usefull for 
		QSimpleRichText *m_srt;

		bool m_dirty; //if dirty we will be re-rendered before we are shown
	private:
		bool   m_dragging;
		QPoint m_dragOffset;

};





#endif /*AMAROK_OSD_H*/
