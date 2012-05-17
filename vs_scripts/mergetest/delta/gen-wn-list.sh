#!/bin/sh

# quick script to list nodes for the purpose of adding to wn-list.conf
# remember to remove node 39 from the list if appropriate!

RACKS="17 18 19 20 21"
LOWNODE=1
HIGHNODE=40

for rack in $RACKS
do
  r=$rack
  while [ ${#r} -lt 2 ]
  do
    r="0${r}"
  done
  for ((i=$LOWNODE; i<=$HIGHNODE; i++))
  do
    n=$i
    while [ ${#n} -lt 2 ]
    do
      n="0${n}"
    done
    echo "r$r-n$n.ph.liv.ac.uk"
  done
done
