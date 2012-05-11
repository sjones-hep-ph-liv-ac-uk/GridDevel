package liv.ac.uk.vomssnooper;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * blah
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-05-10
 */

public class SidFormatter {

	/**
	 * Private class to facilitate a sort function
	 * 
	 * @author sjones
	 * 
	 */
	private class ByVoName implements java.util.Comparator<VirtOrgInfo> {
		public int compare(VirtOrgInfo first, VirtOrgInfo second) {
			return first.getVoNickName().compareTo(second.getVoNickName());
		}
	}

	private String oldSidDir;
	private String newSidDir;

	private HashMap<String, VirtOrgInfo> voidInfo ;

	/**
	 * Basic constructor
	 * 
	 * @param x
	 *          x
	 * 
	 * @return null
	 */
	public SidFormatter(String os, String ns) {
		oldSidDir = os;
		newSidDir = ns;
		voidInfo = new HashMap<String, VirtOrgInfo>();
	}

	/**
	 * Does all the parsing and selection
	 * 
	 * @return null
	 */
	public void parse() {

		SidParser sp = new SidParser(oldSidDir, voidInfo);

		sp.parseDocument();

	}
	
	public void printResults(){
		ArrayList<VirtOrgInfo> v = new ArrayList<VirtOrgInfo>(voidInfo.values ()); 
		Utils.printVoVariables(v, newSidDir + "/site-info.def", newSidDir + "/vo.d", false);
	}

	/**
	 * Main
	 * 
	 * @return null
	 */

	// List of the CLI options
	private enum OptList {
		oldsiddir, newsiddir, help
	}

	public static void main(String[] args) {

		String oldSidDir = null;
		String newSidDir = null;

		// Announcement
		System.out.print("Copyright © The University of Liverpool, 2012 (Licensed under the Academic Free License version 3.0)\n\n");
		System.out.print("Version 1.0\n\n");

		StringBuffer sb = new StringBuffer();
		String arg;

		LongOpt[] longopts = new LongOpt[OptList.values().length];

		longopts[OptList.oldsiddir.ordinal()] = new LongOpt(OptList.oldsiddir.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.oldsiddir.ordinal());
		longopts[OptList.newsiddir.ordinal()] = new LongOpt(OptList.newsiddir.name(), LongOpt.REQUIRED_ARGUMENT, sb,
				OptList.newsiddir.ordinal());

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

			if ((char) (new Integer(sb.toString())).intValue() == OptList.oldsiddir.ordinal()) {
				oldSidDir = ((arg != null) ? arg : "null");
			}

			if ((char) (new Integer(sb.toString())).intValue() == OptList.newsiddir.ordinal()) {
				newSidDir = ((arg != null) ? arg : "null");
			}
		}

		if (oldSidDir == null) {
			System.out.print("The --oldsiddir argument must be given\n");
			System.exit(1);
		}

		if (!(new File(oldSidDir)).isDirectory()) {
			System.out.print("The --oldSidDir(" + oldSidDir + ") doesn't exist\n");
			System.exit(1);
		}

		if (newSidDir == null) {
			System.out.print("The --newsiddir argument must be given\n");
			System.exit(1);
		}
		if (!(new File(newSidDir)).isDirectory()) {
			System.out.print("The --newSidDir(" + newSidDir + ") doesn't exist\n");
			System.exit(1);
		}

		// Make the Controller class
		SidFormatter sf = new SidFormatter(oldSidDir, newSidDir);

		// Parse the File
		sf.parse();
		
		// Print out various results
		sf.printResults();
		
	}
}
