BIN=$(DESTDIR)/usr/bin
LIB=$(DESTDIR)/usr/lib
DOC=$(DESTDIR)/usr/doc/irate-client-motif
export GCJ=gcj -g -pipe -O0
IRATE_CLIENT=irate-client
IRATE_SERVER=irate-server
export PLATFORM=linux-motif
#export PLATFORM=linux-gtk2
#export PLATFORM=win32
PWD=$(shell pwd)
export CLASSPATH=$(PWD):$(PWD)/lib/nanoxml-lite-2.2.3.jar:$(PWD)/lib/swt.jar

CLIENT_DEPS = irate-swt.o irate-common.o irate-client.o irate-download.o
CLIENT_LIBS = lib/libswt.o lib/libnanoxml-lite.o 

#cute hack to see if we can bundle the docs too
#ifneq ($(shell html2text -help 2> /dev/null),)
#CLIENT_DEPS += resource_irate_client_*.o
#endif

ifeq ($(PLATFORM),win32)
IRATE_CLIENT=irate-client.exe
IRATE_SERVER=irate-server.exe
export GCJ=/usr/local/crossgcc/bin/i586-pc-mingw32-gcj -g -pipe -O0
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

all: lib $(IRATE_CLIENT) $(IRATE_SERVER)

install: $(IRATE_CLIENT)
	install -d $(BIN) $(LIB) $(DOC)
	install $(IRATE_CLIENT) $(BIN)
	install lib/libswt-gnome-motif-3009.so $(LIB)
	install lib/libswt-kde-motif-3009.so $(LIB)
	install lib/libswt-motif-3009.so $(LIB)
	install README $(DOC)
	install COPYING $(DOC)

lib:
	$(MAKE) -C lib/

lib/libswt.o:
	$(MAKE) -C lib/ libswt.o

lib/libnanoxml-lite.o:

lib/libnanoxml-lite.o:
	$(MAKE) -C lib/ libnanoxml-lite.o

lib/libhttpclient.o:
	$(MAKE) -C lib/ libhttpclient.o
	
$(IRATE_CLIENT): $(CLIENT_LIBS) $(CLIENT_DEPS)
	$(GCJ)  $(CLIENT_DEPS) $(CLIENT_LIBS) -o $(IRATE_CLIENT) --main=irate.swt.Client


$(IRATE_SERVER): $(SERVER_LIBS) $(SERVER_DEPS) 
	$(GCJ)  $(SERVER_DEPS) $(SERVER_LIBS) -o $(IRATE_SERVER) --main=irate.server.Server
	
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

deb:
	dpkg-buildpackage -b -rfakeroot

#for help txt + resources
resource_irate_client_*.o:
	./build-help

setup.exe: $(IRATE_CLIENT)
	iscc irate-client.iss
	mv Output/setup.exe .

tarball: $(IRATE_CLIENT)
	bin/mktarball.sh

clean:
	rm -f $(DEPS) $(IRATE_CLIENT) $(IRATE_SERVER) *.a *.o setup.exe -R irate-linux-i586*
	$(MAKE) -C lib clean
