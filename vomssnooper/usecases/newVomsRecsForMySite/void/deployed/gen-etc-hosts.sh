#!/bin/sh

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
    echo -e "192.168.$rack.$i\t\tr$r-n$n.ph.liv.ac.uk\tr$r-n$n"
  done
done
