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
#ifndef TRACKDATABASE_H
#define TRACKDATABASE_H

#include <qobject.h>
#include <qxml.h>
#include <qdict.h>
#include <qptrlist.h>
//#include <qfile.h>
#include <qtextstream.h>
#include <qsocket.h>
#define DEFAULT_RATING -1f
#define DEFAULT_SERIAL -1
/**Class Track Info holds all informations for each track playable or not
* it has some wrapper inline function for simplicity
*/
class TrackInfo {
	public:
		/**Constructor insert all attributes for a given track into is Map
		*/
		TrackInfo(const QXmlAttributes& atts);

		/**Destructor does nothing
		*/
		virtual ~TrackInfo() {}
		/**Method to know if a track can be played
		*@return true if can be played
		*/
		bool isPlayable() {
			return (!(this->mProperties.contains("state")))&&(this->property("deleted")!="true");
		}
		/**Last Time played
		*Currently this property is not used should parse it into a QDateTime or something
		*return 0 if not played
		*/
		QString getLastPlayed() {
			return this->property("last");
		}
		/**return state property 0 if nothing
		*/
		QString getState() {
			return this->property("state");
		}
		/**Return serial -1 if not set
		*/
		int getSerial() {
			return this->property("serial","-1").toInt();
		}
		/**Set the serial property
		*@param serial the new serial number
		*/
		void setSerial(const int & serial) {
			this->setProperty("serial",QString::number(serial));
		}
		/**Getter for artist property
		*/
		QString getArtist() {
			return this->property("artist");
		}
		/**Getter for copyright property
		*/
		QString getCopyright() {
			return this->property("copyright");
		}
		/**Return localfilename
		*/
		QString getLocalFileName() {
			return this->property("file");
		}
		/**Return the title notice that this property may differ from the one of iRate since we read the id3 tags and put it here
		*/
		QString getTitle() {
			return this->property("title");
		}
		/**Location for downloading the file
		*/
		QString getURL() {
			return this->property("url");
		}
		/**Return current rating
		*@return -1.0f if not rated
		*/
		float getRating() {
			return this->property("rating","-1").toFloat();
		}
		/**Set a new rating for this song
		*/
		void setRating(const float& rate) {
			//QString ra;
			this->setProperty("rating",QString::number(rate,'f',2));
		}
		/**Number of time played
		*/
		int getPlayed() {
			return this->property("played","0").toInt();
		}
		/**Add one to the time played and set Time property
		*/
		void addPlayed();
		void calcDate();
		//Setter methods
		/**Getter for the property key
		*@param key property name
		*@param def default value returned if property doesn't exist
		*/
		QString property (const QString &key, const QString &def=0) const {
			if(this->mProperties.contains(key)) {
				return this->mProperties[key];
			} else return def;
		}
		/**set the value of property key
		*@param key property name
		*@param property value of this property
		*/
		void setProperty (const QString &key, const QString &property) {
			if(this->mProperties.contains(key)) {
				this->mProperties.remove(key);
			}
			this->mProperties[key]=property;
		}
		/**Unset the property key
		*@param key the property name to unset
		*/
		void clearProperty (const QString &key) {
			this->mProperties.remove(key);
		}
		/**List all property name available
		*@return list of keys
		*/
		QStringList properties () const {
			return QStringList(this->mProperties.keys());
		}
		/**Return true if contain a value for property key (the value can be empty)
		*/
		bool isProperty (const QString &key) const {
			return this->mProperties.contains(key);
		}
		/**Write an XML representation of this track on the stream
		*@param the stream to write into
		*/
		void saveAsString(QTextStream &s);
		/**This is a static method that should be called befor anything else
		* It's a quite ugly Hack in order to interact with noatun property system
		*/
		static void init();
		


	private:
		///Our properties are stored here
		QMap<QString,QString> mProperties;

	protected:
		///A list of attribute's name used by iRate, @see init()
		static QStringList xmlAttributes;
};
/**
Class managing all track. Broken or not. Managing trackdatabase.xml
You should only have one instance of TrackDatabase!!!!
@author Matthias Studer
*/

class TrackDatabase : public QObject, public QXmlDefaultHandler {
		Q_OBJECT
	public:
		/**Construct the Database but doesn't parse the file
		* @param irateDir iRate directory (where trackdatabase.xml is located)
		*/
		TrackDatabase(const QString &irateDir,QObject *parent = 0, const char *name = 0);

		/**Destructor
		* Destroy all TrackInfo
		*/
		virtual ~TrackDatabase();
		/**Convenience methods read trackDatabase.xml file
		*/
		void readFile(const QString& xmlFile);

		/**Return a list of Track that can be played
		*/
		QPtrList<TrackInfo> getPlayable(const float &minrating=0);
		QPtrList<TrackInfo> getDeleted();


		/**Method for parsing xml in trackdatabase.xml format
		* Called by the parser
		*/
		bool startElement(const QString &, const QString &,const QString &qName,const QXmlAttributes &atts);
		bool endElement(const QString &, const QString &,const QString &qName);
		/**If notify is set to true all track added will result in a trackAdded signal
		*/
		void setNotify(const bool& notify) {
			this->mNotify=notify;
		}
		/**Write an XML representation of this database including all track
		*/
		void saveAsString(QTextStream &s);
		/**Write an XML representation of this database only including new track
		*@return number of track written
		*/
		int saveAsSerialString(QTextStream &s);
		/**Parse the xml from the given source
		*/
		void processXML(QXmlInputSource * source);
		/**Return the irate directory location
		*/
		QString getIRateDir() {
			return this->mIrateDir;
		}
		void setIRateDir(const QString& newDir){
			this->mIrateDir=newDir;
		}
		int getSerial() {
			return this->mSerial;
		}
		int getAutoDownload() {
			return this->mAutoDown;
		}
		void setAutoDownload(const int& newAuto) {
			this->mAutoDown=newAuto;
		}
		void setPlaylistSize(const int& newPlaySize) {
			this->mPlaylist=newPlaySize;
		}
		int getPlaylistSize() {
			return this->mPlaylist;
		}
		void setNewAccountValue(const QString& host, const int &port, const QString& user,const QString& password);
		/*bool hasAccount(){
			return !this->mHost.isEmpty()&&!this->mUser.isEmpty()&&!this->mPassword.isEmpty();
		}*/


	protected:
		/**Place where all TrackInfo are stored
		*Unicity of TrackInfo is checked with unique url as key
		*/
		QDict<TrackInfo> mHashTrack;
		///If true emit signals when track added
		bool mNotify;
		//Info
		QString mIrateDir,mUser,mHost,mPassword;
		int mAutoDown, mDownCount, mPlaylist,mSerial,mPort, connectCount;

		//Network things
		QSocket *socket;

	public slots:
		/**Connect to the server for new track request and/or new account
		*/
		void connectToServer();
		void cleanDownloadDir();
		/**Save to trackdatabase.xml this trackdatabase
		*/
		void saveFile();
	protected slots:
		//Internal for Network protocol
		void readResponse();
		void connected();
		void connectionClosed();
		void socketError(int i);

	signals:
		///Emitted when a new track is added
		void trackAdded(TrackInfo*);
		///Emitted when a new mesage is needed typically error
		void serverError(QString code, QString url);
		/**Emitted when a socket error appear
		*@see QSocket for more about int information
		*/
		void networkError(int);
		/**Emit state of the connection
		* value of int can be :
		* 1 => connecting to server
		* 2 => connected and starting writing
		* 3 => starting reading server response
		* 4 => done reading response
		* 5 => connection closed
		* 6 => an socket error as appear
		*/
		void connectionState(int);

};

#endif
