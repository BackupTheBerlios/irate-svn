<?php
// iRATE server v0.1
// GPL licensed
// PeerMajor.org


Class irate_server {

var $cfg;
var $db;
var $correlation;
var $user;
var $grabber;

var $out_status;
var $out_message;


function irate_server() {

 $this->VARS=array_merge($argv,$_SESSION,$GLOBALS["HTTP_COOKIE_VARS"],$GLOBALS["HTTP_GET_VARS"],$GLOBALS["HTTP_POST_VARS"]);
 
 require_once("config.php");

 $this->cfg=$cfg;

  //we use PEAR.
 require_once("DB.php");

 $this->db= DB::connect($this->cfg["dsn"],true);

 $this->db->setFetchMode(DB_FETCHMODE_ASSOC);

}





function error($code) {

$this->out_status="error";

$this->out_message=$code;


$this->output();

$this->end();


}





function parse() {

 $this->initAuth($this->VARS["u"],$this->VARS["p"],$this->VARS["h"]);

 if ($this->VARS["do"]=="getnew") {
  $this->initCorrelation(); 


  $this->out_status="getnew";
  $this->out_message=$this->correlation->get($this->VARS["n"],$this->VARS["accept"]);

 } elseif ($this->VARS["do"]=="rate") {
  $this->rate($this->VARS["rate"]);

  $this->out_status="rate";
//  $this->out_message="OK";

 } elseif ($this->VARS["do"]=="getratings") {
  $this->out_status="getratings";
  $a=$this->getRatings();
  $this->out_message=$this->formatRatings($a);
 
 
  //admin tasks
 } elseif ($this->VARS["do"]=="admin") {
  
  if ($this->user["user"]=="admin") {
   if ($this->VARS["action"]=="grab") {
    $this->initGrabber($this->VARS["grab"]);
    $this->grabber->grab();
   }

  } else {
   $this->error("WRONG_ADMIN_PASSWORD");
  }
 }



}


function registerUser($username,$password) {


 //todo preg on username/password



 $this->db->query("INSERT INTO users(id,user,pass,dateinscr,datelastlogin,ipinscr) VALUES(?,?,?,now(),now(),?)",array($this->db->nextID("users"),$username,$password,$_SERVER["REMOTE_ADDR"]));


}


function initAuth($u,$p,$h) {

 if (empty($u) OR (empty($p) AND empty($h))) {
  $this->error("MUST_LOGIN");
 } else {
  
  $user=$this->db->getRow("SELECT * FROM users WHERE user=?",array($u));
  if (!empty($user)) {
   if ($user["pass"]==$p OR sha1("irate".$user["pass"])==$h) {
    $this->user=$user;
    $this->db->query("UPDATE users SET datelastlogin=now() WHERE user=?",array($u));
   } else {
    $this->error("WRONG_PASSWORD");
   }
  } elseif (!empty($p)) {
   $this->registerUser($u,$p);
  $user=$this->db->getRow("SELECT * FROM users WHERE user=?",array($u));
   $this->user=$user;
  } else {
   $this->error("REGISTERING_NEEDS_PASSWORD");
  }
 
 }

}


function initCorrelation() {


 require_once("modules/correlation/common.php");

  // hoping for better one :)
 $corr="sylvinus1";

 require_once("modules/correlation/".$corr.".php");

 $classname="IRS_Correlation_".$corr;

 $this->correlation = new $classname(&$this);


}


//XXXX-YYY-ZZ-0 => XXXXYYYZZ0
function id2int($id) {

return intval(str_replace("-","",$id));

}

//reverse one
function int2id($int) {

 return floor($int/1000000)."-".(floor($int/1000)%1000)."-".(floor($int/10)%100)."-".($int%10);

}



/*
 * Rate some tracks
 * $ratings : ID:NOTE,ID:NOTE,ID:NOTE,....

 */
function rate($ratings) {

 $r=explode(",",$ratings);

 for ($i=0;$i<count($r);$i++) {
  $rr=explode(":",$r[$i]);

  $this->_rate($this->id2int($rr[0]),$rr[1]);

 }

}





function _rate($trackid,$note) {

 $row=$this->db->getRow("SELECT * FROM ratings WHERE userid=? AND trackid=?",array($this->user["id"],$trackid));

 if (count($row)>0) {
  $this->db->query("UPDATE ratings SET rating=?,ratingdate=now(),ratingnum=ratingnum+1 WHERE id=?",array($note,$row["id"]));
 } else {
  
  $rid=$this->db->nextId("ratings");
 
  $this->db->query("INSERT INTO ratings(id,trackid,userid,rating,ratingdate,ratingnum) VALUES(?,?,?,?,now(),0)",array($rid,$trackid,$this->user["id"],$note));
 }


}



function getratings() {

 $a=$this->db->getAll("SELECT trackid,rating FROM ratings WHERE userid=?",array($this->user["id"]));

 return $a;

}



function output() {

 //echo $this->out_status."\n".$this->out_message;
 echo $this->out_message;

}



function formatRatings($a) {

 for ($i=0;$i<count($a);$i++) {
  $a[$i]=$this->int2id($a[$i]["trackid"]).":".$a[$i]["rating"];
 }
 $a=implode(",",$a);

 return $a;

}



function end() {
 
 die();

}


function addTrack($data) {

  //from libredb
 if (isset($data["id"])) {
  $id=$this->id2int($data["id"]);

  
  //server-specific file
 } else {

  $id=$this->db->nextID("tracks");
  $id=($id*1000)+1; // 001 for audio
  $id=($id*100)+($id%97); // modulo 97
  $id=($id*10)+1; //-1 for server-specific

 }
  
  $q=$this->db->query("INSERT INTO tracks(id,artistname,duration,pubdate,albumname,license,trackname,adddate,crediturl) VALUES (?,?,?,?,?,?,?,now(),?)",array($id,$data["artistname"],$data["duration"],$data["pubdate"],$data["albumname"],$data["license"],$data["trackname"],$data["crediturl"]));


  return $id;
 

}


 function addDistribution($data) {
  
  $id=$this->db->nextID("distributions");

  $this->db->query("INSERT INTO distributions(id,trackid,codec,averagebitrate,crediturl,adddate,filesize,hash_sha1) VALUES(?,?,?,?,?,now(),?,?)",array($id,$data["trackid"],$data["codec"],$data["averagebitrate"],$data["crediturl"],$data["filesize"],$data["hash_sha1"]));
 
  return $id;
 }



 function addSource($data) {
  
  $i=$this->db->nextId("sources");
 
 
  $this->db->query("INSERT INTO sources(id,distribid,media,protocol,link,crediturl,adddate) VALUES(?,?,?,?,?,?,now())",array($i,$data["distribid"],$data["media"],$data["protocol"],$data["link"],$data["crediturl"]));

 }


 function initGrabber($grabber) {
  
  $name="IRS_Grabber_".$grabber;

  require_once("modules/grabber/common.php");
  require_once("modules/grabber/".$grabber.".php");

  $this->grabber=new $name(&$this);


 }


}

$i=new irate_server();

$i->parse();
$i->output();
$i->end();



?>
