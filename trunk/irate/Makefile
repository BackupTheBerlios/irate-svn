BIN=$(DESTDIR)/usr/bin
LIB=$(DESTDIR)/usr/lib
DOC=$(DESTDIR)/usr/doc/irate
GCJ=gcj -g
IRATE_CLIENT=irate-client
PWD=$(shell pwd)
export CLASSPATH=.:$(PWD)/lib/nanoxml-lite-2.2.3.jar:$(PWD)/lib/swt.jar


CLIENT_DEPS = irate-swt.o irate-common.o irate-client.o irate-download.o
CLIENT_LIBS = lib/libswt.o lib/libnanoxml-lite.o
ifdef WITH_GTK2
CLIENT_LIBS += lib/libswt-pi.o
endif

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

SERVER_DEPS = irate-server.o irate-common.o
SERVER_LIBS = lib/libnanoxml-lite.o

all: lib $(IRATE_CLIENT) irate-server

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

lib/libswt.o:
	$(MAKE) -C lib/ libswt.o

lib/libswt-pi.o:
	$(MAKE) -C lib/ libswt-pi.o

lib/libnanoxml-lite.o:

lib/libnanoxml-lite.o:
	$(MAKE) -C lib/ libnanoxml-lite.o
	
irate-client: $(CLIENT_DEPS) $(CLIENT_LIBS)
	$(GCJ)  $(CLIENT_DEPS) $(CLIENT_LIBS) -o irate-client --main=irate.swt.Client

irate-client.exe: lib/libnanoxml-lite.o $(CLIENT_DEPS) lib/libswt-win32.a
	$(GCJ) lib/libnanoxml-lite.o $(CLIENT_DEPS) lib/libswt-win32.a  --main=irate.swt.Client -o irate-client.exe

irate-server: $(SERVER_DEPS) $(SERVER_LIBS)
	$(GCJ)  $(SERVER_DEPS) $(SERVER_LIBS) -o irate-server --main=irate.server.Server
	
irate-swt.o: irate/swt/*.java irate/swt/plugin/*.java irate/swt/plugin/*/*.java
	$(GCJ) -c irate/swt/*.java irate/swt/plugin/*.java irate/swt/plugin/*/*.java -o irate-swt.o

irate-client.o: $(CLIENT_FILES)
	$(GCJ) -c $(CLIENT_FILES) -o irate-client.o

irate-download.o: irate/download/*.java
	$(GCJ) -c irate/download/*.java -o irate-download.o
	
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
