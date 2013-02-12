#!/usr/bin/perl

#---------------------------------------------
# Tool to update VODs.
#
# This tool takes 3 dirs as parameters.
#
# One of them, --oldvoddir, identifies the location of
# an existing, deployed vo.d dir. 
#
# Another, --deltavoddir, identifies the location of 
# changes to the vo.d dir that will be 
# merged into the deployed ones.
#
# The final parameter, --newvoddir, gives the
# location where the results will get written.
#
# If deltas are found for a VOD that does not yet
# exist, the new VOD is printed out with generated fields.
#
# 12/02/13, sj, init version
#
#---------------------------------------------

use Getopt::Long;
use strict;
use File::Basename;

# Global options
my %parameter;

#-------------------------------------
# Read the options
initParams();

#-------------------------------------
# Deal with the VOD files
processVodFiles();

#-------------------------------------
#- SUBROUTINES
#-------------------------------------
sub processVodFiles () {

  my $oldVodDir = $parameter{'OLDVODDIR'}   ;
  my $deltaVodDir = $parameter{'DELTAVODDIR'};
  my $newVodDir = $parameter{'NEWVODDIR'} ;

  my %oldVodHash = ();
  my %deltaVodHash = ();
  
  opendir (my($oldVodDirHandle), "$oldVodDir") or die ("Couldn't open dir '$oldVodDir': $!");
  my @oldVodFiles = grep { !/^\./ } readdir $oldVodDirHandle; 
  closedir $oldVodDirHandle;
  foreach my $vodFile (@oldVodFiles) {
    $oldVodHash{$vodFile} = 1;
  }

  opendir (my($deltaVodDirHandle), "$deltaVodDir") or die ("Couldn't open dir '$deltaVodDir': $!");

  my @deltaFiles = grep { !/^\./ } readdir $deltaVodDirHandle; 
  closedir $deltaVodDirHandle;
  foreach my $deltaFile (@deltaFiles) {
    $deltaVodHash{$deltaFile} = 1;
  }

  # If old vod file exists for which delta exists, update it.
  # If a delta file is provided for which no old vod exists, create it, but warn.
  # If an old vod file exists for which no delta exists, warn.


  foreach my $deltaFile (@deltaFiles) {
    if (defined($oldVodHash{$deltaFile})) {
      #  an old vod exists - it will be a merge
      print("Merging $deltaFile\n");
      mergeVod("$oldVodDir","$deltaVodDir",$deltaFile,$newVodDir);
      $oldVodHash{$deltaFile} = 2;
    }
    else {
      # no old vod exists - it will created
      print("Warning: No old file exists for $deltaVodDir/$deltaFile. Please check new VO, $newVodDir/$deltaFile\n");
      createVod("$deltaVodDir",$deltaFile,$newVodDir);
    }
  }
   
  # Final warnings
  foreach my $oldVodFile(sort(keys(%oldVodHash))) {
    if ( $oldVodHash{$oldVodFile} == 1) {
      print("Warning: Vod file for $oldVodFile not updated as no deltas found\n");
    }
  }
}

#-------------------------------------
sub mergeVod() {
  my $oldVodDir = shift;
  my $deltaVodDir = shift;
  my $voFile      = shift;
  my $newVodDir = shift;
  my @oldVodLines = ();
  my @deltaVodDir = ();

  open(my($vh),"$oldVodDir/$voFile") or die("Could not open $oldVodDir/$voFile: $!");
  while(<$vh>) {
    push(@oldVodLines,$_);
  }
  close($vh);

  open(my($dh),"$deltaVodDir/$voFile") or die("Could not open $deltaVodDir/$voFile: $!");
  while(<$dh>) {
    push(@deltaVodDir,$_);
  }
  close($dh);

  my %deltaVodHash = ();
  foreach my $dline (@deltaVodDir) {
    if ($dline =~ /^([0-9A-Za-z\_\-]+)\=(.*)/) {
      my $name = $1; my $payload = $2;
      $deltaVodHash{$name} = $payload;
    }
  }

  # Go over the lines in the old file, looking for matches with
  # the deltavoddir hash

  my $olCount = scalar(@oldVodLines);
  for (my $jj = 0; $jj <=  $olCount ; $jj++ ) {
    my $l = $oldVodLines[$jj];
    if ($l =~ /^([0-9A-Za-z\_\-]+)\=(.*)/) {
      my $name = $1; my $payload = $2;

      # Here's where I do the match. If I find one, update the record with the deltavoddir data.
      if (defined($deltaVodHash{$name})) {
        $oldVodLines[$jj]  = $name . '=' . $deltaVodHash{$name} . "\n";
      }
    }
  }
  open(my ($ns),">$newVodDir/$voFile") or die("Could not open new vod $newVodDir/$voFile\n");
  foreach my $l (@oldVodLines) {
    print  ($ns $l);
  }
  close($ns);
}
#-------------------------------------
sub createVod() {
  my $deltaVodDir   = shift;
  my $voFile  = shift;
  my $newVodDir  = shift;
  my @newLines = ();

  open(my($dh),"$deltaVodDir/$voFile") or die("Could not open $deltaVodDir/$voFile: $!");
  my $shortName ='';
  while(<$dh>) {
    my $line = $_;
    push(@newLines,$line);
    if ($line =~ /^VOMSES\=[\"\'\s]*([a-z0-9A-Z\_\-\.]+)\s/) {
      my $fullVo = $1;
      $fullVo =~ s/^vo\.//;
      $fullVo =~ s/\..*//;
      $shortName = $fullVo;
    }
  }
  close($dh);

  die("Error: Could not find right name for VO $voFile\n") unless length($shortName); 

  open(my ($ns),">$newVodDir/$voFile") or die("Could not open new vod $newVodDir/$voFile\n");
  print($ns "SW_DIR\=\$VO_SW_DIR/$shortName\n");
  print($ns "DEFAULT_SE\=\$DPM_HOST\n");
  print($ns "STORAGE_DIR\=\$STORAGE_PATH\/$shortName\n");

  foreach my $l (@newLines) {
    print  ($ns $l);
  }
  close($ns);

}
  
#--------------------------------------------
# Read the command line options
#--------------------------------------------
sub initParams() {
  
  # Read the options
  GetOptions ('h|help'           =>   \$parameter{'HELP'},
              'o|oldvoddir:s'    =>   \$parameter{'OLDVODDIR'} ,
              'd|deltavoddir:s'  =>   \$parameter{'DELTAVODDIR'} ,
              'n|newvoddir:s'    =>   \$parameter{'NEWVODDIR'} ,
              );
  
  if (defined($parameter{'HELP'})) {
    print <<TEXT;

Abstract: Tool to update VODs.

  -h  --help                  Prints this help page
  -o  --oldvoddir    file     Location of old vo.d dir
  -n  --newvoddir    file     Location of new vo.d dir
  -d  --deltavoddir  file     Location of new delta vo.d dir

 This tool takes 3 dirs as parameters.

 One of them, --oldvoddir, identifies the location of
 an existing, deployed vo.d dir. 

 Another, --deltavoddir, identifies the location of 
 changes to the vo.d dir that will be 
 merged into the deployed ones.

 The final parameter, --newvoddir, gives the
 location where the results will get written.

 If deltas are found for a VOD that does not yet
 exist, the new VOD is printed out with generated fields.

 12/02/13, sj, init version

TEXT
    exit(0);
  }

  # Validate the args

  if (! (-d $parameter{'OLDVODDIR'})) { 
    die ("You must give an --oldvoddir option\n");
  }

  if (! (-d $parameter{'DELTAVODDIR'})) { 
    die ("You must give an --deltavoddir option\n");
  }

  if (! (defined($parameter{'NEWVODDIR'}))) { 
    die ("You must give a  --newvoddir option\n");
  }
}

