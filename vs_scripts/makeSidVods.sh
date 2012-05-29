#!/bin/bash

wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Make all the VODs
~/git/GridDevel/vs_scripts/VomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos allvos.txt --vodfile allvos.txt --voddir glitecfg/vo.d --outfile glitecfg/site-info.def --nosillysids --printvodtitle

# Make all the SIDs
~/git/GridDevel/vs_scripts/VomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos allvos.txt --vodfile novos.txt  --voddir glitecfg/vo.d --outfile glitecfg/site-info.def --nosillysids --printvodtitle

./assemble_content.pl glitecfg/ > sidvods.txt





