#!/usr/bin/perl -w

# This is a proof-of-concept and test program for the iRATE client
# external control plugin.

# iRATE: http://irate.sf.net
# Plugin specs: http://www.kallisti.net.nz/wiki/IrateDev/ExternalControlProto

# Depends on XML::Simple, Digest::MD5

# Copyright (C) 2003 Robin Sheat <robin@kallisti.net.nz>
# Released under the GNU General Public License

# Version 0.03 - 22/9/2003

# $Id: irate-control.pl,v 1.5 2003/09/21 14:56:35 eythian Exp $

use XML::Simple;
use IO::Socket;
use Digest::MD5 qw(md5 md5_hex md5_base64);

# Start by connecting to iRATE, default port is 12473
my $remote_host = "127.0.0.1";
my $remote_port = "12473";
my $password =    "mypassword";

$socket = IO::Socket::INET->new(PeerAddr => $remote_host,
                                PeerPort => $remote_port,
                                Proto    => "tcp",
                                Type     => SOCK_STREAM)
    or die "Couldn't connect to $remote_host:$remote_port : $!\n";

my ($test, $input) = undef;
my ($finished, $poke) = 0;
my %inputToRating = (
                   1 => 0,
                   2 => 2,
                   3 => 5,
                   4 => 7,
                   5 => 10
                   );

# Do we need to login?
my %command = (
               'type' => 'currenttrack'
               );
print $socket XMLout(\%command, rootname => 'Command');
my $reply = <$socket>;
my %result = %{XMLin($reply)};
if ($result{type} eq "login-required") {
# Plaintext login:
#    %command = (
#                'type' => 'login',
#                'format' => 'plaintext',
#                'password' => $password
#                );
#    print $socket XMLout(\%command, rootname => 'Command');
#    $reply = <$socket>;
#    %result = %{XMLin($reply)};
#    if ($result{type} eq "login-failure") {
#        die "Invalid password!\n";
#    }

    # Digest authentication
    # Request a challenge
    %command = (
                'type' => 'login',
                'format' => 'digest-md5-getchallenge'
                );
    print $socket XMLout(\%command, rootname => 'Command');
    $reply = <$socket>;
    %result = %{XMLin($reply)};
    my $challenge = $result{challenge};
    # Create a response with the combination of the password and the 
    # challenge
    %command = (
                'type' => 'login',
                'format' => 'digest-md5',
                'password' => md5_hex($password.$challenge)
                );
    print $socket XMLout(\%command, rootname => 'Command');
    $reply = <$socket>;
    %result = %{XMLin($reply)};
    if ($result{type} eq "login-failure") {
        die "Invalid password!\n";
    }
}

while (!$finished) {
    my $reply;
    if ($poke) {
        $reply = <$socket>; # we may need to throw away a line
    }
    $poke = 0;
    # Get the current track playing
    my %command = (
                   'type' => 'currenttrack'
                   );
    
    print $socket XMLout(\%command, rootname => 'Command');
    
    $reply = <$socket>;
    chomp $reply;
    
    my %result = %{XMLin($reply)};
    
    print "Track:\t$result{artist} \\ $result{title}\n";
    print "Plays:\t$result{rating}\n";
    print "Rate:\t$result{state}\n\n";

    print "[S]kip\t\t[P]ause/unpause\n";
    print "[1] This sux\t[2] Yawn\t[3] Not bad\n";
    print "[4] Cool\t[5] Love it\n";
    print "[W]hats playing\t[Q]uit\n";
    
    $test = undef;
    while (!$test) {
        $input = <STDIN>;
        chomp $input;
        $test = $input =~ /^s|p|u|1|2|3|4|5|q|w$/i;
    }

    if ($input =~ /s/i) {
        %command = ( 'type' => 'skip' );
        $poke = 1;
    } elsif ($input =~ /p/i) {
        %command = ( 'type' => 'invert-pause' );
    } elsif ($input =~ /q/i) {
        %command = ( 'type' => 'disconnect' );
        $finished = 1;
    } elsif ($input =~ /1|2|3|4|5/) {
        %command = (
                    'type' => 'rateplaying',
                    'rate' => $inputToRating{$input}
                    );
        $poke = 1;
    }
    print $socket XMLout(\%command, rootname => 'Command');
    
    # work around for iRATE bug
    if ($input =~ /s|1/i) {
        sleep 3; # takes a moment for the player to change
    }
}
close($socket);
