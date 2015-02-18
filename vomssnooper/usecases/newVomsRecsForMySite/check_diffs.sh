#!/bin/bash

for f in `find ./void/merged/ -name "*"  -type f | find ./void/merged/ -name "*"  -type f | sed -e "s/\.\/void\/merged\///g"`; do
  if [ -f /root/svn/puppet/trunk/modules/lcg-common/files/$f ]; then
    echo
    echo ------------------------------------------------------------------------------------------------------
    echo Diffs for ./void/merged/$f  /root/svn/puppet/trunk/modules/lcg-common/files/$f :
    diff ./void/merged/$f /root/svn/puppet/trunk/modules/lcg-common/files/$f
  else
    echo Warning: The matching file for ./void/merged/$f does not exist
  fi
done

