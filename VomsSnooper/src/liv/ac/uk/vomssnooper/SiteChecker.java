package liv.ac.uk.vomssnooper;
/**
 * Reads in an XML file and a site-info.def file (and related vo.d files) and compares them.
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-07-20
 * 
 */

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liv.ac.uk.snooputils.Utils;
import liv.ac.uk.snooputils.Wgetter;

public class SiteChecker {

	private String xmlurl;    // The url to get the XML from
	private String sidfile;   // The location of the site-info.def file (vo.d should be in same directory)

	
	private ArrayList<VirtOrgInfo> voidInfoFromXml;       // XML VO Info
	private HashMap<String, VirtOrgInfo> voidInfoFromSid; // SID VO Info

	/**
	 * Constructor
	 * 
	 * @param xmlurl url to get XML from
	 * @param sidfile site-info.def file
	 */

	public SiteChecker(String x, String s) {
		xmlurl = x;
		sidfile = s;
		voidInfoFromXml = new ArrayList<VirtOrgInfo>();
		voidInfoFromSid = new HashMap<String, VirtOrgInfo>();
	}
  /**
   * Parse an XML file and a SID setup
   */
	public void parse() {

		// Parse the XML
		VoidCardXmlParser xmlParser = new VoidCardXmlParser(xmlurl, (ArrayList<VirtOrgInfo>) voidInfoFromXml);
		xmlParser.parseDocument();

		// I'm interested in them all, nominally
		Iterator<VirtOrgInfo> it = voidInfoFromXml.iterator();
		while (it.hasNext()) {
			VirtOrgInfo voi = it.next();
			voi.setAtMySite(true);
			voi.checkComplete();
		}

		// Parse the sid

		// Storage for the record I consider
		String caDnLine = null;
		String vomsServersLine = null;
		String vomsesLine = null;

		File sid = new File(sidfile);
		if (!sid.isFile()) {
			System.out.println("site-info.def file " + sid + " not found");
			System.exit(1);
		}

		// Get the yaim variables
		ArrayList<String> yaimVariables = new ArrayList<String>();
		try {
			yaimVariables = Utils.cmdExec("bash -x " + sid.toString());
		}
		catch (Exception e) {
			System.out.println("Problem while while reading site-info.def " + e.getMessage());
			System.exit(1);
		}

		// Sort so we can depend on the order
		Collections.sort(yaimVariables, String.CASE_INSENSITIVE_ORDER);

		// Go over the yaim variables, selecting VO lines
		Pattern pattern = Pattern.compile("VO_(.*)_VOMS.*");
		Iterator<String> var = yaimVariables.iterator();

		while (var.hasNext()) {
			String yaimVariable = (String) var.next();

			Matcher matcher = pattern.matcher(yaimVariable);
			if (matcher.find()) {

				String voName = matcher.group(1).toLowerCase();

				// Collect all the lines first to overcome any order problems

				if (yaimVariable.matches(".*CA_DN.*")) {
					if (voidInfoFromSid.containsKey(voName) == true) {
						System.out.println("Warning: the " + voName + " sid records are duplicated! Results may be chaotic.");
					}
					else {
						// Make a new set of records
						voidInfoFromSid.put(voName, new VirtOrgInfo());
					}

					// Initial values
					voidInfoFromSid.get(voName).setVoNameAndVoNickName(voName);
					voidInfoFromSid.get(voName).setVodStyle(false);
					voidInfoFromSid.get(voName).setAtMySite(true);

					// Store the CA DNs
					caDnLine = yaimVariable;
				}

				if (yaimVariable.matches(".*VOMS_SERVERS.*")) {
					// Store the VOMS Servers
					vomsServersLine = yaimVariable;
				}

				if (yaimVariable.matches(".*VOMSES.*")) {
					// Store the VOMSES
					vomsesLine = yaimVariable;

					// As it is sorted, this triggers the end of a run, so now do more parsing
					// Break that CA DN variable up and go setting fields
					ArrayList<String> elements = breakString(caDnLine);

					Iterator<String> els = elements.iterator();
					while (els.hasNext()) {
						String caDn = (String) els.next();
						VomsServer theVomsServer = new VomsServer();
						theVomsServer.setMembersListUrl("dummy");
						theVomsServer.setCaDn(caDn);
						voidInfoFromSid.get(voName).addVomsServer(theVomsServer);
					}

					// VOMSES found - break the variable up and go setting fields
					elements = breakString(vomsesLine);
					els = elements.iterator();
					int ii = -1;
					while (els.hasNext()) {
						ii++;
						ArrayList<VomsServer> vomsServers = voidInfoFromSid.get(voName).getVomsServers();
						String vomses = (String) els.next();

						// More pattern matching to save a lot of tinkering
						Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)");
						Matcher m = p.matcher(vomses);
						if (m.find()) {
							String vo1 = m.group(1);
							String host = m.group(2);
							String vomsServerPort = m.group(3);
							String dn = m.group(4);
							String vo2 = m.group(5);

							vomsServers.get(ii).setHostname(host);
							vomsServers.get(ii).setVomsServerPort(Integer.parseInt(vomsServerPort));
							vomsServers.get(ii).setDn(dn);
						}
					}

					// Voms Servers found - break the variable up and go setting fields
					elements = breakString(vomsServersLine);

					els = elements.iterator();
					while (els.hasNext()) {
						ArrayList<VomsServer> vomsServers = voidInfoFromSid.get(voName).getVomsServers();
						String el = (String) els.next();

						// Pattern matching to save a lot of tinkering
						Pattern p = Pattern.compile("vomss:\\/\\/(\\S+)\\:(\\d+).*");
						Matcher m = p.matcher(el);
						if (m.find()) {
							String hostPart = m.group(1);
							Integer httpsPort = Integer.parseInt(m.group(2));

							// Find the voms server that this record applies to
							Iterator<VomsServer> vs = vomsServers.iterator();
							Boolean setPort = false;
							while (vs.hasNext()) {
								VomsServer v = vs.next();
								String h = v.getHostname();
								if (h.equalsIgnoreCase(hostPart)) {
									// This is the one
									v.setHttpsPort(httpsPort);
									setPort = true;
								}
							}
							if (!setPort) {
								System.out.println("Warning: Unable to find a voms server for one of these: " + vomsServersLine);
							}
						}
						else {
							System.out.println("Warning: Weird VOMS_SERVER line: " + el.toString());
						}
					}
				}
			}
		}

		// Next get the vod files, that lie in files in the vo.d directory
		File dir = new File(sid.getParent());
		String[] vodFiles = dir.list();
		if (vodFiles == null) {
			System.out.println("the vo.d directory does not exist");
			System.exit(1);
		}
		for (int i = 0; i < vodFiles.length; i++) {
			String vodFile = vodFiles[i];

			// Read the yaim variables for each files found
			ArrayList<String> vodYaimVariables = new ArrayList<String>();
			try {
				vodYaimVariables = Utils.cmdExec("bash -x " + dir.toString() + "/vo.d/" + vodFile);
			}
			catch (Exception e) {
				System.out.println("Problem while while reading vod. file " + vodFile + ", " + e.getMessage());
				System.exit(1);
			}
			// Sort it so we can depend on the order
			Collections.sort(vodYaimVariables, String.CASE_INSENSITIVE_ORDER);

			// Go over the lines, look for VO ones
			Pattern vodVomsPattern = Pattern.compile("^\\+ VOMS.*");
			Iterator<String> iter = vodYaimVariables.iterator();

			while (iter.hasNext()) {
				String vodYaimVariable = (String) iter.next();

				Matcher matcher = vodVomsPattern.matcher(vodYaimVariable);
				if (matcher.find()) {

					// Name is same as file, for VODs
					String voName = vodFile.toLowerCase();
					// VirtOrgInfo thisVo = voidInfoFromSid.get(voName);

					// Get all the fields in advance
					if (vodYaimVariable.matches(".*CA_DN.*")) {
						caDnLine = vodYaimVariable;
						// Make a new record, if we need to
						if (voidInfoFromSid.containsKey(voName.toLowerCase()) == true) {
							System.out.println("Warning: the " + voName + " vod records are duplicated! Results may be chaotic.");
						}
						else {
							voidInfoFromSid.put(voName.toLowerCase(), new VirtOrgInfo());
						}
						voidInfoFromSid.put(voName, new VirtOrgInfo());
						voidInfoFromSid.get(voName).setVoNameAndVoNickName(voName);
						voidInfoFromSid.get(voName).setVodStyle(true);
						voidInfoFromSid.get(voName).setAtMySite(true);

					}
					if (vodYaimVariable.matches(".*VOMS_SERVERS.*")) {
						vomsServersLine = vodYaimVariable;
					}
					if (vodYaimVariable.matches(".*VOMSES.*")) {
						vomsesLine = vodYaimVariable;

						// Last one found, so parse the fields

						// Break up the CA DN variable
						ArrayList<String> elements = breakString(caDnLine);
						Iterator<String> ei = elements.iterator();
						while (ei.hasNext()) {
							String caDn = (String) ei.next();
							VomsServer theVomsServer = new VomsServer();
							theVomsServer.setCaDn(caDn);
							theVomsServer.setMembersListUrl("dummy");
							voidInfoFromSid.get(voName).addVomsServer(theVomsServer);
						}

						// VOMSES found - break the variable up and go setting fields
						elements = breakString(vomsesLine);
						ei = elements.iterator();
						int ii = -1;
						while (ei.hasNext()) {
							ii++;
							ArrayList<VomsServer> vomsServers = voidInfoFromSid.get(voName).getVomsServers();
							String vomses = (String) ei.next();

							// Pattern matching to save a lot of tinkering
							Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)");
							Matcher m = p.matcher(vomses);
							if (m.find()) {
								String vo1 = m.group(1);
								String host = m.group(2);
								String vomsServerPort = m.group(3);
								String dn = m.group(4);
								String vo2 = m.group(5);
								vomsServers.get(ii).setHostname(host);
								vomsServers.get(ii).setVomsServerPort(Integer.parseInt(vomsServerPort));
								vomsServers.get(ii).setDn(dn);
							}
						}

						// Voms Servers found - break the variable up and go setting
						// fields
						elements = breakString(vomsServersLine);
						ei = elements.iterator();
						ii = -1;
						while (ei.hasNext()) {
							ii++;
							ArrayList<VomsServer> vomsServers = voidInfoFromSid.get(voName).getVomsServers();
							String el = (String) ei.next();

							// Pattern matching to save a lot of tinkering
							Pattern p = Pattern.compile("vomss:\\/\\/(\\S+)\\:(\\d+).*");
							Matcher m = p.matcher(el);
							if (m.find()) {
								String hostToFind = m.group(1);
								Integer httpsPort = Integer.parseInt(m.group(2));

								// Go over all the VOMS Servers, finding the one that matches this record
								Iterator<VomsServer> vs = vomsServers.iterator();
								Boolean setPort = false;
								while (vs.hasNext()) {
									VomsServer v = vs.next();
									String h = v.getHostname();
									if (h.equalsIgnoreCase(hostToFind)) {
										v.setHttpsPort(httpsPort);
										setPort = true;
									}
								}
								if (!setPort) {
									System.out.println("Warning: Unable to find a voms server for one of these: " + vomsServersLine);
								}
							}
							else {
								System.out.println("Warning: Weird VOMS_SERVER line: " + el.toString());
							}
						}
					}
				}
			}
		}

		// Finally, sort those voms servers
		ArrayList<VirtOrgInfo> v = new ArrayList<VirtOrgInfo>(voidInfoFromSid.values());

		Iterator<VirtOrgInfo> allIt = v.iterator();
		while (allIt.hasNext()) {
			VirtOrgInfo voi = allIt.next();
			voi.sortVomsServers();
		}

		// end of sid

	}

	public void compare() {
		System.out.println("Parsed both sources. Now compare.");

		Iterator<VirtOrgInfo> x = voidInfoFromXml.iterator();
		while (x.hasNext()) {
			VirtOrgInfo xmlVo = x.next();
			if (voidInfoFromSid.containsKey(xmlVo.getVoName())) {
				VirtOrgInfo sidVo = voidInfoFromSid.get(xmlVo.getVoName());
				String xmlVoString = xmlVo.toString();
				String sidVoString = sidVo.toString();
				if (!xmlVoString.equals(sidVoString)) {
					System.out.println("\nDiscrepancy in VO:" + xmlVo.getVoName());
					System.out.println("XML Data : \n" + xmlVoString);
					System.out.println("Site-info: \n" + sidVoString);
					System.out.println(" ");
				}
				else {
					System.out.println("\nVO: " + xmlVo.getVoName() + " matches");
					System.out.println(" ");
				}
			}
		}
		System.out.println("\nDone compare.");
	}

	/**
	 * Main
	 * 
	 * @return null
	 */

	// List of the CLI options
	private enum OptList {
		xmlurl, sidfile, help,
	}

	public static void printHelpPage() {
		System.out.println("");
		System.out.println("This tool checks if a site complies with the CIC portal XML");
		System.out.println("");
		System.out.println("Mandatory arguments: ");
		System.out.println("  --xmlurl  f       # Url of XML file (i.e. CIC portal)");
		System.out.println("  --sidfile f       # Location of site-info.def file");
		System.out.println("Optional arguments: ");
		System.out.println("  --help            # Prints this info");
	}

	public static void main(String[] args) {

		String xmlurl = null;
		String sidfile = null;

		// Announcement
		Utils.printVersion();

		StringBuffer sb = new StringBuffer();
		String arg;

		LongOpt[] longopts = new LongOpt[OptList.values().length];

		longopts[OptList.help.ordinal()] = new LongOpt(OptList.help.name(), LongOpt.NO_ARGUMENT, sb, OptList.help.ordinal());
		longopts[OptList.xmlurl.ordinal()] = new LongOpt(OptList.xmlurl.name(), LongOpt.REQUIRED_ARGUMENT, sb, OptList.xmlurl.ordinal());
		longopts[OptList.sidfile.ordinal()] = new LongOpt(OptList.sidfile.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.sidfile.ordinal());

		Getopt g = new Getopt("testprog", args, "", longopts);
		g.setOpterr(false);

		int c;
		while ((c = g.getopt()) != -1) {

			arg = g.getOptarg();

			// It only takes long options
			if (c != 0) {
				System.out.print("Some option was given that I don't understand, " + sb.toString() + " \n");
				printHelpPage();
				System.exit(1);
			}

			if ((char) (new Integer(sb.toString())).intValue() == OptList.help.ordinal()) {
				printHelpPage();
				System.exit(1);
			}
			if ((char) (new Integer(sb.toString())).intValue() == OptList.xmlurl.ordinal()) {
				xmlurl = ((arg != null) ? arg : "null");
			}
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.sidfile.ordinal()) {
				sidfile = ((arg != null) ? arg : "null");
			}
		}

		// voidCardFile is mandatory input
		if (xmlurl == null) {
			System.out.print("The --xmlurl argument must be given\n");
			System.exit(1);
		}
		if (sidfile == null) {
			System.out.print("The --sidfile  argument must be given\n");
			System.exit(1);
		}

		// If you give a sidfile, it must exist
		if (!(new File(sidfile)).isFile()) {
			System.out.print("The --sidfile (" + sidfile + ") doesn't exist\n");
			System.exit(1);
		}

		// Make the Controller class
		SiteChecker vs = new SiteChecker(xmlurl, sidfile);

		// Parse the XML File
		vs.parse();
		
		// Compare the XML to the SID
		vs.compare();

	}


	/**
	 * Breaks up a yaim variable string, according to this contract Contract: + NAME= then: Nothing - string is empty Space - string
	 * is empty String, without leading ' char - whole content is one element
	 * 
	 * String with leading ' char: then: without '\''' - string is simple string bounded by '', one element with '\''' ... string is a
	 * sequence of elements separated by that
	 * 
	 * @param cmdLine command to execute
	 * @return output from command
	 */

	private static ArrayList<String> breakString(String s) {

		String payload = s;

		payload = payload.substring(s.indexOf('=') + 1);
		payload = payload.trim();
		if (payload.isEmpty()) {
			// No elements
			return new ArrayList<String>();
		}

		if (!payload.startsWith("'")) {
			// One element
			ArrayList<String> res = new ArrayList<String>();
			res.add(payload);
			return res;
		}
		else {
			// String with leading ' char.
			// Cut off bounding chars
			payload = payload.substring(1, payload.length() - 1);
			if (payload.startsWith("'\\''")) {
				// Some elements ...

				// This is what '\'' looks like in Java as a regex. Don't blame me.
				String[] tokens = payload.split("\\'\\\\\'\'");
				ArrayList<String> res = new ArrayList<String>();
				for (int ii = 0; ii < tokens.length; ii++) {
					if (!tokens[ii].matches("^\\s*$")) {
						res.add(tokens[ii]);
					}
				}
				return res;
			}
			else {
				// One element
				ArrayList<String> res = new ArrayList<String>();
				res.add(payload);
				return res;
			}
		}
	}
}
