-- phpMyAdmin SQL Dump
-- version 2.6.0-pl2
-- http://www.phpmyadmin.net
-- 
-- Host: localhost
-- Generation Time: Jan 11, 2005 at 11:48 PM
-- Server version: 4.0.22
-- PHP Version: 4.3.9
-- 
-- Database: `irate`
-- 

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_distributions`
-- 

CREATE TABLE `irate_distributions` (
  `trackid` bigint(16) NOT NULL default '0',
  `id` bigint(16) NOT NULL default '0',
  `codec` varchar(16) NOT NULL default '',
  `crediturl` text NOT NULL,
  `adddate` date NOT NULL default '0000-00-00',
  `filesize` bigint(15) NOT NULL default '0',
  `hash_sha1` varchar(40) NOT NULL default '',
  PRIMARY KEY  (`id`),
  KEY `trackid` (`trackid`),
  KEY `hash_sha1` (`hash_sha1`)
) TYPE=MyISAM;

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_distributions_seq`
-- 

CREATE TABLE `irate_distributions_seq` (
  `id` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_prepare`
-- 

CREATE TABLE `irate_prepare` (
  `userid` int(12) NOT NULL default '0',
  `trackid` bigint(16) NOT NULL default '0',
  `date` datetime NOT NULL default '0000-00-00 00:00:00',
  KEY `userid` (`userid`),
  KEY `date` (`date`)
) TYPE=MyISAM;

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_ratings`
-- 

CREATE TABLE `irate_ratings` (
  `id` int(12) NOT NULL default '0',
  `trackid` bigint(16) NOT NULL default '0',
  `userid` int(12) NOT NULL default '0',
  `rating` int(2) NOT NULL default '0',
  `ratingdate` datetime NOT NULL default '0000-00-00 00:00:00',
  `ratingnum` int(4) NOT NULL default '0',
  `weight` float NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `trackid` (`trackid`),
  KEY `userid` (`userid`),
  KEY `rating` (`rating`)
) TYPE=MyISAM;

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_ratings_seq`
-- 

CREATE TABLE `irate_ratings_seq` (
  `id` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_sources`
-- 

CREATE TABLE `irate_sources` (
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

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_sources_seq`
-- 

CREATE TABLE `irate_sources_seq` (
  `id` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_tracks`
-- 

CREATE TABLE `irate_tracks` (
  `artistname` text NOT NULL,
  `trackname` text NOT NULL,
  `license` varchar(255) NOT NULL default '',
  `albumname` text NOT NULL,
  `pubdate` date NOT NULL default '0000-00-00',
  `id` bigint(16) NOT NULL default '0',
  `duration` int(4) NOT NULL default '0',
  `adddate` date NOT NULL default '0000-00-00',
  `crediturl` text NOT NULL,
  PRIMARY KEY  (`id`,`id`)
) TYPE=MyISAM;

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_tracks_seq`
-- 

CREATE TABLE `irate_tracks_seq` (
  `id` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_users`
-- 

CREATE TABLE `irate_users` (
  `id` int(12) NOT NULL default '0',
  `user` varchar(32) NOT NULL default '',
  `pass` varchar(32) NOT NULL default '',
  `dateinscr` datetime NOT NULL default '0000-00-00 00:00:00',
  `datelastlogin` datetime NOT NULL default '0000-00-00 00:00:00',
  `datelastprepare` datetime NOT NULL default '0000-00-00 00:00:00',
  `ipinscr` varchar(15) NOT NULL default '',
  PRIMARY KEY  (`id`),
  KEY `user` (`user`),
  KEY `datelastlogin` (`datelastlogin`),
  KEY `datelastprepare` (`datelastprepare`)
) TYPE=MyISAM;

-- --------------------------------------------------------

-- 
-- Table structure for table `irate_users_seq`
-- 

CREATE TABLE `irate_users_seq` (
  `id` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`id`)
) TYPE=MyISAM;
