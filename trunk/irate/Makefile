BIN=$(DESTDIR)/usr/bin
LIB=$(DESTDIR)/usr/lib
DOC=$(DESTDIR)/usr/doc/irate
GCJ=gcj

PWD=$(shell pwd)
export CLASSPATH=.:$(PWD)/lib/nanoxml-lite-2.2.3.jar:$(PWD)/lib/swt.jar

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
	irate/client/Speech.java \
	irate/client/Help.java
#	irate/client/AudioDevice.java 
#	irate/client/JavaSoundAudioDevice.java 
#	irate/client/JavaLayerPlayer.java 

SERVER_DEPS = irate-server.o irate-common.a
SERVER_LIBS = lib/libnanoxml-lite.a

# You need to set this as executable by going:
# chmod a+x `find thisiscool-gcc/ -name \*.exe`

GCJ_WIN32 = thisiscool-gcc/gcc-3.3/bin/$(GCJ).exe  

all: lib irate-client irate-server

install: irate-client
	install -d $(BIN) $(LIB) $(DOC)
	install irate-client $(BIN)
	install lib/libswt-gnome-motif-3009.so $(LIB)
	install lib/libswt-kde-motif-3009.so $(LIB)
	install lib/libswt-motif-3009.so $(LIB)
	install README $(DOC)
	install COPYING $(DOC)

lib:
	$(MAKE) -C lib/

lib/libswt.a:
	$(MAKE) -C lib/ libswt.a

lib/libnanoxml-lite.a:
	$(MAKE) -C lib/ libnanoxml-lite.a
	
irate-client: $(CLIENT_DEPS) $(CLIENT_LIBS)
	$(GCJ)  $(CLIENT_DEPS) $(CLIENT_LIBS) -o irate-client --main=irate.swt.Client

irate-server: $(SERVER_DEPS)
	$(GCJ)  $(SERVER_DEPS) $(SERVER_LIBS) -o irate-server --main=irate.server.Server
	
irate-swt.o: irate/swt/*.java irate/swt/plugin/*.java irate/swt/plugin/*/*.java
	$(GCJ) -c irate/swt/*.java irate/swt/plugin/*.java irate/swt/plugin/*/*.java -o irate-swt.o

irate-client.o: $(CLIENT_FILES)
	$(GCJ) -c $(CLIENT_FILES) -o irate-client.o

irate-download.o: irate/download/*.java
	$(GCJ) -c irate/download/*.java -o irate-download.o
	
irate-common.a:irate-common.o
	ar rcs irate-common.a irate-common.o
	 
irate-common.o:irate/common/*.java irate/plugin/*.java irate/plugin/*/*.java
	$(GCJ) -c irate/common/*.java irate/plugin/*.java irate/plugin/*/*.java -o irate-common.o

irate-server.o:irate/server/*.java
	$(GCJ) -c irate/server/*.java -o irate-server.o

#using $(GCJ) from http://thisiscool.com/gcc33_mingw.htm
win32:
	$(GCJ_WIN32) \
	$(CLIENT_FILES) \
	irate/swt/*.java \
	irate/swt/plugin/*.java \
	irate/swt/plugin/*/*.java \
	irate/common/*.java \
	irate/plugin/*.java \
	irate/plugin/*/*.java \
	irate/download/*.java \
	thisiscool-gcc/swt/win32/lib/libswt.a \
	lib/nanoxml-lite-2.2.3.jar \
	--main=irate.swt.Client \
	-g -O2 -mwindows -o irate-client.exe

deb:
	dpkg-buildpackage -b -rfakeroot

clean:
	rm -f $(DEPS) irate-client irate-server *.a *.o
	$(MAKE) -C lib clean
