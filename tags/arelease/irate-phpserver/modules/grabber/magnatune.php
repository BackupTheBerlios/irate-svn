<?



Class IRS_Grabber_Magnatune extends IRS_Grabber {

 var $irs;

 function IRS_Grabber_Magnatune(&$irs) {
  $this->irs=&$irs;

 }




 function grab() {

 set_time_limit(0);


 $str=implode("",file("/home/sylvinus/song_info.xml"));
 
  preg_match_all(
  "/<Track>.*?".

  "<artist>(.*?)<\/artist>.*?".
  "<albumname>(.*?)<\/albumname>.*?".
  "<trackname>(.*?)<\/trackname>.*?".
  "<license>(.*?)<\/license>.*?".
  "<seconds>(.*?)<\/seconds>.*?".
  "<url>(.*?)<\/url>.*?".
  "<home>(.*?)<\/home>.*?".
  "<launchdate>(.*?)<\/launchdate>.*?".


  "<\/Track>/is",$str,$matches,PREG_SET_ORDER);



  for ($i=0;$i<count($matches);$i++) {
   
   $id=$this->irs->addTrack(array(
    "artistname"=>$matches[$i][1],
    "albumname"=>$matches[$i][2],
    "trackname"=>$matches[$i][3],
    "license"=>$matches[$i][4],
    "duration"=>$matches[$i][5],
    "pubdate"=>$matches[$i][8]
   ));


   if ($id) {
   $did=$this->irs->addDistribution($id,array(
     "crediturl"=>$matches[$i][7],
     "averagebitrate"=>128,
     "codec"=>"MPEG/L3"
     ));

    $this->irs->addSource($did,array(
     "protocol"=>"http",
     "link"=>$matches[$i][6],
     "trackid"=>$id,
     "crediturl"=>$matches[$i][7]
    ));
   }
  }




 }







}






?>
