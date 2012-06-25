#!/usr/bin/perl

#---------------------------------------------
# Tool to update a site-info.def file.
#
# This tool takes three files as parameters.
#
# One of them, --oldsid, identifies the location of
# an existing site-info.def file.
#
# Another, --deltas, identifies the location of 
# changes to site-info.def that will be merged into the old one.
#
# The final parameter, --newsid, gives the
# location where the results will get written.
#
# 25/06/12, sj, init version
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

my @oldLines;
open(OLDFILE,$parameter{'OLDSID'}) or die("No open $parameter{'OLDSID'} ");
my $assemble='';      # Var to handle (join) split lines
while(<OLDFILE>) {
  if (/\\$/) {
    my $tmp = $_;
    chomp($tmp);
    $tmp =~ s/\\$//;
    $assemble = $assemble . $tmp;
  }
  else {
    $assemble = $assemble . $_;
    push (@oldLines,$assemble) ;
    $assemble = '';
  }
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

  if (! (-f $parameter{'NEWSID'})) { 
    die ("You must give a  --newsid option\n");
  }
}

