#!/bin/bash

wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

cat  VOIDCardInfo.xml | sed -e "s%</VoDump>%%" > deleteme1
cat  ExtraVOIDCardInfo.xml | grep -v "^<VoDump>"  > deleteme2

cat deleteme1 deleteme2 > VOIDCardInfo.xml

./VomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile vod.txt --voddir /user2/sjones/git/GridDevel/vs_scripts/test/xml/vo.d --outfile /user2/sjones/git/GridDevel/vs_scripts/test/xml/site-info.def


