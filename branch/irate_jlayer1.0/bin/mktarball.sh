OUT_DIR=irate-linux-i586
LIB_DIR=$OUT_DIR/lib
DEPS=`ldd irate-client | grep libgcj | sed 's/.* \//\//' | sed 's/ .*//'`
#DOC_DIR=irate/client/help
#DOCS=`find $DOC_DIR -name '*.txt'`

rm -fR $OUT_DIR
mkdir -vp $LIB_DIR
#mkdir -vp $OUT_DIR/$DOC_DIR 

cp -v lib/*so* $LIB_DIR
cp -v irate-client $LIB_DIR
for file in $DEPS; do 
	cp -v $file $LIB_DIR
done

#for file in $DOCS; do 
#	cp -v $file $OUT_DIR/$DOC_DIR
#done

echo 'export LD_LIBRARY_PATH=lib/
cp -v ~/irate/trackdatabase.xml ~/irate/trackdatabase.xml.`date +%b-%d`
./lib/irate-client' > $OUT_DIR/irate.sh
chmod +x $OUT_DIR/irate.sh

cp -v COPYING README $OUT_DIR

echo -n Creating  $OUT_DIR.tar.bz2...
tar -cjf $OUT_DIR.tar.bz2 $OUT_DIR
echo done
