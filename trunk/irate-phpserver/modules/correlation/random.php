<?php



Class IRS_Correlation_random extends IRS_Correlation {





function _get($num,$accept) {

 $t=$this->irs->db->getCol("
SELECT tracks.id 
FROM tracks 
LEFT JOIN ratings 
ON tracks.id=ratings.trackid AND ratings.userid=".$this->irs->user["id"]." 
WHERE ((ratings.trackid IS NULL) OR (ratings.weight<100))
ORDER by rand() 
LIMIT 0,".$num);

 return $t;

}




}





?>
