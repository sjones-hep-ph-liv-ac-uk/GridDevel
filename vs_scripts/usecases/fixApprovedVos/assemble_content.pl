#!/usr/bin/perl
#---------------------------------------------
# Assemble some SID-VOD records for inclusion in the Approved VOs document
#
# Notes:
# The contract for this software:                     
# A directory must give given that contains a site-info.def with records
# for 1 or more VOs. The records must be for VOMS_SERVERS, VOMSES and VOMS_CA_DN.
# For each VO, another semantically identical set of records must also
# exist in a vo.d subdirectory.
#
# There is no error handling - if the contract is broken, the script dies.
#
# The script will merge these records into a format that is suitable for
# adding them (by hand) to https://www.gridpp.ac.uk/wiki/GridPP_approved_VOs
#
# In due course, a script will be provided to merge these records into the
# content without manual edits.
#
# 28/05/12, sj, initial version
#
#---------------------------------------------
use strict;

use Getopt::Long;

# Global options
my %parameter;

#-------------------------------------

# Read the options
initParams();

# Read site-info.def yaim variables
open(SID,"$parameter{'DIR'}/site-info.def") or die("No open site-info.def\n");

my @lines;

my %voData;
my @voList;
while(<SID>) {

  # Get the sid version
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
    chomp();
    s/#\s*//; # get rid of poss comment (silly sids)
    s/VO_//;
    s/_.*//;
    my $ucName = $_;
    my $lcName = lc($ucName);
    chomp($lcName);
    open(VOD,"$parameter{'DIR'}/vo.d/$lcName") or die("Unable to find $parameter{'DIR'}/vo.d/$lcName\n");
    push (@lines, "''' vo.d version (vod)'''\n");
    push (@lines, "<pre><nowiki>\n");
    
    while (<VOD>) {
      push (@lines,$_);
    }
    push (@lines, "</nowiki></pre>\n");

    # Print out the guff for this VO
    push (@voList,$ucName);
    $voData{$ucName} = [];
    push (@{$voData{$ucName}},"\n\n<!-- VOMS RECORDS for $ucName-->\n");
    foreach my $l (@lines) { 
      push (@{$voData{$ucName}},$l);
    }
    $#lines = -1;
  }
}
close(SID);

my @surrounds;
my $inSurrounds = 1;
my $currentVo = '';
my $endTagCount = 0;
open(OUTFILE,"> $parameter{'OUTFILE'}") or die("Unable to write to $parameter{'OUTFILE'}");
open(WIKI,"$parameter{'WIKIFILE'}") or die("Unable to open $parameter{'WIKIFILE'}\n");
while(<WIKI>) {
  my $line = $_;
  if (($line =~ /\{\{BOX VO\|([a-zA-Z0-9\_\-\.]*).*\|/) and (defined(@{$voData{$1}}))) {
    $currentVo = $1;
    $inSurrounds = 0;
    print OUTFILE $line;
    my @inserts = @{$voData{$currentVo}};
    foreach my $i (@inserts) {
      print(OUTFILE $i);
    }
  }
  else {
    if ($line =~ /^Notes:/) {
      $inSurrounds = 1;
    }
    if ($inSurrounds) {
      print OUTFILE $line;
    }
  }
}
close(WIKI);
close(OUTFILE);

#---------------------------------------------
# Read the command line options
#---------------------------------------------
sub initParams() {

  # Read the options
  GetOptions ('h|help'            =>   \$parameter{'HELP'},
              'd|dir:s'           =>   \$parameter{'DIR'} ,
              'wf:s'           =>      \$parameter{'WIKIFILE'} ,
              'of:s'           =>      \$parameter{'OUTFILE'} ,
              );

  if (defined($parameter{'HELP'})) {
    print <<TEXT;

Abstract: 
Assemble some SID-VOD records for inclusion in the Approved VOs document

  -h  --help                  Prints this help page
  -d  --dir        dir        Dir where site-info.def and vo.d directory reside
  -wf              wfile      Wiki file
  -of              ofile      Output file
TEXT
    exit(0);
  }

  if (!(defined($parameter{'OUTFILE'}))) {
    die ("Please give a output file .\n");
  }
  if (!(defined($parameter{'DIR'}))) {
    die ("Please give a directory.\n");
  }
  if (!(-d $parameter{'DIR'})) {
    die ("Please give an existing directory.\n");
  }

  if (!(-f "$parameter{'DIR'}/site-info.def")) {
    die ("Please give a directory where a site-info.def file exists.\n");
  }
  if (!(-d "$parameter{'DIR'}/vo.d")) {
    die ("Please give a directory where a vo.d sub-dir exists.\n");
  }

  if (!(-f "$parameter{'WIKIFILE'}")) {
    die ("Please give a file holding the wiki content.\n");
  }
}
