#!/usr/bin/perl

use Getopt::Long;
use strict;
use File::Basename;

# Functions

# Global options
my %parameter;
my %cswdir = ();

# Get the options

initParams();

# File locations
my $sid = $parameter{'SID'};
my $dir = dirname ($parameter{'SID'}) . "/vo.d/";

# Read the VODs
opendir (my($vodHandle), "$dir") or die ("Couldn't open dir '$dir': $!");
my @vodFiles = grep { !/^\./ } readdir $vodHandle;
closedir $vodHandle;
foreach my $vodFile (@vodFiles) {

  # Add defaults to each VOD
  addVodDefaults($dir . "/" , $vodFile);
}

# Read the SIDs
open FILE, "$parameter{'SID'}" or die "Couldn't read file $parameter{'SID'}: $!";
binmode FILE;
my @lines = <FILE>;
close FILE;

my %sidVoGroups = ();
my @others;

# Prepare to comment out SIDs is required
my $firstCol = '';
if ( $parameter{'COMMENTSIDS'} == 1) {
  $firstCol = '#';
}

# Go over all the lines of SIDs
foreach my $l (@lines) {
  my $isOther = 1;

  # Collect up the SID records, organise by VO
  if ($l =~ /^VO_(.*)_SW_DIR=/) {
    if (!defined($sidVoGroups{$1})) {
      $sidVoGroups{$1} = [];
    }
    push(@{$sidVoGroups{$1}},$firstCol . $l);
    $isOther = 0;
  }

  if ($l =~ /^VO_(.*)_DEFAULT_SE=/) {
    if (!defined($sidVoGroups{$1})) {
      $sidVoGroups{$1} = [];
    }
    push(@{$sidVoGroups{$1}},$firstCol . $l);
    $isOther = 0;
  }

  if ($l =~ /^VO_(.*)_STORAGE_DIR=/) {
    if (!defined($sidVoGroups{$1})) {
      $sidVoGroups{$1} = [];
    }
    push(@{$sidVoGroups{$1}},$firstCol . $l);
    $isOther = 0;
  }

  if ($l =~ /^VO_(.*)_VOMS_SERVERS=/) {
    if (!defined($sidVoGroups{$1})) {
      $sidVoGroups{$1} = [];
    }
    push(@{$sidVoGroups{$1}},$firstCol . $l);
    $isOther = 0;
  }

  if ($l =~ /^VO_(.*)_VOMSES=/) {
    if (!defined($sidVoGroups{$1})) {
      $sidVoGroups{$1} = [];
    }
    push(@{$sidVoGroups{$1}},$firstCol . $l);
    $isOther = 0;
  }

  if ($l =~ /^VO_(.*)_VOMS_CA_DN=/) {
    if (!defined($sidVoGroups{$1})) {
      $sidVoGroups{$1} = [];
    }
    push(@{$sidVoGroups{$1}},$firstCol . $l);
    $isOther = 0;
  }

  # It's something else, not a VO records
  if ($isOther) {
    push(@others,$l);
  }
}

# Go over each group of SIDs (for a certain VO)
foreach my $k (keys(%sidVoGroups)) {

  # Upper and lower case keys are both used
  my $lck = lc($k);

  # Add SW_DIR if necessary
  if ( ! contains("SW_DIR",@{$sidVoGroups{$k}}))   {

    # Check for special case if CVMFS
    if (defined($cswdir{$lck})) {
      push(@{$sidVoGroups{$k}},$firstCol . "VO_" . $k . "_SW_DIR=" . $cswdir{$lck} . "\n");
    }
    else {
      push(@{$sidVoGroups{$k}},$firstCol . "VO_". $k . "_SW_DIR=" . $parameter{'SWDIR'} . "/$lck\n");
    }
  }

  # Add DEFAULT_SE if necessary
  if ( ! contains("DEFAULT_SE",@{$sidVoGroups{$k}}))   {
    push(@{$sidVoGroups{$k}}, $firstCol . "VO_" . $k . "_DEFAULT_SE=" . $parameter{'DEFSE'} . "\n"  );
  }

  # Add STORAGE_DIR if necessary
  if ( ! contains("STORAGE_DIR",@{$sidVoGroups{$k}}))   {
    push(@{$sidVoGroups{$k}}, $firstCol . "VO_" . $k . "_STORAGE_DIR=" . $parameter{'SPATH'} .  "/$lck\n" );
  }

  # Now write the material back
  open FILE, ">$parameter{'SID'}" or die "Couldn't read file $parameter{'SID'}: $!";


  # First, print the other "records"
  my $blanks = 0;
  foreach my $l (@others) {

    # Suppress multiple blanks
    if ($l =~ /^\s*$/) {
      $blanks++;
    }
    else {
      $blanks = 0;
    }
    print FILE $l unless $blanks > 1;
  }

  # Print out the groups for each VO
  foreach my $k (sort(keys(%sidVoGroups))) {
    foreach my $l (@{$sidVoGroups{$k}}) {
      print FILE $l;
    }
    print FILE "\n";
  }
  close FILE;
}

#---------------------------------------------------
# Funtion to add the defaults to the VOD files
#---------------------------------------------------
sub addVodDefaults() {

  # File location
  my $dir = shift();
  my $vf = shift();

  # Read the while file
  local $/ = undef;
  open FILE, "$dir/$vf" or die "Couldn't read file $vf: $!";
  binmode FILE;
  my $string = <FILE>;
  close FILE;

  # Check the file
  die ("For VO $vf, VOMS_CA_DN record not found\n")unless ($string =~ /VOMS_CA_DN/);
  die ("For VO $vf, VOMS_SERVERS record not found\n")unless ($string =~ /VOMS_SERVERS/);
  die ("For VO $vf, VOMSES record not found\n")unless ($string =~ /VOMSES/);

  # Add SW_DIR if necessary.
  if ($string !~ /SW_DIR/) {
    # Check for special case; cvmfs
    if (defined($cswdir{$vf})) {
      $string .= "SW_DIR=" . $cswdir{$vf} . "\n";
    }
    else {
      $string .= "SW_DIR=" . $parameter{'SWDIR'} . "/$vf\n";
    }
  }

  # Add DEFAULT_SE if necessary.
  if ($string !~ /DEFAULT_SE/) {
    $string .= "DEFAULT_SE=" . $parameter{'DEFSE'} . "\n";
  }

  # Add STORAGE_DIR if necessary.
  if ($string !~ /STORAGE_DIR/) {
    $string .= "STORAGE_DIR=" . $parameter{'SPATH'} .  "/$vf\n";
  }

  # Write the file back
  open(FILE,"> $dir/$vf") or die("Couldn't write file $vf: $!") ;
  print FILE $string;
  close(FILE);

}

#--------------------------------------------
# Read the command line options
#--------------------------------------------
sub initParams() {

  $parameter{'CSWDIR'} = [];

  # Read the options
  GetOptions (  'h|help'       =>   \$parameter{'HELP'},
                'sid:s'        =>   \$parameter{'SID'} ,
                'swdir:s'      =>   \$parameter{'SWDIR'} ,
                'cswdir:s'     =>    $parameter{'CSWDIR'} ,
                'spath:s'      =>   \$parameter{'SPATH'} ,
                'defse:s'      =>   \$parameter{'DEFSE'} ,
                'commentsids:i' =>  \$parameter{'COMMENTSIDS'} ,
              );

  if (defined($parameter{'HELP'})) {
    print <<TEXT;

Abstract: Tool to update the default values in siD/VODs.

  -h                    Prints this help page
  -sid          file    Location of site-info.def
  -swdir        dir     Standard software directory (max of 1)
  -cswdir       spec    Cvmfs software dir, in the form vo:dir (multiple)
  -spath        dir     Storage path
  -defse        se      Default SE
  -commentsids  0/1     Comment out the SIDs


 20/02/13, sj, init version

TEXT
    exit(0);
  }

  # Validate the args
  die ("You must give the -sid param\n") unless defined($parameter{'SID'});
  die ("You must give the -swdir param\n") unless defined($parameter{'SWDIR'});
  die ("You must give the -spath param\n") unless defined($parameter{'SPATH'});
  die ("You must give the -defse param\n") unless defined($parameter{'DEFSE'});

  if (!(defined($parameter{'COMMENTSIDS'}))) {
    $parameter{'COMMENTSIDS'} = 0;
  }

  foreach my $spec (@{$parameter{'CSWDIR'}}) {
    my @parts = split(":",$spec);
    if ($#parts != 1) {
      die ("Poor cvmfs spec; must be vo:/cvmfs/path");
    }
    $cswdir{$parts[0]} =  $parts[1];
  }
}

#--------------------------------------------
# Check if an array contains a certain string
#--------------------------------------------
sub contains () {
  my $string = shift();
  my @array  = @_;
  foreach my $l (@array) {
    if ($l =~ /.*$string.*/) {
      return 1;
    }
  }
  return 0;
}

#--------------------------------------------

