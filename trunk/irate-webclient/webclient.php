<?

 //start the php session and send the cookie
session_start();

?>
<html>
<head>
 <title>iRATE web client</title>
</head>
<body>
This is iRATE proof-of-concept web client. It was written to show how simple the new XMLRPC protocol is. Warning, it's very slow and hammers the iRATE server ! You must have cookies turned on. To use irate.jamendo.com, please use the account irate-test / password test (defaults). The server must have PEAR's XML/RPC.php (pear install -o XML_RPC)<br><br>
<?

 //ask for login
if (empty($_SESSION["user"]) AND empty($_REQUEST["user"]) OR isset($_REQUEST["logout"])) {
?>

Please login :
<form action='webclient.php' method='post'>
server host : <input type='text' name='server' value='irate.jamendo.com'><br />
username : <input type='text' name='user' value='irate-test'><br />
password : <input type='password' name='pass' value='test'><br />
<input type='submit' value='OK'>
</form>

<?
//we're logged in.
} else {

  // store login info
 if (!empty($_REQUEST["user"])) {
  $_SESSION["user"]=$_REQUEST["user"];
  $_SESSION["pass"]=$_REQUEST["pass"];
  $_SESSION["server"]=$_REQUEST["server"];
 }

  //include XMLRPC for php. (uses PEAR !)
 require_once("XML/RPC.php");
 require_once("xmlrpcpln.php");


  //connect to the server
 $XMLRPC= new XML_RPC_Client("/?u=".$_SESSION["user"]."&h=".sha1("irate".sha1($_SESSION["pass"])), $_SESSION["server"], 80);


  // if there's a rating to do
 if (!empty($_REQUEST["rate_id"])) {
 
  $action="irate.rate";
  
  if ($_REQUEST["rate_rating"]=="-1") { //if -1 given, unrate the track
   $action="irate.unrate";  
  }
  
  $msg = new XML_RPC_Message($action,array(XML_RPC_PLN::php2xmlrpc(array(array(
   "id"=>$_REQUEST["rate_id"],
   "rating"=>$_REQUEST["rate_rating"]
  )))));
  $XMLRPC->send($msg);
  
 }



  //ask for all the previous ratings
 $msg = new XML_RPC_Message("irate.getRatings");
 $rep=$XMLRPC->send($msg);
 $rated_tracks=XML_RPC_PLN::xmlrpc2php($rep->value());


  //for each rated track, get metadata
 for ($i=0;$i<count($rated_tracks);$i++) {
  $msg = new XML_RPC_Message("irate.getInfo",array(XML_RPC_PLN::php2xmlrpc( array( "id" => $rated_tracks[$i]["id"] ))));
  $rep=$XMLRPC->send($msg);
  $tracks_metadata[$i]=XML_RPC_PLN::xmlrpc2php($rep->value());
 }


  //ask for one new track
 $msg = new XML_RPC_Message("irate.getNew",array(XML_RPC_PLN::php2xmlrpc( array( "n"=>1 ) )));
 $rep=$XMLRPC->send($msg);
 $new_track=XML_RPC_PLN::xmlrpc2php($rep->value());


 //that's it ! now we only need to display the tracks.
 
?>

<table border=1>
<tr>
 <th>Track name</th>
 <th>Artist name</th>
 <th>Crediturl</th>
 <th>Duration</th>
 <th>Rated</th>
 <th>Rate again ?</th>
</tr>

<?

$select="<select name='rate_rating'><option value='0'>0</option><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option><option value='4'>4</option><option value='5'>5</option><option value='6'>6</option><option value='7'>7</option><option value='8'>8</option><option value='9'>9</option><option value='10'>10</option><option value='-1'>unrate</option></select>";


for ($i=0;$i<count($rated_tracks);$i++) {

 echo "
  <tr>
   <td>".$tracks_metadata[$i]["trackname"]."</td>
   <td>".$tracks_metadata[$i]["artistname"]."</td>
   <td>".$tracks_metadata[$i]["crediturl"]."</td>
   <td>".$tracks_metadata[$i]["duration"]."</td>
   <td>".$rated_tracks[$i]["rating"]."</td>
   <td><form action='webclient.php?rate_id=".$rated_tracks[$i]["id"]."' method='post'>".$select." <input type='submit' value='OK'></form></td>
  </tr>";
}

echo "
<tr>
   <td><b>NEW :</b> ".$new_track[0]["trackname"]."</td>
   <td>".$new_track[0]["artistname"]."</td>
   <td>".$new_track[0]["crediturl"]."</td>
   <td>".$new_track[0]["duration"]."</td>
   <td>unrated</td>
   <td><form action='webclient.php?rate_id=".$new_track[0]["id"]."' method='post'>".$select." <input type='submit' value='OK'></form></td>
</tr>
</table>
";

//button to play the tracks as m3u
echo "
<form action='m3u.php' method='post'>
<input type='hidden' name='list' value='";

for ($i=0;$i<count($rated_tracks);$i++) {
echo $tracks_metadata[$i]["Distributions"][0]["Sources"][0]["link"]."\n";
}
echo $new_track[0]["Distributions"][0]["Sources"][0]["link"]."\n";


echo "'>
<br><input type='submit' value='Play tracks !'></form>
";






}

?>
</table>


</body>
</html>
