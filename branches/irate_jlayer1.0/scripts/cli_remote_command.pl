#!/usr/bin/perl -w

# iRATE: http://irate.sf.net
# Plugin specs: http://www.kallisti.net.nz/wiki/IrateDev/ExternalControlProto

# Depends on XML::Simple, Digest::MD5

# Copyright (C) 2003 Robin Sheat <robin@kallisti.net.nz>
# Released under the GNU General Public License

#The code I originally used, in it's entirety:
#	`echo '<Command type="$ARGV[0]"/>'|nc jupiter 12473 -w1`;
# Well, ok, there was a shebang, et al. :D

# Ported to use Rob Renaud's <rrenaud@ruslug.rutgers.edu> codebase 
# -Dave Harding <harda@ruslug.rutgers.edu>

=head1 cli_remote_command.pl
Remote Command of a irate client from the CLI.

=head2 Possible Values

	See the file in your irate source:

	irate/plugin/externalcontrol/ExternalControlCommunicator.java

	Or read the documentation at:

	http://www.kallisti.net.nz/wiki/IrateDev/ExternalControlProto

=head3 Quick Start

	Some quickies that I use:

	./cli_remote_command invert-pause #pauses/unpauses
	./cli_remote_command rateplaying #rates song "This Sux"

	--

	I Mainly use this command with the multimedia keys on my keyboard.
	I use a program called hotkeys: http://ypwong.org/hotkeys/
	You'll want to read the documentation there for binding this command
	to your multimedia keys.  It's pretty keyboard-specific.

	Under Debian the package is called, um, "hotkeys".

=cut

use XML::Simple;
use IO::Socket;
use Digest::MD5 qw(md5 md5_hex md5_base64);

# Start by connecting to iRATE, default port is 12473
my $remote_host = "localhost";
my $remote_port = "12473";
my $password =    "empty";
my $timeout =	  ".1"; #seconds

$socket = IO::Socket::INET->new(PeerAddr => $remote_host,
                                PeerPort => $remote_port,
                                Proto    => "tcp",
                                Type     => SOCK_STREAM)
    or die "Couldn't connect to $remote_host:$remote_port : $!\n";

my ($test, $input) = undef;

# Make sure the connection doesn't hang, this is important for if this command
# is bound to multi-media keys where you could spawn hundreds of proccess
# over time.

die "Can't Fork: $!" unless defined ($childpid = fork());

if ($childpid) {
  sleep $timeout;
  kill("TERM" => $childpid);
  }
  
else {
  
  # Do we need to login?
  my %command = (
                 'type' => $ARGV[0]
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
  
  # Send the given command
  %command = (
              'type' => $ARGV[0]
              );
  
  print $socket XMLout(\%command, rootname => 'Command');
  
  }
  
$socket->shutdown(0); # Since it was a the child proccess that did the work
close($socket); 
