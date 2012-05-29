#!/bin/bash

# Test lines
#wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true
#cat VOIDCardInfo.xml | md5sum > VOIDCardInfo.xml.md5sum

# Get old checksum, if any
OLDSUM=
if [ -f VOIDCardInfo.xml.md5sum ]; then
  OLDSUM=`cat VOIDCardInfo.xml.md5sum`
fi

# Get new checksum
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true
#echo ABC >> VOIDCardInfo.xml
cat VOIDCardInfo.xml | md5sum > VOIDCardInfo.xml.md5sum
NEWSUM=`cat VOIDCardInfo.xml.md5sum`

# Show if XML has changed
if [ "$OLDSUM" == "$NEWSUM" ]; then
  echo No change, as far as I can tell
else
  if [ "$OLDSUM" = "" ]; then
    echo Run for the first time in this directory
  else
    echo The XML has changed
  fi
fi

