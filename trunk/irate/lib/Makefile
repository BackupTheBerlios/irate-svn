GCJ = gcj
all: libswt.a libnanoxml-lite.a 

#libswt.so:libswt.o
#	gcj -shared -o libswt.so libswt.o
#libswt-pi.a:libswt-pi.o 
#	ar -rcs libswt-pi.a libswt-pi.o
libswt.a:libswt.o 
	ar -rcs libswt.a libswt.o
libswt.o: swt.jar 
	$(GCJ) -classpath swt-pi.jar -fjni -o libswt.o -march=i586 -mcpu=i586 -Os -pipe -c  swt.jar 
#libswt-pi.o: swt-pi.jar
#	$(GCJ)  -fjni -o libswt-pi.o -march=i586 -mcpu=i586 -Os -pipe -c swt-pi.jar
libnanoxml-lite.o: nanoxml-lite-2.2.3.jar
	$(GCJ) -o libnanoxml-lite.o -c nanoxml-lite-2.2.3.jar
libnanoxml-lite.a: libnanoxml-lite.o
	ar rcs libnanoxml-lite.a libnanoxml-lite.o
clean:
	rm -f libswt.so libswt.o libswt.a libnanoxml-lite.o libnanoxml-lite.a
