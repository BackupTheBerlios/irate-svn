<?php


// sylvinus' first try :)
//
// Ma, it's kind of working, for a quick test (quick, I
// mean 2/3 hours of wondering about LEFT JOINs...)
// It basically selects up to 50 users that have the closest
// preferences to you, and returns the files that they
// liked and that you didn't rate previously.


Class IRS_Correlation_sylvinus1 extends IRS_Correlation {




function _get($num,$min_old_weight) {

 $this->min_old_weight=$min_old_weight;

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





$numusers=50;

$users=$this->getUsersLike($numusers);


$tracks=$this->getTracksLike($users,7,$num);


if (count($tracks)<$num) {
 $tracks=$this->getTracksLike($users,6,$num);
}

if (count($tracks)<$num) {
 $numusers+=50;
 $maxdiff++;
 $users=$this->getUsersLike($numusers);
 $tracks=$this->getTracksLike($users,5,$num);
}



//print_r($users);
//print_r($tracks);

 return $tracks;

}





function getTracksLike($users,$minrating,$num) {

 if (count($users)==0) {
  return array();
 }


//todo pondérer avgrating par le weight !

$tracks=$this->irs->db->getAll("
SELECT 
 ratings.trackid,
 avg(ratings.rating) as avgrating,
 avg(ratings.rating) * ( sum(ratings.weight) * sum(ratings.weight) ) / ( (sum(ratings.weight)+1) * (sum(ratings.weight)+1) ) as result
 
FROM irate_ratings as ratings LEFT JOIN irate_ratings as ratings2                                
ON ratings.trackid=ratings2.trackid   
AND ratings2.userid=!
WHERE (ratings.userid=".implode(" OR ratings.userid=",$users).") 
AND (ratings2.id IS NULL OR ratings2.weight<".$this->min_old_weight.")
GROUP BY ratings.trackid
HAVING avgrating>!
ORDER BY result DESC
LIMIT 0,!
",array($this->irs->user["id"],$minrating,$num));

$tracks1=array();
for ($i=0;$i<count($tracks);$i++) {
 $tracks1[]=$tracks[$i]["trackid"];
}

return $tracks1;


}




function getUsersLike($num) {


//avg_similarity est la moyenne pondérée par la somme des weight, de la différence entre mêmes notations.
//sum_weight est la somme des poids des pistes notées par les 2 utilisateurs.
// result est (10-y)*(x/(x+1))² , formule du classement sylvinus1.


/*

wrong one


SELECT
 ratings2.userid,
 
 sum(ratings.weight) as sum_weight,
 
 sum( abs(ratings2.rating-ratings.rating) * (ratings2.weight+ratings.weight) )/sum(ratings2.weight+ratings.weight) as avg_similarity,

 (10-avg_similarity)*(sum_weight/(sum_weight+1))*(sum_weight/(sum_weight+1)) as result

FROM irate_ratings as ratings LEFT JOIN irate_ratings as ratings2
ON ratings.trackid=ratings2.trackid 

WHERE ratings.userid=! AND ratings2.userid<>!
GROUP BY ratings2.userid 
HAVING avg_similarity<!
ORDER BY result DESC
LIMIT 0,!

*/




$users=$this->irs->db->getAll("

SELECT
 ratings2.userid,
 
 (10- sum( abs(ratings2.rating-ratings.rating) * (ratings2.weight+ratings.weight) )/sum(ratings2.weight+ratings.weight))*(sum(ratings.weight)/(sum(ratings.weight)+1))*(sum(ratings.weight)/(sum(ratings.weight)+1)) as result

FROM irate_ratings as ratings LEFT JOIN irate_ratings as ratings2
ON ratings.trackid=ratings2.trackid 

WHERE ratings.userid=! AND ratings2.userid<>!
GROUP BY ratings2.userid 
ORDER BY result DESC
LIMIT 0,!

",array($this->irs->user["id"],$this->irs->user["id"],$num)); 

$users1=array();
for ($i=0;$i<count($users);$i++) {
 $users1[]=$users[$i]["userid"];
}

return $users1;

}



}





?>
