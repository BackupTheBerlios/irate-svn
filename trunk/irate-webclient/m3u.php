<?

//get m3u file

header("Cache-control: public",TRUE);
header("Content-type: audio/x-mpegurl",TRUE);
header("Content-Disposition: inline; filename=irate-playlist.m3u",TRUE);

echo $_REQUEST["list"];


?>
