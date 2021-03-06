#!/bin/bash

PATH=$VS_WRAPPER_DIR:$PATH

# Tool to get the CIC XML File, and parse it, making SID and VOD records
#
# You will have to edit this if you install the tools somewhere else.
# sj, 29 May 2012

rm -rf glitecfg/vo.d
mkdir -p glitecfg/vo.d

# Get the XML from the CIC Portal
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Make all the VODs
vomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos allvos.txt --vodfile allvos.txt --voddir glitecfg/vo.d --outfile glitecfg/site-info.def --nosillysids --printvodtitle

# Make all the SIDs
vomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos allvos.txt --vodfile novos.txt  --voddir glitecfg/vo.d --outfile glitecfg/site-info.def --nosillysids --printvodtitle

# Put it altogether and merge it with the old wiki content
./assemble_content.pl -dir glitecfg/ -wf wiki.txt -of int.wiki.txt

# Make the twiki table of resources
voResources.sh --xmlfile VOIDCardInfo.xml  --myvos allvos.txt --res res.txt

# Put the new table in the twiki
./insert_resource_table.pl -wf int.wiki.txt  -res res.txt -of new.wiki.txt 


