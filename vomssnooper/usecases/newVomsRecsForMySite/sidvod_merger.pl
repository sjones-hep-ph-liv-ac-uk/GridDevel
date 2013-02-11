#!/usr/bin/perl

#---------------------------------------------
# Tool to update SIDs and VODs.
#
# This tool takes three files as parameters.
#
# One of them, --oldsid, identifies the location of
# an existing site-info.def file. It is assumed that a
# vo.d dir lies with it.
#
# Another, --deltas, identifies the location of 
# changes to site-info.def and vo.d dir that will be 
# merged into the old ones.
#
# The final parameter, --newsid, gives the
# location where the results will get written.
#
# If deltas are found for a VOD that does not yet
# exist, the VOD is printed out with generated fields.
#
# 17/01/12, sj, init version
#
#---------------------------------------------

use Getopt::Long;
use strict;
use File::Basename;
use File::Copy;

# Global options
my %parameter;

#-------------------------------------
# Read the options
initParams();

#-------------------------------------
# Deal with the SID file
processSidFile ();

#-------------------------------------
# Deal with the VOD files
processVodFiles();

#-------------------------------------
#- SUBROUTINES
#-------------------------------------
sub processVodFiles () {

  my $vodDir = dirname ($parameter{'OLDSID'})   . '/vo.d/';
  my $deltaDir = dirname($parameter{'DELTAS'})  . '/vo.d/';
  my $newVodDir = dirname($parameter{'NEWSID'}) . '/vo.d/';

  my %oldVodHash = ();
  my %deltasHash = ();
  
  opendir (my($oldVodDirHandle), "$vodDir") or die ("Couldn't open dir '$vodDir': $!");
  my @oldVodFiles = grep { !/^\./ } readdir $oldVodDirHandle; 
  closedir $oldVodDirHandle;
  foreach my $vodFile (@oldVodFiles) {
    $oldVodHash{$vodFile} = 1;
  }

  opendir (my($deltasDirHandle), "$deltaDir") or die ("Couldn't open dir '$deltaDir': $!");
  my @deltaFiles = grep { !/^\./ } readdir $deltasDirHandle; 
  closedir $deltasDirHandle;
  foreach my $deltasFile (@deltaFiles) {
    $deltasHash{$deltasFile} = 1;
  }

  # Only update old vod files for which delta files exists
  # If old vod file exists for which delta exists, update it.
  # If a delta file is provided for which no old vod exists, use it directly, but warn.
  # If an old vod file exists for which no delta exists, warn.

  foreach my $deltasFile (@deltaFiles) {
    if (defined($oldVodHash{$deltasFile})) {
      #  an old vod exists - it will be a merge
      print("Merging $deltasFile\n");
      mergeVod("$vodDir","$deltaDir",$deltasFile,$newVodDir);
      $oldVodHash{$deltasFile} = 2;
    }
    else {
      # no old vod exists - it will be a copy and warning
      print("Warning: No old file exists for $deltaDir/$deltasFile. Please check new VO, $newVodDir/$deltasFile\n");
      createVod("$deltaDir",$deltasFile,$newVodDir);
      #### copy("$deltaDir/$deltasFile","$newVodDir/$deltasFile") or die "Copy failed: $!";
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
  my $deltasDir = shift;
  my $file      = shift;
  my $newVodDir = shift;
  my @oldVodLines = ();
  my @deltasLines = ();

  open(my($vh),"$oldVodDir/$file") or die("Could not open $oldVodDir/$file: $!");
  while(<$vh>) {
    push(@oldVodLines,$_);
  }
  close($vh);

  open(my($dh),"$deltasDir/$file") or die("Could not open $deltasDir/$file: $!");
  while(<$dh>) {
    push(@deltasLines,$_);
  }
  close($dh);

  my %deltasHash = ();
  foreach my $dline (@deltasLines) {
    if ($dline =~ /^([0-9A-Za-z\_\-]+)\=(.*)/) {
      my $name = $1; my $payload = $2;
      $deltasHash{$name} = $payload;
    }
  }

  # Go over the lines in the old file, looking for matches with
  # the deltas hash

  my $olCount = scalar(@oldVodLines);
  for (my $jj = 0; $jj <=  $olCount ; $jj++ ) {
    my $l = $oldVodLines[$jj];
    if ($l =~ /^([0-9A-Za-z\_\-]+)\=(.*)/) {
      my $name = $1; my $payload = $2;

      # Here's where I do the match. If I find one, update the record with the deltas data.
      if (defined($deltasHash{$name})) {
        $oldVodLines[$jj]  = $name . '=' . $deltasHash{$name} . "\n";
      }
    }
  }

  open(my ($ns),">$newVodDir/$file") or die("Could not open new vod $newVodDir/$file\n");
  foreach my $l (@oldVodLines) {
    print  ($ns $l);
  }
  close($ns);
}
#-------------------------------------
sub createVod() {
  my $deltasDir   = shift;
  my $voFile  = shift;
  my $newVodDir  = shift;
  my @newLines = ();

  open(my($dh),"$deltasDir/$voFile") or die("Could not open $deltasDir/$voFile: $!");
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
#-------------------------------------
sub processSidFile () {

  my @oldLines;
  open(OLDFILE,$parameter{'OLDSID'}) or die("No open $parameter{'OLDSID'} ");
  while(<OLDFILE>) {
    push (@oldLines,$_);
  }

  close (OLDFILE);
  
  my @deltaLines;
  open(DELTAFILE,$parameter{'DELTAS'}) or die("No open $parameter{'DELTAS'}");
  my $assemble = '';    # Var to handle (join) split lines
  while(<DELTAFILE>) {
    if (/\\$/) {
      my $tmp = $_;
      chomp($tmp);
      $tmp =~ s/\\$//;
      $assemble = $assemble . $tmp;
    }
    else {
      $assemble = $assemble . $_;
      push (@deltaLines ,$assemble) ;
      $assemble = '';
    }
  }
  close (DELTAFILE);
  
   
  # Make the deltas data into a hash - indexes are easier to program. 
  my %deltasHash;
  foreach my $dline (@deltaLines) {
    if ($dline =~ /^([0-9A-Za-z\_\-]+)\=(.*)/) {
      my $name = $1; my $payload = $2;
      $deltasHash{$name} = $payload;
    }
  }
  
  my $olCount = scalar(@oldLines);
   
  # Go over the lines in the old file, looking for matches with
  # the deltas hash
   
  for (my $jj = 0; $jj <=  $olCount ; $jj++ ) {  
    my $l = $oldLines[$jj];
    if ($l =~ /^([0-9A-Za-z\_\-]+)\=(.*)/) {
      my $name = $1; my $payload = $2;
  
      # Here's where I do the match. If I find one, update the record with the deltas data.
      if (defined($deltasHash{$name})) {
        $oldLines[$jj]  = $name . '=' . $deltasHash{$name} . "\n";  
      }
    }
  }
  
  # Now that all the data from the deltas records has been merged into the
  # old data, write the merged data out. 
  
  open(NEWSID,">$parameter{'NEWSID'}") or die("Could not open new sid $parameter{'NEWSID'}");
    
  foreach my $l (@oldLines) {
    print NEWSID  $l;
  }
  close(NEWSID);
}
  
#--------------------------------------------
# Read the command line options
#--------------------------------------------
sub initParams() {
  
  # Read the options
  GetOptions ('h|help'        =>   \$parameter{'HELP'},
              'o|oldsid:s'    =>   \$parameter{'OLDSID'} ,
              'd|deltas:s'    =>   \$parameter{'DELTAS'} ,
              'n|newsid:s'    =>   \$parameter{'NEWSID'} ,
              );
  
  if (defined($parameter{'HELP'})) {
    print <<TEXT;

Abstract: this can be used to merge a set of deltas from an old site-info.def
to a new site-info.def.

  -h  --help                  Prints this help page
  -o  --oldsid    file      location of old site-info.def file
  -n  --newsid    file      location of new site-info.def file
  -d  --deltas    file      location of new deltas site-info.def file

This tool takes three files as parameters.

One of them, --oldsid, identifies the location of
an existing site-info.def file.

Another, --deltas, identifies the location of a
changes to site-info.def that will be merged into the old one.

The final parameter, --newsid, gives the
location where the results will get written.

If deltas are found for a VOD that does not yet
exist, the VOD is printed out with generated fields.

25/06/12, sj, init version

TEXT
    exit(0);
  }

  # Validate the args

  if (! (-f $parameter{'OLDSID'})) { 
    die ("You must give an --oldsid option\n");
  }

  if (! (-f $parameter{'DELTAS'})) { 
    die ("You must give an --deltas option\n");
  }

  if (! (defined($parameter{'NEWSID'}))) { 
    die ("You must give a  --newsid option\n");
  }
}

