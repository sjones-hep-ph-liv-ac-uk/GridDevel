package liv.ac.uk.vomssnooper;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import liv.ac.uk.snooputils.Utils;

/**
 * Reads in a site-info.def file (and related vo.d files) and formats them out in sorted and standard manner
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-05-10
 */

public class SidFormatter {

	private String oldSidDir;     // the location of the old site-info.def file (and vo.d directory)
	private String newSidDir;     // the location of the new site-info.def file (and vo.d directory)
	private Boolean flat;         // flat output, for diff
	private String mvVOsFileName; // list of VOs that we support
	private WordList myVOs;       // VOs supported here

	// I'll index the VO info on a name
	private HashMap<String, VirtOrgInfo> voidInfo;

	/**
	 * Basic constructor
	 * 
	 * @param os old SiteInfo.def dir
	 * @param ns new SiteInfo.def dir
	 * @param f file of VOs that I support
	 * @param mv VOs that I support
	 * 
	 * @return null
	 */
	public SidFormatter(String os, String ns, Boolean f, String mv) {
		oldSidDir = os;
		newSidDir = ns;
		flat = f;
		voidInfo = new HashMap<String, VirtOrgInfo>();
		mvVOsFileName = mv;
		myVOs = new WordList();
		myVOs.readWords(mvVOsFileName);
	}

	/**
	 * Does all the parsing and selection
	 * 
	 * @return null
	 */
	public void parse() {

		SidParser sp = new SidParser(oldSidDir, voidInfo, myVOs);

		sp.parseDocument();

	}

	/**
	 * Pass the results info a utility for printing
	 * 
	 * @return null
	 */
	public void printResults() {
		ArrayList<VirtOrgInfo> v = new ArrayList<VirtOrgInfo>(voidInfo.values());
		Utils.printVoVariables(v, newSidDir + "/site-info.def", newSidDir + "/vo.d", false, false, false, flat, false);
	}

	/**
	 * Main
	 * 
	 * @return null
	 */

	// List of the CLI options
	private enum OptList {
		oldsiddir, newsiddir, flat, help, myvos,
	}

	public static void printHelpPage() {
		System.out.println("");
		System.out.println("The SidFormatter tool takes an existing populated site-info.def file and vo.d directory, and ");
		System.out.println("formats it into a standard, sorted manner. It writes its output into a new, ");
		System.out.println("unpopulated site-info.def file and vo.d directory.");
		System.out.println("");
		System.out.println("Mandatory arguments: ");
		System.out
				.println("  --oldsiddir dir   # Some existing directory that contains a populated site-info.def file and vo.d directory");
		System.out
				.println("  --newsiddir dir   # Some existing directory that contains an unpopulated site-info.def file and vo.d directory");
		System.out.println("  --myvos   f       # Names of VOs that I support");
		System.out.println("Optional arguments: ");
		System.out.println("  --flat            # Print all the records out in a site-info.def, even when they are silly sids");
		System.out.println("  --help            # Prints this info");
	}

	public static void main(String[] args) {

		String oldSidDir = null;
		String newSidDir = null;
		Boolean flat = false;
		String myVos = null;

		// Arg processing
		StringBuffer sb = new StringBuffer();
		String arg;
		LongOpt[] longopts = new LongOpt[OptList.values().length + 1];

		longopts[OptList.help.ordinal()] = new LongOpt(OptList.help.name(), LongOpt.NO_ARGUMENT, sb, OptList.help.ordinal());
		longopts[OptList.oldsiddir.ordinal()] = new LongOpt(OptList.oldsiddir.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.oldsiddir.ordinal());
		longopts[OptList.newsiddir.ordinal()] = new LongOpt(OptList.newsiddir.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.newsiddir.ordinal());
		longopts[OptList.myvos.ordinal()] = new LongOpt(OptList.myvos.name(), LongOpt.REQUIRED_ARGUMENT, sb, OptList.myvos.ordinal());
		longopts[OptList.flat.ordinal()] = new LongOpt(OptList.flat.name(), LongOpt.NO_ARGUMENT, sb, OptList.flat.ordinal());

		// Get the options
		Getopt g = new Getopt("testprog", args, "", longopts);
		g.setOpterr(false);

		int c = -1;
		try {
			c = g.getopt();
		}
		catch (NullPointerException e) {
			System.out.println("Could not parse those options ; " + e.getMessage());
			printHelpPage();
			System.exit(1);
		}

		// Process the options
		while (c != -1) {

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

			// Option for old site-info.def setup
			if ((char) (new Integer(sb.toString())).intValue() == OptList.oldsiddir.ordinal()) {
				oldSidDir = ((arg != null) ? arg : "null");
			}

			// Option for new site-info.def setup
			if ((char) (new Integer(sb.toString())).intValue() == OptList.newsiddir.ordinal()) {
				newSidDir = ((arg != null) ? arg : "null");
			}

			// Option for my VOs list
			if ((char) (new Integer(sb.toString())).intValue() == OptList.myvos.ordinal()) {
				myVos = ((arg != null) ? arg : "null");
			}

			// Flat output (for diff)
			if ((char) (new Integer(sb.toString())).intValue() == OptList.flat.ordinal()) {
				flat = true;
			}

			// Get next option
			try {
				c = g.getopt();
			}
			catch (NullPointerException e) {
				System.out.println("Could not parse those options: " + e.getMessage());
				printHelpPage();
				System.exit(1);
			}
		}

		// Validate the options
		if (oldSidDir == null) {
			System.out.print("The --oldsiddir argument must be given\n");
			System.exit(1);
		}

		if (!(new File(oldSidDir)).isDirectory()) {
			System.out.print("The --oldsiddir (" + oldSidDir + ") doesn't exist\n");
			System.exit(1);
		}

		if (newSidDir == null) {
			System.out.print("The --newsiddir argument must be given\n");
			System.exit(1);
		}
		if (!(new File(newSidDir)).isDirectory()) {
			System.out.print("The --newsiddir (" + newSidDir + ") doesn't exist\n");
			System.exit(1);
		}

		if (myVos == null) {
			System.out.print("You must give the --myvos argument\n");
			System.exit(1);
		}

		if (!(new File(myVos)).isFile()) {
			System.out.print("The --myvos file (" + myVos + ") doesn't exist\n");
			System.exit(1);
		}

		// Make an object to organise the run
		SidFormatter sf = new SidFormatter(oldSidDir, newSidDir, flat, myVos);

		// Parse the File
		sf.parse();

		// Print out various results
		sf.printResults();

	}
}
