#!/usr/bin/python

from SimpleXMLRPCServer import SimpleXMLRPCServer
from SimpleXMLRPCServer import SimpleXMLRPCRequestHandler
import time
import smtplib
import logging

logging.basicConfig(level=logging.DEBUG,
  format='%(asctime)s %(levelname)s %(message)s',
  filename="/user2/sjones/git/GridDevel/SpaceMon/log",
  filemode='a')

# email details
smtpserver = 'hep.ph.liv.ac.uk'
recipients = ['sjones@hep.ph.liv.ac.uk','sjones@hep.ph.liv.ac.uk']
sender = 'root@hepgrid8.ph.liv.ac.uk'
msgheader = "From: root@hepgrid8.ph.liv.ac.uk\r\nTo: sys@hep.ph.liv.ac.uk\r\nSubject: spacemon\r\n\r\n"

session = smtplib.SMTP(smtpserver)
smtpresult = session.sendmail(sender, recipients, "Test message.")
session.quit()

limit = 90

# Restrict to a particular path.
class RequestHandler(SimpleXMLRPCRequestHandler):
    rpc_paths = ('/RPC2',)

# Create server
server = SimpleXMLRPCServer(("localhost", 8000), requestHandler=RequestHandler)
server.register_introspection_functions()

class SpaceMon:
  def post_report(address,hostname,report):
    lines = report.split('\n')
    for l in lines[1:]:
      fields = l.split()
      if (len(fields) >= 5):
        fs = fields[0]
        pc = int(fields[4][:-1])
        if (pc  >= limit ):
          print("File system " + fs + " on " + hostname + " is getting full.")
    return 1

server.register_instance(SpaceMon())

server.serve_forever()

