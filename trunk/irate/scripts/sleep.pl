#!/usr/bin/perl -w

# This allows iRATE to run for a while, then it pauses it. The time is
# specified in minutes on the command line.

# Depends on XML::Simple

# Copyright (C) 2003 Robin Sheat <robin@kallisti.net.nz>
# Released under the GNU General Public License

# Version 0.01 - 19/9/2003

# TODO: allow the volume to slowly wind down before pausing.

use XML::Simple;
use IO::Socket;

my $time = $ARGV[0] * 60;

print "Waiting $time seconds.\n";

sleep $time;

my $remote_host = "127.0.0.1";
my $remote_port = "12473";
$socket = IO::Socket::INET->new(PeerAddr => $remote_host,
                                PeerPort => $remote_port,
                                Proto    => "tcp",
                                Type     => SOCK_STREAM)
    or die "Couldn't connect to $remote_host:$remote_port : $!\n";

my %command = ( 'type' => 'pause' );

print $socket XMLout(\%command, rootname => 'command');

%command = ( 'type' => 'disconnect' );

print $socket XMLout(\%command, rootname => 'command');

close($socket);
