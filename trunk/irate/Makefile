#export CLASSPATH=.:lib/nanoxml-lite-2.2.3.jar:lib/swt.jar
export CLASSPATH=.:lib/nanoxml-lite-2.2.3.jar:thisiscool-gcc/swt/win32/lib/swt.jar

CLIENT_DEPS = irate-swt.o irate-common.a irate-client.o irate-download.o
CLIENT_LIBS = lib/libswt.a lib/libnanoxml-lite.a
CLIENT_FILES = \
	irate/client/PlayThread.java \
	irate/client/PlayerException.java \
	irate/client/PlayListManager.java \
	irate/client/Player.java \
	irate/client/PlayerList.java \
	irate/client/ExternalPlayer.java \
	irate/client/MadplayPlayer.java \
	irate/client/Speech.java
#	irate/client/AudioDevice.java 
#	irate/client/JavaSoundAudioDevice.java 
#	irate/client/JavaLayerPlayer.java 

SERVER_DEPS = irate-server.o irate-common.a
SERVER_LIBS = lib/libnanoxml-lite.a

# You need to set this as executable by going:
# chmod a+x `find thisiscool-gcc/ -name \*.exe`

GCJ_WIN32 = thisiscool-gcc/gcc-3.3/bin/gcj.exe  

all: lib irate-client irate-server

lib:
	$(MAKE) -C lib/

lib/libswt.a:
	$(MAKE) -C lib/ libswt.a

lib/libnanoxml-lite.a:
	$(MAKE) -C lib/ libnanoxml-lite.a
	
irate-client: $(CLIENT_DEPS) $(CLIENT_LIBS)
	gcj  $(CLIENT_DEPS) $(CLIENT_LIBS) -o irate-client --main=irate.swt.Client

irate-server: $(SERVER_DEPS)
	gcj  $(SERVER_DEPS) $(SERVER_LIBS) -o irate-server --main=irate.server.Server
	
irate-swt.o: irate/swt/*.java
	gcj -c irate/swt/*.java -o irate-swt.o

irate-client.o: $(CLIENT_FILES)
	gcj -c $(CLIENT_FILES) -o irate-client.o

irate-download.o: irate/download/*.java
	gcj -c irate/download/*.java -o irate-download.o
	
irate-common.a:irate-common.o
	ar rcs irate-common.a irate-common.o
	 
irate-common.o:irate/common/*.java
	gcj -c irate/common/*.java -o irate-common.o

irate-server.o:irate/server/*.java
	gcj -c irate/server/*.java -o irate-server.o

#using gcj from http://thisiscool.com/gcc33_mingw.htm
win32:
	$(GCJ_WIN32) \
	$(CLIENT_FILES) \
	irate/swt/*.java \
	irate/common/*.java \
	irate/download/*.java \
	thisiscool-gcc/swt/win32/lib/libswt.a \
	lib/nanoxml-lite-2.2.3.jar \
	--main=irate.swt.Client \
	-g -O2 -mwindows -o irate-client.exe

clean:
	rm -f $(DEPS) irate-client irate-server *.a *.o
