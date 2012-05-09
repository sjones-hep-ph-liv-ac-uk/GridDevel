#!/bin/bash

wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

./VomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile vod.txt --voddir vo.d --outfile vo-info.def 


./VomsSnooper.sh --xmlfile ExtraVOIDCardInfo.xml  --myvos myvos.txt --vodfile vod.txt --voddir vo.d --outfile Extra-vo-info.def 

cat Extra-vo-info.def >> vo-info.def

cat ExtraFields.txt  >> vo-info.def

sort < vo-info.def | uniq > t
mv t vo-info.def

./addSpaces.pl < vo-info.def > t
mv t vo-info.def





