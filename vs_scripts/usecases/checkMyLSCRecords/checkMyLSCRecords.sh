#!/bin/bash

# Set up some dirs for this job
rm -rf site-formatted/ cic_portal_siddir/ cic_portal_vomsdir/ site_vomsdir/
rm  VOIDCardInfo.xml  site_vomsdir.md5  diffs.txt  cic_portal_vomsdir.md5


mkdir -p cic_portal_siddir/vo.d
mkdir -p cic_portal_vomsdir
mkdir -p site_vomsdir

# Get the CIC Portal XML
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Make a vomsdir from it (the other stuff is a by-product)
~/git/GridDevel/vs_scripts/ant/VomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile novos.txt  --voddir cic_portal_siddir/vo.d --outfile cic_portal_siddir/site-info.def  --vomsdir cic_portal_vomsdir

# Now get the site's voms dir
rsync -av --delete root@hepgrid6:/etc/grid-security/vomsdir/ site_vomsdir/


# Get a sorted list of site lsc files, sort each one, and print the md5 of it
for f in `find site_vomsdir/ -type f -name "*.lsc" | sort `; do 
  echo -n $f" " |  sed -e "s/^[^/]*\///" ; cat  $f | sort | md5sum; 
done > site_vomsdir.md5


# Get a sorted list of CIC Portal lsc files, sort each one, and print the md5 of it
for f in `find cic_portal_vomsdir/ -type f -name "*.lsc" | sort `; do 
  echo -n $f" " | sed -e "s/^[^/]*\///" ; cat  $f | sort | md5sum; 
done > cic_portal_vomsdir.md5

# Compare results
diff site_vomsdir.md5 cic_portal_vomsdir.md5 > diffs.txt


