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
#ifndef STDIRATETRACK_H
#define STDIRATETRACK_H
#include <iratedef.h>
#include <string>
#include <iratetrack.h>
#include <iratecenter.h>
/**
@author Matthias Studer
*/
#ifdef _CPPIR_USE_CHAR
void setDefaultEncoding(char * encoding,bool nullTerminated=true);
std::string decodeSTDString(string_jt);
bool std_isNullOrEmptyString(const std::string &s);
void initSTDIRateCenter(char * encoding, bool nullTerminated);

#elif defined(_CPPIR_USE_WCHAR_T)
std::wstring decodeSTDString(string_jt);
bool std_isNullOrEmptyString(const std::wstring& s);
#endif


#endif
