
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


#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <c_irate.h>
#include <util_irate.h>
/*This file is a kind of tutorial about using the C interface for IRate, it is not a program that was intended to be used in any way. I will try to explain all things done
You will find ugly C here just because this is not the scope of this file
I recommand you to start on function main
*/


/*This function will act as a callback receiving message about creation of a new account
* statut connection statut and state is a kind of flag set to 1 if creation is in process, set to 2 if creation was successfull and to 3 if account creation as failed
*/
void newAcountCreationMessage(string_jt statut,int state){
	if(state==1){//Processing we just print this ugly message
		int size=0;
		//This function decode a string to a specified string format (see start of main for this)
		char* buf= ir_getMallocedStringDecoded(statut,&size);
		//The string returned above is not null terminated (since this depend on format we are using)
		//we do it her
//		buf[size]='\0';
		printf("Account Creation: %s\n",buf);
		//the string was created using malloc, so we must free it
		free(buf);
	}else if(state==2){//Yeah account was successfully created
		printf("Account successfully created\n");
		//Account is set, so we start downloading songs!
		ir_startDownloading();
	}
	else if(state==3){
		//Account creation has failed (usually we should prompt for a next try depending on error we have received see below)
		printf("Account creation as failed\n");
	}
	else{
		//We don't know what happen, so just print it
		int size=0;
		char* buf= ir_getMallocedStringDecoded(statut,&size);
//		buf[size]='\0';
		printf("Account Creation [Unknow %d]: %s\n",state,buf);
		free(buf);
	}
}
//This function is an utility function for prompting for a new account
void newAccountRequested() {
	printf("New account requested was requested\nPlease fill the following information:\n");
	//I know this is ugly C
	char username[20];
	char password[30];
	char host[100];
	char requestedDir[100];
	char yesno='n';
	int real_port=2278;
	int state=1;
	printf("\nusername:");
	scanf("%s",&username);
	printf("\npassword:");
	scanf("%s",&password);
	printf("\nhost(default is server.irateradio.org):");
	scanf("%s",&host);
	printf("\nport(default is 2278):");
	scanf("%d",&real_port);
	printf("\nWhich directory should IRate use(default homedirectory):");
	scanf("%s",&requestedDir);
	printf("\n\nYou have entered the following information :\nusername: %s\npassword: %s\nhost: %s\nport:%d\ndirectory:%s\n\nIs that correct ? (y/n):",username,password,host,real_port,requestedDir);
	scanf("%s",&yesno);
	if(yesno=='y'||yesno=='Y'){
		//Here we try to create the new account 
		//see the doc for full documentation but newAcountCreationMessage is a callback to get informed about creation process
		ir_createNewAccount(username,password,host,real_port,requestedDir,newAcountCreationMessage);
	}
}
//This function is a callback (ie called by the engine when an error occur
//This is not a complete description of the error but just some ID
void handleErrorCallback(string_jt url, string_jt message) {
	int size=0;
	char* urlbuf= ir_getMallocedStringDecoded(url,&size);
//	urlbuf[size]='\0';
	char* messagebuf= ir_getMallocedStringDecoded(message,&size);
//	messagebuf[size]='\0';
	printf("Error code : [%s] message : %s\n",urlbuf,messagebuf);
	free(urlbuf);
	free(messagebuf);
}
//Callback called when a download of a track is finished
//Here we just print this to the screen
//Usually you should enable the track on your playlist because it can be played now
//track_t t is an handle, your can get info about it (look in c_irate.h doc)
//s is a flag (true or false) if download was successfull or not
void downloadFinishedCallback(track_t t, short s){
	int size=0;
	//ir_getName return a formatted name with title and artist of the song
	char* buf= ir_getMallocedStringDecoded(ir_getName(t),&size);
//	buf[size]='\0';
	printf("download of %s finished\n",buf);
	free(buf);
}
//Callback to be informed about download progress of a track
//p is the percent completed 0<p<100
void downloadProgressedCallback(track_t t, int p) {
	int size=0;
	char* buf= ir_getMallocedStringDecoded(ir_getName(t),&size);
//	buf[size]='\0';
	printf("downloaded %d of %s\n",p,buf);
	free(buf);
}
//Download of this track has started
//you should add this track to your GUI but in a manner where it canno't be played
void downloadStartedCallback(track_t t) {
	int size=0;
	char* buf= ir_getMallocedStringDecoded(ir_getName(t),&size);
//	buf[size]='\0';
	printf("download of %s started\n",buf);
	free(buf);
}
//Callback to tell that this track has been updated like when we rate it or play it
void updateTrackCallback(track_t t) {
	int size=0;
	char* buf= ir_getMallocedStringDecoded(ir_getName(t),&size);
//	buf[size]='\0';
	printf("Track %s updated\n",buf);
	free(buf);
}
//Utility function we always print song with this
void printSong(track_t t){
	int size=0;
	char* namebuf= ir_getMallocedStringDecoded(ir_getName(t),&size);
//	namebuf[size]='\0';
	char* filebuf= ir_getMallocedStringDecoded(ir_getFile(t),&size);
//	filebuf[size]='\0';
	printf("Song: %s\nFilename: %s\n",namebuf,filebuf);
	free(filebuf);
	free(namebuf);
}
//utility function to print song when we will print the list
void print_song_list(track_t t){
	int size=0;
	char* namebuf= ir_getMallocedStringDecoded(ir_getName(t),&size);
//	namebuf[size]='\0';
	//ir_getState return a string containing info about the song (like percent done, or rating etc)
	char* filebuf= ir_getMallocedStringDecoded(ir_getState(t),&size);
//	filebuf[size]='\0';
	//ir_getRating return the current rating for this track in a float value
	printf("%s | %s | %d\n",namebuf,filebuf,(int)ir_getRating(t));
	free(filebuf);
	free(namebuf);
}
//Main function, here come serious things
int main(int argc, char *argv[]) {
	//definition of variable we will use
	track_t currentTrack=NULL;
	char input='h';
	track_t t=NULL;
	int l=0;
	track_t* list;
	//This is one of the most important function, it start and init the IRate engine
	//This must be called befor all other IRate functions even function about strings
	//we pass as parameter the function we would like to be called (see def of this function above)
	ir_initEngineWithCallback(
	    handleErrorCallback,
	    downloadFinishedCallback,
	    downloadProgressedCallback,
	    downloadStartedCallback,
	    updateTrackCallback
	);
	//if you want to use function from util_irate you must call this before any other call
	//But always after ir_initEngineWithCallback
	//this function set the encoding to use see doc for more
	ir_setDefaultEncoding("UTF-8",1);//"ISO-8859-1");
	//Do we need to create a new account or there's already one, if not..
	if(!ir_needNewAccount()){
		//Start the downloading, connection to server and so on
		ir_startDownloading();
	}
	else{//we can't start download because there's nothing to download starting of download is set when the account is actually created
		newAccountRequested();
	}
	//this is a loop to look for user input
	while(input!='q'&&input!='Q'){
		
		switch(input){
			case 'h':
			printf("The following command are available:\nn - print next song\n\
m - print next song and set current as played\np - print previous song\nl - print list of songs\n\nRatings\n\
0 - rate current as \"This sux\"\n1 - rate current as \"Yawn\"\n\
2 - rate current as \"Not Bad\"\n3 - rate current as \"Cool\"\n4 - rate \
current as \"Love it\"\n\nq - quit this program");
			break;
			case 'n':
				//ir_next tell you the next track to play
				//if the parameter is set to 0 then we don't set the last track as played
				//this is usefull if the track was skipped 
				currentTrack=ir_next(0);
				//print the track returned by next
				printSong(currentTrack);
			break;
			case 'm':
				//Here we look for the next song but we set the parameter to 1
				//the last track will be updated with a new last played date and a new numberOfTime played
				currentTrack=ir_next(1);
				printSong(currentTrack);
			break;
			case 'p':
				//Here we look for the previous track
				//ir_previous can return a null value (ie we didn't play any track before or we call it until the end of the stack
				t=ir_previous();
				if(t!=NULL){
					currentTrack=t;
					printSong(currentTrack);
					t=NULL;
				}
			break;
			case 'l':
				//Here we get a list of all track, &l is needed to know the number of tracks we have in ower database
				list= ir_availableTracks(&l);
				//ir_availableTracks can return a NULL value if no tracks are in ower database
				if(l!=0){
					for(int i=0;i<l;i++){
						//We don't want to show the track if it is a broken download, a track rated as This Sux and so on, so we check for it with ir_isHidden
						if(!ir_isHidden(list[i])){
							print_song_list(list[i]);
						}
					}
				}
			break;
			case '0':
				//Rate the track 0
				ir_setRating(currentTrack,0);
				//Since the user don't like this song we check for the next one
				currentTrack=ir_next(0);
				printSong(currentTrack);
			break;
			case '1':
				//Set the rating to 2 (Yawn)
				ir_setRating(currentTrack,2);
			break;
			case '2':
				//Set rating to 5 (Not Bad)
				ir_setRating(currentTrack,5);
			break;
			case '3':
				//Set rating to 7 (Cool)
				ir_setRating(currentTrack,7);
			break;
			case '4':
				//Set rating to 10 (Love it)
				ir_setRating(currentTrack,10);
			break;
		}
		//prompt
		printf("\n\ncommand>");
		//scan for input, ugly c I know
		scanf("%s",&input);
	}
	//We must always call this function before leaving, it will save the database, clean erased file and so on
	//This will also free the memory taken by the engine
	ir_closeAndQuitEngineWithCallback();
	//Success of our program
	return EXIT_SUCCESS;
}//if you have more questions tell me

