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
 #define _CPPIR_USE_CHAR
#include <iratedef.h>
#include <string>
#include <iostream>
#include <list>
#include <iratetrack.h>
#include <iratecenter.h>
#include <stdiratetrack.h>
using namespace std;
class MyIRListener: public IRateCenterListener<std::string>{
	public:
	MyIRListener():IRateCenterListener<std::string>(){}
	virtual ~MyIRListener(){}
	virtual void updateTrack (IRateTrack<std::string>* track){
		cout<<"updated "<<track->getName()<<endl;
	}
	virtual void handleError (std::string code, std::string url){
		cout<<"Error ["<<code<<"] : "<<url;
	}
	virtual void downloadFinished (IRateTrack<std::string>* track, bool success){
		cout<<"Finished downloading "<<track->getName();
		if(success){
			cout<<" with success"<<endl;
		}else{
			cout<<" with failure"<<endl;
		}
	}
	virtual void downloadProgressed (IRateTrack<std::string>* track, const int& percent){
		cout<<"Downloaded "<<percent<<"% of "<<track->getName()<<endl;
	}
	virtual void downloadStarted (IRateTrack<std::string>* track){
		cout<<"Started download of "<<track->getName();
	}
	virtual void newAccountCreationMessage(std::string statut,int state){
		switch(state){
			case 1:
				cout<<"Account creation : "<<statut<<endl;
			break;
			case 2:
				cout<<"Account successfully created"<<endl;
				IRateCenter<std::string>::instance()->startDownloading();
				cout<<"Download started"<<endl;
			break;
			case 3:
				cout<<"Account creation has failed"<<endl;
			break;
			default:
				cout<<"Account creation [unknow "<<state<<"] "<<statut<<endl;
		}
	}
};
//This function is an utility function for prompting for a new account
void newAccountRequested() {
	printf("New account requested was requested\nPlease fill the following information:\n");
	//I know this is ugly C
	string username;
	string password;
	string host;
	string requestedDir;
	string yesno("n");
	int real_port=2278;
	cout<<endl<<"username:";
	cin>>username;
	cout<<endl<<"password:";
	cin>>password;
	cout<<endl<<"host(default is server.irateradio.org):";
	cin>>host;
	cout<<endl<<"port(default is 2278):";
	cin>>real_port;
	cout<<endl<<"Which directory should IRate use(default homedirectory):";
	cin>>requestedDir;
	cout<<"\n\nYou have entered the following information :"<<endl<<"username: "<<username<<endl<<"password: "<<password<<endl<<"host: "<<real_port<<endl<<"directory:"<<requestedDir<<endl<<endl<<"Is that correct ? (y/n):";
	cin>>yesno;
	if(yesno[0]=='y'||yesno[0]=='Y'){
		//Here we try to create the new account 
		//see the doc for full documentation but newAcountCreationMessage is a callback to get informed about creation process
		IRateCenter<std::string>::instance()->createNewAccount(username.c_str(),password.c_str(),host.c_str(),real_port,requestedDir.c_str());
	}
}

void printFullSong(IRateTrack<std::string>* t){
	cout<<"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"<<endl;
	cout<<"isRated "<<t->isRated()<<endl;
	cout<<"Rating "<<t->getRating()<<endl;
	cout<<"getNoOfTimesPlayed "<<t->getNoOfTimesPlayed()<<endl;
	cout<<"getVolume "<<t->getVolume()<<endl;
	cout<<"isBroken "<<t->isBroken()<<endl;
	cout<<"isMissing "<<t->isMissing()<<endl;
	cout<<"isDeleted "<<t->isDeleted()<<endl;
	cout<<"isActive "<<t->isActive()<<endl;
	cout<<"isNotDownloaded "<<t->isNotDownloaded()<<endl;
	cout<<"isHidden "<<t->isHidden()<<endl;
	cout<<"isPendingPurge "<<t->isPendingPurge()<<endl;
	cout<<"isOnPlayList "<<t->isOnPlayList()<<endl;
	cout<<"getProbability "<<t->getProbability()<<endl;
	cout<<"exists "<<t->exists ()<<endl;
	cout<<"getPlayingTime "<<t->getPlayingTime()<<endl;
	cout<<"getDownloadAttempts "<<t->getDownloadAttempts()<<endl;
	cout<<"getName "<<t->getName()<<endl;
	cout<<"getLastPlayed "<<t->getLastPlayed()<<endl;
	cout<<"getArtist "<<t->getArtist ()<<endl;
	cout<<"getTitle "<<t->getTitle ()<<endl;
	cout<<"getURL "<<t->getURL ()<<endl;
	cout<<"getKey "<<t->getKey ()<<endl;
	cout<<"getFile "<<t->getFile ()<<endl;
	cout<<"getState "<<t->getState()<<endl;
	cout<<"getWebSite "<<t->getWebSite ()<<endl;
	cout<<"getLicense "<<t->getLicense ()<<endl;
	cout<<"getAlbum "<<t->getAlbum ()<<endl;
	cout<<"getComment "<<t->getComment ()<<endl;
	cout<<"getCopyrightInfo "<<t->getCopyrightInfo()<<endl;
	cout<<"getGenre "<<t->getGenre ()<<endl;
	cout<<"getPlayingTimeString "<<t->getPlayingTimeString ()<<endl;
	cout<<"getYear "<<t->getYear ()<<endl;
	cout<<"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"<<endl<<endl;
}


//Main function, here come serious things
int main(int argc, char *argv[]) {
	//This is one of the most important function, it start and init the IRate engine
	//This must be called befor all other IRate functions even function about strings
	//we pass as parameter the function we would like to be called (see def of this function above)
	cout<<"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"<<endl<<"Welcome on the example of using libirate with C++"<<endl<<"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"<<endl;
	list<IRateTrack<std::string>*> l;
	IRateTrack<std::string>* currentTrack;
	IRateTrack<std::string>* t;
	std::string input("h");
	initSTDIRateCenter("UTF-8", false);
	cout<<"IRate Started"<<endl;
	MyIRListener * listener= new MyIRListener();
	IRateCenter<std::string>::instance()->addListener(listener);
	
	//Do we need to create a new account or there's already one, if not..
	/*if(!IRateCenter<std::string>::instance()->needNewAccount()){
		//Start the downloading, connection to server and so on
		IRateCenter<std::string>::instance()->startDownloading();
	}
	else{//we can't start download because there's nothing to download starting of download is set when the account is actually created
		newAccountRequested();
	}*/
	//this is a loop to look for user input
	while(input[0]!='q'&&input[0]!='Q'){
		
		switch(input[0]){
			case 'h':
			cout<<"The following command are available:\nn - print next song\n\
m - print next song and set current as played\np - print previous song\nl - print list of songs\n\nRatings\n\
0 - rate current as \"This sux\"\n1 - rate current as \"Yawn\"\n\
2 - rate current as \"Not Bad\"\n3 - rate current as \"Cool\"\n4 - rate \
current as \"Love it\"\n\nq - quit this program"<<endl;
			break;
			case 'n':
				//ir_next tell you the next track to play
				//if the parameter is set to 0 then we don't set the last track as played
				//this is usefull if the track was skipped 
				currentTrack=IRateCenter<std::string>::instance()->next(0);
				//print the track returned by next
				printFullSong(currentTrack);
				//cout<<"Current song is "<<currentTrack->getName()<<" Filename "<<currentTrack->getFile()<<endl;
				//printSong(currentTrack);
			break;
			case 'm':
				//Here we look for the next song but we set the parameter to 1
				//the last track will be updated with a new last played date and a new numberOfTime played
				currentTrack=IRateCenter<std::string>::instance()->next(0);
				//cout<<"Current song is "<<currentTrack->getName()<<" Filename "<<currentTrack->getFile()<<endl;
				printFullSong(currentTrack);
			break;
			case 'p':
				//Here we look for the previous track
				//ir_previous can return a null value (ie we didn't play any track before or we call it until the end of the stack
				t=IRateCenter<std::string>::instance()->previous();
				if(t!=NULL){
					currentTrack=t;
					//cout<<"Current song is "<<currentTrack->getName()<<" Filename "<<currentTrack->getFile()<<endl;
					printFullSong(currentTrack);
					t=NULL;
				}
			break;
			case 'l':
				//Here we get a list of all track, &l is needed to know the number of tracks we have in ower database
				l= IRateCenter<std::string>::instance()->availableTracks();
				//ir_availableTracks can return a NULL value if no tracks are in ower database
				if(!l.empty()){
					list<IRateTrack<std::string>*>::iterator it;
					for(it=l.begin();it!=l.end();++it){
						//We don't want to show the track if it is a broken download, a track rated as This Sux and so on, so we check for it with ir_isHidden
						t=*it;
						if(!t->isHidden()){
							//cout<<t->getName()<<" || "<<t->getState()<<" || "<<t->getRating()<<endl;
							printFullSong(t);
						}
					}
					t=NULL;
				}
			break;
			case '0':
				//Rate the track 0
				currentTrack->setRating(0);
				//Since the user don't like this song we check for the next one
				currentTrack=IRateCenter<std::string>::instance()->next(0);
				//print the track returned by next
				cout<<"Current song is "<<currentTrack->getName()<<" Filename "<<currentTrack->getFile()<<endl;
			break;
			case '1':
				//Set the rating to 2 (Yawn)
				currentTrack->setRating(2);
			break;
			case '2':
				//Set rating to 5 (Not Bad)
				currentTrack->setRating(5);
			break;
			case '3':
				//Set rating to 7 (Cool)
				currentTrack->setRating(7);
			break;
			case '4':
				//Set rating to 10 (Love it)
				currentTrack->setRating(10);
			break;
		}
		//prompt
		cout<<"\n\ncommand>"<<endl;
		//scan for input, ugly c I know
		input.clear();
		cin>>input;
		if(input.length()==0)
			input.append("h");
	}
	//We must always call this function before leaving, it will save the database, clean erased file and so on
	//This will also free the memory taken by the engine
	IRateCenter<std::string>::instance()->closeAndQuitEngine();
	//Success of our program
	return EXIT_SUCCESS;
}//if you have more questions tell me


