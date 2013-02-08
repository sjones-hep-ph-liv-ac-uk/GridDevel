#!/bin/sh

if [ ! -x $VS_JAR_DIR/CicToLsc.jar  ]; then
  echo Somehow, this program has been installed poorly.
  echo You could try setting the VS_JAR_DIR directory
  echo to point to the location of the correct JAR files.
  exit 1
fi

java -jar $VS_JAR_DIR/CicToLsc.jar "$@"

