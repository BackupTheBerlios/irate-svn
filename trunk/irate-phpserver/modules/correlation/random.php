<?php



Class IRS_Correlation_random extends IRS_Correlation {





function _get($num,$min_old_weight) {

 $t=$this->irs->db->getCol("
SELECT irate_tracks.id 
FROM irate_tracks 
LEFT JOIN irate_ratings 
ON irate_tracks.id=irate_ratings.trackid AND irate_ratings.userid=".$this->irs->user["id"]." 
WHERE ((irate_ratings.trackid IS NULL) OR (irate_ratings.weight<".$min_old_weight."))
ORDER by rand() 
LIMIT 0,".$num);
 return $t;

}




}





?>
