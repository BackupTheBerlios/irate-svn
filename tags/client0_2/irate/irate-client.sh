#!/bin/bash
DIR=`dirname "$0"`

export LD_LIBRARY_PATH="${DIR}/lib"
java -jar "${DIR}/irate-client.jar"
