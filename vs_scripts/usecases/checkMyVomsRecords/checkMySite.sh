#!/bin/bash

rm -rf glitecfg/ site-formatted/ newest/

mkdir -p glitecfg/vo.d
mkdir -p site-formatted/vo.d
mkdir -p newest/vo.d

wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true
~/git/GridDevel/vs_scripts/ant/VomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile novos.txt  --voddir newest/vo.d --outfile newest/site-info.def 

rsync -av --delete root@hepgrid6:/root/glitecfg/ glitecfg/

~/git/GridDevel/vs_scripts/ant/SidFormatter.sh  --flat --oldsiddir glitecfg/ --newsiddir site-formatted

diff  site-formatted/site-info.def newest/site-info.def > diffs.txt
ls -lrt diffs.txt



