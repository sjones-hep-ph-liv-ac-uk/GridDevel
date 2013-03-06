#!/bin/bash

PATH=/opt/GridDevel/bin:$PATH

# Get the newest VOID XML
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Convert XML into vod format
rm -rf glitecfg
mkdir -p glitecfg/vo.d
vomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile myvos.txt --voddir glitecfg/vo.d --outfile /dev/null

# Make a list of VOS for inclusion in the site-info.def
echo "" >  glitecfg/site-info.def.tmp1
echo "###################################" >>  glitecfg/site-info.def.tmp1
echo "# This is a fragment that shows   #" >>  glitecfg/site-info.def.tmp1
echo "# the VOs that have been          #" >>  glitecfg/site-info.def.tmp1
echo "# processed. Please include       #" >>  glitecfg/site-info.def.tmp1
echo "# this fragment in your           #" >>  glitecfg/site-info.def.tmp1
echo "# site-info.def                   #" >>  glitecfg/site-info.def.tmp1
echo "###################################" >>  glitecfg/site-info.def.tmp1
echo "# Note that records expected in   #" >>  glitecfg/site-info.def.tmp1
echo "# vo.d format; else uncomment     #" >>  glitecfg/site-info.def.tmp1
echo "# sids below.                     #" >>  glitecfg/site-info.def.tmp1
echo "###################################" >>  glitecfg/site-info.def.tmp1
echo " " >>  glitecfg/site-info.def.tmp1

echo -n "VOS=\"" >> glitecfg/site-info.def.tmp1
for vo in `ls glitecfg/vo.d/* | xargs -n1 basename`; do echo -n $vo" "; done  >> glitecfg/site-info.def.tmp1
echo -n "\"" >> glitecfg/site-info.def.tmp1
echo " " >>  glitecfg/site-info.def.tmp1
echo " " >>  glitecfg/site-info.def.tmp1

# Convert XML into sid format
rm -rf novos.txt; touch novos.txt
vomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile novos.txt --voddir glitecfg/vo.d --outfile glitecfg/site-info.def.tmp2

# Add default values to the sid and vod records
./addDefaults.pl -commentsids 1 -sid glitecfg/site-info.def.tmp2  -swdir '$VO_SW_DIR' -spath '$STORAGE_PATH' -defse '$DPM_HOST' -cswdir atlas:/cvmfs/atlas.cern.ch/repo/sw -cswdir lhcb:/cvmfs/lhcb.cern.ch -cswdir cms:/cvmfs/cms.cern.ch -cswdir mice:/cvmfs/mice.gridpp.ac.uk -cswdir na62.vo.gridpp.ac.uk:/cvmfs/na62.gridpp.ac.uk -cswdir hone:/cvmfs/hone.gridpp.ac.uk


# Concat the lot togther
cat glitecfg/site-info.def.tmp1 glitecfg/site-info.def.tmp2 >> glitecfg/site-info.def.frag
rm glitecfg/site-info.def.tmp1 glitecfg/site-info.def.tmp2

# Make the tar ball
tar -cf glitecfg.tar glitecfg/


