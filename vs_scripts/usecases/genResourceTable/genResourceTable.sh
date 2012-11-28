#!/bin/bash

rm  VOIDCardInfo.xml  

# Get the CIC Portal XML
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Make a table of resources (wiki format)
java -jar $VS_JAR_DIR/VoResources.jar --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --res res.txt

