<?



Class IRS_Grabber_Libredb_Audio1 extends IRS_Grabber {

 var $irs;

 function IRS_Grabber_Libredb_Audio1(&$irs) {
  $this->irs=&$irs;

 }




 function grab() {

 set_time_limit(0);

 //$str=implode("",file("/home/sylvinus/cvsroot/libredb/site/libredb/pub/audio1.xml"));
 $str=implode("",gzfile($this->irs->cfg["grabber_libredb_audio1_url"]));
 
  preg_match_all(
  "/<track>(.*?)<\/track>/is",$str,$matches,PREG_SET_ORDER);

  for ($i=0;$i<count($matches);$i++) {
   
  $arr=$this->makeXMLTree($matches[$i][0]);
  $id=$arr["track"][0]["ldbid"][0]."-0";

  
  $exists=$this->irs->db->getOne("SELECT 1 FROM tracks WHERE id=?",array($this->irs->id2int($id))); 

 if (!$exists) {
   $trackid=$this->irs->addTrack(array(
    "artistname"=>$arr["track"][0]["artistname"][0],
    "id"=>$id,
    "albumname"=>$arr["track"][0]["albumname"][0],
    "duration"=>$arr["track"][0]["duration"][0],
    "pubdate"=>$arr["track"][0]["pubdate"][0],
    "license"=>$arr["track"][0]["license"][0],
    "trackname"=>$arr["track"][0]["trackname"][0],
    "crediturl"=>$arr["track"][0]["crediturl"][0],
   ));

   
   $dists=&$arr["track"][0]["distributions"][0]["distribution"];
   for ($y=0;$y<count($dists);$y++) {

    $did=$this->irs->addDistribution(array(
      "trackid"=>$trackid,
      "crediturl"=>$dists[$y]["crediturl"][0],
      "averagebitrate"=>$dists[$y]["averagebitrate"][0],
      "crediturl"=>$dists[$y]["crediturl"][0],
      "filesize"=>$dists[$y]["filesize"][0],
      "hash_sha1"=>$dists[$y]["hash_sha1"][0],
      "codec"=>$dists[$y]["codec"][0]
      ));

    $sources=&$dists[$y]["sources"][0]["source"];
    for ($z=0;$z<count($sources);$z++) {

     $this->irs->addSource(array(
      "distribid"=>$did,
      "protocol"=>$sources[$z]["protocol"][0],
      "crediturl"=>$sources[$z]["crediturl"][0],
      "link"=>$sources[$z]["link"][0],
      "media"=>1,
     ));
     
    }
    
   }


  }




 }


}




}






?>
