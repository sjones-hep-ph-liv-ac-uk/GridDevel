#!/bin/bash

#voms-proxy-init --voms dteam

glite-wms-job-delegate-proxy -a > temp.out
id=`cat temp.out | grep identifier | sed -e "s/.*identifier://"`

glite-wms-job-submit -d $id -o list_wms.$$ test.jdl 

