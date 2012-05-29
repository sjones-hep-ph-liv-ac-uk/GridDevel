package liv.ac.uk.vomssnooper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import liv.ac.uk.vomssnooper.VirtOrgInfo.ByVoName;

/**
 * General spot for nick knacks
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
	private static void printStandardStyle(VirtOrgInfo v, PrintStream ps, Boolean extraFields, Boolean noSillySids) {

		ArrayList<String> vomsLines = v.getVomsLines(true, extraFields);
		Iterator<String> i = vomsLines.iterator();
		while (i.hasNext()) {
			String vs = i.next();

			String n = v.getVoName().toUpperCase() ;
			
			if (noSillySids) {
				if (n.contains(".") == false) {
  			  ps.print("VO_" + n + "_" + vs + "\n");
				}
				else {
					System.out.print("Silly SID was dropped - " + n + "\n");
  			  ps.print("# VO_" + n + "_" + vs + "\n");
				}
			}
			else {
				if (n.contains(".") == true) {
					System.out.print("Silly SID was found, but printed anyway - " + n + "\n");
				}
  			ps.print("VO_" + n + "_" + vs + "\n");
			}
		}
		ps.print("\n");
	}

	/**
	 * Prints in the vo.d style
	 * 
	 * @param The vo to print
	 * @return null
	 */
	private static void printVodStyle(VirtOrgInfo v, String vodDir, Boolean extraFields, Boolean printVodTitleLine) {

		String filename = vodDir + "/" + v.getVoName();
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename);
			PrintStream ps = new PrintStream(fos);
			ArrayList<String> vomsLines = v.getVomsLines(true, extraFields);
			
			if (printVodTitleLine) {
				ps.print("# $YAIM_LOCATION/vo.d/" + v.getVoName() + "\n");
			}
			Iterator<String> i = vomsLines.iterator();
			while (i.hasNext()) {
				String vs = i.next();
				ps.print(vs + "\n");
			}
			ps.print("\n");
		}

		catch (IOException e) {
			System.out.print("Unable (1) to write to file" + e);
			System.exit(1);
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
			}
		}
	}
	/**
	 * Prints the VO variable results, in VOD or SID format
	 * SID format is that used in site-info.def files. But when
	 * DNS style VO names were brought in, there was a problem
	 * using VO variables in SID format, because of bash restrictions
	 * on using the dot character in variable names. So VOD format
	 * was introduced. In this format, discrete files are written
	 * for each VO in a vo.d directory.
	 * 
	 * @return null
	 */
	public static void printVoVariables(ArrayList<VirtOrgInfo> voidInfo, String sidFile, String vodDir , 
			Boolean extraFields, Boolean noSillySids, Boolean printVodTitle) {

		Collections.sort(voidInfo, new ByVoName());

		// Print out the standard ones
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(sidFile);
			PrintStream ps = new PrintStream(fos);
			Iterator<VirtOrgInfo> allIt = voidInfo.iterator();
			while (allIt.hasNext()) {

				VirtOrgInfo v = allIt.next();
				if (v.isAtMySite()) {
					if (v.isVodStyle()) {
						printVodStyle(v, vodDir, extraFields, printVodTitle);
					} else {
						printStandardStyle(v, ps, extraFields,noSillySids);
					}
				}
			}
		}

		catch (IOException e) {
			System.out.print("Unable to Write to file" + e);
			System.exit(1);
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
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
				Iterator<VirtOrgInfo> allIt = voidInfo.iterator();
				while (allIt.hasNext()) {

					VirtOrgInfo v = allIt.next();
					if (v.isAtMySite()) {
						ps.print("Contacts for " + v.getVoName() + ":\n");
						ArrayList<IndividualContact> ics = v.getIndividualContacts();

						Iterator<IndividualContact> i = ics.iterator();
						while (i.hasNext()) {
							IndividualContact ic = i.next();
							ps.print(ic.toString() + "\n");
						}
						ps.print("\n");

					}
				}
			}

			catch (IOException e) {
				System.out.print("Unable (2) to write to file" + e);
				System.exit(1);
			} finally {
				try {
					cfs.close();
				} catch (IOException e) {
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
			Iterator<VirtOrgInfo> allIt = voidInfo.iterator();
			while (allIt.hasNext()) {

				VirtOrgInfo vo = allIt.next();
				if (vo.isAtMySite()) {
					File newVomsDir = new File(vomsDir + "/" + vo.getVoName() + "/");
					newVomsDir.mkdir();
					ArrayList<VomsServer> vomsServers = vo.getVomsServers();
					Iterator<VomsServer> i = vomsServers.iterator();
					while (i.hasNext()) {

						VomsServer vs = i.next();
						if (vs.isComplete()) {
							try {
								lsc = new FileOutputStream(newVomsDir + "/" + vs.getHostname() + ".lsc");
								PrintStream ps = new PrintStream(lsc);
								ps.print(vs.getDn() + "\n");
								ps.print(vs.getCaDn() + "\n");
							} catch (IOException e) {
								System.out.print("Unable (3) to write to file" + e);
								System.exit(1);
							} finally {
								try {
									lsc.close();
								} catch (IOException e) {
								}
							}
						} else {
							System.out.print("Skipping LSC file for " + vo.getVoName() + " as XML record is incomplete, " + vs.toString() + "\n");
						}
					}
				}
			}
		}
	}
}
