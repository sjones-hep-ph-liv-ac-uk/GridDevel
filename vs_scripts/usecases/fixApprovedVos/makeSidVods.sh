#!/bin/bash

source ../../set_paths.sh


# Tool to get the CIC XML File, and parse it, making SID and VOD records
#
# You will have to edit this if you install the tools somewhere else.
# sj, 29 May 2012

mkdir -p glitecfg/vo.d

# Get the XML from the CIC Portal
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Make all the VODs
VomsSnooper.jar --xmlfile VOIDCardInfo.xml  --myvos allvos.txt --vodfile allvos.txt --voddir glitecfg/vo.d --outfile glitecfg/site-info.def --nosillysids --printvodtitle

# Make all the SIDs
VomsSnooper.jar --xmlfile VOIDCardInfo.xml  --myvos allvos.txt --vodfile novos.txt  --voddir glitecfg/vo.d --outfile glitecfg/site-info.def --nosillysids --printvodtitle

# Put it altogether and merge it with the old wiki content
./assemble_content.pl -dir glitecfg/ -wf wiki.txt -of new.wiki.txt

