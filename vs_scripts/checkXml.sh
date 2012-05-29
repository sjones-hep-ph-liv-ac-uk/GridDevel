#!/bin/bash

# Get the XML

wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Get its md5sum

cat VOIDCardInfo.xml > VOIDCardInfo.xml.md5sum

