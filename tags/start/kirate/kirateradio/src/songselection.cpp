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
#include "songselection.h"
#include <math.h>
BasicSelection::BasicSelection(){
	minRating=0;
	unratedWeight=QString::number(15);
	carePlayed=true;
	useExp=false;
}

BasicSelection::~BasicSelection(){
}
int BasicSelection::setSongWeight(SafeListViewItem *i){
	if(!i->isEnabled())return 0;
	float rating =i->property("rating",this->unratedWeight).toFloat();
	if(rating<this->minRating)return 0;
	if(this->useExp)rating*=rating;
	float played=1;
	if(this->carePlayed){
		played+=i->property("played","0").toFloat();
	}
	if(this->useSqrtPlayed){
		played=sqrtf(played);
	}
	return (int)((rating/played)*100);
}

