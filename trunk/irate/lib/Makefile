GCJ = gcj
all: libswt.so libswt.a libnanoxml-lite.a

libswt.so:libswt.o
	gcj -shared -o libswt.so libswt.o
libswt.a:libswt.o
	ar rcs libswt.a libswt.o
libswt.o: swt.jar 
	$(GCJ)  -fjni -o libswt.o -march=i686 -mcpu=i686  -mmmx -O1 -pipe -c  swt.jar 
libnanoxml-lite.o: nanoxml-lite-2.2.3.jar
	$(GCJ) -o libnanoxml-lite.o -c nanoxml-lite-2.2.3.jar
libnanoxml-lite.a: libnanoxml-lite.o
	ar rcs libnanoxml-lite.a libnanoxml-lite.o
clean:
	rm -f libswt.so libswt.o libswt.a libnanoxml-lite.o libnanoxml-lite.a
