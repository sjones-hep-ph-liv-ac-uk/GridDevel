#!/bin/bash

PATH=$VS_WRAPPER_DIR:$PATH

rm -rf glitecfg/ site-formatted/ newest/

mkdir -p glitecfg/vo.d
mkdir -p site-formatted/vo.d
mkdir -p newest/vo.d

wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

vomsSnooper.sh --ignorecernrule --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile novos.txt  --voddir newest/vo.d --outfile newest/site-info.def 

rsync -a --delete root@hepgrid6:/root/glitecfg/ glitecfg/

sidFormatter.sh  --flat --myvos myvos.txt --oldsiddir glitecfg/ --newsiddir site-formatted

diff  site-formatted/site-info.def newest/site-info.def > diffs.txt
ls -lrt diffs.txt


