<?php
// iRATE server v0.1
// GPL licensed
// PeerMajor.org


 //database connection : driver:://user:password@host/databasename
$cfg["dsn"]="mysql://root:@localhost/irate";

 //can users register only by logging in with a new username ?
$cfg["allow_registering"]=true;

 //the default correlation algorithm
$cfg["dft_correlation"]="sylvinus1"; //hoping for a better one :)

 //the probability (0-100) of a random track being sent to the client
$cfg["random_frequency"]=2;

 //Location of LibreDB_audio1 file
$cfg["grabber_libredb_audio1_url"]="http://www.libredb.org/pub/audio1.xml.gz";

 //Activate the prepare function (compute correlation in background, needs cron)
$cfg["prepare"]=true;

 //How many users to prepare at each batch
$cfg["prepare_users"]=10;

 //How many tracks to prepare for each user
$cfg["prepare_tracks"]=10;

 //After how long a prepared track is outdated (in days)
$cfg["prepare_expire"]=20;

 //How long after the user's last login tracks stop being prepared (days)
$cfg["prepare_idle"]=6*30;

?>
