<?php
// iRATE server v0.2
// GPL licensed



require_once("irate.php");


$GLOBALS["IRS"]=new irate_server();

$GLOBALS["IRS"]->service();



?>
