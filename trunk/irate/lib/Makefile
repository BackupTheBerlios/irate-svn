
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

ifeq ($(PLATFORM),linux-gtk2)
	unzip swt-2.1.1-linux-gtk.zip

else

ifeq ($(PLATFORM),linux-libswt-gtk2)
	unzip swt-2.1.1-linux-gtk.zip

else
	echo Unknown PLATFORM $(PLATFORM), please specify on commandline && exit 1 
endif

endif

endif

endif

libswt.o: swt.jar
ifeq ($(PLATFORM),win32)
	echo libswt.o should come from the zip file ':)'	
else
ifeq ($(PLATFORM),linux-gtk2)
	$(GCJ) -fjni -o libswt.o -c  swt-pi.jar swt.jar
else
	$(GCJ) -fjni -o libswt.o -c  swt.jar
endif
endif

libnanoxml-lite.o: nanoxml-lite-2.2.3.jar
	$(GCJ) -o libnanoxml-lite.o -c nanoxml-lite-2.2.3.jar

#libnanoxml-lite.a: libnanoxml-lite.o
#	ar rcs libnanoxml-lite.a libnanoxml-lite.o

clean:
	rm -f libswt.so libswt.o libswt.a libnanoxml-lite.o libnanoxml-lite.a *.o *.a
# Remove the files created during the unzip process.
# gtk2 zip file
	rm -f swt.jar swt-pi.jar swtsrc.zip swt-pisrc.zip about.html cpl-v10.html lgpl-v21.txt libswt-gnome-gtk-2135.so libswt-gtk-2135.so libswt-pi-gtk-2135.so
# motif zip file
	rm -f libswt-gnome-motif-3009.so libswt-gnome-motif-3009.so  libswt-kde-motif-3009.so libswt-motif-3009.so libXm.so libXm.so.2 libXm.so.2.1 swt.jar
