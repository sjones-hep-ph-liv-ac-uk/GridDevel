package liv.ac.uk.vomssnooper;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.File;
import java.util.ArrayList;

import liv.ac.uk.snooputils.Utils;

/**
 * Convert CIC Portal data to LSC files directly
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-06-15
 * 
 */
public class CicToLsc {

	private String xmlFile; // the xml file that contains vo data
	private String approvedVos; // list of VOs that we support
	private String vomsDir; // directory where LSC files would get written

	private ArrayList<VirtOrgInfo> voidInfo = new ArrayList<VirtOrgInfo>();;

	/**
	 * Basic constructor
	 * 
	 * @param xmlFile xml file to read for the VO info
	 * @param approvedVos list of Vos to select for this site
	 * @param vomsDir where to write LSC files
	 * 
	 * @return null
	 * 
	 */
	public CicToLsc(String xmlFile, String approvedVos, String vomsDir) {
		this.xmlFile = xmlFile;
		this.approvedVos = approvedVos;
		this.vomsDir = vomsDir;
	}

	/**
	 * Does all the parsing and selection
	 * 
	 * @return null
	 */
	public void parse() {

		// Parse that XML file
		VoidCardXmlParser parser = new VoidCardXmlParser(xmlFile, (ArrayList<VirtOrgInfo>) voidInfo);
		parser.parseDocument();

		// Get a list of VOs to support
		WordList vos = new WordList();
		if (approvedVos != null) {
			vos.readWords(approvedVos);
		}

		// Get the ones at my site
		for(VirtOrgInfo voi: voidInfo){
			if (approvedVos != null) {
				voi.setAtMySite(vos.containsNoCase((voi.getVoName())));
			}
			else {
				voi.setAtMySite(true);
			}
			voi.checkComplete();
		}

		// Find any VOs supposedly at my site, but which were not found in the XML
		ArrayList<String> spares = vos.getSpareWords();
		if (!spares.isEmpty()) {
			System.out.print("Warning: No void info could not be found for some of your VOs:\n");
		}
		
		for(String spare: spares){
			System.out.print("  No void info found for : " + spare + "\n");
		}
	}

	public void printResults() {
		Utils.printLscFiles(voidInfo, vomsDir);
	}

	/**
	 * Main
	 * 
	 * @return null
	 */

	// List of the CLI options
	private enum OptList {
		help, xmlfile, approvedvos, vomsdir
	}

	public static void printHelpPage() {
		System.out.println("");
		System.out.println("The CicToLsc tool takes an XML file from the CIC portal, and ");
		System.out.println("creates a set of LSC files from the data, bypassing Yaim.");
		System.out.println("");
		System.out.println("Mandatory arguments: ");
		System.out.println("  --xmlfile       f       # Input XML file downloaded from CIC portal");
		System.out.println("  --approvedvos   f       # File of names of VOs that I support");
		System.out.println("  --vomsdir               # Where to print LSC Files ");
		System.out.println("Optional arguments: ");
		System.out.println("  --help                  # Prints this info");
	}

	public static void main(String[] args) {

		String xmlFile = null;
		String approvedVos = null;
		String vomsDir = null;

		// Arg processing
		StringBuffer sb = new StringBuffer();
		String arg;
		LongOpt[] longopts = new LongOpt[OptList.values().length];

		longopts[OptList.help.ordinal()] = new LongOpt(OptList.help.name(), LongOpt.NO_ARGUMENT, sb, OptList.help.ordinal());
		longopts[OptList.xmlfile.ordinal()] = new LongOpt(OptList.xmlfile.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.xmlfile.ordinal());
		longopts[OptList.approvedvos.ordinal()] = new LongOpt(OptList.approvedvos.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.approvedvos.ordinal());
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

			if ((char) (new Integer(sb.toString())).intValue() == OptList.xmlfile.ordinal()) {
				xmlFile = ((arg != null) ? arg : "null");
			}
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.approvedvos.ordinal()) {
				approvedVos = ((arg != null) ? arg : "null");
			}
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.vomsdir.ordinal()) {
				vomsDir = ((arg != null) ? arg : "null");
			}
		}

		// approvedvos is mandatory input
		if (approvedVos == null) {
			System.out.print("The --approvedvos argument must be given\n");
			System.exit(1);
		}
		// xmlFile is mandatory input
		if (xmlFile == null) {
			System.out.print("The --xmlfile argument must be given\n");
			System.exit(1);
		}
		
		// as is the vomsDir
		if (vomsDir == null) {
			System.out.print("The --vomsdir argument must be given\n");
			System.exit(1);
		}

		// check vomsDir
		if (!(new File(vomsDir)).isDirectory()) {
			System.out.print("The --vomdsdir (" + vomsDir + ") is not a directory\n");
			System.exit(1);
		}

		// Make the controller class
		CicToLsc cicToLsc = new CicToLsc(xmlFile, approvedVos, vomsDir);

		// Parse the XML File
		cicToLsc.parse();

		// Print out various results
		cicToLsc.printResults();
	}
}
