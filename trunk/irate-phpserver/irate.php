<?php
// iRATE server v0.1
// GPL licensed
// PeerMajor.org


Class irate_server {

var $cfg;
var $db;
var $correlation;
var $correlation_random;
var $user;
var $grabber;
var $options;

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
  $t=$this->getNew($this->VARS["n"]);
  
  $this->out_message=$this->correlation->format($this->correlation->gettrackdata($t));

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

     //prepare correlation
   } elseif ($this->VARS["action"]=="prepare") {
    if ($this->cfg["prepare"]) {
     $this->prepareCorrelation();
    }
   }

  } else {
   $this->error("WRONG_ADMIN_USER");
  }



  //deprecated functions (for old irate)
 } elseif ($this->VARS["do"]=="OLD_url2id") {
  $this->out_status="http2id";
  $this->out_message=$this->OLD_url2id($this->VARS["url"]);

 }



}



function getNew($number,$include_random=true,$min_old_weight=100) {

 if (!$this->init["correlation"]) {
  $this->initCorrelation();
 }

 $ids=array();

 /*
 * STEP 1 : we get the prepared results.
 */

 if ($this->cfg["prepare"]) {
  $ids=$this->db->getCol("SELECT trackid FROM prepare WHERE userid=".$this->user["id"]." LIMIT 0,".$number);
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
 * STEP 3 : we get the correlated results
 */ 
 
 $old=$this->db->getOne("SELECT count(*) FROM ratings WHERE userid=".$this->user["id"]);

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
   if ($this->cfg["allow_registering"]) {
    $this->registerUser($u,$p);
   } else {
    $this->error("REGISTERING_NOT_ALLOWED");
   }
   $user=$this->db->getRow("SELECT * FROM users WHERE user=?",array($u));
   $this->user=$user;
  } else {
   $this->error("REGISTERING_NEEDS_PASSWORD");
  }
 
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

return intval(str_replace("-","",$id));

}

//reverse one
function int2id($int) {

 return floor(intval($int)/1000000)."-".(floor(intval($int)/1000)%1000)."-".(floor(intval($int)/10)%100)."-".(intval($int)%10);

}



/*
 * Rate some tracks
 * $ratings : ID:NOTE,ID:NOTE,ID:NOTE,....

 */
function rate($ratings) {

 $r=explode(",",$ratings);

 for ($i=0;$i<count($r);$i++) {
  $rr=explode(":",$r[$i]);

  $this->rateOne($this->id2int($rr[0]),$rr[1]);
  

 }

}





function rateOne($trackid,$note,$weight=100) {

 $row=$this->db->getRow("SELECT * FROM ratings WHERE userid=? AND trackid=?",array($this->user["id"],$trackid));
 if (count($row)>0) {
  if ($weight>=$row["weight"]) {
   if (empty($note)) { // set unrated.
    $this->db->query("DELETE FROM ratings WHERE id=!",array($row["id"]));
   } else { //update the rating.
    $this->db->query("UPDATE ratings SET weight=?,rating=?,ratingdate=now(),ratingnum=ratingnum+1 WHERE id=?",array($weight,$note,$row["id"]));
   }
  }
 } elseif (!empty($note)) {
  
  $rid=$this->db->nextId("ratings");
 
  $a=$this->db->query("INSERT INTO ratings(id,trackid,userid,rating,ratingdate,ratingnum,weight) VALUES(?,?,?,?,now(),0,?)",array($rid,$trackid,$this->user["id"],$note,$weight));
 }

  $this->db->query("DELETE FROM prepare WHERE userid=! AND trackid=!",array($this->user["id"],$trackid));


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

  require_once($this->options["root"]."modules/grabber/common.php");
  require_once($this->options["root"]."modules/grabber/".$grabber.".php");

  $this->grabber=new $name(&$this);


 }



 function OLD_url2id($http) {
  
  $id=$this->getOne("SELECT distributions.trackid FROM distributions,sources WHERE sources.distribid=distributions.id AND sources.link=?",array($http));

  return $this->int2id($id);
  
 }






 function prepareCorrelation() {

 $oldid=$this->user["id"];

  if (rand(0,100)<3) {
   $this->db->query("DELETE FROM prepare WHERE now() - INTERVAL ! day > date",array($this->cfg["prepare_expire"]));
  }


  $this->initCorrelation();

  $users=$this->db->getAll("SELECT users.id as id
  FROM users
  LEFT  JOIN prepare ON users.id = prepare.userid
  WHERE now() - INTERVAL ! day < users.datelastlogin
  GROUP BY users.id
  ORDER BY users.datelastprepare ASC
  LIMIT 0,".$this->cfg["prepare_users"],array($this->cfg["prepare_idle"]));
 

  for ($i=0;$i<count($users);$i++) {
   $this->user["id"]=$users[$i]["id"];
    $this->db->query("DELETE FROM prepare WHERE userid=!",array($users[$i]["id"]));
    $this->db->query("UPDATE users SET datelastprepare=now() WHERE id=!",array($users[$i]["id"]));

   $tracks=$this->getNew($this->cfg["prepare_tracks"],false);

   for ($y=0;$y<count($tracks);$y++) {
    $this->db->query("INSERT INTO prepare(trackid,userid,date) VALUES (!,!,now())",array($tracks[$y],$users[$i]["id"]));
   }
  }
  


 $this->user["id"]=$oldid;

}


}


?>
