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
#ifndef _UTIL_IRATE_H_
#define _UTIL_IRATE_H_ 

 	/**Set the default encoding to use in the next functions if not set latin-1 is assumed
	* The following encoding are available for sure, others may be available 
	* If a character is not available if the destination charset, result is unpredicable<ul>
	*<li>US-ASCII Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set</li>
	*<li>ISO-8859-1 ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1</li>
	*<li>UTF-8 Eight-bit UCS Transformation Format</li>
	* <li>UTF-16BE Sixteen-bit UCS Transformation Format, big-endian byte order</li>
	* <li>UTF-16LE Sixteen-bit UCS Transformation Format, little-endian byte order</li>
	* <li>UTF-16 Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark</li>
	*</ul>
	*@param def the encoding to use
	*
	*/
 	  void ir_setDefaultEncoding(const char * def, short nullTerminated);
	/** Return the number of character of this string (this is different than the size of char needed
	* @param str the string we want to know chars
	*/
	  int ir_getStringLength(string_jt str);
	//char * ir_getChars(string_jt str);
	/**Copy the string char into a buffer
	* this function is not recommended @see ir_decodeString
	* @param str string to copy
	* @param buffer buffer to copy the string into, enougth space must be available otherwise only a part of chars are returned (!=part of string!!!!)
	* @param maxlen max number of char we can use
	* @param realLength the real size of the string when decoded
	* @return int the number of char used
	*/
	  int ir_getStringChars(string_jt str,char **buffer,int maxlen,int* realLength);
	/**This function decode a string returned pointer <b>must be freed</b> with free()
	* Return of string are not null terminated (since this depend on encoding)<br>
	* typical use of this function is as follow <code>
	* ir_setDefaultEncoding("ISO-8859-1");<br>
	* int size=0;<br>
	* char * buffer=ir_getMallocedStringDecoded(mystr,&size)<br>
	* buffer[size]='\0';<br>
	* printf("%s",buffer);<br>
	* free(buffer);<br>
	*</code>
	* @param s the string to decode
	* @param length a pointer to int that will hold the size of the decoded string
	*/  
	char *ir_getMallocedStringDecoded(string_jt s,int* length);
	/**This function decode a string returned pointer <b>must be deleted</b> with delete
	* Return of string are not null terminated (since this depend on encoding)<br>
	* typical use of this function is as follow <code>
	* ir_setDefaultEncoding("ISO-8859-1");<br>
	* int size=0;<br>
	* char * buffer=ir_getNewedStringDecoded(mystr,&size)<br>
	* buffer[size]='\0';<br>
	* printf("%s",buffer);<br>
	* delete buffer;<br>
	*</code>
	* @param s the string to decode
	* @param length a pointer to int that will hold the size of the decoded string
	*/  
	char *ir_getNewedStringDecoded(string_jt s,int* length);

#endif
