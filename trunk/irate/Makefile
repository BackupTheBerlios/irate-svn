DEPS = SWTClient.o irate-common.a
LIBS = lib/libswt.a lib/libnanoxml-lite.a
client: $(DEPS) 
	gcj  $(DEPS) $(LIBS) -o client --main=irate.client.SWTClient
SWTClient.o: irate/client/SWTClient.java
	gcj -c irate/client/SWTClient.java -o SWTClient.o
irate-common.a:irate-common.o
	 ar rcs irate-common.a irate-common.o
irate-common.o:irate/common/*.java
	gcj -c irate/common/*.java -o irate-common.o	
clean:
	rm -f $(DEPS) client irate-common.o
