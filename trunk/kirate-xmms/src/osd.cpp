/*
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
*/

/*
  osd.cpp  -  Provides an interface to a plain QWidget, which is independent of KDE (bypassed to X11)
  begin:     Fre Sep 26 2003
  copyright: (C) 2003 by Christian Muehlhaeuser
  email:     muesli@chareit.net
*/
/*
 Actually taken from amarok
*/
//#include "amarokconfig.h" //previewWidget
#include "osd.h"

#include <qapplication.h>
#include <qbitmap.h>
#include <qpainter.h>

#include <kdebug.h>
#include <kglobalsettings.h> //unsetColors()

#include <X11/Xlib.h> //reposition()
#include <qregexp.h>


OSDWidget::OSDWidget( const QString &context, QWidget *parent, const char *name )
		: QWidget( parent, name, WType_TopLevel | WNoAutoErase | WStyle_Customize | WX11BypassWM | WStyle_StaysOnTop )
		, m_duration( 5000 )
		,m_linking(false)
		,m_context(context)
		, m_alignment( Middle )
		, m_screen( 0 )
		, m_y( MARGIN )
		,m_srt(0)
		, m_dirty( false ) 
		,m_dragging( false )
{
	//Attempt to change cursor over link
//	this->setMouseTracking(true);
	setFocusPolicy( NoFocus );
	setBackgroundMode( NoBackground );

	connect( &timer,     SIGNAL( timeout() ), SLOT( hide() ) );
	connect( &timerMin,  SIGNAL( timeout() ), SLOT( minReached() ) );
}


void OSDWidget::renderOSDText( const QString &text) {
	static QBitmap mask;

	// Set a sensible maximum size, don't cover the whole desktop or cross the screen
	//Actually unused but it definitly should
	//QSize max = QApplication::desktop() ->screen( m_screen ) ->size() - QSize( MARGIN*2 + 20, 100 );
	if(this->m_srt!=0){
		delete m_srt;
	}
	this->m_srt=new QSimpleRichText(text,this->font(),this->m_context);
	// The title cannnot be taller than one line
	// AlignAuto = align Arabic to the right, etc.
	int w=this->m_srt->widthUsed();
	int h=this->m_srt->height();
	osdBuffer.resize(w,h);
	mask.resize(w,h);

	// Start painting!
	QPainter bufferPainter( &osdBuffer );
	QPainter maskPainter( &mask );

	// Draw backing rectangle
	//bufferPainter.save();
	bufferPainter.setPen( Qt::black );
	bufferPainter.setBrush( backgroundColor() );
	bufferPainter.drawRoundRect( 0,0,w,h, 1500 /w, 1500/h);
	//bufferPainter.restore();
	this->m_srt->draw(&bufferPainter,0,0,QRect(),this->colorGroup());

	// Masking for transparency
	mask.fill( Qt::black );
	maskPainter.setBrush( Qt::white );
	maskPainter.drawRoundRect(0,0,w,h, 1500/w,1500/h);
	setMask( mask );

	//do last to reduce noticeable change when showing multiple OSDs in succession
	reposition(QSize(w,h));

	m_currentText = text;
	m_dirty = false;
	
	if(this->m_linking&&this==this->mouseGrabber()){
		this->releaseMouse();
		this->m_linking=false;
	}
	update();
}
void OSDWidget::setText(const QString &text) {
	static QRegExp exp("<body\\s[^>]*bgcolor=\"(#[0-9A-Fa-f]{6}|[a-z]+)\"[^>]*>(.*)</body>");
	exp.search(text,0);
	if(!exp.cap(1).isEmpty()){
		//Needed to avoid some really ugly background
		this->setBackgroundColor(QColor(exp.cap(1)));
	//}
		if(exp.cap(2).isEmpty()||exp.cap(2)==this->m_currentText)return;
		m_currentText = exp.cap(2);
	}else{
		this->m_currentText=text;
	}
	
	refresh();
}

void OSDWidget::showOSD( const QString &text, bool preemptive ) {
	if ( isEnabled() && !text.isEmpty() ) {
		if ( preemptive || !timerMin.isActive() ) {
			//m_currentText = text;
			//*dirty set in refresh if needed (called in setText)
			//m_dirty = true; 
			this->setText(text);
			show();
		} else textBuffer.append( text ); //queue
	}
}


void OSDWidget::minReached() //SLOT
{
	if ( !textBuffer.isEmpty() ) {
		renderOSDText( textBuffer.front() );
		textBuffer.pop_front();

		if( m_duration )
			//timerMin is still running
			timer.start( m_duration, TRUE );
	} else timerMin.stop();
}


void OSDWidget::setDuration( int ms ) {
	m_duration = ms;

	if( !m_duration ) timer.stop();
}


void OSDWidget::setOffset( int /*x*/, int y ) {
	//m_offset = QPoint( x, y );
	m_y = y;
	reposition();
}

void OSDWidget::setAlignment( Alignment a ) {
	m_alignment = a;
	reposition();
}

void OSDWidget::setScreen( uint screen ) {
	const int n = QApplication::desktop()->numScreens();
	m_screen = (screen >= n) ? n-1 : (int)screen;
	reposition();
}


bool OSDWidget::event( QEvent *e ) {
	switch( e->type() ) {
	case QEvent::Paint:
		bitBlt( this, 0, 0, &osdBuffer );
		return TRUE;

//	case QEvent::ApplicationPaletteChange:
		/*if ( !AmarokConfig::osdUseCustomColors() ) //FIXME not portable!
		    
		*/
//		unsetColors();
	//	return TRUE;

	default:
		return QWidget::event( e );
	}
}

/*void OSDWidget::mousePressEvent( QMouseEvent* ) {
	hide();
}*/

void OSDWidget::show() {
	if ( m_dirty ) renderOSDText( m_currentText );

	QWidget::show();

	if ( m_duration ) //duration 0 -> stay forever
	{
		timer.start( m_duration, TRUE ); //calls hide()
		timerMin.start( 150 ); //calls minReached()
	}
}

void OSDWidget::refresh() {
	if ( isVisible() ) {
		//we need to update the buffer
		renderOSDText( m_currentText );
	} else m_dirty = true; //ensure we are re-rendered before we are shown
}

void OSDWidget::reposition( QSize newSize ) {
	if( !newSize.isValid() ) newSize = size();

	QPoint newPos( MARGIN, m_y );
	const QRect screen = QApplication::desktop()->screenGeometry( m_screen );

	//TODO m_y is the middle of the OSD, and don't exceed screen margins

	switch ( m_alignment ) {
	case Left:
		break;

	case Right:
		newPos.rx() = screen.width() - MARGIN - newSize.width();
		break;

	case Center:
		newPos.ry() = (screen.height() - newSize.height()) / 2;

		//FALL THROUGH

	case Middle:
		newPos.rx() = (screen.width() - newSize.width()) / 2;
		break;
	}

	//ensure we don't dip below the screen
	if( newPos.y()+newSize.height() > screen.height()-MARGIN ) newPos.ry() = screen.height()-MARGIN-newSize.height();

	// correct for screen position
	newPos += screen.topLeft();

	//ensure we are painted before we move
	if( isVisible() ) paintEvent( 0 );

	//fancy X11 move+resize, reduces visual artifacts
	XMoveResizeWindow( x11Display(), winId(), newPos.x(), newPos.y(), newSize.width(), newSize.height() );
}



//////  OSDPreviewWidget below /////////////////////

#include <kcursor.h>         //previewWidget
#include <klocale.h>



void OSDWidget::mousePressEvent( QMouseEvent *event ) {
	if(!this->m_srt->anchorAt(event->pos()).isEmpty()&&event->button() == LeftButton && !m_dragging ){
		//kdDebug()<<"contains"<<endl;
		//event->accept();
		emit linkActivated(this->m_srt->anchorAt(event->pos()));
		return;
	}
	m_dragOffset = event->pos();

	if ( event->button() == LeftButton && !m_dragging ) {
		grabMouse( KCursor::sizeAllCursor() );
		m_dragging = true;
	}
}

void OSDWidget::mouseReleaseEvent( QMouseEvent * /*event*/ ) {
	if ( m_dragging ) {
		m_dragging = false;
		releaseMouse();

		// compute current Position && offset
		QDesktopWidget *desktop = QApplication::desktop();
		int currentScreen = desktop->screenNumber( pos() );

		if ( currentScreen != -1 ) {
			// set new data
			m_screen = currentScreen;
			m_y      = QWidget::y();

			emit positionChanged();
		}
	}
}

void OSDWidget::mouseMoveEvent( QMouseEvent *e ) {
	//Doesn't work, I mean it's ugly (blinking)
	/*if(!m_linking&&!this->m_srt->anchorAt(e->pos()).isEmpty()){
		this->grabMouse(KCursor::handCursor());
		m_linking=true;
		return;
	}
	else if(m_linking&&this==mouseGrabber()){
		releaseMouse();
		this->m_linking=false;
		return;
	}*/
	if ( m_dragging && this == mouseGrabber() ) {
		const QRect screen      = QApplication::desktop()->screenGeometry( m_screen );
		const uint  hcenter     = screen.width() / 2;
		const uint  eGlobalPosX = e->globalPos().x() - screen.left();
		const uint  snapZone    = screen.width() / 8;

		QPoint destination = e->globalPos() - m_dragOffset - screen.topLeft();
		int maxY = screen.height() - height() - MARGIN;
		if( destination.y() < MARGIN ) destination.ry() = MARGIN;
		if( destination.y() > maxY ) destination.ry() = maxY;

		if( eGlobalPosX < (hcenter-snapZone) ) {
			m_alignment = Left;
			destination.rx() = MARGIN;
		} else if( eGlobalPosX > (hcenter+snapZone) ) {
			m_alignment = Right;
			destination.rx() = screen.width() - MARGIN - width();
		} else {
			const uint eGlobalPosY = e->globalPos().y() - screen.top();
			const uint vcenter     = screen.height()/2;

			destination.rx() = hcenter - width()/2;

			if( eGlobalPosY >= (vcenter-snapZone) && eGlobalPosY <= (vcenter+snapZone) ) {
				m_alignment = Center;
				destination.ry() = vcenter - height()/2;
			} else m_alignment = Middle;
		}

		destination += screen.topLeft();

		move( destination );
	}
}