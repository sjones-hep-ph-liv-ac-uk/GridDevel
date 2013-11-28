#!/bin/bash

if [ $# != 1 ]; then
  echo Give it a file
  exit 1
fi

for j in `cat $1 | grep -v Submitted`; do
  dir=`echo $j | sed -e "s/.*9000.//" | sed -e "s/^[-]//" `
  glite-wms-job-output --dir $dir $j
done


 
