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
 #include <util_irate.h>
 #include <gcj/cni.h>
 #include <gcj/array.h>
 #include <stdlib.h>
 static jstring _priv_ir_Encoding;
 static short _priv_ir_nullTerminated=0;
void ir_setDefaultEncoding(const char * def,short nullTerminated){
	_priv_ir_Encoding=JvNewStringLatin1(def);
	_priv_ir_nullTerminated=nullTerminated;
}
int ir_getStringLength(string_jt str){
 	return ((jstring)str)->length();
}
int ir_getStringChars(string_jt str,char **buffer,int maxlen,int* realLength){
	if(str==NULL){
		*realLength=0;
		return 0;
	}
	JArray<jbyte> *myarray =((jstring) str)->getBytes(_priv_ir_Encoding);
	int l=myarray->length;
	*realLength=l;
	jbyte * mybyte=elements(myarray);
	int i=0;
	for(;i<l&&i<maxlen;i++) {
		(*buffer)[i]=(char)mybyte[i];
	}
		//buffer[i]='\0';
	delete myarray;
	return i;
}

char *ir_getMallocedStringDecoded(string_jt s,int* length){
	if(s==NULL){
		*length=0;
		return NULL;
	}
	JArray<jbyte> *myarray =((jstring) s)->getBytes(_priv_ir_Encoding);
	(*length)=myarray->length;
	jbyte * mybyte=elements(myarray);
	int i=0;
	if(_priv_ir_nullTerminated){
		(*length)++;
	}
	char * buffer=(char *)malloc((*length)*sizeof(char));
	for(;i<(*length);i++) {
		buffer[i]=(char)mybyte[i];
	}
	if(_priv_ir_nullTerminated){
		(*length)--;
		buffer[i]='\0';
	}
	
	//delete myarray;
	return buffer;
}
char *ir_getNewedStringDecoded(string_jt s,int* length){
	if(s==NULL){
		*length=0;
		return NULL;
	}
	JArray<jbyte> *myarray =((jstring) s)->getBytes(_priv_ir_Encoding);
	(*length)=myarray->length;
	if(_priv_ir_nullTerminated){
		(*length)++;
	}
	jbyte * mybyte=elements(myarray);
	int i=0;
	char * buffer=new char[(*length)];
	for(;i<(*length);i++) {
		buffer[i]=(char)mybyte[i];
	}
	if(_priv_ir_nullTerminated){
		(*length)--;
		buffer[i]='\0';
	}
	//delete myarray;
	return buffer;
}
