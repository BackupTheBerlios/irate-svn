<?php


// sylvinus' second try :)
//
// This is my implementation of item-based recommendation
// This algorithm only stores and get the 100 closest items to each item
// from the table irate_items_links

Class IRS_Correlation_sylvinus2 extends IRS_Correlation {

//warning, 100% untested

function _cacheOneItem($itemid) {
 
 //todo transaction ?
$this->irs->db->query("DELETE FROM irate_items_links WHERE id_from=!",array($itemid));

$this->irs->db->query("
INSERT INTO irate_items_links(id_from,id_to,value)
SELECT 
 !,
 r2.trackid as id_to,
 ( sum(r2.rating*r2.weight)/sum(r2.weight) ) * (r2.weight/(r2.weight+1))*(r2.weight/(r2.weight+1)) as result
FROM irate_ratings as r 
LEFT JOIN irate_ratings as r2
ON r2.userid=r.userid
WHERE r.trackid=!
AND r.rating>5

GROUP BY r2.trackid
ORDER BY result DESC
LIMIT 0,100",array($itemid,$itemid));

}


function _cache() {

set_time_limit(0);

$ids=$this->irs->db->getCol("SELECT id FROM irate_tracks");

foreach ($ids as $id) {
$this->_cacheOneItem($id);
}






}


function _get($num,$min_old_weight) {

 $this->min_old_weight=$min_old_weight;

 $ret=array();


//todo
$tracks=$this->irs->db->getCol("
SELECT 
 id_to
 sum(i.value) as result
FROM irate_items_links as i LEFT JOIN irate_ratings as r


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
