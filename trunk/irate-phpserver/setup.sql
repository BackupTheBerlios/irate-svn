#
# Table structure for table `distributions`
#

CREATE TABLE `distributions` (
  `trackid` bigint(16) NOT NULL default '0',
  `id` bigint(16) NOT NULL default '0',
  `codec` varchar(16) NOT NULL default '',
  `averagebitrate` int(4) NOT NULL default '0',
  `crediturl` text NOT NULL,
  `adddate` date NOT NULL default '0000-00-00',
  `filesize` bigint(15) NOT NULL default '0',
  `hash_sha1` varchar(40) NOT NULL default '',
  PRIMARY KEY  (`id`),
  KEY `trackid` (`trackid`)
) TYPE=MyISAM;

# --------------------------------------------------------

#
# Table structure for table `ratings`
#

CREATE TABLE `ratings` (
  `id` int(12) NOT NULL default '0',
  `trackid` bigint(16) NOT NULL default '0',
  `userid` int(12) NOT NULL default '0',
  `rating` int(2) NOT NULL default '0',
  `ratingdate` datetime NOT NULL default '0000-00-00 00:00:00',
  `ratingnum` int(4) NOT NULL default '0',
  `weight` tinyint(3) NOT NULL default '100',
  PRIMARY KEY  (`id`),
  KEY `trackid` (`trackid`),
  KEY `userid` (`userid`),
  KEY `rating` (`rating`)
) TYPE=MyISAM;

# --------------------------------------------------------

#
# Table structure for table `sources`
#

CREATE TABLE `sources` (
  `id` int(12) NOT NULL default '0',
  `distribid` bigint(16) NOT NULL default '0',
  `media` tinyint(3) NOT NULL default '0',
  `protocol` varchar(32) NOT NULL default '',
  `link` text NOT NULL,
  `crediturl` text NOT NULL,
  `adddate` date NOT NULL default '0000-00-00',
  PRIMARY KEY  (`id`),
  KEY `distribid` (`distribid`)
) TYPE=MyISAM;

# --------------------------------------------------------

#
# Table structure for table `tracks`
#

CREATE TABLE `tracks` (
  `artistname` varchar(64) NOT NULL default '',
  `trackname` varchar(128) NOT NULL default '',
  `license` varchar(128) NOT NULL default '',
  `albumname` text NOT NULL,
  `pubdate` date NOT NULL default '0000-00-00',
  `id` bigint(16) NOT NULL default '0',
  `duration` int(4) NOT NULL default '0',
  `adddate` date NOT NULL default '0000-00-00',
  `crediturl` text NOT NULL,
  PRIMARY KEY  (`id`,`id`)
) TYPE=MyISAM;

# --------------------------------------------------------

#
# Table structure for table `users`
#

CREATE TABLE `users` (
  `id` int(12) NOT NULL default '0',
  `user` varchar(32) NOT NULL default '',
  `pass` varchar(32) NOT NULL default '',
  `dateinscr` datetime NOT NULL default '0000-00-00 00:00:00',
  `datelastlogin` datetime NOT NULL default '0000-00-00 00:00:00',
  `ipinscr` varchar(15) NOT NULL default '',
  PRIMARY KEY  (`id`),
  KEY `user` (`user`)
) TYPE=MyISAM;
