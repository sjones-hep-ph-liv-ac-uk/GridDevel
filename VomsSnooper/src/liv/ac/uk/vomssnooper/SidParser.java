package liv.ac.uk.vomssnooper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads in a site-info.def file (and related vo.d files) and makes the fields into objects
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-05-10
 */

public class SidParser {

	private String oldSidDir;                            // The side file to read from
	
	private HashMap<String, VirtOrgInfo> allVoidInfo;    // A collection of VO info

	public SidParser(String os, HashMap<String, VirtOrgInfo> vi) {
		oldSidDir = os;
		allVoidInfo = vi;
	}

	/**
	 * Parse the VOID XML file
	 * 
	 * @return null
	 */
	public void parseDocument() {

		// First get the sid lines
		File sid = new File(oldSidDir + "/site-info.def");
		if (!sid.isFile()) {
			System.out.println("site-info.def file " + sid + " not found");
			System.exit(1);
		}
		
		// Get the yaim variables
		ArrayList<String> yaimVariables = new ArrayList<String> ();
		try {
		  yaimVariables = cmdExec("bash -x " + oldSidDir + "/site-info.def");
		}
		catch (Exception e) {
			System.out.println("Problem while while reading old site-info.def " + e.getMessage());
			System.exit(1);
		}
		
		// Sort them all, so we can depend on the order
		Collections.sort(yaimVariables, String.CASE_INSENSITIVE_ORDER);

		// Go over the yaim variables, selecting VO lines
		Pattern vomsPattern = Pattern.compile("VO_(.*)_VOMS.*");
		Iterator<String> yvi = yaimVariables.iterator();
		
		while (yvi.hasNext()) {
			String yaimVariable = (String) yvi.next();
			Matcher matcher = vomsPattern.matcher(yaimVariable);
			if (matcher.find()) {
				String voName = matcher.group(1);

				// Make a record, if not there yet
				if (allVoidInfo.containsKey(voName) != true) {
					allVoidInfo.put(voName, new VirtOrgInfo());
				}
				VirtOrgInfo thisVo = allVoidInfo.get(voName);

				// Set up the record
				thisVo.setVoNameAndVoNickName(voName);
				thisVo.setVodStyle(false);
				thisVo.setAtMySite(true);

				// Now do more parsing
				if (yaimVariable.matches(".*CA_DN.*")) {
					
					// CA DN Found - set up some blanks voms servers
					thisVo.setVomsServers(new ArrayList<VomsServer>());
					
					// Now break that CA DN variable up and go setting fields
					ArrayList<String> elements = breakString(yaimVariable);
					Iterator<String> ei = elements.iterator();
					while (ei.hasNext()) {
						VomsServer currentVomsServer = new VomsServer();
						currentVomsServer.setMembersListUrl("dummy");
						String caDn = (String) ei.next();
						currentVomsServer.setCaDn(caDn);
						thisVo.addVomsServer(currentVomsServer);
					}
				}

				if (yaimVariable.matches(".*VOMS_SERVERS.*")) {
					// Voms Servers found - break the variable up and go setting fields
					
					ArrayList<String> elements = breakString(yaimVariable);
					
					Iterator<String> ei = elements.iterator();
					int ii = -1;
					while (ei.hasNext()) {
						ii++;
						ArrayList<VomsServer> vses = thisVo.getVomsServers();
						String vs = (String) ei.next();

						// Fancy group pattern matching to save a lot of tinkering
						Pattern p = Pattern.compile("vomss:\\/\\/(\\S+)\\:(\\d+).*");
						Matcher m = p.matcher(vs);
						if (m.find()) {
							String h = m.group(1);
							vses.get(ii).setHostname(h);
							String n = m.group(2);
							int nPort = Integer.parseInt(n);
							vses.get(ii).setHttpsPort(nPort);
						}
						else {
							System.out.println("Weird VOMS_SERVER line: " + vs.toString());
						}
					}
				}
				if (yaimVariable.matches(".*VOMSES.*")) {
					// VOMSES found - break the variable up and go setting fields
					ArrayList<String> elements = breakString(yaimVariable);
					Iterator<String> ei = elements.iterator();
					int ii = -1;
					while (ei.hasNext()) {
						ii++;
						ArrayList<VomsServer> vses = thisVo.getVomsServers();
						String vomses = (String) ei.next();

						// More fancy group pattern matching to save a lot of tinkering
						Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)");
						Matcher m = p.matcher(vomses);
						if (m.find()) {
							String exp = m.group(1);
							String host = m.group(2);
							String port = m.group(3);
							String dn = m.group(4);
							String exp2 = m.group(5);
							vses.get(ii).setHostname(host);
							int nPort = Integer.parseInt(port);
							vses.get(ii).setVomsServerPort(nPort);
							vses.get(ii).setDn(dn);
						}
					}
				}

			}
		}

		// Next get the vod files, that lie in files in the vo.d directory
		File dir = new File(oldSidDir + "/vo.d");
		String[] chld = dir.list();
		if (chld == null) {
			System.out.println("the vo.d directory does not exist.");
			System.exit(1);
		} else {
			for (int i = 0; i < chld.length; i++) {
				String fileName = chld[i];

				// Read the yaim variables for each files found
				ArrayList<String> vodYaimVariables = new ArrayList<String> (); 
				try {
				  vodYaimVariables = cmdExec("bash -x " + oldSidDir + "/vo.d/" + fileName);
				}
				catch (Exception e) {
					System.out.println("Problem while while reading old vod. file " + fileName + ", " + e.getMessage());
					System.exit(1);
				}
				// Sort it so we can depend on the order
				Collections.sort(vodYaimVariables, String.CASE_INSENSITIVE_ORDER);

				// Go over the lines, look for VO ones
				Pattern vodVomsPattern = Pattern.compile("^\\+ VOMS.*");
				Iterator<String> vyvi = vodYaimVariables.iterator();
				
				while (vyvi.hasNext()) {
					String vodYaimVariable = (String) vyvi.next();
					Matcher matcher = vodVomsPattern.matcher(vodYaimVariable);
					
					if (matcher.find()) {
						String voName = fileName;

						// Make a new record, if we need to
						if (allVoidInfo.containsKey(voName) != true) {
							allVoidInfo.put(voName, new VirtOrgInfo());
						}

						// Set up some generic fields
						VirtOrgInfo thisVo = allVoidInfo.get(voName);
						thisVo.setVoNameAndVoNickName(voName);
						thisVo.setVodStyle(true);
						thisVo.setAtMySite(true);
						
						if (vodYaimVariable.matches(".*CA_DN.*")) {
							// CA DN Found - set up some blank voms servers
							thisVo.setVomsServers(new ArrayList<VomsServer>());
							
							// Now break that CA DN variable up and go setting fields
							ArrayList<String> elements = breakString(vodYaimVariable);
							Iterator<String> ei = elements.iterator();
							while (ei.hasNext()) {
								VomsServer currentVomsServer = new VomsServer();
								currentVomsServer.setMembersListUrl("dummy");
								String caDn = (String) ei.next();
								currentVomsServer.setCaDn(caDn);
								thisVo.addVomsServer(currentVomsServer);
							}
						}

						if (vodYaimVariable.matches(".*VOMS_SERVERS.*")) {
							// Voms Servers found - break the variable up and go setting fields
							ArrayList<String> elements = breakString(vodYaimVariable);
							Iterator<String> ei = elements.iterator();
							int ii = -1;
							while (ei.hasNext()) {
								ii++;
								ArrayList<VomsServer> vses = thisVo.getVomsServers();
								String vs = (String) ei.next();
								
								// Fancy group pattern matching to save a lot of tinkering
								Pattern p = Pattern.compile("vomss:\\/\\/(\\S+)\\:(\\d+).*");
								Matcher m = p.matcher(vs);
								if (m.find()) {
									String h = m.group(1);
									vses.get(ii).setHostname(h);
									String n = m.group(2);
									int nPort = Integer.parseInt(n);
									vses.get(ii).setHttpsPort(nPort);
								}
								else {
									System.out.println("Weird VOMS_SERVER line: " + vs.toString());
								}
							}
						}
						if (vodYaimVariable.matches(".*VOMSES.*")) {
							// VOMSES found - break the variable up and go setting fields
							ArrayList<String> elements = breakString(vodYaimVariable);
							Iterator<String> ei = elements.iterator();
							int ii = -1;
							while (ei.hasNext()) {
								ii++;
								ArrayList<VomsServer> vses = thisVo.getVomsServers();
								String vomses = (String) ei.next();

								// Fancy group pattern matching to save a lot of tinkering
								Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)");
								Matcher m = p.matcher(vomses);
								if (m.find()) {
									String exp = m.group(1);
									String host = m.group(2);
									String port = m.group(3);
									String dn = m.group(4);
									String exp2 = m.group(5);
									vses.get(ii).setHostname(host);
									vses.get(ii).setVomsServerPort(Integer.parseInt(port));
									vses.get(ii).setDn(dn);
								}
							}
						}
					}
				}
			}
		}
		
		// Finally, sort those voms servers
		ArrayList<VirtOrgInfo> v = new ArrayList<VirtOrgInfo>(allVoidInfo.values ()); 
		
		Iterator<VirtOrgInfo> allIt = v.iterator();
		while (allIt.hasNext()) {
			VirtOrgInfo voi = allIt.next();
			voi.sortVomsServers();
		}
	}
	
  /**
   * Executes some command and returns its stdout
   * @param cmdLine command to execute
   * @return output from command
   */
	public static ArrayList<String> cmdExec(String cmdLine) throws Exception {
		ArrayList<String> output = new ArrayList<String>();

		String lineBack;
		try {
			Process p = Runtime.getRuntime().exec(cmdLine);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((lineBack = input.readLine()) != null) {
				output.add(lineBack);
			}
			input.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("Some problem occured while running " + cmdLine + " " + ex.getMessage());
		}
		return output;
	}

  /**
   * Breaks up a yaim variable string, according to this contract
 	 * Contract:
   *  + NAME=
	 * then: 
	 *  Nothing - string is empty
	 *  Space   - string is empty
	 *   String, without leading ' char - whole content is one element
	 *	
	 *   String with leading ' char:
	 *     then:
	 *       without '\''' - string is simple string bounded by '', one element
	 *       with '\''' ... string is a sequence of elements separated by that
   * 
   * @param cmdLine command to execute
   * @return output from command
   */
	
	private ArrayList<String> breakString(String s) {
    
		String payload = s;
		
		payload = payload.substring(s.indexOf('=') + 1);
		payload = payload.trim();
		if (payload.isEmpty()) {
			// No elements
			return new ArrayList<String>();
		}
		
		if (!payload.startsWith("'")) {
			// One element
			ArrayList <String> res = new ArrayList<String>();
			res.add(payload);			
			return res;
		}
		else {
			// String with leading ' char.
			// Cut off bounding chars
			payload = payload.substring(1,payload.length() - 1);
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
				ArrayList <String> res = new ArrayList<String>();
				res.add(payload);			
				return res;
			}
		}
	}
}
