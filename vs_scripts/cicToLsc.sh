#!/bin/sh

if [ ! -x $VS_JAR_DIR/CicToLsc.jar  ]; then
  echo Somehow, this program has been installed poorly.
  exit 1
fi

java -jar $VS_JAR_DIR/CicToLsc.jar "$@"

