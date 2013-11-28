#!/bin/bash

if [ $# != 1 ]; then
  echo Give it a file
  exit 1
fi

for j in `cat $1  | grep -v Submitted`; do
  glite-wms-job-status $j 
done
 
