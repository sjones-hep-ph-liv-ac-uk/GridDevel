package liv.ac.uk.vomssnooper;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;


import liv.ac.uk.snooputils.Utils;

/**
 * Control the whole job
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-02-24
 */

public class VomsSnooper {

	private String xmlFile; // the xml file that contains vo data
	private String myVos; // list of VOs that we support
	private String vodVos; // VOs that need to be printed in VOD format
	private String vodDir; // where to print the VOD files
	private String sidFile; // where to print the SID records
	private Boolean extraFields; // Whether to print some common extra fields
	private Boolean noSillySids; // Whether to omit silly SIDs, (e.g. VO_VO.LONDONGRID.AC.UK)
	private Boolean ignoreCernRule ; // Take no heed of the cern rule
	private Boolean printVodTitleLine; // Whether to print the vo.d dir name used
	private String contactsOutFile; // File to write contacts to
	private String vomsDir; // Directory where LSC files would get written

	private ArrayList<VirtOrgInfo> voidInfo = new ArrayList<VirtOrgInfo>();;

	/**
	 * Basic constructor
	 * 
	 * @param x xml file to read for the VO info
	 * @param vos list of Vos to select for this site
	 * @param vods List of vos that need to be on vo.d format
	 * @param dir Place to put the vo.d files
	 * @param out Regular output file for other (not vo.d) VOs
	 * @param ef Whether to print extra fields
	 * @param cof Contacts output file
	 * @param vd Where to write LSC files
	 * 
	 * @return null
	 */
	public VomsSnooper(String x, String vos, String vods, String dir, String out, Boolean ef, Boolean nss, Boolean icr, Boolean pvd, String cos,
			String vd) {

		xmlFile = x;
		myVos = vos;
		vodVos = vods;
		vodDir = dir;
		sidFile = out;
		extraFields = ef;
		noSillySids = nss;
		ignoreCernRule = icr;
		printVodTitleLine = pvd;
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

		for(VirtOrgInfo voi : voidInfo){
			if (myVos != null) {
				voi.setAtMySite(siteVos.containsNoCase(voi.getVoName()));
			}
			else {
				voi.setAtMySite(true);
			}
			voi.checkComplete();
		}

		// Find if some VOs need VOD format
		WordList myVodList = new WordList();
		if (vodVos != null) {
			myVodList.readWords(vodVos);
		}

		for(VirtOrgInfo v: voidInfo){
			if (vodVos != null) {
				v.setVodStyle(myVodList.containsNoCase(v.getVoName()));
			}
			else {
				v.setVodStyle(false);
			}
		}

		ArrayList<String> spares = siteVos.getSpareWords();
		if (!spares.isEmpty()) {
			System.out.print("Warning: No void info could not be found for some of your VOs:\n");
		}
		for(String spare : spares){
			System.out.print("  No void info found for : " + spare + "\n");
		}
	}

	public void printResults() {

		Utils.printVoVariables(voidInfo, sidFile, vodDir, extraFields, noSillySids, printVodTitleLine, false, ! ignoreCernRule);
		Utils.printContacts(voidInfo, contactsOutFile);
		Utils.printLscFiles(voidInfo, vomsDir);
	}

	/**
	 * Main
	 * 
	 * @return null
	 */

	// List of the CLI options
	private enum OptList {
		printvodtitle, nosillysids, ignorecernrule, extrafields, xmlfile, myvos, vodfile, voddir, outfile, help, contactsfile, vomsdir
	}

	public static void printHelpPage() {
		System.out.println("");
		System.out.println("This tool takes an XML file from the CIC portal, and ");
		System.out.println("formats it into a standard, sorted manner for Yaim. ");
		System.out.println("");
		System.out.println("Mandatory arguments: ");
		System.out.println("  --xmlfile f       # Input XML file downloaded from CIC portal");
		System.out.println("  --myvos   f       # Names of VOs that I support");
		System.out.println("  --vodfile f       # Names of VOs that must be output in VOD format");
		System.out.println("  --outfile f       # Where to write SIDs (records that can be represented in a site-info.def)");
		System.out.println("Optional arguments: ");
		System.out.println("  --help            # Prints this info");
		System.out.println("  --voddir  d       # Where to write VODs (records that cannot be represented in a site-info.def)");
		System.out.println("  --printvodtitle   # When printing VODs, put the name of the VOD file in the output");
		System.out.println("  --nosillysids     # When printing SIDs, reject ones with silly names (DNS/dot style)");
		System.out.println("  --ignorecernrule  # Ignore the cern rule (use only voms.cern.ch)");
		System.out.println("  --extrafields     # Print some extra fields (not recommended)");
		System.out.println("  --contactsfile    # Where to print the VO contacts (not recommended)");
		System.out.println("  --vomsdir         # Where to print LSC files ");
	}

	public static void main(String[] args) {

		String xmlFile = null;
		String myVos = null;
		String vodDir = null;
		String vodFile = null;
		String outFile = null;
		Boolean extraFields = false;
		Boolean noSillySids = false;
		Boolean ignoreCernRule = false;
		Boolean printVodTitle = false;
		String contactsOutFile = null;
		String vomsDir = null;

		// Announcement
		Utils.printVersion();

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
		longopts[OptList.nosillysids.ordinal()] = new LongOpt(OptList.nosillysids.name(), LongOpt.NO_ARGUMENT, sb,
				OptList.nosillysids.ordinal());
		longopts[OptList.ignorecernrule.ordinal()] = new LongOpt(OptList.ignorecernrule.name(), LongOpt.NO_ARGUMENT, sb,
				OptList.ignorecernrule.ordinal());
		longopts[OptList.printvodtitle.ordinal()] = new LongOpt(OptList.printvodtitle.name(), LongOpt.NO_ARGUMENT, sb,
				OptList.printvodtitle.ordinal());
		longopts[OptList.contactsfile.ordinal()] = new LongOpt(OptList.contactsfile.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.contactsfile.ordinal());
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
				printHelpPage();
				System.exit(1);
			}

			if ((char) (new Integer(sb.toString())).intValue() == OptList.help.ordinal()) {
				printHelpPage();
				System.exit(1);
			}

			if ((char) (new Integer(sb.toString())).intValue() == OptList.extrafields.ordinal()) {
				extraFields = true;
			}
			if ((char) (new Integer(sb.toString())).intValue() == OptList.nosillysids.ordinal()) {
				noSillySids = true;
			}
			if ((char) (new Integer(sb.toString())).intValue() == OptList.ignorecernrule.ordinal()) {
				ignoreCernRule = true;
			}
			if ((char) (new Integer(sb.toString())).intValue() == OptList.printvodtitle.ordinal()) {
				printVodTitle = true;
			}
			if ((char) (new Integer(sb.toString())).intValue() == OptList.xmlfile.ordinal()) {
				xmlFile = ((arg != null) ? arg : "null");
			}
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.myvos.ordinal()) {
				myVos = ((arg != null) ? arg : "null");
			}
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.vodfile.ordinal()) {
				vodFile = ((arg != null) ? arg : "null");
			}
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.voddir.ordinal()) {
				vodDir = ((arg != null) ? arg : "null");
			}
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.outfile.ordinal()) {
				outFile = ((arg != null) ? arg : "null");
			}
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.contactsfile.ordinal()) {
				contactsOutFile = ((arg != null) ? arg : "null");
			}
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.vomsdir.ordinal()) {
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

		VomsSnooper vs = new VomsSnooper(xmlFile, myVos, vodFile, vodDir, outFile, extraFields, noSillySids, ignoreCernRule, printVodTitle,
				contactsOutFile, vomsDir);

		// Parse the XML File
		vs.parse();

		// Print out various results
		vs.printResults();
	}
}
