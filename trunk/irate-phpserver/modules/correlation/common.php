<?php




Class IRS_Correlation {

var $irs;

function IRS_Correlation(&$irs) {

 $this->irs=&$irs;

}

function get($num,$min_old_weight) {

 $trackids=$this->_get($num,$min_old_weight);

 return $trackids;


}



}

?>
