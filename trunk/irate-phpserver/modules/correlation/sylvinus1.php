<?php


// sylvinus' first try :)
//
// Ma, it's kind of working, for a quick test (quick, I
// mean 2/3 hours of wondering about LEFT JOINs...)
// It basically selects up to 50 users that have the closest
// preferences to you, and returns the files that they
// liked and that you didn't rate previously.


Class IRS_Correlation_sylvinus1 extends IRS_Correlation {


// two times :
//  1) Get user list
//





function _get($num,$accept) {

 $ret=array();


/*
TESTING
$this->irs->db->query("TRUNCATE TABLE ratings");


$ratings=array(
array(1,1,9),
array(2,1,9),
array(3,1,9),
array(4,1,9),
array(2,2,8),
array(3,2,8),
array(4,2,8),
array(5,2,0),
array(3,3,9),
array(4,3,9),
array(5,3,9),
array(6,3,9),
);


for ($i=0;$i<count($ratings);$i++) {
  
  $id=$this->irs->db->nextID("ratings");
 
 $this->irs->db->query("INSERT INTO ratings(id,trackid,userid,rating) VALUES (?,?,?,?)",array($id,$ratings[$i][0],$ratings[$i][1],$ratings[$i][2]));
}
*/



$maxdiff=2;
$numusers=50;

$users=$this->getUsersLike($maxdiff,$numusers);

if (count($users)<5) {
 $maxdiff++;
 $users=$this->getUsersLike($maxdiff,$numusers);

 if (count($users)<10) {
  $users=$this->getUsersLike($maxdiff,$numusers);
 }
}

$tracks=$this->getTracksLike($users,7,$num);


if (count($tracks)<$num) {
 $tracks=$this->getTracksLike($users,7,$num);
}
if (count($tracks)<$num) {
 $tracks=$this->getTracksLike($users,6,$num);
}

if (count($tracks)<$num) {
 $numusers+=50;
 $maxdiff++;
 $users=$this->getUsersLike($maxdiff,$numusers);
 $tracks=$this->getTracksLike($users,5,$num);
}




//print_r($users);
//print_r($tracks);

 return $tracks;

}



function getTracksLike($users,$minrating,$num) {



$tracks=$this->irs->db->getAll("
SELECT ratings.trackid, avg(ratings.rating) as avgrating, sum(ratings.weight) as c1
FROM ratings LEFT JOIN ratings as ratings2                                
ON ratings.trackid=ratings2.trackid   
AND ratings2.userid=!
WHERE (ratings.userid=".implode(" OR ratings.userid=",$users).") 
AND ratings2.id IS NULL
GROUP BY ratings.trackid
HAVING avgrating>!
ORDER BY c1 DESC , avgrating DESC 
LIMIT 0,!
",array($this->irs->user["id"],$minrating,$num));

$tracks1=array();
for ($i=0;$i<count($tracks);$i++) {
 $tracks1[]=$tracks[$i]["trackid"];
}

return $tracks1;


}



function getUsersLike($maxdiff,$num) {

$users=$this->irs->db->getAll("
SELECT ratings2.userid,sum(ratings.weight) as c1,sum(abs(ratings2.rating-ratings.rating))/count(*) as diff
FROM ratings LEFT JOIN ratings as ratings2
ON ratings.trackid=ratings2.trackid 
WHERE ratings.userid=! AND ratings2.userid<>!
GROUP BY ratings2.userid 
HAVING diff<!
ORDER BY c1 DESC , diff ASC
LIMIT 0,!
",array($this->irs->user["id"],$this->irs->user["id"],$maxdiff,$num)); 

$users1=array();
for ($i=0;$i<count($users);$i++) {
 $users1[]=$users[$i]["userid"];
}

return $users1;

}



}





?>
