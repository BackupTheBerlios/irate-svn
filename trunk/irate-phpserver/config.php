<?php
// iRATE server v0.2
// GPL licensed



/////////
//
// Basic settings
//
////////////////////


 //database connection : driver:://user:password@host/databasename
$cfg["dsn"]="mysql://root:@localhost/irate";

 //the probability (0-100) of a random track being sent to the client
$cfg["random_frequency"]=2;

 //Activate the prepare function (compute correlation in background, needs cron)
$cfg["prepare"]=true;

 //How many users to prepare at each batch
$cfg["prepare_users"]=10;

 //How many tracks to prepare for each user
$cfg["prepare_tracks"]=10;

 //After how long a prepared track is outdated (in days)
$cfg["prepare_expire"]=20;

 //How long after the user's last login tracks stop being prepared (days)
$cfg["prepare_idle"]=2*30;

 //the default correlation algorithm
$cfg["dft_correlation"]="sylvinus1"; //hoping for a better one :)




/////////
//
// Advanced settings
//
////////////////////

// //can users register only by logging in with a new username ?
//$cfg["allow_registering"]=true;


 //Registered grabbers
$cfg["grabbers"][]="libredb_audio1";

 //Location of LibreDB_audio1 file
$cfg["grabber_libredb_audio1_url"]="http://pub1.jamendo.com/libredb/audio1.xml.gz";


?>
