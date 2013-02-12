#!/bin/bash

PATH=/opt/GridDevel/bin:$PATH

##########################################################################
# Script automate the update to our vo.d records
# sj, 17 May 2012                                                        #
##########################################################################
rm -rf void/deployed/vo.d void/merged/vo.d void/xml/vo.d

mkdir -p void/deployed/vo.d
mkdir -p void/merged/vo.d
mkdir -p void/xml/vo.d

# Note:
# void/myvos.txt and void/vod.txt are presumed to exist already.
# They contain the VOs to select and the ones to print in VOD style,
# respectively. At Liverpool, we only use VODs, which is much
# simpler.

############################################
# Section 1 - use the CIC portal XML,      #
# and our own site XML, to make the newest #
# VOD data.                                #
############################################

# Get the newest VOID XML
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Concatenate the site specific VOID info
cat  VOIDCardInfo.xml | sed -e "s%</VoDump>%%" > deleteme1
cat  ExtraVOIDCardInfo.xml | grep -v "^<VoDump>"  > deleteme2
cat deleteme1 deleteme2 > VOIDCardInfo.xml

# Convert it into vod format
vomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos void/myvos.txt --vodfile void/vod.txt --voddir void/xml/vo.d --outfile void/xml/site-info.def

############################################
# Section 2 - Get the operational data,    #
# and merge the deltas from the XML into   #
# it.                                      #
############################################
#
# Transfer 
rsync -a --delete /root/svn/puppet/trunk/modules/lcg-common/files/vo.d/ void/deployed/vo.d/

# Merge those records in
./vod_merger.pl --oldvoddir void/deployed/vo.d --deltavoddir void/xml/vo.d --newvoddir void/merged/vo.d


############################################
# Section 3 -                              #
############################################

echo 
echo --- These VODs have changed ----------------------------------------
echo 

rsync -avc void/merged/vo.d/ /root/svn/puppet/trunk/modules/lcg-common/files/vo.d/ --dry-run


