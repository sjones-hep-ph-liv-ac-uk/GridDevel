#!/bin/bash

wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

./VomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile vod.txt --voddir vo.d --outfile site-info.def 

./SidFormatter.sh --oldsiddir . --newsiddir newsitedir

