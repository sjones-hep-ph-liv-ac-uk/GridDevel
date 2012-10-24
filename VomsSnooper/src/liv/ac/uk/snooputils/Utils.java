package liv.ac.uk.snooputils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import liv.ac.uk.vomssnooper.Fqan;
import liv.ac.uk.vomssnooper.IndividualContact;
import liv.ac.uk.vomssnooper.VirtOrgInfo;
import liv.ac.uk.vomssnooper.VomsServer;
import liv.ac.uk.vomssnooper.VirtOrgInfo.ByVoName;

/**
 * Utilities 
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-05-10
 */

public class Utils {

	/**
	 * Prints the SID in regular output file
	 * 
	 * @param The vo to print
	 * @param Where to print it
	 * 
	 * @return null
	 */
	private static void printSIDStyle(VirtOrgInfo v, PrintStream ps, Boolean extraFields, Boolean noSillySids,
			Boolean applyCernRule) {

		ArrayList<String> vomsLines = v.getVomsLines(true, extraFields, applyCernRule);
		HashMap<String, Boolean> sillySidAcceptedWarnings = new HashMap<String, Boolean>();
		HashMap<String, Boolean> sillySidDroppedWarnings = new HashMap<String, Boolean>();

		for (String vs : vomsLines) {

			String n = v.getVoName().toUpperCase();

			if (noSillySids) {
				if (n.contains(".") == false) {
					ps.print("VO_" + n + "_" + vs + "\n");
				}
				else {
					sillySidDroppedWarnings.put(n, true);
					ps.print("# VO_" + n + "_" + vs + "\n");
				}
			}
			else {
				if (n.contains(".") == true) {
					sillySidAcceptedWarnings.put(n, true);
				}
				ps.print("VO_" + n + "_" + vs + "\n");
			}
		}
		ps.print("\n");

		// Print the warnings
		for (Entry<String, Boolean> entry : sillySidAcceptedWarnings.entrySet()) {
			System.out.print("Warning: silly SIDs for - " + entry.getKey() + " - were found but processed anyway\n");
		}

		for (Entry<String, Boolean> entry : sillySidDroppedWarnings.entrySet()) {
			System.out.print("Warning: silly SIDs were dropped for - " + entry.getKey() + "\n");
		}

	}

	/**
	 * Prints in the vo.d style
	 * 
	 * @param The vo to print
	 * @return null
	 */
	private static void printVODStyle(VirtOrgInfo v, String vodDir, Boolean extraFields, Boolean printVodTitleLine,
			Boolean applyCernRule) {

		String filename = vodDir + "/" + v.getVoName();

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename);
			PrintStream ps = new PrintStream(fos);
			ArrayList<String> vomsLines = v.getVomsLines(true, extraFields, applyCernRule);

			if (printVodTitleLine) {
				ps.print("# $YAIM_LOCATION/vo.d/" + v.getVoName() + "\n");
			}
			for (String vs : vomsLines) {
				ps.print(vs + "\n");
			}
			ps.print("\n");
		}

		catch (IOException e) {
			System.out.print("Unable (1) to write to file" + e);
			System.exit(1);
		}
		finally {
			try {
				fos.close();
			}
			catch (IOException e) {
			}
		}
	}

	/**
	 * Prints the VO variable results, in VOD or SID format SID format is that used in site-info.def files. But when DNS style VO
	 * names were brought in, there was a problem using VO variables in SID format, because of bash restrictions on using the dot
	 * character in variable names. So VOD format was introduced. In this format, discrete files are written for each VO in a vo.d
	 * directory.
	 * 
	 * @return null
	 */
	public static void printVoVariables(ArrayList<VirtOrgInfo> voidInfo, String sidFile, String vodDir, Boolean extraFields,
			Boolean noSillySids, Boolean printVodTitle, Boolean flat, Boolean applyCernRule) {

		Collections.sort(voidInfo, new ByVoName());

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(sidFile);
			PrintStream ps = new PrintStream(fos);
			for (VirtOrgInfo v : voidInfo) {
				if (v.isAtMySite()) {

					// Print out on standard style if either not a VOD, or if flat is specified
					if ((v.isVodStyle() != true) | (flat)) {
						printSIDStyle(v, ps, extraFields, noSillySids, applyCernRule);
					}
					else {
						printVODStyle(v, vodDir, extraFields, printVodTitle, applyCernRule);
					}
				}
			}
		}

		catch (IOException e) {
			System.out.print("Unable to Write to file" + e);
			System.exit(1);
		}
		finally {
			try {
				fos.close();
			}
			catch (IOException e) {
			}
		}

	}

	/**
	 * Prints the contacts out
	 * 
	 * @return null
	 */

	public static void printContacts(ArrayList<VirtOrgInfo> voidInfo, String contactsOutFile) {
		FileOutputStream cfs = null;
		// Deal with the contacts, if reqd.
		if (contactsOutFile != null) {
			// Print out the standard ones
			cfs = null;
			try {
				cfs = new FileOutputStream(contactsOutFile);
				PrintStream ps = new PrintStream(cfs);
				
				for (VirtOrgInfo v: voidInfo) {
					if (v.isAtMySite()) {
						ps.print("Contacts for " + v.getVoName() + ":\n");
						ArrayList<IndividualContact> ics = v.getIndividualContacts();

						for (IndividualContact ic : ics) {
							ps.print(ic.toString() + "\n");
						}
						ps.print("\n");

					}
				}
			}

			catch (IOException e) {
				System.out.print("Unable (2) to write to file" + e);
				System.exit(1);
			}
			finally {
				try {
					cfs.close();
				}
				catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Prints the LSC Files out
	 * 
	 * @return null
	 */
	public static void printLscFiles(ArrayList<VirtOrgInfo> voidInfo, String vomsDir) {
		FileOutputStream lsc = null;
		// Deal with the vosdir (LSC Files ), if reqd.
		if (vomsDir != null) {
			// Going to print out LSC files for the VOs

			// Make dirs
			for (VirtOrgInfo vo :voidInfo) {
				if (vo.isAtMySite()) {
					File newVomsDir = new File(vomsDir + "/" + vo.getVoName() + "/");
					newVomsDir.mkdir();
					ArrayList<VomsServer> vomsServers = vo.getVomsServers();
					
					for (VomsServer vs : vomsServers) {
						if (vs.isComplete()) {
							try {
								lsc = new FileOutputStream(newVomsDir + "/" + vs.getHostname() + ".lsc");
								PrintStream ps = new PrintStream(lsc);
								ps.print(vs.getDn() + "\n");
								ps.print(vs.getCaDn() + "\n");
							}
							catch (IOException e) {
								System.out.print("Unable (3) to write to file" + e);
								System.exit(1);
							}
							finally {
								try {
									lsc.close();
								}
								catch (IOException e) {
								}
							}
						}
						else {
							System.out.print("Skipping LSC file for " + vo.getVoName() + " as XML record is incomplete, " + vs.toString() + "\n");
						}
					}
				}
			}
		}
	}
	
	/**
	 * Executes some command and returns its stdout
	 * 
	 * @param cmdLine command to execute
	 * @return output from command
	 */
	public static ArrayList<String> cmdExec(String cmdLine) throws Exception {

		// Return output as a list
		ArrayList<String> output = new ArrayList<String>();

		String lineBack;
		try {
			Process p = Runtime.getRuntime().exec(cmdLine);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((lineBack = input.readLine()) != null) {
				output.add(lineBack);
			}
			input.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("Some problem occured while running " + cmdLine + " " + ex.getMessage());
		}
		return output;
	}
	
	public static void printVersion() {
		System.out.print("Copyright © The University of Liverpool, 2012 (Licensed under the Academic Free License version 3.0)\n\n");
		System.out.print("Version 1.21\n\n");
	}
	
	
	/**
	 * Prints the fqans
	 * 
	 * @return null
	 */

	public static void printFqans(ArrayList<VirtOrgInfo> voidInfo, String fqansOutFile) {
		FileOutputStream cfs = null;
		// Deal with the contacts, if reqd.
		if (fqansOutFile != null) {
			// Print out the standard ones
			cfs = null;
			try {
				cfs = new FileOutputStream(fqansOutFile);
				PrintStream ps = new PrintStream(cfs);
				
				for (VirtOrgInfo v: voidInfo) {
					if (v.isAtMySite()) {
						ArrayList<Fqan> fqans = v.getFqans();

						for (Fqan fqan : fqans) {
							if (fqan.getIsUsed()) {
							  ps.print(fqan.getFqanExpr() + "\n");
							}
						}
					}
				}
			}

			catch (IOException e) {
				System.out.print("Unable (2) to write to file" + e);
				System.exit(1);
			}
			finally {
				try {
					cfs.close();
				}
				catch (IOException e) {
				}
			}
		}
	}
	
	
}