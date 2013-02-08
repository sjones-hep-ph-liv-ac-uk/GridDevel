#!/bin/bash

PATH=$VS_WRAPPER_DIR:$PATH

rm  VOIDCardInfo.xml  

# Get the CIC Portal XML
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Make a table of resources (wiki format)
voResources.sh --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --res res.txt

