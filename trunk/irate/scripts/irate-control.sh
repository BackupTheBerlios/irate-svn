#!/bin/sh

HOST=localhost
PORT=12473

case "$1" in
  skip)
        echo "Skipping to next track"
        echo '<command type="skip"/>' |telnet ${HOST} ${PORT} >/dev/null
	;;
  pause)
        echo "Pausing audio play"
        echo '<command type="pause"/>' |telnet ${HOST} ${PORT} >/dev/null
	;;
  *)
	echo Usage $0 "<command>"
        echo
        echo "Available commands are:"
        echo "  skip - skip to the next track"
        echo "  pause - pause audio play"
        exit 1
esac

exit 0 
