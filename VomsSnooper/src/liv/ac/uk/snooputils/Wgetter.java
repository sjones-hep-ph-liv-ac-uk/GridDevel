package liv.ac.uk.snooputils;
/**
 * Java wget routine
 * @author      Steve Jones  <sjones@hep.ph.liv.ac.uk>
 * @since       2012-07-20          
 */


import java.io.*;
import java.net.*;

/**
 * Simple class get some web content
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-07-20
 */
public class Wgetter {
	
	public static StringBuffer get(String url) throws IOException{
		
		StringBuffer buff = new StringBuffer();
		String s;
		
		// Read a URL until it runs out, getting each line
		BufferedReader r = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		while ((s = r.readLine()) != null) {
		  buff.append(s);	
		}
		r.close();
		return buff ;
	}
}

