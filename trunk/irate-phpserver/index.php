<?php
// iRATE server v0.1
// GPL licensed
// PeerMajor.org



require_once("irate.php");


$i=new irate_server();

$i->parse();
$i->output();
$i->end();



?>
