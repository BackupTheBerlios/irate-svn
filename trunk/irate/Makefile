# Author: Taras Glek
# Modifed by: Anthony Jones, Philip Brown
include make.configure

BIN=$(DESTDIR)/usr/bin
LIB=$(DESTDIR)/usr/lib
RES=$(DESTDIR)/usr/share
JPATH=.:lib/nanoxml-lite-2.2.3.jar:lib/skinlf.jar:lib/jl030.jar:lib/jlGui2.2.jar:lib/jd3lib.jar
JAVAC=javac -classpath $(JPATH)
export GCJ=gcj -g -pipe -O0
IRATE_CLIENT=irate-client
IRATE_SERVER=irate-server
#export PLATFORM=linux-motif
export PLATFORM=linux-gtk2
#export PLATFORM=win32
PWD=$(shell pwd)
export CLASSPATH=$(PWD):$(PWD)/lib/nanoxml-lite-2.2.3.jar:lib/jd3lib.jar

CLIENT_DEPS = irate-swt.o irate-common.o irate-client.o irate-download.o #libmadplayer.o
CLIENT_LIBS = lib/libnanoxml-lite.o lib/libjd3.o

JAVA_SWTSRC=irate/swt/*.java irate/swt/plugin/*.java irate/swt/plugin/*/*.java
JAVA_SWINGSRC=irate/swing/*.java
JAVA_CLIENT=irate/client/*.java
JAVA_COMMONSRC=irate/common/*.java irate/plugin/*.java irate/plugin/*/*.java irate/resources/*.java
JAVA_DOWNLOAD=irate/download/*.java
HELPFILES=irate/client/help/*.html
JAVA_OBJS=$(JAVA_SWINGSRC:.java=.class) $(JAVA_COMMONSRC:.java=.class) $(JAVA_CLIENT:.java=.class) $(JAVA_DOWNLOAD:.java=.class)

#request resource compilation if not gcj 3.0.4
#set in make.configure
ifndef NO_RESOURCE
IMAGES_GIF = $(shell find irate/resources/ -name '*.gif')
IMAGES_PNG = $(shell find irate/resources/ -name '*.png')
CLIENT_DEPS += $(IMAGES_GIF:.gif=.gifres.o)
CLIENT_DEPS += $(IMAGES_PNG:.png=.pngres.o)
LOCALES = \
  $(wildcard irate/common/locale*.properties) \
  $(wildcard irate/download/locale*.properties) \
  $(wildcard irate/swt/locale*.properties)
CLIENT_DEPS += $(LOCALES:.properties=.properties.o)  
TXT_RESOURCES = \
  irate/common/timestamp.txt \
  irate/common/version.txt
CLIENT_DEPS += $(TXT_RESOURCES:.txt=.txt.o)  
endif

ifeq ($(PLATFORM),win32)
IRATE_CLIENT=irate-client.exe
IRATE_SERVER=irate-server.exe
#turn on below thing for error messages
#CLIENT_LINKFLAGS=-Dgnu.gcj.runtime.NameFinder.use_addr2line=false -Dgnu.gcj.runtime.NameFinder.demangle=false
export GCJ=/usr/local/crossgcc/bin/i586-pc-mingw32-gcj -g -pipe -O0
CLIENT_LIBS += lib/libswt.o icon.o
CLASSPATH:=$(PWD)/lib/swt.jar:$(CLASSPATH)
else

#configure script can set these in make.configure
ifdef SWT_INCLUDE
CLIENT_LINKFLAGS=$(SWT_LIB)
CLASSPATH:=$(SWT_INCLUDE):$(CLASSPATH)
else
CLIENT_LIBS+= lib/libswt.o
CLASSPATH:=$(PWD)/lib/swt.jar:$(CLASSPATH)
endif

endif

CLIENT_FILES = \
	irate/client/AbstractClient.java \
	irate/client/PlayThread.java \
	irate/client/PlayerException.java \
	irate/client/PlayListManager.java \
	irate/client/Player.java \
	irate/client/PlayerList.java \
	irate/client/ExternalPlayer.java \
	irate/client/MadplayPlayer.java \
	irate/client/Speech.java \
	irate/client/Help.java \
	irate/client/AbstractPlayer.java \
	irate/client/PlayerListener.java \
	irate/client/Resources.java \
	irate/client/SoundEventPlayer.java
#	irate/client/LibMadPlayer.java
#	irate/client/AudioDevice.java 
#	irate/client/JavaSoundAudioDevice.java 
#	irate/client/JavaLayerPlayer.java 

SERVER_DEPS = irate-server.o irate-common.o
SERVER_LIBS = lib/libnanoxml-lite.o lib/libjd3.o

all: make.configure lib $(IRATE_CLIENT) $(IRATE_SERVER)

make.configure:
	touch $@

install: $(IRATE_CLIENT)
	install -d $(BIN) $(LIB)
ifdef NO_RESOURCE
	install $(IRATE_CLIENT) $(BIN)/irate-client-bin
	install irate-client-resource.sh $(BIN)/irate-client
	install -d $(RES)/irate/
	install -d $(RES)/irate/common/
	install irate/common/locale*.properties $(RES)/irate/common/
	install -d $(RES)/irate/download/
	install irate/download/locale*.properties $(RES)/irate/download/
	install -d $(RES)/irate/swt/
	install irate/swt/locale*.properties $(RES)/irate/swt/
	install -d $(RES)/irate/resources/
	install irate/resources/*.gif $(RES)/irate/resources/
else
	install $(IRATE_CLIENT) $(BIN)
	install -d $(RES)/icons/
	install irate/resources/icon.xpm $(RES)/icons/irate.xpm
	install -d $(RES)/gnome/
	install -d $(RES)/gnome/apps/
	install -d $(RES)/gnome/apps/Multimedia/
	install irate/resources/irate-client.desktop $(RES)/gnome/apps/Multimedia/
endif

ifeq ($(SWT_LIB),)
ifeq ($(PLATFORM),linux-motif)	
	install lib/libswt-gnome-motif-2133.so $(LIB)
	install lib/libswt-kde-motif-2133.so $(LIB)
	install lib/libswt-motif-2133.so $(LIB)
else
ifeq ($(PLATFORM),linux-gtk2)
	install lib/libswt-gnome-gtk-2135.so $(LIB)
	install lib/libswt-gtk-2135.so $(LIB)
	install lib/libswt-pi-gtk-2135.so $(LIB)
else
	echo Unknown PLATFORM $(PLATFORM), please specify on commandline && exit 1 
endif
endif
endif

lib:
	$(MAKE) -C lib/

lib/libswt.o:
	$(MAKE) -C lib/ libswt.o

lib/swt.jar:
	$(MAKE) -C lib/ swt.jar

lib/libnanoxml-lite.o:
	$(MAKE) -C lib/ libnanoxml-lite.o

lib/libjd3.o:
	$(MAKE) -C lib/ libjd3.o

lib/libhttpclient.o:
	$(MAKE) -C lib/ libhttpclient.o
	
$(IRATE_CLIENT): $(CLIENT_LIBS) $(CLIENT_DEPS) irate/resources/icon.xpm
	$(GCJ) $(CLIENT_LINKFLAGS) $(CLIENT_DEPS) $(CLIENT_LIBS) -o $(IRATE_CLIENT) --main=irate.swt.Client

$(IRATE_SERVER): $(SERVER_LIBS) $(SERVER_DEPS) 
	$(GCJ)  $(SERVER_DEPS) $(SERVER_LIBS) -o $(IRATE_SERVER) --main=irate.server.Server
	
irate-swt.o: irate/swt/*.java irate/swt/plugin/*.java irate/swt/plugin/*/*.java lib/swt.jar
	$(GCJ) -c $(JAVA_SWTSRC) -o irate-swt.o

irate-client.o: $(CLIENT_FILES)
	$(GCJ) -c $(CLIENT_FILES) -o irate-client.o

irate-download.o: $(JAVA_DOWNLOAD)
	$(GCJ) -c $(JAVA_DOWNLOAD) -o irate-download.o
	
irate-common.o:	$(JAVA_COMMONSRC)
	$(GCJ) -c $^ -o irate-common.o

irate-server.o:irate/server/*.java
	$(GCJ) -c irate/server/*.java -o irate-server.o

%.gifres.o: %.gif
	$(GCJ) -c --resource $^ $^ -o $@

%.pngres.o: %.png
	$(GCJ) -c --resource $^ $^ -o $@

%.properties.o: %.properties	
	$(GCJ) -c --resource $^ $^ -o $@

%.txt.o: %.txt
	$(GCJ) -c --resource $^ $^ -o $@

icon.o: irate/resources/icon.png
	png2ico icon.ico $^ 
	echo irate_icon ICON DISCARDABLE "icon.ico" > icon.rc
	i586-pc-mingw32-windres -i icon.rc -o $@
	rm -f icon.ico icon.rc

%.xpm: %.gif
	convert $^ $@

libmadplayer.o: irate/client/AbstractPlayer.java irate/client/LibMadPlayer.java irate/client/LibMadPlayer.cc
	gcjh -o irate/client/AbstractPlayer.h irate.client.AbstractPlayer
	gcjh -o irate/client/LibMadPlayer.h irate.client.LibMadPlayer
	$(GCJ) -c -I. -o libmadplayer.o irate/client/LibMadPlayer.cc

.PHONY: irate/common/timestamp.txt
irate/common/timestamp.txt:
	sed "s/@TIMESTAMP@/`date +'%Y%m%d%H%M'`/" \
        < irate/common/timestamp.txt.in \
        > irate/common/timestamp.txt

deb:
	dpkg-buildpackage -b -rfakeroot

#for help txt + resources
resource_irate_client_*.o:
	./build-help

setup.exe: $(IRATE_CLIENT)
	#upx irate-client.exe
	iscc irate-client.iss
	mv Output/setup.exe .

tarball: $(IRATE_CLIENT)
	bin/mktarball.sh

# for "pure java" compilation of a "pure java" client
irate-client-swing.jar:
	$(JAVAC) $(JAVA_COMMONSRC)
	$(JAVAC) $(JAVA_SWINGSRC)
	$(JAVAC) $(JAVA_CLIENT)
	$(JAVAC) $(JAVA_DOWNLOAD)
	jar -cmf META-INF/manifest.irate-client irate-client-swing.jar $(JAVA_OBJS) $(HELPFILES)

clean:
	rm -f $(DEPS) $(IRATE_CLIENT) $(IRATE_SERVER) *.a *.o setup.exe -R irate-linux-i586*
	$(MAKE) -C lib clean
