#!/usr/bin/perl -w

# iRATE: http://irate.sf.net
# Plugin specs: http://www.kallisti.net.nz/wiki/IrateDev/ExternalControlProto

# Depends on XML::Simple, Digest::MD5

# Copyright (C) 2003 Robin Sheat <robin@kallisti.net.nz>
# Released under the GNU General Public License

# This was originally a proof of concept script, I dumbed it down even
# more, so it just prints out the current track.  Now it's useful as a
# music detector for LogJam: http://logjam.danga.com/
# Rob Renaud <rrenaud@ruslug.rutgers.edu>

# $Id: get_track.pl,v 1.1 2004/05/27 00:44:10 eythian Exp $

use XML::Simple;
use IO::Socket;
use Digest::MD5 qw(md5 md5_hex md5_base64);

# Start by connecting to iRATE, default port is 12473
my $remote_host = "127.0.0.1";
my $remote_port = "12473";
my $password =    "empty";

$socket = IO::Socket::INET->new(PeerAddr => $remote_host,
                                PeerPort => $remote_port,
                                Proto    => "tcp",
                                Type     => SOCK_STREAM)
    or die "Couldn't connect to $remote_host:$remote_port : $!\n";

my ($test, $input) = undef;

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

# Get the current track playing
%command = (
            'type' => 'currenttrack'
            );

print $socket XMLout(\%command, rootname => 'Command');

$reply = <$socket>;
chomp $reply;

%result = %{XMLin($reply)};

#print "<a href=\"$result{url}\">$result{artist} - $result{title}</a>\n";
print "$result{artist} - $result{title}";
close($socket);
