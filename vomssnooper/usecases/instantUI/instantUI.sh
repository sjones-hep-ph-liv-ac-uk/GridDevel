#!/bin/bash

##########################################################################
# Script to quickly setup a UI                                           #
# sj, 22 Nov 2013                                                        #
##########################################################################

PATH=/opt/GridDevel/bin:$PATH

# Must be run as root
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 
   exit 1
fi

# Must have emi-ui package
if [[ "$(rpm -q emi-ui)" =~ "not installed" ]]; then
  echo "emi-ui package should be installed"
  exit 1
fi

# Must have VomsSnooper package
if [[ "$(rpm -q VomsSnooper)" =~ "not installed" ]]; then
  echo "VomsSnooper package should be installed"
  exit 1
fi


rm -rf glitecfg/vo.d; rm -rf glitecfg/site-info.def; 
mkdir -p glitecfg/vo.d

############################################
# Section 1 - use the CIC portal XML to    # 
# make the newest VOMS records             #
############################################

# Get the newest VOID XML
wget -O VOIDCardInfo.xml http://operations-portal.egi.eu/xml/voIDCard/public/all/true

# Convert it into vod format
vomsSnooper.sh --xmlfile VOIDCardInfo.xml  --myvos myvos.txt --vodfile myvos.txt --voddir glitecfg/vo.d --outfile glitecfg/site-info.def

############################################
# Section 2 - now, from the default BDII   #
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
# Section 3 - Copy the config into place   #
# and run yaim                             #
############################################

rsync -av glitecfg/ /root/glitecfg/

/opt/glite/yaim/bin/yaim -c -s /root/glitecfg/site-info.def -n UI

