#!/bin/bash

PATH=$VS_WRAPPER_DIR:$PATH

vomsSnooper.sh --xmlfile   http://operations-portal.egi.eu/xml/voIDCard/public/all/true   --myvos void/myvos.txt  --outfile /dev/null --fqans fqans

./makeGroupsConf.pl < fqans  > groups.conf.new


scp root@hepgrid6:/opt/glite/yaim/etc/groups.conf .

sort < groups.conf > groups.conf.s
sort < groups.conf.new > groups.conf.new.s
