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
#include <iratedef.h>
#include <string>
#include <stdiratetrack.h>
#include <gcj/cni.h>
#include <gcj/array.h>
#include <iratecenter.h>
#ifdef _CPPIR_USE_CHAR
jstring _priv_def_std_encoding;
bool _priv_std_null_terminated=true;
void stdir_setDefaultEncoding(char * encoding,bool nullTerminated){
	_priv_def_std_encoding=JvNewStringLatin1(encoding);
	_priv_std_null_terminated=nullTerminated;
}

std::string decodeSTDString(string_jt str){
	if(str==NULL)return std::string();
	JArray<jbyte> *myarray =((jstring) str)->getBytes(_priv_def_std_encoding);
	int size= myarray->length;
	if(_priv_std_null_terminated){
		size++;
	}
	std::string mystr((char *)elements(myarray),size);
	if(_priv_std_null_terminated){
		mystr[size-1]='\0';
	}
	return mystr;
}
bool std_isNullOrEmptyString(const std::string &s){
	return s.length()==0;
}
void initSTDIRateCenter(char * encoding, bool nullTerminated){
	IRateCenter<std::string>::initEngine(std_isNullOrEmptyString,decodeSTDString);
	stdir_setDefaultEncoding(encoding,nullTerminated);
	
}
#elif defined(_CPPIR_USE_WCHAR_T)
std::wstring decodeSTDString(string_jt str){
	#ifdef _IR_SIZEOF_WCHAR_EQUAL_USHORT
		std::wstring wstr((wchar_t*)JvGetStringChars((jstring)str),((jstring)str)->length());
		return wstr;
	#else
		//This a bit more tricky since we must copy all by value
		jchar * myarray=JvGetStringChars((jstring)str);
		int size=(int)((jstring)str)->length();
		wchar_t* warray=new wchar_t[size];
		for(int i=0;i<size;i++){
			warray[i]=(wchar_t)myarray[i];
		}
		std::wstring wstr(warray,size);
		delete warray;
		return wstr;
	#endif
}
bool std_isStringNullOrEmpty(const std::wstring& s){
	return s.length()==0;
}
#endif
