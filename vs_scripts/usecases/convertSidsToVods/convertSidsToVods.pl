#!/usr/bin/perl
#----------------------------------------------------------------------------
# A tool to convert the records in a site-info.def into new, vo.d style files
#----------------------------------------------------------------------------

use Getopt::Long;
use strict;
use File::Basename;
use File::Temp qw/ tempfile /;

# Global options
my %parameter;

#-------------------------------------

# Read the options
initParams();

# Find vo.d dir
my $vodDir  = dirname($parameter{'SID'}) . '/vo.d/';
my $baseSid = basename $parameter{'SID'};

# Prepare to backup the current sid
my ($backupSidFh, $backupSid) = tempfile( "$baseSid.XXXXXX", CLEANUP => 0, SUFFIX => '.bak', DIR => dirname($parameter{'SID'}));

# Read the old SID, backing it up as you go
my @oldSidFile;
open(SID,"$parameter{'SID'}") or die("Could not open $parameter{'SID'}, $!");
while(<SID>) {
  push(@oldSidFile,$_) ;
  print $backupSidFh $_;
}
close(SID);
close($backupSidFh);

my @newSidFile;
my %vodArrays;

# Split the file into a new sid-info.def, that contains no VO_ variables (bar VO_SW_DIR),
# and set of new individual files for each VOD

foreach my $l (@oldSidFile) {
  if (($l =~ /^VO_/) and ($l !~ /^VO_SW_DIR/)) {
    # Hm... SIDs style VOMS Record
    $l =~ /^VO_([^_]*)_/;
    my $vo = $1;
    if (!(defined($vodArrays{$vo}))) {
      $vodArrays{$vo} = [];
    }
    push(@{$vodArrays{$vo}},$l);
  }
  else {
    push (@newSidFile, $l);
  } 
}

# Make sure VOs are not defined twice
my $okToGo = 1;
my $count = 0;
foreach my $k (keys(%vodArrays)) {
  my $voName = lc($k);
  $count++;
  if (-f $vodDir . $voName) {
    print("The vo.d directory for VO " . $vodDir . $voName . " already exists\n");
    $okToGo = 0;
  }
}

die ("Clear up conflicts first. No changes made.\n") unless $okToGo;
die ("Nothing to do. No changes made.\n") unless $count > 0;

if ($parameter{'NOVODS'} == 0) {
  # Print out the new VODs
  foreach my $k (keys(%vodArrays)) {
    my $voName = lc($k);
    my $f = $vodDir . $voName;
    open(VOD,">$f") or die("Unable to open VOD file $f, $!");
    my @vodLines = @{$vodArrays{$k}}; 
    foreach my $l (@vodLines) {
      $l =~ s/^VO_[A-Z0-9]+_//;
      print VOD $l;
    }
    close(VOD);
  }
}

if ($parameter{'KEEPSIDS'} == 0) {
  # Print out the new SID
  open(SID,"> $parameter{'SID'}") or die("Unable to open $parameter{'SID'}, $!\n");
  foreach my $l (@newSidFile) {
    print SID $l;
  }
}
close(SID);

#---------------------------------------------
# Read the command line options
#---------------------------------------------
sub initParams() {

  # Can accept a set of log files

  # Read the options
  GetOptions ('h|help'         => \$parameter{'HELP'},
              'sid:s'          => \$parameter{'SID'} ,
              'keepsids:i'     => \$parameter{'KEEPSIDS'} ,
              'novods:i'       => \$parameter{'NOVODS'} ,
             );

  if (defined($parameter{'HELP'})) {
    print <<TEXT;

Abstract: A tool to convert the records in a site-info.def into new, vo.d style files

  -h              Prints this help page
  -sid   file     Name of site-info.def file

TEXT
    exit(0);
  }

  die ("You must give a sid parameter\n") unless (defined($parameter{'SID'}));
  die ("site-info.def file must exist\n") unless ( -f     $parameter{'SID'} );
  die ("Directory where site-info.def file resides must contain a vo.d directory as well\n") unless ( -d dirname($parameter{'SID'}) . "/vo.d" );

  if (!(defined($parameter{'KEEPSIDS'}))) {
    $parameter{'KEEPSIDS'} = 0;
  }

  if (!(defined($parameter{'NOVODS'}))) {
    $parameter{'NOVODS'} = 0;
  }
}

#------- end -------


