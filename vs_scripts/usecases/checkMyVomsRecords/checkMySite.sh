#!/bin/bash


#source ../../set_paths.sh

rm -rf glitecfg/ site-formatted/ newest/

mkdir -p glitecfg/vo.d
mkdir -p site-formatted/vo.d
mkdir -p newest/vo.d

wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

java -jar $VS_JAR_DIR/VomsSnooper.jar --ignorecernrule --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile novos.txt  --voddir newest/vo.d --outfile newest/site-info.def 

rsync -a --delete root@hepgrid6:/root/glitecfg/ glitecfg/

java -jar $VS_JAR_DIR/SidFormatter.jar  --flat --myvos myvos.txt --oldsiddir glitecfg/ --newsiddir site-formatted

diff  site-formatted/site-info.def newest/site-info.def > diffs.txt
ls -lrt diffs.txt


