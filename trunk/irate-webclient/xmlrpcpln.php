<?

// XML RPC pour les nuls :)
//
// code a la con pour pas s'embeter avec XML_RPC_Value.


Class XML_RPC_PLN {




 function php2xmlrpc($d) {

  if (XML_RPC_PLN::is_assoc($d)) {

   $struct=array();
   while (list($k,$v)=each($d)) {
    $struct[$k]=XML_RPC_PLN::php2xmlrpc($v);
   }

   return new XML_RPC_Value($struct,"struct");

  } elseif (is_array($d)) {

   $arr=array();
   for ($i=0;$i<count($d);$i++) {
    $arr[$i]=XML_RPC_PLN::php2xmlrpc($d[$i]);
   }

   return new XML_RPC_Value($arr,"array");

  } elseif (is_string($d)) {
   return new XML_RPC_Value($d,"string");
  } elseif (is_bool($d)) {
   return new XML_RPC_Value($d,"boolean");
  } elseif (is_float($d)) {
   return new XML_RPC_Value($d,"double");
  } elseif (is_int($d)) {
   return new XML_RPC_Value($d,"int");
  }


  //error ?
  return new XML_RPC_Value("");

 }



 function xmlrpc2php($d) {

  if ($d->kindOf()=="scalar") {
   return $d->scalarval();
  } elseif ($d->kindOf()=="array") {
   $a=array();
   for ($i=0;$i<$d->arraysize();$i++) {
    $a[$i]=XML_RPC_PLN::xmlrpc2php($d->arraymem($i));
   }
   return $a;
  } elseif ($d->kindOf()=="struct") {
   $a=array();
   while (list($k,$v)=$d->structeach()) {
    $a[$k]=XML_RPC_PLN::xmlrpc2php($v);
   }
   return $a;
  }

  //error ?
 return "";

 }

function is_assoc($var) {
 return is_array($var) && array_keys($var)!==range(0,sizeof($var)-1);
}






}

?>
