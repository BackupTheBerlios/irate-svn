<?php
// iRATE server v0.2
// GPL licensed



Class irate_server {

var $cfg;
var $db;
var $correlation;
var $correlation_random;
var $user;
var $grabber;
var $options;
var $xmlrpc;

var $out_status;
var $out_message;

var $init;

function irate_server($options="") {

 if (is_array($options)) {
  $this->options=$options;
 } else {
  $this->options=array();
 }

 $this->VARS=array_merge($argv,$_SESSION,$GLOBALS["HTTP_COOKIE_VARS"],$GLOBALS["HTTP_GET_VARS"],$GLOBALS["HTTP_POST_VARS"]);
 


 require_once($this->options["root"]."config.php");

 $this->cfg=$cfg;

  //we use PEAR.
 require_once("DB.php");
 require_once("XML/RPC/Server.php");
 require_once("xmlrpcpln.php");

 $this->db= DB::connect($this->cfg["dsn"],true);


 if (DB::isError($this->db)) {
  $this->error(1,$this->db->getMessage());
 }
			 

 $this->db->setFetchMode(DB_FETCHMODE_ASSOC);

 $DispMap=array(
  "irate.rate"=>array("function"=>"IRS_xmlrpc_rate"),
  "irate.unrate"=>array("function"=>"IRS_xmlrpc_unrate"),
  "irate.getRatings"=>array("function"=>"IRS_xmlrpc_getRatings"),
  "irate.getNew"=>array("function"=>"IRS_xmlrpc_getNew"),
  "irate.getInfo"=>array("function"=>"IRS_xmlrpc_getInfo"),
  "irate.registerUser"=>array("function"=>"IRS_xmlrpc_registerUser")
 );



 $this->xmlrpc=new XML_RPC_Server($DispMap, 0);

}


// for use in irate.extended.php
function extendedService() {

}


function service() {

 $this->extendedService();

 if (empty($this->VARS["m"])) {
  $this->xmlrpc->service();



 } else { //admin actions (don't use XMLRPC so that we can call them from a crontable easily)
  $this->requireAuth();
  $this->requireAdmin();

  if ($this->VARS["m"]=="grab") {
   
   for ($i=0;$i<count($this->cfg["grabbers"]);$i++) {
    $this->initGrabber($this->cfg["grabbers"][$i]);
    $this->grabber->grab();
   }

  }

 }


}



function error($code,$desc="") {

 global $XML_RPC_erruser; // import user errcode value


 //definition of error strings
 $str=array(
  1=>$desc,
  2=>"MUST_LOGIN",
  3=>"UNKNOWN_USER",
  4=>"USER_NOT_ADMIN",
  5=>"WRONG_PASSWORD", 
  6=>"USER_ALREADY_EXISTS",
  7=>"REGISTERING_NOT_ALLOWED",
  10=>"TRACK_NOT_FOUND",
  11=>"RATING_MISSING",
  12=>"BAD_INPUT"

 );









 $error= new XML_RPC_Response(0, $XML_RPC_erruser+$code,$str[$code]);


  //quite dirty todo
 $payload = "<?xml version=\"1.0\"?>\n" .
            $error->serialize();

        header('Content-Length: ' . strlen($payload));
        header('Content-Type: text/xml');
        print $payload; 

 die();
}





function getNew($number,$include_random=true,$min_old_weight=1) {

 if (!$this->init["correlation"]) {
  $this->initCorrelation();
 }

 $ids=array();

 /*
 * STEP 1 : we get the prepared results.
 */

 if ($this->cfg["prepare"]) {
  $ids=$this->db->getCol("SELECT trackid FROM irate_prepare WHERE userid=".$this->user["id"]." LIMIT 0,".$number);
 }
 
 $number-=count($ids);

 /*
 * STEP 2 : we see how many random ones we can send to the user
 */

 
 $num_random=0;

 if ($include_random) {
  for ($i=0;$i<count($number);$i++) {
   if (rand(0,99)<$this->cfg["random_frequency"]) {
    $num_random++;
   }
  }
 }

 /*
 * STEP 3 : we get the correlated results if needed
 */ 
 
 $old=$this->db->getOne("SELECT count(*) FROM irate_ratings WHERE userid=".$this->user["id"]);

 $c=array();
 
 if ($number-$num_random>0 AND $old>0) {
 
  $c=$this->correlation->get($number-$num_random,$min_old_weight);
  
  for ($i=0;$i<count($c);$i++) {
   $ids[]=$c[$i];
  }
  
 }
 /*
 * STEP 4 : we get the random results (wanted, or left)
 */

 $random=$number-count($c);

 if ($random>0 and $include_random) {
  $r=$this->correlation_random->get($random,$min_old_weight);
 
  //put the random ones at the end (todo : put them random ?)

  for ($i=0;$i<count($r);$i++) {
   $ids[]=$r[$i];
  }

 }
 return $ids;

}



function requireAdmin() {
 
 if ($this->user["user"]=!"admin") {
  $this->error(4);
 }

}


function requireAuth() {


 $u=$_SERVER["PHP_AUTH_USER"];
 $h=$_SERVER["PHP_AUTH_PW"];

 if (empty($u) OR empty($h)) {
  $this->error(2);
 }
 
 $user=$this->db->getRow("SELECT * FROM irate_users WHERE user=?",array($u));

 if (empty($user)) {
  $this->error(3);
 }

 if (sha1("irate".sha1($user["pass"]))==$h) {
  $this->user=$user;
  $this->db->query("UPDATE irate_users SET datelastlogin=now() WHERE user=?",array($u));
 } else {
  $this->error(4);
 }

}





function initCorrelation($corr="") {

 $this->init["correlation"]=true;

 require_once($this->options["root"]."modules/correlation/common.php");

 if (empty($corr)) {
  $corr=$this->cfg["dft_correlation"];
 }

 require_once($this->options["root"]."modules/correlation/".$corr.".php");
 $classname="IRS_Correlation_".$corr;
 $this->correlation = new $classname(&$this);

 require_once($this->options["root"]."modules/correlation/random.php");
 $classname="IRS_Correlation_random";
 $this->correlation_random = new $classname(&$this);

}


//XXXX-YYY-ZZ-0 => XXXXYYYZZ0
function id2int($id) {

return str_replace("-","",$id) + 0;

}

//reverse one
function int2id($int) {

 $int+=0;

 return floor(($int)/1000000)."-".(floor(($int)/1000)%1000)."-".(floor(($int)/10)%100)."-".(($int)%10);

}




function findTrackID($params) {

 

 if (empty($params["id"])) {

  if (!empty($params["hash_sha1"])) {
   $params["id"]=$this->db->getOne("SELECT trackid FROM irate_distributions WHERE hash_sha1=!",array($params["hash_sha1"]));
  }
  
//todo link+protocol

 }

 if (empty($params["id"])) {
  $this->error(10);
 }

 return $params;

}




function unrateOne($params) {

 $params=$this->findTrackID($params);

 $this->db->query("DELETE FROM irate_ratings WHERE userid=? AND trackid=?",array($this->user["id"],$params["id"]));

}



function rateOne($params) {

 
 //step 1 : get irateID of the track.
  $params=$this->findTrackID($params);


 if (empty($params["rating"])) {
  $this->error(11);
 }
 if (empty($params["weight"])) {
  $params["weight"]=1;
 }


 //step 2 : has the user already rated the track ?
 $row=$this->db->getRow("SELECT * FROM irate_ratings WHERE userid=? AND trackid=?",array($this->user["id"],$params["id"]));
 
 if (count($row)>0) {

   //we update the rating only if the weight is higher than before.
   //todo : add, avg, and reduce to weight=1 ?

  if ($params["weight"]>=$row["weight"]) {
    $this->db->query("UPDATE irate_ratings SET weight=?,rating=?,ratingdate=now(),ratingnum=ratingnum+1 WHERE id=?",array($params["weight"],$params["rating"],$row["id"]));
  }
  
 } else  {
  

   //new rating
  $rid=$this->db->nextId("irate_ratings");
 
  $a=$this->db->query("INSERT INTO irate_ratings(id,trackid,userid,rating,ratingdate,ratingnum,weight) VALUES(?,?,?,?,now(),0,?)",array($rid,$params["id"],$this->user["id"],$params["rating"],$params["weight"]));
 }

 //step 3 : delete track from suggestion cache.
$this->db->query("DELETE FROM irate_prepare WHERE userid=! AND trackid=!",array($this->user["id"],$params["id"]));


return true;

}



function getratings() {

 $a=$this->db->getAll("SELECT trackid as id,rating,weight FROM irate_ratings WHERE userid=?",array($this->user["id"]));

 return $a;

}




function addTrack($data) {

  //from libredb
 if (isset($data["id"])) {
  $id=$this->id2int($data["id"]);

  
  //server-specific file
 } else {

  $id=$this->db->nextID("irate_tracks");
  $id=($id*1000)+1; // 001 for audio
  $id=($id*100)+($id%97); // modulo 97
  $id=($id*10)+1; //-1 for server-specific

 }

 if ($this->db->query("SELECT 1 FROM irate_tracks WHERE id=!",array($id))) {
  $this->updateTrack($id,$data);
 } else {

  $q=$this->db->query("INSERT INTO irate_tracks(id,artistname,duration,pubdate,albumname,license,trackname,adddate,crediturl) VALUES (?,?,?,?,?,?,?,now(),?)",array($id,$data["artistname"],$data["duration"],$data["pubdate"],$data["albumname"],$data["license"],$data["trackname"],$data["crediturl"]));
}

  return $id;
 

}

function updateTrack($id,$data) {

 $this->db->query("UPDATE irate_tracks SET artistname=?,duration=?,pubdate=?,albumname=?,license=?,trackname=?,crediturl=? WHERE id=!",array(
 $data["artistname"],
 $data["duration"],
 $data["pubdate"],
 $data["albumname"],
 $data["license"],
 $data["trackname"],
 $data["crediturl"],
 $id));

}


 function addDistribution($data) {
  
  $id=$this->db->nextID("irate_distributions");

  $this->db->query("INSERT INTO irate_distributions(id,trackid,codec,crediturl,adddate,filesize,hash_sha1) VALUES(?,?,?,?,now(),?,?)",array($id,$data["trackid"],$data["codec"],$data["crediturl"],$data["filesize"],$data["hash_sha1"]));
 
  return $id;
 }



 function addSource($data) {
  
  $i=$this->db->nextId("irate_sources");
 
 
  $this->db->query("INSERT INTO irate_sources(id,distribid,media,protocol,link,crediturl,adddate) VALUES(?,?,?,?,?,?,now())",array($i,$data["distribid"],$data["media"],$data["protocol"],$data["link"],$data["crediturl"]));

 }


 function initGrabber($grabber) {
 

  $name="IRS_Grabber_".$grabber;

  require_once($this->options["root"]."modules/grabber/common.php");
  require_once($this->options["root"]."modules/grabber/".$grabber.".php");

  $this->grabber=new $name(&$this);


 }



 //todo debug
 function prepareCorrelation() {

 $oldid=$this->user["id"];

  if (rand(0,100)<3) {
   $this->db->query("DELETE FROM irate_prepare WHERE now() - INTERVAL ! day > date",array($this->cfg["prepare_expire"]));
  }


  $this->initCorrelation();

  $users=$this->db->getAll("SELECT users.id as id
  FROM irate_users as users
  LEFT  JOIN irate_prepare ON users.id = irate_prepare.userid
  WHERE now() - INTERVAL ! day < users.datelastlogin
  GROUP BY users.id
  ORDER BY users.datelastprepare ASC
  LIMIT 0,".$this->cfg["prepare_users"],array($this->cfg["prepare_idle"]));
 

  for ($i=0;$i<count($users);$i++) {
   $this->user["id"]=$users[$i]["id"];
    $this->db->query("DELETE FROM irate_prepare WHERE userid=!",array($users[$i]["id"]));
    $this->db->query("UPDATE irate_users SET datelastprepare=now() WHERE id=!",array($users[$i]["id"]));

   $tracks=$this->getNew($this->cfg["prepare_tracks"],false);

   for ($y=0;$y<count($tracks);$y++) {
    $this->db->query("INSERT INTO irate_prepare(trackid,userid,date) VALUES (!,!,now())",array($tracks[$y],$users[$i]["id"]));
   }
  }
  


 $this->user["id"]=$oldid;

}






function getTrackXMLRPC($params) {

 $params=$this->findTrackID($params);

 $a=$this->getTrackArray($params["id"]);

 return XML_RPC_PLN::php2xmlrpc($a);


}

function getTrackXMLRPCbyArray($arr) {

 $a=array();
 for ($i=0;$i<count($arr);$i++) {
  $a[$i]=$this->getTrackXMLRPC($arr[$i]);
 }

 return new XML_RPC_Value($a,"array");

}

function getTrackXMLRPCbyIDs($ids) {

 $a=array();
 for ($i=0;$i<count($ids);$i++) {
  $a[$i]=$this->getTrackXMLRPC(array("id"=>$ids[$i]));
 }

 return new XML_RPC_Value($a,"array");

}




function getTrackArray($trackid) {
 
 $trackrow=$this->db->getRow("SELECT artistname,trackname,license,albumname,pubdate,id,duration,crediturl FROM irate_tracks WHERE id=!",array($trackid));
 
 $distribs=$this->db->getALL("SELECT codec,crediturl,filesize,hash_sha1,id FROM irate_distributions WHERE trackid=!",array($trackid));


 for ($i=0;$i<count($distribs);$i++) {
  $sources=$this->db->getAll("SELECT protocol,link,crediturl FROM irate_sources WHERE distribid=!",array($distribs[$i]["id"]));
  
  unset($distribs[$i]["id"]);
  
  $distribs[$i]["Sources"]=$sources;

 }

 $trackrow["Distributions"]=$distribs; 
 
 return $trackrow;
 
}


}


























//////////
//
// XMLRPC functions
//
///////////////////


function IRS_xmlrpc_registerUser($params) {
 
 global $IRS;

if ($cfg["allow_registering"]) {

 $p=$params->getParam(0);
 $username=$p->structmem("username");
 $password=$p->structmem("password");

 //todo preg on username/password


if ($IRS->db->getOne("SELECT 1 FROM irate_users WHERE user=?",array($username->scalarval()))) {
 $IRS->error(6);
}


$IRS->db->query("INSERT INTO irate_users(id,user,pass,dateinscr,datelastlogin,ipinscr) VALUES(?,?,?,now(),now(),?)",array($IRS->db->nextID("users"),$username->scalarval(),$password->scalarval(),$_SERVER["REMOTE_ADDR"]));

return new XML_RPC_Response(new XML_RPC_Value("OK", "string"));

} else {
$IRS->error(7);
}


}







function IRS_xmlrpc_unrate($params) {

 global $IRS;
 
 $IRS->requireAuth();
 
 $p=$params->getParam(0);
 
 $arr=XML_RPC_PLN::xmlrpc2php($p);
  
  //for each rating
 for ($i=0;$i<count($arr);$i++) {
  
  $IRS->unrateOne($arr[$i]);
 
 }
 
 return new XML_RPC_Response(new XML_RPC_Value("OK", "string"));

}



function IRS_xmlrpc_rate($params) {

 global $IRS;

 $IRS->requireAuth();
 
 $p=$params->getParam(0);

 $arr=XML_RPC_PLN::xmlrpc2php($p);

  //for each rating
 for ($i=0;$i<count($arr);$i++) {

  $IRS->rateOne($arr[$i]);

 }

 return new XML_RPC_Response(new XML_RPC_Value("OK", "string"));

}



function IRS_xmlrpc_getInfo($params) {
 global $IRS;

 $IRS->requireAuth();

 $p=$params->getParam(0);

 $r1=XML_RPC_PLN::xmlrpc2php($p);

 return new XML_RPC_Response($IRS->getTrackXMLRPCbyArray($r1));
 

}





function IRS_xmlrpc_getRatings($params) {
 global $IRS;

 $IRS->requireAuth();


 $r=$IRS->getratings();

 return new XML_RPC_Response(XML_RPC_PLN::php2xmlrpc($r));

}









function IRS_xmlrpc_getNew($params) {

 global $IRS;

 $IRS->requireAuth();

 $p=$params->getParam(0);

 $num=$p->structmem("n");

 if (empty($num) OR !preg_match("/^[0-9]+$/",$num->scalarval())) {
  $IRS->error(12);
 }
 
 $ids=$IRS->getNew($num->scalarval());
 
 return new XML_RPC_Response($IRS->getTrackXMLRPCbyIDS($ids));

}





?>
