#!/bin/bash

source ../../set_paths.sh

##########################################################################
# Script automate the update to our site-info.def_vo.d (i.e. "sidvods"). #
# sj, 17 May 2012                                                        #
##########################################################################

mkdir -p void/deployed/vo.d
mkdir -p void/merged/vo.d
mkdir -p void/xml/vo.d

# Note:
# void/myvos.txt and void/vod.txt are presumed to exist already.
# They contain the VOs to select and the ones to print in VOD style,
# respectively.

############################################
# Section 1 - use the CIC portal XML,      #
# and our own site XML, to make the newest #
# SIDVOD data.                             #
############################################

# Get the newest VOID XML
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Concatenate the site specific VOID info
cat  VOIDCardInfo.xml | sed -e "s%</VoDump>%%" > deleteme1
cat  ExtraVOIDCardInfo.xml | grep -v "^<VoDump>"  > deleteme2
cat deleteme1 deleteme2 > VOIDCardInfo.xml

# Convert it into sidvod format
VomsSnooper.jar --xmlfile VOIDCardInfo.xml  --myvos void/myvos.txt --vodfile void/vod.txt --voddir ~/git/GridDevel/vs_scripts/usecases/newVomsRecsForMySite/void/xml/vo.d --outfile $HOME//git/GridDevel/vs_scripts/usecases/newVomsRecsForMySite/void/xml/site-info.def

############################################
# Section 2 - Get the operational data,    #
# and merge the deltas from the XML into   #
# it.                                      #
############################################
#
# Transfer from hepgrid6 via:
rsync -a --delete root@hepgrid6:/root/glitecfg/ $HOME/git/GridDevel/vs_scripts/usecases/newVomsRecsForMySite/void/deployed

# Merge those records in
./sid_merger.pl --oldsid void/deployed/site-info.def --deltas void/xml/site-info.def --newsid void/merged/site-info.def

# Completely replace the vo.d files
rsync -a --delete void/xml/vo.d/ void/merged/vo.d

