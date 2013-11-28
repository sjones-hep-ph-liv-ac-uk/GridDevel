Instant UI Brief Instructions
*****************************

A UI is a system that is used to submit jobs to the Grid.

Instant UI is a procedure to quickly set up a UI for submitting
grid jobs.  It is distributed with VomsSnooper, which is described here:
  https://www.gridpp.ac.uk/wiki/VomsSnooper_Tools

Brief instructions follow. For more details, see the VomsSnooper
documentation.

Prerequisites:
**************

To host the UI, set-up a standard Linux system using SL6 or RHEL6.
Each grid user needs to have a user account on the system, and
each grid user must be a member of at least one Virtual
Organisation (VO). But, prior to joining any VO, a user needs a grid
certificate. Here's the run down.

a) Get a grid certificate

Advice on acquiring a UK grid certificate is given here:
  https://ca.grid-support.ac.uk

b) Install it

Once a grid certificate is received for the user, install it
as per the instructions here:
  https://twiki.cern.ch/twiki/bin/view/LHCb/FAQ/Certificate

c) Use the grid certificate to join a VO

Once users have a grid certificate, they can join a VO. Advice on
joining an example VO, dteam, is given here:
  https://wiki.egi.eu/wiki/Dteam_vo

Note: Each VO will have its own membership process.

Install Steps:
**************

Using some tool such a yum or rpm, install these packages:

1) emi-ui (http://repository.egi.eu/sw/production/umd/3/sl6/x86_64)

2) VomsSnooper (http://www.sysadmin.hep.ac.uk/rpms/fabric-management/RPMS.vomstools)

More advice will be made available on this in the  VomsSnooper web site, above.

Next, as root, cd /opt/GridDevel/vomssnooper/usecases/instantUI/

Run the ./installUI script.

# ./installUI

If you give it no options, it sets up the UI to use all the VOs that
are approved for use at the time of writing. You can also give specific
VOs as options, e.g.

# ./installUI zeus t2k.org

In this case, the UI will only support zeus and t2k.org.

Note: Any VO must be present in the Operations Portal:
  http://operations-portal.egi.eu/

Test Steps:
***********

A brief test suite is provided. Follow these steps to use it.

Switch user from root to the grid user, e.g.
# su - sjones

Make a test directory
# mkdir ttmptest; cd tmptest

Copy the tests
# cp -R /opt/GridDevel/vomssnooper/usecases/instantUI/tests .
# cd tests

Make a short term "copy" of your certificate called a proxy
# voms-proxy-init --voms <yourVO>

Submit the test
# ./submit_test.sh

Check the status until it's done (list_wms.* contains the job id)
# ./check.sh /user2/sjones/tests/tests/list_wms.17291

Finally get the output
# ./get.sh /user2/sjones/tests/tests/list_wms.17291

Steve Jones
28 November 2013

