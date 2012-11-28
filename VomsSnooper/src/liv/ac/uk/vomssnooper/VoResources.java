package liv.ac.uk.vomssnooper; 

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.File;
import java.util.ArrayList;
import liv.ac.uk.snooputils.Utils;

/**
 * List the resource requirements of a VO, in the form of a wiki table
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-11-27
 * 
 */
public class VoResources {

	private String xmlFile; // the xml file that contains vo data
	private String myVos;   // list of VOs that we support
	private String resFile; // table where VO resource requirements are written

	private ArrayList<VirtOrgInfo> voidInfo = new ArrayList<VirtOrgInfo>();;

	/**
	 * Basic constructor
	 * 
	 * @param xmlFile xml file to read for the VO info
	 * @param myVos list of Vos to select for this site
	 * @param resFile where VO resource requirements are written
	 * 
	 * @return null
	 * 
	 */
	public VoResources(String xmlFile, String myVos, String resFile) {
		this.xmlFile = xmlFile;
		this.myVos = myVos;
		this.resFile = resFile;
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
		if (myVos != null) {
			vos.readWords(myVos);
		}

		// Get the ones at my site
		for(VirtOrgInfo voi: voidInfo){
			if (myVos != null) {
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
		Utils.printResourcesTable(voidInfo, resFile);
	}

	/**
	 * Main
	 * 
	 * @return null
	 */

	// List of the CLI options
	private enum OptList {
		help, xmlfile, myvos, resfile
	}

	public static void printHelpPage() {
		System.out.println("");
		System.out.println("The VoResources tool takes an XML file from the CIC portal, and ");
		System.out.println("creates a set of LSC files from the data, bypassing Yaim.");
		System.out.println("");
		System.out.println("Mandatory arguments: ");
		System.out.println("  --xmlfile       f       # Input XML file downloaded from CIC portal");
		System.out.println("  --myvos         f       # File of names of VOs that I support");
		System.out.println("  --res           f       # File to print resources to");
		System.out.println("Optional arguments: ");
		System.out.println("  --help                  # Prints this info");
	}

	public static void main(String[] args) {

		String xmlFile = null;
		String myvos = null;
		String resFile = null;

		// Arg processing
		StringBuffer sb = new StringBuffer();
		String arg;
		LongOpt[] longopts = new LongOpt[OptList.values().length];

		longopts[OptList.help.ordinal()] = new LongOpt(OptList.help.name(), LongOpt.NO_ARGUMENT, sb, OptList.help.ordinal());
		longopts[OptList.xmlfile.ordinal()] = new LongOpt(OptList.xmlfile.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.xmlfile.ordinal());
		longopts[OptList.myvos.ordinal()] = new LongOpt(OptList.myvos.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.myvos.ordinal());
		longopts[OptList.resfile.ordinal()] = new LongOpt(OptList.resfile.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.resfile.ordinal());

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
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.myvos.ordinal()) {
				myvos = ((arg != null) ? arg : "null");
			}
			else if ((char) (new Integer(sb.toString())).intValue() == OptList.resfile.ordinal()) {
				resFile = ((arg != null) ? arg : "null");
			}
		}

		// myvos is mandatory input
		if (myvos == null) {
			System.out.print("The --myvos argument must be given\n");
			System.exit(1);
		}
		// xmlFile is mandatory input
		if (xmlFile == null) {
			System.out.print("The --xmlfile argument must be given\n");
			System.exit(1);
		}
		
		// as is the vomsDir
		if (resFile == null) {
			System.out.print("The --res argument must be given\n");
			System.exit(1);
		}

		if (!(new File(myvos)).isFile()) {
			System.out.print("The --myvos (" + myvos + ") is not a file\n");
			System.exit(1);
		}
		if (!(new File(xmlFile)).isFile()) {
			System.out.print("The --xmlfile(" + xmlFile  + ") is not a file\n");
			System.exit(1);
		}

		// Make the controller class
		VoResources VoResources = new VoResources(xmlFile, myvos, resFile);

		// Parse the XML File
		VoResources.parse();

		// Print out various results
		VoResources.printResults();
	}
}
