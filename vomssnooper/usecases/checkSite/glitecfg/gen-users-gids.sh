#!/bin/sh

# Starting ID to use (first value actually used will be +1)
GID=2000

# List of VOS (new VOs should be added at the end of the list)
VOS="ALICE ATLAS BABAR BIOMED CALICE CAMONT CDF CEDAR CMS DTEAM DZERO ESR FUSION GEANT4 HONE GRIDPP ILC LHCB MAGIC MANMACE MICE MINOS.VO.GRIDPP.AC.UK NA48 NGS.AC.UK OPS PHENO PLANCK SNO SUPERNEMO.VO.EU-EGEE.ORG T2K ZEUS NORTHGRID SIXT"

for vo in $VOS
do
  GID=$(($GID+1))
  echo "${vo}_GID=$GID"
  GID=$(($GID+1))
  echo "${vo}_PGID=$GID"
  GID=$(($GID+1))
  echo "${vo}_SGID=$GID"
done

