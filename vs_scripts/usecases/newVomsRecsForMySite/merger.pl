#!/usr/bin/perl

#---------------------------------------------
# Tool to update a site-info.def file.
#
# This tool takes three directories for parameters.
#
# One of them, --oldsiddir, identifies the location of
# an existing site-info.def file and vo.d directory.
# This is an input.
#
# Another, --deltadir, identifies the location of a
# new set of site-info.def and/or vo.d files
# that will be merged into the old ones.
#
# The final parameter, --newsiddir, gives the
# location where the results will get written.
#
# I'm sure I can do this is a more simple way!
#
# 15/05/12, sj, init version
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

my %hashOfOldFiles;   # array to hold the lines of each file

# Get the right files from the old sid dir

open (FINDOLDFILES,"find $parameter{'OLDSIDDIR'} -name '*' -type f|") or die("Could not find old files\n");
while(<FINDOLDFILES>) {
  my $fileName = $_;
  chomp($fileName);

  next unless ! /\/services\//;
  next unless ! /\/.svn\//;

  if (/\/vo.d\//) {
    $hashOfOldFiles{$fileName} = [];   
  }
  else {
    if (/site-info.def/) {
      $hashOfOldFiles{$fileName} = [];
    }
  }
}
close(FINDOLDFILES);

# Now read those files in

foreach my $k (keys( %hashOfOldFiles)) {
  open(OLDFILE,$k) or die("No open $k ");
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
      push (@{$hashOfOldFiles{$k}},$assemble) ;
      $assemble = '';
    }
  }
  close (OLDFILE);
}


my %hashOfDeltaFiles;  # array to hold the lines of each file

# Get the right files from the delta sid dir

open (FINDDELTAFILES,"find $parameter{'DELTADIR'} -name '*' -type f|") or die("Could not find delta files\n");
while(<FINDDELTAFILES>) {
  my $fileName = $_;
  chomp($fileName);

  next unless ! /\/services\//;
  next unless ! /\/.svn\//;

  if (/\/vo.d\//) {
    $hashOfDeltaFiles{$fileName} = [];
  }
  else {
    if (/site-info.def/) {
      $hashOfDeltaFiles{$fileName} = [];
    }
  }
}
close(FINDDELTAFILES);

# Now read those files in

foreach my $k (keys( %hashOfDeltaFiles)) {
  open(DELTAFILE,$k) or die("No open $k ");
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
      push (@{$hashOfDeltaFiles{$k}},$assemble) ;
      $assemble = '';
    }
  }
  close (DELTAFILE);
}

my @oldKeysInOrder ;   # This is used to link the old files to the delta ones
my $oldFileBases = '';
foreach my $k (sort (keys( %hashOfOldFiles))) {
  push(@oldKeysInOrder,$k);
  $oldFileBases = $oldFileBases . basename($k) . ', ';
}
  
my @deltaKeysInOrder; # This is used to link the old files to the delta ones
my $deltaFileBases = '';

# Ensure that every file in the old dir is matched by one in the delta dir
foreach my $k (sort (keys( %hashOfDeltaFiles))) {
  push(@deltaKeysInOrder,$k);
  $deltaFileBases = $deltaFileBases . basename($k) . ', ';;
}
my @oldKeysInOrder = sort (keys( %hashOfOldFiles));
my @deltaKeysInOrder = sort (keys( %hashOfDeltaFiles));
  
# For every old file, you need a new file, and vice versa.
die ("Incompatible - different files in each repo\n$oldFileBases\n$deltaFileBases\n") unless ($oldFileBases eq $deltaFileBases);

 # Now go over the old files 
for (my $ii = 0; $ii <= $#oldKeysInOrder; $ii++) {
  
  my $deltaKey = $deltaKeysInOrder[$ii];
  my @deltaLines = @{$hashOfDeltaFiles{$deltaKey}};
 
  # Make the delta data into a hash - indexes are easier to program. 
  my %deltaHash;
  foreach my $dline (@deltaLines) {
    if ($dline =~ /^([0-9A-Za-z\_\-]+)\=(.*)/) {
      my $name = $1; my $payload = $2;
      $deltaHash{$name} = $payload;
    }
  }

  # Now get the lines fro the old file
  my $oldKey = $oldKeysInOrder[$ii];
  my @oldLines = @{$hashOfOldFiles{$oldKey}};
  my $olCount = scalar(@{$hashOfOldFiles{$oldKey}});
 
  # Go over the lines in the old file, looking for matches with
  # the delta hash
 
  for (my $jj = 0; $jj <=  $olCount ; $jj++ ) {  
    my $l = ${$hashOfOldFiles{$oldKey}}[$jj];
    if ($l =~ /^([0-9A-Za-z\_\-]+)\=(.*)/) {
      my $name = $1; my $payload = $2;

      # Here's where I do the match. If I find one, update the record with the delta data.
      if (defined($deltaHash{$name})) {
        ${$hashOfOldFiles{$oldKey}}[$jj]  = $name . '=' . $deltaHash{$name} . "\n";  
      }
    }
  }
}

# Now that all the data from the delta records has been merged into the
# old data, write the merged data out. This is done by just
# switching the directory.
  
foreach my $k (keys( %hashOfOldFiles)) {
  # Convert the path to the old dir to a path in the new dir
  my $newPath = $k;
  $newPath =~ s/$parameter{'OLDSIDDIR'}/$parameter{'NEWSIDDIR'}/;
  print("Printing $newPath\n");
  my @lines = @{$hashOfOldFiles{$k}};
  open(NEWPATH,">$newPath") or die("Can't write $newPath");
  foreach my $l (@lines) {
    print NEWPATH $l;
  }
  close(NEWPATH);
}
  
#--------------------------------------------
# Read the command line options
#--------------------------------------------
sub initParams() {
  
  # Read the options
  GetOptions ('h|help'           =>   \$parameter{'HELP'},
              'o|oldsiddir:s'    =>   \$parameter{'OLDSIDDIR'} ,
              'n|newsiddir:s'    =>   \$parameter{'NEWSIDDIR'} ,
              'd|deltadir:s'     =>   \$parameter{'DELTADIR'} ,
              
              );
  
  if (defined($parameter{'HELP'})) {
    print <<TEXT;

Abstract: this can be used to merge a set of deltas from an old site-info.def
to a new site-info.def, inc. vo.d files.

  -h  --help                  Prints this help page
  -o  --oldsiddir    dir      location of old site-info.def file
  -n  --newsiddir    dir      location of new site-info.def file
  -d  --deltadir     dir      location of new delta site-info.def file

Tool to update a site-info.def file.

This tool takes three directories for parameters.

One of them, --oldsiddir, identifies the location of
an existing site-info.def file and vo.d directory.
This is an input.

Another, --deltadir, identifies the location of a
new set of site-info.def and/or vo.d files
that will be merged into the old ones.

The final parameter, --newsiddir, gives the
location where the results will get written.

15/05/12, sj, init version

TEXT
    exit(0);
  }

  # Validate the args

  if (! (-d $parameter{'OLDSIDDIR'})) { 
    die ("You must give an --oldsiddir option\n");
  }
  else { 
    if (! ( -f ($parameter{'OLDSIDDIR'} . '/site-info.def'))) {
      die ("oldsiddir must be a dir\n");
    }
    if (! ( -d ($parameter{'OLDSIDDIR'} . '/vo.d'))) {
      die ("oldsiddir must has a vo.d dir\n");
    }
  }

  if (! (-d $parameter{'DELTADIR'})) { 
    die ("You must give an --deltadir option\n");
  }
  else { 
    if (! ( -f ($parameter{'DELTADIR'} . '/site-info.def'))) {
      die ("deltadir must have a site-info.def\n");
    }
    if (! ( -d ($parameter{'DELTADIR'} . '/vo.d'))) {
      die ("deltadir  must has a vo.d dir\n");
    }
  }

  if (! (-d $parameter{'NEWSIDDIR'})) { 
    die ("You must give a  --newsiddir option\n");
  }
  else { 
    if (! ( -d ($parameter{'NEWSIDDIR'} . '/vo.d'))) {
      die ("newsiddir must has a vo.d dir\n");
    }
  }

  # Put in some slashes, to make sure
  $parameter{'OLDSIDDIR'} .= '/';
  $parameter{'NEWSIDDIR'} .= '/';
  $parameter{'DELTADIR'} .= '/';
}


