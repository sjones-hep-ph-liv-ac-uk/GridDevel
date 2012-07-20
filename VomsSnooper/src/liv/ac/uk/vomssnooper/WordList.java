/** Basic class to hold a list of bare words
 * @author      Steve Jones  <sjones@hep.ph.liv.ac.uk>
 * @since       2012-02-24
 * 
 *           
 * Modified to use hash to record the fact that a word has been queried.
 */

package liv.ac.uk.vomssnooper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class WordList {

	/** The words in this list */
	private Hashtable<String, Boolean> dictionaryOfWords;

	/** Basic constructor */
	public WordList() {
		dictionaryOfWords = new Hashtable<String, Boolean>();
	}

	/**
	 * Read the word into the list from some file
	 * 
	 * @param file File to read
	 * @return null
	 */
	void readWords(String file) {

		try {

			FileInputStream fstream = new FileInputStream(file);

			DataInputStream in = new DataInputStream(fstream);

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				dictionaryOfWords.put(strLine.trim(), false);
			}
			in.close();
		}
		catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Check if a given word is in the list
	 * 
	 * @param s word to check
	 * @return whether the word is in the list
	 */

	public boolean containsWithCase(String s) {

		Enumeration<String> keys = dictionaryOfWords.keys();

		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.equals(s)) {
				dictionaryOfWords.put(key, true);
				return true;
			}
		}
		return false;
	}

	public boolean containsNoCase(String s) {

		Enumeration<String> keys = dictionaryOfWords.keys();

		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.equalsIgnoreCase(s)) {
				dictionaryOfWords.put(key, true);
				return true;
			}
		}
		return false;
	}

	/**
	 * Return a list of any words not looked up
	 * 
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getSpareWords() {
		Enumeration<String> keys = dictionaryOfWords.keys();
		ArrayList<String> spares = new ArrayList<String>();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (dictionaryOfWords.get(key).booleanValue() == false) {
				spares.add(key);
			}
		}
		return spares;
	}
}
