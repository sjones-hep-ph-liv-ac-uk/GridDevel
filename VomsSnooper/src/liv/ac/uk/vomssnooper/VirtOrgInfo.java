package liv.ac.uk.vomssnooper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import liv.ac.uk.vomssnooper.VomsServer.ByVomsServerDn;

/**
 * Represents one VO
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-02-24
 */
public class VirtOrgInfo {

	/**
	 * Class to facilitate a sort function
	 * 
	 * @author sjones
	 * 
	 */
	public static class ByVoName implements java.util.Comparator<VirtOrgInfo> {

		public int compare(VirtOrgInfo first, VirtOrgInfo second) {
			String f = first.getVoNickName().toLowerCase();
			String s = second.getVoNickName().toLowerCase();
			return f.compareTo(s);
		}
	}

	private ArrayList<VomsServer> vomsServers; // List of VOMS servers for this VO
	private String voName; // Name of this VO
	private String voNickName; // Short name, explained below
	private Boolean atMySite; // If it supported flag
	private Boolean vodStyle; // Is it in vo.d style for DNS style names (default
														// is site-info.def style)
	private ArrayList<IndividualContact> individualContacts; // Contacts for this
																														// VO

	/**
	 * Basic Constructor
	 */
	public VirtOrgInfo() {
		voName = "";
		voNickName = "";
		atMySite = false;
		vodStyle = false;

		vomsServers = new ArrayList<VomsServer>();
		individualContacts = new ArrayList<IndividualContact>();
	}

	/**
	 * Returns a representative string
	 * 
	 * @return String to represent the object
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("VOInfo: " + voName + ", ");
		Iterator<VomsServer> v = vomsServers.iterator();
		while (v.hasNext()) {
			buffer.append(" Voms Server : " + v.next().makeUrl(voName));
		}
		return buffer.toString();
	}

	/**
	 * Add a new VOMS server for this VO
	 * 
	 * @param v
	 *          vomsserver
	 */
	public void addVomsServer(VomsServer v) {
		vomsServers.add(v);
	}

	/**
	 * Add a new contact for this VO
	 * 
	 * @param ic
	 *          contact
	 */
	public void addIc(IndividualContact ic) {
		individualContacts.add(ic);
	}

	/**
	 * Invoked to ask all Voms Servers to check their state of completion
	 */
	public void checkComplete() {
		Iterator<VomsServer> v = vomsServers.iterator();
		while (v.hasNext()) {
			v.next().setWhetherComplete();
		}
	}

	/**
	 * Invoked to retrieve all the VOMS lines for this VO
	 * 
	 * @param validOnly
	 *          only VOMS that are valid/complete
	 * @param extraFields
	 *          some extra fields this program can generate
	 * @return VOMS lines
	 */
	public ArrayList<String> getVomsLines(Boolean validOnly, Boolean extraFields) {

		ArrayList<String> vomsLines = new ArrayList<String>();
		StringBuffer vomsServerLine = new StringBuffer();
		StringBuffer vomsesLine = new StringBuffer();
		StringBuffer cadnLine = new StringBuffer();

		vomsServerLine.append("VOMS_SERVERS=\"");
		vomsesLine.append("VOMSES=\"");
		cadnLine.append("VOMS_CA_DN=\"");

		Iterator<VomsServer> vi;

		Collections.sort(vomsServers, new liv.ac.uk.vomssnooper.VomsServer.ByVomsServerDn());

		// Loop to do check if data is complete
		vi = vomsServers.iterator();
		while (vi.hasNext()) {
			VomsServer vs = vi.next();
			vs.setWhetherComplete();
			if ((!vs.isComplete()) & (validOnly)) {
				System.out.print("Warning: Some voms server data for " + this.getVoName() + " is incomplete and will be excluded\n");
				vs.printIncomplete();
			}
		}

		// Loop to do vomses line and caDn line
		vi = vomsServers.iterator();
		while (vi.hasNext()) {

			VomsServer vs = vi.next();
			if ((!vs.isComplete()) & (validOnly)) {
				continue;
			}

			// Populate vomses line
			vomsesLine.append("'");
			vomsesLine.append(voName.toLowerCase() + " " + vs.getHostname() + " " + vs.getVomsesPort() + " " + vs.getDn() + " "
					+ voName.toLowerCase());
			vomsesLine.append("' ");

			// Populate cadn line
			cadnLine.append("'");
			cadnLine.append(vs.getCaDn());
			cadnLine.append("' ");
		}

		// Loop to do the voms servers line; first find out if it's all
		// superceded by the cern rule (For CERN servers it was deemed desirable
		// that
		// grid-mapfiles be generated using voms.cern.ch only, because
		// lcg-voms.cern.ch
		// is already running the VOMRS (sic) service as an extra load).

		ArrayList<String> urls = new ArrayList<String>();
		String superceder = null;

		vi = vomsServers.iterator();
		while (vi.hasNext()) {

			VomsServer vs = vi.next();
			if ((!vs.isComplete()) & (validOnly)) {
				continue;
			}
			// Only use this Vomes Server if it has all the fields defined
			// (some only have a subset)
			if (vs.getHttpsPort() == -1) {
				continue;
			}
			
			String url = vs.makeUrl(this.voName.toLowerCase());
			urls.add(url);
			if (url.contains("voms.cern.ch")) {
				superceder = url;
			}
		}
		
		if (superceder == null) {
			Iterator<String> i = urls.iterator();
			while (i.hasNext()) {
				vomsServerLine.append("'");
				vomsServerLine.append(i.next());
				vomsServerLine.append("' ");
			}
		} else {
			vomsServerLine.append("'");
			vomsServerLine.append(superceder);
			vomsServerLine.append("' ");
		}

		// Finalise lines
		vomsServerLine.append("\"");
		vomsesLine.append("\"");
		cadnLine.append("\"");

		vomsLines.add(vomsServerLine.toString());
		vomsLines.add(vomsesLine.toString());
		vomsLines.add(cadnLine.toString());

		if (extraFields) {
			vomsLines.add("SW_DIR=$VO_SW_DIR/" + voNickName);
			vomsLines.add("DEFAULT_SE=$DPM_HOST");
			vomsLines.add("STORAGE_DIR=$STORAGE_PATH/" + voNickName);
		}
		return vomsLines;
	}

	/**
	 * Getter for a field - is this VO at my site
	 * 
	 * @return the field requested
	 */
	public Boolean isAtMySite() {
		return atMySite;
	}

	/**
	 * Setter for a field - is this VO at my site
	 * 
	 * @return null
	 */
	public void setAtMySite(Boolean atMySite) {
		this.atMySite = atMySite;
	}

	/**
	 * Getter for a field - does this data need to be in VOD style
	 * 
	 * @return the field requested
	 */
	public Boolean isVodStyle() {
		return vodStyle;
	}

	/**
	 * Setter for a field - does this data need to be in VOD style
	 * 
	 * @return null
	 */
	public void setVodStyle(Boolean vodStyle) {
		this.vodStyle = vodStyle;
	}

	/**
	 * Getter for a field - vo name
	 * 
	 * @return the field requested
	 */
	public String getVoName() {
		return voName;
	}

	/**
	 * Getter for a field - vo nick name
	 * 
	 * @return the field requested
	 */
	public String getVoNickName() {
		return voNickName;
	}

	/**
	 * Setter for two field, name and nickname. Nickname is derived from name
	 * 
	 * @return null
	 */
	public void setVoNameAndVoNickName(String voName) {

		this.voName = voName.toLowerCase();

		// The nickname is sometimes the same as the voName.
		// Otherwise, if the voName is one of those DNS style names,
		// with dots in it, the nickname is usually the first part.
		// But sometimes, they put the string "vo." in front, in which
		// case the nickname is the second part.

		voNickName = voName.toLowerCase();

		if (voName.indexOf('.') > -1) {
			String tmp = voName;
			if (voName.startsWith("vo.")) {
				tmp = voName.substring(3);
			}
			voNickName = tmp.substring(0, tmp.indexOf('.'));
		}
		// System.out.println("VO NICK NAME: " + this.voNickName);
	}

	/**
	 * Getter for a field - all the voms server for this VO
	 * 
	 * @return the field requested
	 */
	public ArrayList<VomsServer> getVomsServers() {
		return vomsServers;
	}

	/**
	 * Setter for a field - all the VOMS server for this VO
	 * 
	 * @return null
	 */
	// public void setVomsServers(ArrayList<VomsServer> vomsServers) {
	// this.vomsServers = vomsServers;
	// }

	/**
	 * Getter for a field - the contacts list of the VO
	 * 
	 * @return the field requested
	 */
	public ArrayList<IndividualContact> getIndividualContacts() {
		return individualContacts;
	}

	/**
	 * Setter for a field - the contact list for this VO
	 * 
	 * @return null
	 */
	public void setIndividualContacts(ArrayList<IndividualContact> individualContacts) {
		this.individualContacts = individualContacts;
	}

	public void sortVomsServers() {
		Collections.sort(vomsServers, new liv.ac.uk.vomssnooper.VomsServer.ByVomsServerDn());
	}
}
