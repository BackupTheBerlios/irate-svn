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

?>
