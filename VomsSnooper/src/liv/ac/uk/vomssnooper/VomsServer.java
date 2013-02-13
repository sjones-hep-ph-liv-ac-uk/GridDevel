/** Represents one VOMS server
 * @author      Steve Jones  <sjones@hep.ph.liv.ac.uk>
 * @since       2012-02-24          
 */

package liv.ac.uk.vomssnooper;

public class VomsServer {

	/**
	 * Class to facilitate a sort function
	 * 
	 * @author sjones
	 * 
	 */
	public static class ByVomsServerDn implements java.util.Comparator<VomsServer> {
		public int compare(VomsServer first, VomsServer second) {
			return ((first.getDn() != null) ? first.getDn() : "").compareTo(((second.getDn() != null) ? second.getDn() : ""));
		}
	}

	private Integer httpsPort;
	private Integer vomsServerPort;
	private String hostname;
	private String dn;
	private String caDn;
	private String membersListUrl;
	private Boolean complete;

	
	private Boolean isVomsAdminServer;

	/**
	 * Constructor
	 */
	public VomsServer() {
		httpsPort = -1;
		vomsServerPort = -1;
		hostname = null;
		dn = null;
		caDn = null;
		membersListUrl = null;
		complete = false;
		isVomsAdminServer = false;
	}

	/**
	 * Checks if all the fields of the VomsServer have been found Note: I don't care about httpsPort, which is derived from
	 * VOMS_SERVERS line, because VOMS_SERVERS line represents servers that can provide grid-mapfiles, and not all voms servers can do
	 * this, due to the CERN rule (it actually was deemed desirable that grid-mapfiles be generated using voms.cern.ch only, because
	 * lcg-voms.cern.ch is already running the VOMRS (sic) service as an extra load). * @return flag to say if all relevant data is
	 * complete
	 */
	public void setWhetherComplete() {
		complete = false;
		// if (httpsPort == -1)
		// return;
		if (vomsServerPort == -1)
			return;
		if (hostname == null)
			return;
		if (dn == null)
			return;
		if (caDn == null)
			return;
		if (membersListUrl == null)
			return;
		complete = true;
	}

	public void printIncomplete() {
		String msg = "";
		if (httpsPort == -1) {
			msg += ", httpsPort";
		}
		if (vomsServerPort == -1) {
			msg += ", vomsServerPort";
		}
		if (hostname == null) {
			msg += ", hostname";
		}
		if (dn == null) {
			msg += ", dn";
		}
		if (caDn == null) {
			msg += ", caDn";
		}
		if (membersListUrl == null) {
			msg += ", membersListUrl";
		}
		msg = msg.substring(1);
		System.out.println("Missing fields were: " + msg);
	}

	/**
	 * Getter for a field - membersListUrl
	 * 
	 * @return the field requested
	 */
	public String getMembersListUrl() {
		return membersListUrl;
	}

	/**
	 * Setter for a field - membersListUrl
	 * 
	 * @return null
	 */
	public void setMembersListUrl(String membersListUrl) {
		this.membersListUrl = membersListUrl;
	}

	/**
	 * Getter for a field - httpsPort
	 * 
	 * @return the field requested
	 */
	public Integer getHttpsPort() {
		return httpsPort;
	}

	/**
	 * Setter for a field - httpsPort
	 * 
	 * @return null
	 */
	public void setHttpsPort(Integer httpsPort) {
		this.httpsPort = httpsPort;
	}

	/**
	 * Getter for a field - vomsesPort
	 * 
	 * @return the field requested
	 */
	public Integer getVomsesPort() {
		return vomsServerPort;
	}

	/**
	 * Setter for a field - vomsesPort
	 * 
	 * @return null
	 */
	public void setVomsServerPort(Integer vomsesPort) {
		this.vomsServerPort = vomsesPort;
	}

	/**
	 * Getter for a field - hostname
	 * 
	 * @return the field requested
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Setter for a field - hostname
	 * 
	 * @return null
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Getter for a field - dn
	 * 
	 * @return the field requested
	 */
	public String getDn() {
		return dn;
	}

	/**
	 * Setter for a field - dn
	 * 
	 * @return null
	 */
	public void setDn(String dn) {
		this.dn = dn;
	}

	/**
	 * Getter for a field - cadn
	 * 
	 * @return the field requested
	 */
	public String getCaDn() {
		return caDn;
	}

	/**
	 * Setter for a field - cadn
	 * 
	 * @return null
	 */
	public void setCaDn(String caDn) {
		this.caDn = caDn;
	}

	/**
	 * Getter for a field - complete flag
	 * 
	 * @return the field requested
	 */
	public Boolean isComplete() {
		return complete;
	}

	// /**
	// * Setter for a field - complete flag
	// * @return null
	// */
	// public void setComplete(Boolean complete) {
	// this.complete = complete;
	// }

	/**
	 * Returns a representative string
	 * 
	 * @return String to represent the object
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (this.isVomsAdminServer) {
		  sb.append(" " + httpsPort + ",");
		}
		else {
		  sb.append(" -1,");
		}
		sb.append(" " + vomsServerPort + ",");
		sb.append(" " + hostname + ",");
		sb.append(" " + dn + ",");
		sb.append(" " + caDn + ",");
		// sb.append(" " + membersListUrl );
		return sb.toString();
	}

	/**
	 * Returns a representative URL
	 * 
	 * @return URL to represent the object
	 */
	public String makeUrl(String vo) {
		StringBuffer url = new StringBuffer();
		if (this.httpsPort == -1) {
			System.out.println("Warning: httpsPort for " + vo + " is " + this.httpsPort);
		}
		url.append("vomss://" + hostname + ":" + this.httpsPort + "/voms/" + vo + "?/" + vo);

		return url.toString();
	}

	/**
	 * Compares two servers
   * @param other the other server to compare this one to
	 * 
	 * @return Report of diffs
	 */
	public StringBuffer isAlike(VomsServer other) {
		StringBuffer report = new StringBuffer();
		String thisVs = this.toString();
		String otherVs = other.toString();

		if (!otherVs.equals(thisVs)) {
			report.append("This voms server:\n" + thisVs + "\ndiffers from the other one:\n" + otherVs);
		}
		return report;
	}
	
	/**
	 * Getter for isVomsAdminServer
	 * 
	 * @return isVomsAdminServer whether this one is a VomsAdminServer 
	 */
	public Boolean isVomsAdminServer() {
		return isVomsAdminServer;
	}

	/**
	 * Setter for isVomsAdminServer
	 * 
   * @param isVomsAdminServer is it a voms admin server
	 */
	public void setIsVomsAdminServer(Boolean isVomsAdminServer) {
		this.isVomsAdminServer = isVomsAdminServer;
	}
	
	
}
