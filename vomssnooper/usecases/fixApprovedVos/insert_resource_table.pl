#!/usr/bin/perl
#---------------------------------------------
# 
#
# 28/05/12, sj, initial version
#---------------------------------------------

use strict;

use Getopt::Long;

# Global options
my %parameter;

#-------------------------------------

# Read the options
initParams();

my $resourceTable = readAll($parameter{'RESTABLE'});

open(WIKIFILE,"$parameter{'WIKIFILE'}") or die("Can't open wiki file, $parameter{'WIKIFILE'}, $!\n");
open(OUTFILE,">$parameter{'OUTFILE'}") or die("Can't open output wiki file, $parameter{'OUTFILE'}, $!\n");

my $printingFromFile = 1;
while(<WIKIFILE>) {
  if (/== VO Resource Requirements ==/) {
     $printingFromFile = 0;
     print OUTFILE $resourceTable;
  }
  if (/==VO enablement  ==/) {
     $printingFromFile = 1;
  }

  if ($printingFromFile) {
    print OUTFILE $_;
  }
  
}

close(OUTFILE);
close(WIKIFILE);

#---------------------------------------------
# Read a file into a string
#---------------------------------------------
sub readAll () {
  my $f = shift;
  local $/ = undef;
  open(FILE,$f) or die("Can't open resource table, $f, $!\n");
  my $content = <FILE>;
  close(FILE);
  return $content;
}

#---------------------------------------------
# Read the command line options
#---------------------------------------------
# ./insert_resource_table.pl -wf int.wiki.txt  -res res.txt -of new.wiki.txt 
sub initParams() {

  # Read the options
  GetOptions ('h|help'            =>   \$parameter{'HELP'},
              'wf:s'           =>      \$parameter{'WIKIFILE'} ,
              'res:s'           =>     \$parameter{'RESTABLE'} ,
              'of:s'           =>      \$parameter{'OUTFILE'} ,
              );

  if (defined($parameter{'HELP'})) {
    print <<TEXT;

Abstract: 
Insert the resources table

  -h  --help                  Prints this help page
  -wf              wfile      Wiki file
  -res             rfile      Resources table
  -of              ofile      Output file
TEXT
    exit(0);
  }

  if (!(-f "$parameter{'WIKIFILE'}")) {
    die ("Please give a file holding the wiki content.\n");
  }

  if (!(-f "$parameter{'RESTABLE'}")) {
    die ("Please give a file holding the resource table.\n");
  }

  if (!(defined($parameter{'OUTFILE'}))) {
    die ("Please give a output file .\n");
  }

}


