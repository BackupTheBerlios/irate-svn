INCLUDES    = $(all_includes)
METASOURCES = AUTO

# Install this plugin in the KDE modules directory
kde_module_LTLIBRARIES = noatun_kirateradioplugin.la

noatun_kirateradioplugin_la_SOURCES = plugin_kirateradio.cpp \
	plugin_kirateradio_impl.cpp configurationcenter.cpp downloadcenter.cpp exportdialog.cpp \
	infodialog.cpp newaccount.cpp newaccountdialog.cpp osd.cpp safelistviewitem.cpp \
	songlist.cpp songselection.cpp template.cpp trackdatabase.cpp view.cpp \
	viewdeleteddialog.cpp
noatun_kirateradioplugin_la_LDFLAGS = $(all_libraries) -module -avoid-version -no-undefined
noatun_kirateradioplugin_la_LIBADD = $(LIB_KFILE) -lnoatun -lnoatuncontrols \
                                    -lqtmcop -lkmedia2_idl -lsoundserver_idl

noinst_HEADERS = plugin_kirateradio_impl.h configurationcenter.h \
	downloadcenter.h exportdialog.h infodialog.h newaccount.h newaccountdialog.h osd.h \
	safelistviewitem.h songlist.h songselection.h template.h trackdatabase.h view.h \
	viewdeleteddialog.h

# noatun_modules_uidir = $(kde_datadir)/noatun
# noatun_modules_ui_DATA = kirateradio.plugin
          
pluginsdir = $(kde_datadir)/noatun
plugins_DATA = kirateradio.plugin

# kde_services_DATA = kirateradio.desktop

messages: rc.cpp
	$(XGETTEXT) *.cpp *.h -o $(podir)/kirateradio.pot
