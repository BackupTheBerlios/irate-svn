export CLASSPATH=.:lib/nanoxml-lite-2.2.3.jar:lib/swt.jar

CLIENT_DEPS = irate-swt.o irate-common.a irate-client.o irate-download.o
CLIENT_LIBS = lib/libswt.a lib/libnanoxml-lite.a
CLIENT_FILES = \
	irate/client/PlayThread.java \
	irate/client/PlayerException.java \
	irate/client/PlayListManager.java \
	irate/client/Player.java \
	irate/client/Speech.java
#	irate/client/AudioDevice.java 
#	irate/client/JavaSoundAudioDevice.java 
#	irate/client/JavaLayerPlayer.java 

SERVER_DEPS = irate-server.o irate-common.a
SERVER_LIBS = lib/libnanoxml-lite.a


all: lib irate-client irate-server

lib:
	$(MAKE) -C lib/
	
irate-client: $(CLIENT_DEPS) 
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
	
clean:
	rm -f $(DEPS) irate-client irate-server *.a *.o
