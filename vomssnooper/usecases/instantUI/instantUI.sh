#!/bin/bash

##########################################################################
# Script to quickly set up a UI                                          #
# sj, 22 Nov 2013                                                        #
##########################################################################

############################################
# Prepare                                  #
############################################
mkdir -p glitecfg/vo.d
rm  glitecfg/vo.d/* 
rm glitecfg/site-info.def 
PATH=/opt/GridDevel/bin:$PATH

# run as root
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" ; exit 1
fi

# have emi-ui 
if [[ "$(rpm -q emi-ui)" =~ "not installed" ]]; then
  echo "emi-ui package should be installed"; exit 2
fi

# have VomsSnooper 
if [[ "$(rpm -q VomsSnooper)" =~ "not installed" ]]; then
  echo "VomsSnooper package should be installed"; exit 3
fi

############################################
# Get the newest portal VOMS records       #
############################################
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true
if [ ! -s VOIDCardInfo.xml ]; then
  echo "Failed to download operations portal XML file"; exit 5
fi

############################################
# Process VOs to support                   #
############################################
echo -n > myvos.txt
if [ $# -gt 0 ]; then
  # If he's given any VOs on the CLI, use them 
  for vo in "$@"; do
    echo "$vo" >> myvos.txt
  done
else
  # No vos on the CLI; use the approved set
  cat vos.txt >> myvos.txt
fi

############################################
# Check desired records are in portal      #
############################################
good=0
for vo in `cat myvos.txt`; do
  grep "IDCard.*Name=\"$vo\"" VOIDCardInfo.xml > /dev/null 2>&1
  if [ $? != 0 ]; then
    echo The $vo VO is not present in the VOIDCardInfo.xml file
    good=1
  fi
done
if [ $good != 0 ]; then
  echo Exiting early; exit 4
fi

############################################
# Convert it into vod format               #
############################################
vomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile myvos.txt --voddir glitecfg/vo.d --outfile glitecfg/site-info.def

############################################
# Now, from the default BDII               #
# get the wms data for each VO you support #
############################################

# First, define the default BDIIs (in $BDII_LIST)
. template/site-info.def

vos='"'
# Now go over your vos, getting their WMS server lists
for v in `cat myvos.txt`; do
  vos=${vos}$v" "
  list=`echo $(lcg-infosites --is $BDII_LIST  --vo $v wms| sed s%https://%% | sed s%:7443/glite_wms_wmproxy_server%%)`
  if [ "x${list}" = "x" ]; then
    echo "No WMS found for $v"
  fi
  if [ -f glitecfg/vo.d/$v ]; then
    echo WMS_HOSTS=\"${list}\" >> glitecfg/vo.d/$v
  fi
done
vos=${vos}\"
cat template/site-info.def > glitecfg/site-info.def
echo VOS=$vos  >> glitecfg/site-info.def
chmod 700 glitecfg/site-info.def

############################################
# Copy the config into place               #
# and run yaim                             #
############################################
rsync -av glitecfg/ /root/glitecfg/

/opt/glite/yaim/bin/yaim -c -s /root/glitecfg/site-info.def -n UI

