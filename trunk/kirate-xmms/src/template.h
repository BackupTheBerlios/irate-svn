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
#ifndef TEMPLATE_H
#define TEMPLATE_H
#include <qstring.h>
#include <qmap.h>
#include <kmacroexpander.h>
#include "safelistviewitem.h"
/*class SafeListViewItem {
public:
	SafeListViewItem(){}
	QString property(const QString& p,const QString& def){
		if(prop.contains(p))return prop[p];
		else return def;
	}
	
	QMap<QString,QString> prop;
};*/
/**
@author Matthias Studer
*/
class Template {
	public:
	Template();

	~Template();
	
	/**Decode the given Text using information from it
	 * 
	 * @param templ Template
	 * @param it information are tacken from it
	 * @return text according to template and it
	 */
	QString fastTemplate(const QString &tmpl, SafeListViewItem *i){
		return this->decodeText(KMacroExpander::expandMacros(tmpl,this->keywords,'%'),i);
	}
	QString osdTemplate(SafeListViewItem* it){
		return this->decodeText(this->osdTmpl,it);
	}
	QString infoTemplate(SafeListViewItem* it){
		return this->decodeText(this->infoTmpl,it);
	}
	void init(const QString& tmplDir,const QString& iconDir);
	void loadOSDTemplate(const QString &file);
	void loadInfoTemplate(const QString &file);
	QString getCurrentInfoTemplate(){
		return this->infoTmplFile;
	}
	QString getCurrentOSDTemplate(){
		return this->osdTmplFile;
	}
	QStringList getAvailableInfoTemplate();
	QStringList getAvailableOSDTemplate();
	QString getTemplateDir(){
		return templateDir;
	}
/*	QStringList keywordsList(){
		return this->keywords.keys();
	}*/
	/**Singleton for Template this make it possible to access from anywhere
	 * @return instance
	 */
	static Template * instance();
	protected:
	QString decodeText(const QString &templ, SafeListViewItem* it);
	static Template * m_instance;
	
	///Pair of KEY template value
	QMap<QString,QString> keywords;
	QString infoTmpl,osdTmpl,templateDir;
	QString infoTmplFile,osdTmplFile;
};

#endif
