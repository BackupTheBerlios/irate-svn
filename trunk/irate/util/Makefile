GCJ=gcj

check_locale_sync: localechecker
	./localechecker `find .. -type d`

localechecker: LocaleChecker.java
	$(GCJ) LocaleChecker.java --main=util.LocaleChecker -o $@	

%.o: %.java
	$(GCJ) -c $< -o $@

clean:
	rm -f localechecker
