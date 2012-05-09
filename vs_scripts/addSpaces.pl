#!/usr/bin/perl

use strict;

my $lastVo = "";
while(<STDIN>) {

  /VO_([A-Z\.0-9]*)_/;
  my $vo = $1;
  if ($vo ne $lastVo) {
    print("\n");
  }
  print;

  $lastVo = $vo;
}
print("\n");
