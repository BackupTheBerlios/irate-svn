<?php




Class IRS_Grabber {

   function makeXMLTree($data)
   {
     $ret = array();

     $parser = xml_parser_create();
     xml_parser_set_option($parser,XML_OPTION_CASE_FOLDING,0);
     xml_parser_set_option($parser,XML_OPTION_SKIP_WHITE,1);
     xml_parse_into_struct($parser,$data,$values,$tags);
     xml_parser_free($parser);

     $hash_stack = array();

     foreach ($values as $key => $val)
     {
         switch ($val['type'])
         {
           case 'open':
//echo "<br>open ".$val["tag"];
		$countername="_COUNTER_".implode("___",$hash_stack);
//echo "<br><br>$countername<br>";
	      
		if (!isset($$countername)) {
		 $$countername=0;
		}
	      
               array_push($hash_stack, $val['tag']);
              array_push($hash_stack, $$countername);

	       $$countername++;
	       
           break;

           case 'close':
               array_pop($hash_stack);
               array_pop($hash_stack);
           break;

           case 'complete':
               array_push($hash_stack, $val['tag']);

               // uncomment to see what this function is doing
//                echo("\$ret[" . implode($hash_stack, "][") . "] = '{$val[value]}';\n");

               eval("\$ret[" . implode($hash_stack, "][") . "][] = '".str_replace("'","\'",$val[value])."';");
               array_pop($hash_stack);
           break;
         }
     }

     return $ret;
   }





}





?>
