#!/usr/bin/perl

use strict;

my %fqansForVos;

while(<>) {
  my $l = $_;
  chomp($l);
  $l =~ s/^\s*//; $l =~ s/\s*$//;
  $l = '"' . $l . '"';


  $l =~ /^"\/([A-Za-z0-9\-\_\.]+)\/?/;
  die ("Weird line, $l") unless(defined($1));
  my $vo = $1;
  if (!(defined($fqansForVos{$vo}))) {
    $fqansForVos{$vo} = [];
  }
  push(@{$fqansForVos{$vo}},$l);
}

my %uniqLines = ();
foreach my $vo (sort(keys(%fqansForVos))) {
  my @lines = @{$fqansForVos{$vo}};
  foreach my $l (@lines) {
    $l =~ s/Role=/ROLE=/;
    $l =~ s/\/ROLE=NULL//;
 
    # Mandatory ones 
    if ($l =~ /\"\/$vo\"$/) { $l = $l . '::::'; }
    elsif ($l =~ /ROLE\=pilot/) { $l = $l . ':::pilot:'; }
    elsif ($l =~ /ROLE\=production/) { $l = $l . ':::prd:'; }
    elsif ($l =~ /ROLE\=lcgadmin/) { $l = $l . ':::sgm:'; }

    # Some optional ones (heuristic)
    elsif ($l =~ /ROLE\=VO-Admin/i)        { $l = $l . ':::sgm:'; }
    elsif ($l =~ /ROLE\=swadmin/i)         { $l = $l . ':::sgm:'; }
    elsif ($l =~ /ROLE\=SoftwareManager/i) { $l = $l . ':::sgm:'; }
    elsif ($l =~ /ROLE\=.*production/i)    { $l = $l . ':::prd:'; }
    elsif ($l =~ /\"\/$vo\/production/i)   { $l = $l . ':::prd:'; }
    elsif ($l =~ /\"\/$vo\/lcgprod/i)      { $l = $l . ':::prd:'; }

    # Feed it to catchall
    else                                   { $l = $l . '::::'; }

    $uniqLines{"$l"} = 1;
  }
  # All VOs need these - catch alls and a lcgprod mapping
  $uniqLines{"\"/$vo\"::::"} = 1;
  $uniqLines{"\"/$vo/*\"::::"} = 1;
  $uniqLines{"\"/$vo/sgm\":::sgm:"} = 1;
  $uniqLines{"\"/$vo/lcgprod\":::prd:"} = 1;
  $uniqLines{"\"/$vo/ROLE=production\":::prd:"} = 1;
  $uniqLines{"\"/$vo/ROLE=lcgadmin\":::sgm:"} = 1;

}

foreach my $l (keys(%uniqLines)) {
  print("$l\n");
}

# "/atlas/sgm":::sgm:
# "/atlas/lcgprod":::prd:
# "/atlas"::::
# "/atlas/*":::: 
# "/atlas/ROLE=lcgadmin":::sgm:
# "/atlas/ROLE=production":::prd:
# "/atlas/ROLE=pilot":::pilot:

