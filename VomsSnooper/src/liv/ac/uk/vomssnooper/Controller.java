package liv.ac.uk.vomssnooper;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Control the whole job
 * @author      Steve Jones  <sjones@hep.ph.liv.ac.uk>
 * @since       2012-02-24          
 */

public class Controller {
	
	/**
	 * Private class to facilitate a sort function
	 * @author sjones
	 *
	 */
	private class ByVoName implements java.util.Comparator<VirtOrgInfo> {
		public int compare(VirtOrgInfo first, VirtOrgInfo second) {
			return first.getVoNickName().compareTo(second.getVoNickName());
		}
	}

	private String xmlFile;  // the xml file that contains vo data
	private String myVos;    // list of VOs that we support
	private String vodVos;   // VOs that need to be printed in VOD format
	private String vodDir;   // where to print the VOD files
	private String sidFile;  // where to print the SID records
	private Boolean extraFields;    // Whether to print some common extra fields
	private String contactsOutFile; // File to write contacts to
	private String vomsDir;         // Directory where LSC files would get written

	
	private ArrayList<VirtOrgInfo> voidInfo = new ArrayList<VirtOrgInfo>();;

	/**
	 * Basic constructor
	 *  
	 * @param x
	 *          xml file to read for the VO info
	 * @param vos
	 *          list of Vos to select for this site
	 * @param vods
	 *          List of vos that need to be on vo.d format
	 * @param dir
	 *          Place to put the vo.d files
	 * @param out
	 *          Regular output file for other (not vo.d) VOs
	 * @param ef
	 *          Whether to print extra fields
	 * @param cof
	 *          Contacts output file
	 * @param vd
	 *          Where to write LSC files
	 *          
	 * @return null
	 */
	public Controller(String x, String vos, String vods, String dir, String out, Boolean ef, String cos, String vd) {

		xmlFile = x;
		myVos = vos;
		vodVos = vods;
		vodDir = dir;
		sidFile = out;
		extraFields = ef;
		contactsOutFile = cos;
		vomsDir = vd;
	}

	/**
	 * Does all the parsing and selection
	 * 
	 * @return null
	 */
	public void parse() {

		VoidCardXmlParser spe = new VoidCardXmlParser(xmlFile, (ArrayList<VirtOrgInfo>) voidInfo);

		spe.parseDocument();

		WordList siteVos = new WordList();
		if (myVos != null) {
			siteVos.readWords(myVos);
		}

		Iterator<VirtOrgInfo> it = voidInfo.iterator();
		while (it.hasNext()) {
			VirtOrgInfo voi = it.next();
			if (myVos != null) {
				voi.setAtMySite(siteVos.isInList(voi.getVoName()));
			} else {
				voi.setAtMySite(true);
			}
			voi.checkComplete();
		}

		// Find if some VOs need VOD format
		WordList myVodList = new WordList();
		if (vodVos != null) {
			myVodList.readWords(vodVos);
		}

		it = voidInfo.iterator();
		while (it.hasNext()) {
			VirtOrgInfo v = it.next();
			if (vodVos != null) {
				v.setVodStyle(myVodList.isInList(v.getVoName()));
			} else {
				v.setVodStyle(false);
			}
		}

		ArrayList<String> spares = siteVos.getSpareWords();

		Iterator<String> s = spares.iterator();
		if (s.hasNext()) {
			System.out.print("Warning: No void info could not be found for some of your VOs:\n");
		}
		while (s.hasNext()) {
			String spare = s.next();
			System.out.print("  No void info found for : " + spare + "\n");
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
	public void printVoVariables() {

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
						printVodStyle(v);
					} else {
						printStandardStyle(v, ps);
					}
				}
			}
		}

		catch (IOException e) {
			System.out.print("Unable to write to file" + e);
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

	public void printContacts() {
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
				System.out.print("Unable to write to file" + e);
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
	public void printLscFiles() {
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
								System.out.print("Unable to write to file" + e);
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

	/**
	 * Prints the SID in regular output file
	 * 
	 * @param v
	 *          The vo to print
	 * @param ps
	 *          Where to print it
	 * @return null
	 */
	private void printStandardStyle(VirtOrgInfo v, PrintStream ps) {

		ArrayList<String> vomsLines = v.getVomsLines(true, extraFields);
		Iterator<String> i = vomsLines.iterator();
		while (i.hasNext()) {
			String vs = i.next();
			ps.print("VO_" + v.getVoName().toUpperCase() + "_" + vs + "\n");
		}
		ps.print("\n");
	}

	/**
	 * Prints in the vo.d style
	 * 
	 * @param v
	 *          The vo to print
	 * @return null
	 */
	private void printVodStyle(VirtOrgInfo v) {

		String filename = vodDir + "/" + v.getVoName();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename);
			PrintStream ps = new PrintStream(fos);
			ArrayList<String> vomsLines = v.getVomsLines(true, extraFields);
			Iterator<String> i = vomsLines.iterator();
			while (i.hasNext()) {
				String vs = i.next();
				ps.print(vs + "\n");
			}
			ps.print("\n");
		}

		catch (IOException e) {
			System.out.print("Unable to write to file" + e);
			System.exit(1);
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Main
	 * 
	 * @return null
	 */

	// List of the CLI options
	private enum OptList {
		extrafields, xmlfile, myvos, vodfile, voddir, outfile, help, contactsoutfile, vomsdir
	}

	public static void main(String[] args) {

		String xmlFile = null;
		String myVos = null;
		String vodDir = null;
		String vodFile = null;
		String outFile = null;
		Boolean extraFields = false;
		String contactsOutFile = null;
		String vomsDir = null;

		// Announcement
		System.out.print("Copyright © The University of Liverpool, 2012 (Licensed under the Academic Free License version 3.0)\n\n");
		System.out.print("Version 1.5\n\n");

		System.out.print("This program does not provide VO_RBS, VOMS_EXTRA_MAPS nor VOMS_POOL_PATH parameters\n\n");

		StringBuffer sb = new StringBuffer();
		String arg;

		LongOpt[] longopts = new LongOpt[OptList.values().length];

		longopts[OptList.help.ordinal()] = new LongOpt(OptList.help.name(), LongOpt.NO_ARGUMENT, sb, OptList.help.ordinal());
		longopts[OptList.xmlfile.ordinal()] = new LongOpt(OptList.xmlfile.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.xmlfile.ordinal());
		longopts[OptList.myvos.ordinal()] = new LongOpt(OptList.myvos.name(), LongOpt.REQUIRED_ARGUMENT, sb, OptList.myvos.ordinal());
		longopts[OptList.vodfile.ordinal()] = new LongOpt(OptList.vodfile.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.vodfile.ordinal());
		longopts[OptList.voddir.ordinal()] = new LongOpt(OptList.voddir.name(), LongOpt.REQUIRED_ARGUMENT, sb, OptList.voddir.ordinal());
		longopts[OptList.outfile.ordinal()] = new LongOpt(OptList.outfile.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.outfile.ordinal());
		longopts[OptList.extrafields.ordinal()] = new LongOpt(OptList.extrafields.name(), LongOpt.NO_ARGUMENT, sb,
				OptList.extrafields.ordinal());
		longopts[OptList.contactsoutfile.ordinal()] = new LongOpt(OptList.contactsoutfile.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.contactsoutfile.ordinal());
		longopts[OptList.vomsdir.ordinal()] = new LongOpt(OptList.vomsdir.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.vomsdir.ordinal());

		Getopt g = new Getopt("testprog", args, "", longopts);
		g.setOpterr(false);

		int c;
		while ((c = g.getopt()) != -1) {

			arg = g.getOptarg();

			// It only takes long options
			if (c != 0) {
				System.out.print("Some option was given that I don't understand, " + sb.toString() + " \n");
				System.exit(1);
			}

			if ((char) (new Integer(sb.toString())).intValue() == OptList.help.ordinal()) {
				System.out.print("You asked for help.\n");
				System.exit(1);
			}

			if ((char) (new Integer(sb.toString())).intValue() == OptList.extrafields.ordinal()) {
				extraFields = true;
			}

			if ((char) (new Integer(sb.toString())).intValue() == OptList.xmlfile.ordinal()) {
				xmlFile = ((arg != null) ? arg : "null");
			} else if ((char) (new Integer(sb.toString())).intValue() == OptList.myvos.ordinal()) {
				myVos = ((arg != null) ? arg : "null");
			} else if ((char) (new Integer(sb.toString())).intValue() == OptList.vodfile.ordinal()) {
				vodFile = ((arg != null) ? arg : "null");
			} else if ((char) (new Integer(sb.toString())).intValue() == OptList.voddir.ordinal()) {
				vodDir = ((arg != null) ? arg : "null");
			} else if ((char) (new Integer(sb.toString())).intValue() == OptList.outfile.ordinal()) {
				outFile = ((arg != null) ? arg : "null");
			} else if ((char) (new Integer(sb.toString())).intValue() == OptList.contactsoutfile.ordinal()) {
				contactsOutFile = ((arg != null) ? arg : "null");
			} else if ((char) (new Integer(sb.toString())).intValue() == OptList.vomsdir.ordinal()) {
				vomsDir = ((arg != null) ? arg : "null");
			}
		}

		// voidCardFile is mandatory input
		if (xmlFile == null) {
			System.out.print("The --xmlfile argument must be given\n");
			System.exit(1);
		}

		// outFile is mandatory output
		if (outFile == null) {
			System.out.print("The --outfile argument must be given\n");
			System.exit(1);
		}

		// If you give a vosfile, it must exist
		if (myVos != null) {
			if (!(new File(myVos)).isFile()) {
				System.out.print("The --myvos file (" + myVos + ") doesn't exist\n");
				System.exit(1);
			}
		}

		// If you give a vodfile, it must exist, and you need a voddir
		if (vodFile != null) {
			if (!(new File(vodFile)).isFile()) {
				System.out.print("The --vodfile (" + vodFile + ") doesn't exist\n");
				System.exit(1);
			}
			if (vodDir == null) {
				System.out.print("If you provide the --vodfile arg, then you must provide the --voddir arg too\n");
				System.exit(1);
			}
			if (!(new File(vodDir)).isDirectory()) {
				System.out.print("The --voddir (" + vodDir + ") is not a directory\n");
				System.exit(1);
			}
		}

    // Print LSC files in vomsdir?
		if (vomsDir != null) {
			if (!(new File(vomsDir)).isDirectory()) {
				System.out.print("The --vomdsdir (" + vomsDir + ") is not a directory\n");
				System.exit(1);
			}
		}

		// Make the Controller class
		Controller controller = new Controller(xmlFile, myVos, vodFile, vodDir, outFile, extraFields, contactsOutFile, vomsDir);

		// Parse the XML File
		controller.parse();
		
		// Print out various results
		controller.printVoVariables();
		controller.printContacts();
		controller.printLscFiles();
	}
}
