GCJ=gcj -g -pipe -march=i586 -mcpu=i586 -O2

all: libswt.o libnanoxml-lite.o 

#libswt.so:libswt.o
#	gcj -shared -o libswt.so libswt.o
#libswt-pi.a:libswt-pi.o 
#	ar -rcs libswt-pi.a libswt-pi.o
#libswt.a:libswt.o 
#	ar -rcs libswt.a libswt.o
swt.jar:
ifeq ($(PLATFORM),linux-motif)
	unzip swt-2.1-motif-linux.zip		
else

ifeq ($(PLATFORM),win32)
	unzip swt-2.1-win32.zip
	touch libswt.o
else
	echo Unknown PLATFORM $(PLATFORM), please specify on commandline && exit 1 
endif

endif

swt-pi.jar:
ifeq ($(PLATFORM),linux-gtk2)
	unzip swt-2.1.1-linux-gtk.zip
else
	echo Unknown PLATFORM $(PLATFORM), please specify on commandline && exit 1 
endif

libswt.o: swt.jar
ifeq ($(PLATFORM),win32)
	echo libswt.o should come from the zip file ':)'	
else
	$(GCJ) -classpath swt-pi.jar -fjni -o libswt.o -c  swt.jar 
endif

libswt-pi.o: swt-pi.jar
	$(GCJ) -classpath swt.jar -o libswt-pi.o -c  swt-pi.jar

libnanoxml-lite.o: nanoxml-lite-2.2.3.jar
	$(GCJ) -o libnanoxml-lite.o -c nanoxml-lite-2.2.3.jar
#libnanoxml-lite.a: libnanoxml-lite.o
#	ar rcs libnanoxml-lite.a libnanoxml-lite.o

clean:
	rm -f libswt.so libswt.o libswt.a libnanoxml-lite.o libnanoxml-lite.a *.o *.a
