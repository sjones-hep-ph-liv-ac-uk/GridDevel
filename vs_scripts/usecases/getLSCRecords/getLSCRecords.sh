#!/bin/bash

PATH=$VS_WRAPPER_DIR:$PATH

# Set up some dirs for this job
rm -rf vomsdir 
rm  VOIDCardInfo.xml  


mkdir -p vomsdir

# Get the CIC Portal XML
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Make a vomsdir from it (the other stuff is a by-product)
cicToLsc.sh --xmlfile VOIDCardInfo.xml  --approvedvos myvos.txt --vomsdir vomsdir

