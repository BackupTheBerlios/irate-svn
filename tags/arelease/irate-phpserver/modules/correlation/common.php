<?php




Class IRS_Correlation {

var $irs;

function IRS_Correlation(&$irs) {

 $this->irs=&$irs;

}

function get($num,$accept="http") {

 $trackids=$this->_get($num,explode(",",$accept));

 return $this->format($this->gettrackdata($trackids));


}


function gettrackdata($trackids) {



$t=$this->irs->db->getAll("SELECT * FROM tracks WHERE id=".implode(" OR id=",$trackids));



for ($i=0;$i<count($t);$i++) {

 $ret[$i]["track"]=$t[$i];

 $d=$this->irs->db->getAll("SELECT * FROM distributions WHERE trackid=!",array($t[$i]["id"]));

 for ($y=0;$y<count($d);$y++) {

  $ret[$i]["distributions"][$y]["distribution"]=$d[$y];

  $s=$this->irs->db->getAll("SELECT * FROM sources WHERE distribid=!",array($d[$y]["id"]));

  $ret[$i]["distributions"][$y]["sources"]=$s;

 }


}

return $ret;

}



function format($data) {

$o="<tracks>";

 //i tracks
for ($i=0;$i<count($data);$i++) {
$o.="<track>";

 unset($data[$i]["adddate"]);

 reset($data[$i]["track"]);
 while(list($k,$v)=each($data[$i]["track"])) {
  $o.="<".$k.">".$v."</".$k.">";
 }
  
  //y distributions
 for ($y=0;$y<count($data[$i]["distributions"]);$y++) {
  $o.="<distribution>";

  $dist=&$data[$i]["distributions"][$y];

  unset($dist["distribution"]["trackid"]);
  unset($dist["distribution"]["id"]);
  unset($dist["distribution"]["adddate"]);

  reset($dist["distribution"]);
  while(list($dk,$dv)=each($dist["distribution"])) {
   $o.="<".$dk.">".$dv."</".$dk.">";
  }

   //p sources
  for ($p=0;$p<count($dist["sources"]);$p++) {
   $o.="<source>";

   unset($dist["sources"][$p]["distribid"]);
   unset($dist["sources"][$p]["id"]);
   unset($dist["sources"][$p]["adddate"]);
   unset($dist["sources"][$p]["media"]);


   reset($dist["sources"][$p]);
   while(list($sk,$sv)=each($dist["sources"][$p])) {
   $o.="<".$sk.">".$sv."</".$sk.">";
   }
   $o.="</source>";
  }

  
  $o.="</distribution>";
 }

 $o.="</track>";
}

$o.="</tracks>";

return $o;

}


}

?>
