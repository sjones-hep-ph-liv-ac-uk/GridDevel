#!/bin/bash

PATH=$VS_WRAPPER_DIR:$PATH

mkdir -p glitecfg

rsync -a --delete root@hepgrid6:/root/glitecfg/    glitecfg/

siteChecker.sh --xmlurl http://operations-portal.egi.eu/xml/voIDCard/public/all/true  --sidfile glitecfg/site-info.def  


