CXXFLAGS = -pipe -Wall -W -g -D_IR_DEBUG
CXX      = g++
INCPATH  = -I.. -I.
MOC      = $(QTDIR)/bin/moc
LINKFLAG= -lgcj -lirate --shared
QT_LINKFLAG=
GCJ=gcj
GCJFLAGS=-g -pipe -O2
GCJLINKFLAG=
ifdef USE_WCHAR
CXXFLAGS+= -D_CPPIR_USE_WCHAR_T
else
CXXFLAGS+= -D_CPPIR_USE_CHAR
endif
#library defs
LIBCPP_IRATE_O=c_irate.o baseiratetrack.o util_irate.o stdiratetrack.o
LIBC_IRATE_O=c_irate.o util_irate.o
LIBQT_IRATE_O=baseiratetrack.o util_irate.o qt_irate.o moc_qt_irate.o c_irate.o
LIBC_IRATE_SO=libc_irate.so
LIBCPP_IRATE_SO=libcpp_irate.so
LIBQT_IRATE_SO=libqt_irate.so
LIB_IRATE_SO=libirate.so
LIB_IRATE_A=libirate.a
LIB_IRATE_H=irate_h
LIB_IRATE_DEPS = ../irate-common.o ../irate-download.o irate-lib.o libiratenative.o
LIB_IRATE_LIBS = ../lib/libnanoxml-lite.o ../lib/libjd3.o
#QT
CXXQTFLAGS=
QTINCPATH=-I$(QTDIR)/mkspecs/linux-g++ -I$(QTDIR)/include


#Starting with java stuff
export CLASSPATH=$(PWD):$(PWD)/..:$(PWD)/../lib/nanoxml-lite-2.2.3.jar:$(PWD)/../lib/jd3lib.jar

JPATH=.:..:../lib/nanoxml-lite-2.2.3.jar:../lib/jd3lib.jar
JAVAC=javac -classpath $(JPATH)

all:$(LIB_IRATE_SO) $(LIB_IRATE_A) $(LIBCPP_IRATE_SO) $(LIBC_IRATE_SO)
qtlib:$(LIB_IRATE_SO) $(LIBQT_IRATE_SO)
$(LIB_IRATE_SO): $(LIB_IRATE_H) $(LIB_IRATE_LIBS) $(LIB_IRATE_DEPS) 
	$(GCJ) --shared $(GCJFLAGS) $(LIB_IRATE_DEPS) $(LIB_IRATE_LIBS) -o $(LIB_IRATE_SO) $(GCJLINKFLAG)

$(LIB_IRATE_A): $(LIB_IRATE_LIBS) $(LIB_IRATE_DEPS)
	ar rc $(LIB_IRATE_A) $(LIB_IRATE_LIBS) $(LIB_IRATE_DEPS)
	ranlib $(LIB_IRATE_A)
	
$(LIBC_IRATE_SO):$(LIBC_IRATE_O)
	$(CXX) $(CXXFLAGS) $(LIBC_IRATE_O) -o $(LIBC_IRATE_SO) $(LINKFLAG)
$(LIBCPP_IRATE_SO):$(LIBCPP_IRATE_O)
	$(CXX) $(CXXFLAGS) $(LIBCPP_IRATE_O) -o $(LIBCPP_IRATE_SO) $(LINKFLAG)
$(LIBQT_IRATE_SO):$(LIBQT_IRATE_O)
	$(CXX) $(CXXFLAGS) $(CXXQTFLAGS) $(LIBQT_IRATE_O) -o $(LIBQT_IRATE_SO) $(LINKFLAG) $(QT_LINKFLAG)


.cpp.o:
	$(CXX) -c $(CXXFLAGS) $(CXXQTFLAGS) $(INCPATH) $(QTINCPATH) -o $@ $<
#$(CXX) --shared $(CXXFLAGS) $(CLIENT_LIBCPP) -o $(CLIENT_LIBCPP_O)
irate-lib.o: jiratelib/LibIRateNative.java jiratelib/PlayListManager.java
	$(GCJ) -c jiratelib/*.java -o irate-lib.o
irate_h: jiratelib/LibIRateNative.java
	$(JAVAC) ../irate/common/Track.java
	$(JAVAC) ../irate/common/Date.java
	$(JAVAC) jiratelib/LibIRateNative.java
	mkdir -p irate/common
	gcjh -d ./ irate.common.Track
	gcjh -d ./ irate.common.Date
	gcjh -d ./ jiratelib.LibIRateNative

../irate-common.o:
	$(MAKE) -C .. irate-common.o
../irate-download.o:
	$(MAKE) -C .. irate-download.o
../lib/libnanoxml-lite.o:
	$(MAKE) -C ../lib libnanoxml-lite.o
../lib/libjd3.o:
	$(MAKE) -C ../lib libjd3.o

c_irate.o: c_irate.cpp c_irate.h
util_irate.o: util_irate.cpp util_irate.h
libiratenative.o: libiratenative.cpp jiratelib/LibIRateNative.h
baseiratetrack.o:baseiratetrack.cpp iratetrack.h
stdiratetrack.o:stdiratetrack.cpp iratetrack.h stdiratetrack.h
qt_irate.o:qt_irate.cpp qt_irate.h
#qtrack.o: qtrack.cpp qtrack.h iratetrack.h
moc_qt_irate.o: moc_qt_irate.cpp qt_irate.h
moc_qt_irate.cpp: $(MOC) qt_irate.h
	$(MOC) qt_irate.h -o moc_qt_irate.cpp
sample: example/simple_ctest.cpp example/simple_cpptest.cpp
	g++ -g -pipe -O2 -I. example/simple_ctest.cpp -o example/simple_ctest -lc_irate
	g++ -g -pipe -O2 -I. example/simple_cpptest.cpp -o example/simple_cpptest -lcpp_irate -lstdc++
clean:
	rm -f *.o
	rm -f moc_qt_irate.cpp
	rm -f jiratelib/*.class
	rm -f $(LIB_IRATE_SO) $(LIB_IRATE_A) $(LIBCPP_IRATE_SO) $(LIBC_IRATE_SO) $(LIBQT_IRATE_SO)
install:
	cp -p $(LIB_IRATE_SO) /usr/lib/
	cp -p $(LIB_IRATE_A) /usr/lib/
	cp -p $(LIBCPP_IRATE_SO) /usr/lib/
	cp -p $(LIBC_IRATE_SO) /usr/lib/
	cp -p $(LIBQT_IRATE_SO) /usr/lib/
