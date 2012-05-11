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

public class SidParser {

	private String oldSidDir;
	private HashMap<String, VirtOrgInfo> allVoidInfo;

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
		ArrayList<String> yaimVariables = cmdExec("bash -x " + oldSidDir + "/site-info.def");
		Collections.sort(yaimVariables, String.CASE_INSENSITIVE_ORDER);

		Iterator<String> yvi = yaimVariables.iterator();
		Pattern vomsPattern = Pattern.compile("VO_(.*)_VOMS.*");
		while (yvi.hasNext()) {
			String yaimVariable = (String) yvi.next();
			Matcher matcher = vomsPattern.matcher(yaimVariable);
			if (matcher.find()) {
				String voName = matcher.group(1);
				// Make a record, if not there yet

				if (allVoidInfo.containsKey(voName) != true) {
					allVoidInfo.put(voName, new VirtOrgInfo());
				}

				// VO_ATLAS_VOMS_CA_DN="'/DC=ch/DC=cern/CN=CERN Trusted Certification
				// Authority' ...
				VirtOrgInfo thisVo = allVoidInfo.get(voName);
				thisVo.setVoNameAndVoNickName(voName);
				thisVo.setVodStyle(false);
				thisVo.setAtMySite(true);
				

				if (yaimVariable.matches(".*CA_DN.*")) {
					thisVo.setVomsServers(new ArrayList<VomsServer>());
					ArrayList<String> elements = breakString(yaimVariable);
					Iterator<String> ei = elements.iterator();
					while (ei.hasNext()) {
						VomsServer currentVomsServer = new VomsServer();
						String caDn = (String) ei.next();
						currentVomsServer.setCaDn(caDn);
						thisVo.addVomsServer(currentVomsServer);
					}
				}

				if (yaimVariable.matches(".*VOMS_SERVERS.*")) {
					ArrayList<String> elements = breakString(yaimVariable);
					Iterator<String> ei = elements.iterator();
					int ii = -1;
					while (ei.hasNext()) {
						ii++;
						ArrayList<VomsServer> vses = thisVo.getVomsServers();
						// vomss://lcg-voms.cern.ch:8443/voms/atlas?/atlas
						String vs = (String) ei.next();

						Pattern p = Pattern.compile("vomss:\\/\\/(\\S+)\\:(\\d+)");
						Matcher m = p.matcher(vs);
						if (m.find()) {
							String h = m.group(1);
							vses.get(ii).setHostname(h);
							String n = m.group(2);
							vses.get(ii).setHttpsPort(Integer.parseInt(n));
						}
					}
				}
				if (yaimVariable.matches(".*VOMSES.*")) {
					// VO_ATLAS_VOMSES="'atlas lcg-voms.cern.ch 15001 /DC=ch/DC=cern/OU=computers/CN=lcg-voms.cern.ch atlas' 'atlas vo.racf.bnl.gov 15003 /DC=org/DC=doegrids/OU=Services/CN=vo.racf.bnl.gov atlas' 'atlas voms.cern.ch 15001 /DC=ch/DC=cern/OU=computers/CN=voms.cern.ch atlas' "
					ArrayList<String> elements = breakString(yaimVariable);
					Iterator<String> ei = elements.iterator();
					int ii = -1;
					while (ei.hasNext()) {
						ii++;
						ArrayList<VomsServer> vses = thisVo.getVomsServers();
						String vomses = (String) ei.next();

						// atlas lcg-voms.cern.ch 15001
						// /DC=ch/DC=cern/OU=computers/CN=lcg-voms.cern.ch atlas
						Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)");
						Matcher m = p.matcher(vomses);
						if (m.find()) {
							String exp = m.group(1);
							String host = m.group(2);
							String port = m.group(3);
							String dn = m.group(4);
							String exp2 = m.group(5);
							vses.get(ii).setHostname(host);
							vses.get(ii).setVomsesPort(Integer.parseInt(port));
							vses.get(ii).setDn(dn);
							vses.get(ii).setComplete(true);
						}
					}
				}

			}
		}

		// Next get the vod lines

		File dir = new File(oldSidDir + "/vo.d");

		String[] chld = dir.list();
		if (chld == null) {
			System.out.println("the vo.d directory does not exist.");
			System.exit(1);
		} else {
			for (int i = 0; i < chld.length; i++) {
				String fileName = chld[i];

				ArrayList<String> vodYaimVariables = cmdExec("bash -x " + oldSidDir + "/vo.d/" + fileName);
				Collections.sort(vodYaimVariables, String.CASE_INSENSITIVE_ORDER);

				Iterator<String> vyvi = vodYaimVariables.iterator();
				Pattern vodVomsPattern = Pattern.compile("^\\+ VOMS.*");
				while (vyvi.hasNext()) {
					String vodYaimVariable = (String) vyvi.next();
					Matcher matcher = vodVomsPattern.matcher(vodYaimVariable);
					if (matcher.find()) {
						String voName = fileName;

						if (allVoidInfo.containsKey(voName) != true) {
							allVoidInfo.put(voName, new VirtOrgInfo());
						}

						// VOMS_SERVERS="'vomss://voms.gridpp.ac.uk:8443/voms/minos.vo.gridpp.ac.uk?/minos.vo.gridpp.ac.uk' "
						// VOMSES="'minos.vo.gridpp.ac.uk voms.gridpp.ac.uk 15016 /C=UK/O=eScience/OU=Manchester/L=HEP/CN=voms.gridpp.ac.uk minos.vo.gridpp.ac.uk' "
						// VOMS_CA_DN="'/C=UK/O=eScienceCA/OU=Authority/CN=UK e-Science CA 2B' "
						VirtOrgInfo thisVo = allVoidInfo.get(voName);
						thisVo.setVoNameAndVoNickName(voName);
						thisVo.setVodStyle(true);
						thisVo.setAtMySite(true);

						if (vodYaimVariable.matches(".*CA_DN.*")) {
							thisVo.setVomsServers(new ArrayList<VomsServer>());
							ArrayList<String> elements = breakString(vodYaimVariable);
							Iterator<String> ei = elements.iterator();
							while (ei.hasNext()) {
								VomsServer currentVomsServer = new VomsServer();
								String caDn = (String) ei.next();
								currentVomsServer.setCaDn(caDn);
								thisVo.addVomsServer(currentVomsServer);
							}
						}

						if (vodYaimVariable.matches(".*VOMS_SERVERS.*")) {
							ArrayList<String> elements = breakString(vodYaimVariable);
							Iterator<String> ei = elements.iterator();
							int ii = -1;
							while (ei.hasNext()) {
								ii++;
								ArrayList<VomsServer> vses = thisVo.getVomsServers();
								String vs = (String) ei.next();

								Pattern p = Pattern.compile("vomss:\\/\\/(\\S+)\\:(\\d+)");
								Matcher m = p.matcher(vs);
								if (m.find()) {
									String h = m.group(1);
									vses.get(ii).setHostname(h);
									String n = m.group(2);
									vses.get(ii).setHttpsPort(Integer.parseInt(n));
								}
							}
						}
						// VOMS_SERVERS="'vomss://voms.gridpp.ac.uk:8443/voms/minos.vo.gridpp.ac.uk?/minos.vo.gridpp.ac.uk' "
						// VOMSES="'minos.vo.gridpp.ac.uk voms.gridpp.ac.uk 15016 /C=UK/O=eScience/OU=Manchester/L=HEP/CN=voms.gridpp.ac.uk minos.vo.gridpp.ac.uk' "
						// VOMS_CA_DN="'/C=UK/O=eScienceCA/OU=Authority/CN=UK e-Science CA 2B' "
						if (vodYaimVariable.matches(".*VOMSES.*")) {
							// VOMSES="'minos.vo.gridpp.ac.uk voms.gridpp.ac.uk 15016 /C=UK/O=eScience/OU=Manchester/L=HEP/CN=voms.gridpp.ac.uk minos.vo.gridpp.ac.uk' "
							ArrayList<String> elements = breakString(vodYaimVariable);
							Iterator<String> ei = elements.iterator();
							int ii = -1;
							while (ei.hasNext()) {
								ii++;
								ArrayList<VomsServer> vses = thisVo.getVomsServers();
								String vomses = (String) ei.next();

								// minos.vo.gridpp.ac.uk voms.gridpp.ac.uk 15016
								// /C=UK/O=eScience/OU=Manchester/L=HEP/CN=voms.gridpp.ac.uk
								// minos.vo.gridpp.ac.uk
								Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)");
								Matcher m = p.matcher(vomses);
								if (m.find()) {
									String exp = m.group(1);
									String host = m.group(2);
									String port = m.group(3);
									String dn = m.group(4);
									String exp2 = m.group(5);
									vses.get(ii).setHostname(host);
									vses.get(ii).setVomsesPort(Integer.parseInt(port));
									vses.get(ii).setDn(dn);
									vses.get(ii).setComplete(true);
								}
							}
						}
					}
				}
			}
		}
	}

	public static ArrayList<String> cmdExec(String cmdLine) {
		String line;
		ArrayList<String> output = new ArrayList<String>();

		try {
			Process p = Runtime.getRuntime().exec(cmdLine);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = input.readLine()) != null) {
				output.add(line);
			}
			input.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return output;
	}

	private ArrayList<String> breakString(String s) {
		// The contract is that there will be + NAME=, then, inside a set of
		// single_quotes, will
		// be the payload. The payload consists of elements. Each element is bounded
		// by a
		// preceding single_quote, backslash, single_quote, single_quote, i.e. '\''
		// e.g.
		// +
		// VO_OPS_VOMS_SERVERS=''\''vomss://lcg-voms.cern.ch:8443/voms/ops?/ops'\''
		// '\''vomss://voms.cern.ch:8443/voms/ops?/ops'\'' '

		String payload = s.substring(s.indexOf('=') + 1);
		payload = payload.trim();
		payload = payload.substring(1, payload.length() - 1);
		payload = payload.trim();

		// Mad, but this is what '\'' looks like in Java. Don't blame me.
		String[] tokens = payload.split("\\'\\\\\'\'");
		ArrayList<String> t = new ArrayList<String>();
		for (int ii = 0; ii < tokens.length; ii++) {
			if (!tokens[ii].matches("^\\s*$")) {
				t.add(tokens[ii]);
			}
		}

		return t;

	}

}
