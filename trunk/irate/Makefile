export CLASSPATH=.:lib/nanoxml-lite-2.2.3.jar:lib/swt.jar

CDEPS = SWTClient.o irate-common.a irate-client-nongui.o
CLIBS = lib/libswt.a lib/libnanoxml-lite.a

SDEPS = irate-server.o irate-common.a
SLIBS = lib/libnanoxml-lite.a

irate-client: $(CDEPS) 
	gcj  $(CDEPS) $(CLIBS) -o irate-client --main=irate.client.SWTClient
	
irate-server: $(SDEPS)
	gcj  $(SDEPS) $(SLIBS) -o irate-server --main=irate.server.Server
	
SWTClient.o: irate/client/SWTClient.java
	gcj -c irate/client/SWTClient.java -o SWTClient.o

irate-common.a:irate-common.o
	 ar rcs irate-common.a irate-common.o
	 
irate-common.o:irate/common/*.java
	gcj -c irate/common/*.java -o irate-common.o

irate-server.o:irate/server/*.java
	gcj -c irate/server/*.java -o irate-server.o
	
irate-client-nongui.o:irate/client/Play[L]*.java
	gcj -c irate/client/Play[L]*.java -o irate-client-nongui.o
	
clean:
	rm -f $(DEPS) irate-client irate-common.o 
