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
#include <iratetrack.h>
#include <irate/common/Track.h>
bool BaseIRateTrack::isRated() {
	return (bool)((irate::common::Track*)this->_mt)->isRated();
}
float BaseIRateTrack::getRating() {
	return (float)((irate::common::Track*)this->_mt)->getRating();
}
int BaseIRateTrack::getNoOfTimesPlayed () {
	return (int)((irate::common::Track*)this->_mt)->getNoOfTimesPlayed();
}
void BaseIRateTrack::setVolume (int v) {
	((irate::common::Track*)this->_mt)->setVolume((jint)v);
}
int BaseIRateTrack::getVolume () {
	return (int)((irate::common::Track*)this->_mt)->getVolume();
}
void BaseIRateTrack::unSetVolume () {
	((irate::common::Track*)this->_mt)->unSetVolume();
}
bool BaseIRateTrack::isBroken () {
	return (bool)((irate::common::Track*)this->_mt)->isBroken();
}
bool BaseIRateTrack::isMissing () {
	return (bool)((irate::common::Track*)this->_mt)->isMissing();
}
bool BaseIRateTrack::isDeleted () {
	return (bool)((irate::common::Track*)this->_mt)->isDeleted();
}
bool BaseIRateTrack::isActive() {
	return (bool)((irate::common::Track*)this->_mt)->isActive();
}
bool BaseIRateTrack::isNotDownloaded () {return (bool)((irate::common::Track*)this->_mt)->isNotDownloaded();}
bool BaseIRateTrack::isHidden () {return (bool)((irate::common::Track*)this->_mt)->isHidden();}
bool BaseIRateTrack::isPendingPurge () {return (bool)((irate::common::Track*)this->_mt)->isPendingPurge();}
bool BaseIRateTrack::isOnPlayList () {return (bool)((irate::common::Track*)this->_mt)->isOnPlayList();}
int BaseIRateTrack::getProbability () {return (int)((irate::common::Track*)this->_mt)->getProbability();}
bool BaseIRateTrack::exists () {return (bool)((irate::common::Track*)this->_mt)->exists();}
long BaseIRateTrack::getPlayingTime () {
	if(((irate::common::Track*)this->_mt)->exists())
	return(long)((irate::common::Track*)this->_mt)->getPlayingTime();
	else return 0;
}
int BaseIRateTrack::getDownloadAttempts () {return (int)((irate::common::Track*)this->_mt)->getDownloadAttempts();}
