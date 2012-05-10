package liv.ac.uk.vomssnooper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SidParser {
	
	private String oldSidDir;
	private ArrayList<VirtOrgInfo> allVoidInfo;

	public SidParser(String os, ArrayList<VirtOrgInfo> v) {
		oldSidDir = os;
		allVoidInfo = v;
	}
	
	/**
	 * Parse the VOID XML file
	 * @return null
	 */
	public void parseDocument() {
		try {

			FileInputStream fstream = new FileInputStream(new File (oldSidDir + "/site-info.def"));

			DataInputStream in = new DataInputStream(fstream);

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				System.out.print(strLine);
			}
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
		}

	}
	

}
