# set the include path for X, qt and KDE
INCLUDES = -I/home/mat/svn/libcpp $(all_includes)

# these are the headers for your project


# let automoc handle all of the meta source files (moc)
METASOURCES = AUTO

messages: rc.cpp
	$(XGETTEXT) *.cpp -o $(podir)/kirateradioxmms.pot

KDE_ICON = kirateradioxmms

#########################################################################
# APPLICATION SECTION
#########################################################################
# this is the program that gets installed.  it's name is used for all
# of the other Makefile.am variables
bin_PROGRAMS = kirateradioxmms

# the application source, library search path, and link libraries
kirateradioxmms_SOURCES = main.cpp configurationcenter.cpp exportdialog.cpp \
	infodialog.cpp newaccount.cpp newaccountdialog.cpp newaccountdialog.h osd.cpp \
	safelistviewitem.cpp songlist.cpp template.cpp view.cpp xmmswrapper.cpp
kirateradioxmms_LDFLAGS = $(KDE_RPATH) $(all_libraries)
kirateradioxmms_LDADD = $(LIB_KIO) -lxmms -lqt_irate $(LIB_KDEUI)

# this is where the desktop file will go 
shelldesktopdir = $(kde_appsdir)/Utilities
shelldesktop_DATA = kirateradioxmms.desktop

# this is where the shell's XML-GUI resource file goes
shellrcdir = $(kde_datadir)/kirateradioxmms
shellrc_DATA = kirateradioxmmsui.rc

noinst_HEADERS = configurationcenter.h exportdialog.h infodialog.h \
	newaccountdialog.h newaccount.h osd.h safelistviewitem.h songlist.h template.h view.h \
	xmmswrapper.h


AM_CXXFLAGS = `${SHELL} glib-config --cflags`
AM_CFLAGS = `${SHELL} glib-config --cflags`
