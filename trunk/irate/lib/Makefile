
all: libswt.o libnanoxml-lite.o

#libswt.so:libswt.o
#	gcj -shared -o libswt.so libswt.o
#libswt-pi.a:libswt-pi.o 
#	ar -rcs libswt-pi.a libswt-pi.o
#libswt.a:libswt.o 
#	ar -rcs libswt.a libswt.o
ifeq ($(PLATFORM),linux-motif)
swt.jar: swt-2.1.2-linux-motif.zip
	unzip -o swt-2.1.2-linux-motif.zip
else

ifeq ($(PLATFORM),win32)
swt.jar: swt-2.1-win32.zip
	unzip -o swt-2.1-win32.zip
	touch libswt.o
else

ifeq ($(PLATFORM),linux-gtk2)
swt.jar: swt-2.1.1-linux-gtk.zip
	unzip -o swt-2.1.1-linux-gtk.zip
#swt.jar: swt-3.0M6-linux-gtk.zip
#	unzip -o swt-3.0M6-linux-gtk.zip
else

ifeq ($(PLATFORM),linux-libswt-gtk2)
swt.jar: swt-2.1.1-linux-gtk.zip
	unzip -o swt-2.1.1-linux-gtk.zip

else
	echo Unknown PLATFORM $(PLATFORM), please specify on commandline && exit 1 
endif

endif

endif

endif

swt-2.1.2-linux-motif.zip:
	wget http://download.eclipse.org/downloads/drops/R-2.1.2-200311030802/swt-2.1.2-linux-motif.zip

swt-2.1-win32.zip:
	wget http://download2.eclipse.org/downloads/drops/R-2.1-200303272130/swt-2.1-win32.zip

swt-2.1.1-linux-gtk.zip:
	wget http://download2.eclipse.org/downloads/drops/R-2.1.1-200306271545/swt-2.1.1-linux-gtk.zip

swt-3.0M6-linux-gtk.zip:
	wget http://download.eclipse.org/downloads/drops/S-3.0M6-200312182000/swt-3.0M6-linux-gtk.zip

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

libjd3.o: jd3lib.jar
	$(GCJ) -o libjd3.o -c jd3lib.jar

#libnanoxml-lite.a: libnanoxml-lite.o
#	ar rcs libnanoxml-lite.a libnanoxml-lite.o

clean:
	rm -f libswt.so libswt.o libswt.a libnanoxml-lite.o libnanoxml-lite.a *.o *.a
# Remove the files created during the unzip process.
# gtk2 zip file
	rm -f swt.jar swt-pi.jar swtsrc.zip swt-pisrc.zip about.html cpl-v10.html lgpl-v21.txt libswt-gnome-gtk-2135.so libswt-gtk-2135.so libswt-pi-gtk-2135.so swt-mozilla.jar swt-mozillasrc.zip mpl-v11.txt
# motif zip file
	rm -f libswt-gnome-motif-2133.so libswt-gnome-motif-2133.so  libswt-kde-motif-2133.so libswt-motif-2133.so libXm.so libXm.so.2 libXm.so.2.1 swt.jar
