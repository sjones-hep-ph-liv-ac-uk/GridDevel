#!/usr/bin/python

import xmlrpclib
import os
import subprocess
from socket import gethostname 

proc = subprocess.Popen(["df | grep -v ^cvmfs  | grep -v hepraid[0-9][0-9]*_[0-9]"], stdout=subprocess.PIPE, shell=True)
(dfReport, err) = proc.communicate()

s = xmlrpclib.ServerProxy('http://hepgrid8.ph.liv.ac.uk:8000')

status = s.post_report(gethostname(),dfReport) 
if (status != 1):
  print("Client failed");

