<?php
// iRATE server v0.2
// GPL licensed



 //use irate.extended.php for server-specific stuff.
 
if (!is_file("irate.extended.php")) {


 require_once("irate.php");

 $GLOBALS["IRS"]=new irate_server();

 $GLOBALS["IRS"]->service();


} else {

 require_once("irate.php");
 require_once("irate.extended.php");

 $GLOBALS["IRS"]=new irate_extended_server();

 $GLOBALS["IRS"]->service();

}




?>
