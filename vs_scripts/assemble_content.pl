#!/usr/bin/perl

use strict;

die ("Give a directory where a site-info.def file exists!") unless ($#ARGV == 0);

my $sidDir = $ARGV[0];

die ("No such dir") unless (-d  $sidDir);
die ("No site-info.def") unless (-f  "$sidDir/site-info.def");
die ("No vo.d in there") unless (-d  "$sidDir/vo.d/");

open(SID,"$sidDir/site-info.def") or die("No opne site-info.def");

#VO_ZEUS_VOMS_SERVERS="'vomss://grid-voms.desy.de:8443/voms/zeus?/zeus' "
#VO_ZEUS_VOMSES="'zeus grid-voms.desy.de 15112 /C=DE/O=GermanGrid/OU=DESY/CN=host/grid-voms.desy.de zeus' "
#VO_ZEUS_VOMS_CA_DN="'/C=DE/O=GermanGrid/CN=GridKa-CA' "

my @lines;
while(<SID>) {
  if (/VO_.*_VOMS_SERVERS/) { 
    push (@lines, "\n\n''' site-info.def version (sid) '''\n");
    push (@lines, "<pre><nowiki>\n");

    push (@lines,$_); 
  }
  if (/VO_.*_VOMSES/) { push (@lines,$_); }
  if (/VO_.*_VOMS_CA_DN/) { 
    push (@lines,$_); 
    push (@lines, "</nowiki></pre>\n");


    # Now get the vo.d version
    s/#\s*//;
    s/VO_//;
    s/_.*//;
    my $name = lc($_);
    chomp($name);
    open(VOD,"$sidDir/vo.d/$name") or die("Unable to find $sidDir/vo.d/$name");
    push (@lines, "''' vo.d version (vod)'''\n");
    push (@lines, "<pre><nowiki>\n");
    
    while (<VOD>) {
      push (@lines,$_);
    }
    push (@lines, "</nowiki></pre>\n");
    foreach my $l (@lines) { print "$l"; }
    $#lines = -1;
  }
}
close(SID);







