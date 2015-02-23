package com.basingwerk.utilisation;

import java.io.File;
import org.jfree.ui.RefineryUtilities;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Utilisation {

	public static void printVersion() {
		System.out.print("Copyright (c) The University of Liverpool, 2014 (Licensed under the GNU General Public License, version 3)\n\n");
		System.out.print("Version 1.0\n\n");
	}	
	public static void main(final String[] args) {
		String theLogFile = null;
		
		Options options = new Options();

		Option helpOption = new Option("h", "help", false, "print this help");
		Option serverURLOption = new Option("l", "logfile", true,"log file");

		options.addOption(helpOption);
		options.addOption(serverURLOption);

		CommandLineParser argsParser = new PosixParser();

		try {
			CommandLine commandLine = argsParser.parse(options, args);

			if (commandLine.hasOption(helpOption.getOpt())) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "Usage grapher", options );
				System.exit(0);
			}

			theLogFile = commandLine.getOptionValue(serverURLOption.getOpt());
			String[] otherArgs = commandLine.getArgs();

			if (theLogFile == null || otherArgs.length > 1) {
				System.out.println("Please specify a logfile");
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "Usage grapher", options );
				System.exit(0);
			}

		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
		}

		final UsagePlotter demo = new UsagePlotter("Usage", new File(theLogFile));
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}
}
