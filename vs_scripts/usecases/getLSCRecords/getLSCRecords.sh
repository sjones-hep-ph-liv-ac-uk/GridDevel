#!/bin/bash

#source ../../set_paths.sh


# Set up some dirs for this job
rm -rf vomsdir 
rm  VOIDCardInfo.xml  


mkdir -p vomsdir

# Get the CIC Portal XML
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Make a vomsdir from it (the other stuff is a by-product)
java -jar $VS_JAR_DIR/CicToLsc.jar --xmlfile VOIDCardInfo.xml  --approvedvos myvos.txt --vomsdir vomsdir

