#!/bin/bash 

wd=$(pwd)
cd $(dirname $0)
./gradlew run -Pargs="$wd $*"
