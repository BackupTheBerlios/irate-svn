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
#ifndef SONGSELECTION_H
#define SONGSELECTION_H
#include "safelistviewitem.h"
#include <kapplication.h>
/**
@author Matthias Studer
*/
class SongSelection{
public:
	SongSelection():totalWeight(0){}

	virtual ~SongSelection(){}
    
	int setSongWeight(SafeListViewItem *i){
		return 1;
	}
	int getRandom(){
		return KApplication::random()%this->totalWeight ;
	}
	void setTotal(const int& newTotal){
		this->totalWeight=newTotal;
	}
	protected:
		int totalWeight;
};

class BasicSelection:public SongSelection{
	public:
		BasicSelection();
		virtual ~BasicSelection();
		int setSongWeight(SafeListViewItem *i);
		int getMinRating(){
			return minRating;
		}
		void setMinRating(const int& rating){
			this->minRating=rating;
		}
		int getUnratedWeight(){
			return this->unratedWeight.toInt();
		}
		void setUnratedWeight(const int& weight){
			this->unratedWeight=QString::number(weight);
		}
		void setCarePlayed(const bool& care){
			this->carePlayed=care;
		}
		bool getCarePlayed(){
			return this->carePlayed;
		}
		void setUseExp(const bool & exp){
			this->useExp=exp;
		}
		bool getUseExp(){
			return this->useExp;
		}
		bool getUseSqrtPlayed(){
			return this->useSqrtPlayed;
		}
		void setUseSqrtPlayed(const bool & sqrt){
			this->useSqrtPlayed=sqrt;
		}
	protected:
		int minRating;
		QString unratedWeight;
		bool carePlayed;
		bool useExp;
		bool useSqrtPlayed;
};

#endif
