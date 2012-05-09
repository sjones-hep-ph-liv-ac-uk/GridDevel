/** Represents one VOMS server
 * @author      Steve Jones  <sjones@hep.ph.liv.ac.uk>
 * @since       2012-02-24          
 */

package liv.ac.uk.vomssnooper;

public class VomsServer {

	private Integer httpsPort;
	private Integer vomsesPort;
	private String hostname;
	private String dn;
	private String caDn;
	private String membersListUrl;
	private Boolean complete;

	/**
	 * Constructor
	 */
	public VomsServer() {
		httpsPort = -1;
		vomsesPort = -1;
		hostname = null;
		dn = null;
		caDn = null;
		membersListUrl = null;
		complete = false;
	}

	/**
	 * Checks if all the fields of the VomsServer have been found
	 * @return flag to say if all record is complete
	 */
	public void setWhetherComplete() {
		complete = false;
		if (httpsPort == -1)
			return;
		if (vomsesPort == -1)
			return;
		if (hostname == null)
			return;
		if (dn == null)
			return;
		if (caDn == null)
			return;
		if (membersListUrl == null)
			return;
		if (complete == null)
			return;
		complete = true;
	}

	/**
	 * Getter for a field - membersListUrl
	 * @return the field requested
	 */
	public String getMembersListUrl() {
		return membersListUrl;
	}

	/**
	 * Setter for a field - membersListUrl
	 * @return null
	 */
	public void setMembersListUrl(String membersListUrl) {
		this.membersListUrl = membersListUrl;
	}

	/**
	 * Getter for a field - httpsPort
	 * @return the field requested
	 */
	public Integer getHttpsPort() {
		return httpsPort;
	}

	/**
	 * Setter for a field - httpsPort
	 * @return null
	 */
	public void setHttpsPort(Integer httpsPort) {
		this.httpsPort = httpsPort;
	}

	/**
	 * Getter for a field - vomsesPort
	 * @return the field requested
	 */
	public Integer getVomsesPort() {
		return vomsesPort;
	}

	/**
	 * Setter for a field - vomsesPort
	 * @return null
	 */
	public void setVomsesPort(Integer vomsesPort) {
		this.vomsesPort = vomsesPort;
	}

	/**
	 * Getter for a field - hostname
	 * @return the field requested
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Setter for a field - hostname
	 * @return null
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Getter for a field - dn
	 * @return the field requested
	 */
	public String getDn() {
		return dn;
	}

	/**
	 * Setter for a field - dn
	 * @return null
	 */
	public void setDn(String dn) {
		this.dn = dn;
	}

	/**
	 * Getter for a field - cadn
	 * @return the field requested
	 */
	public String getCaDn() {
		return caDn;
	}

	/**
	 * Setter for a field - cadn
	 * @return null
	 */
	public void setCaDn(String caDn) {
		this.caDn = caDn;
	}


	/**
	 * Getter for a field - complete flag
	 * @return the field requested
	 */
	public Boolean isComplete() {
		return complete;
	}

	/**
	 * Setter for a field - complete flag
	 * @return null
	 */
	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	/**
	 * Returns a representative string
	 * @return String to represent the object
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" " + httpsPort + ",");
		sb.append(" " + vomsesPort + ",");
		sb.append(" " + hostname + ",");
		sb.append(" " + dn + ",");
		sb.append(" " + caDn + ",");
		sb.append(" " + membersListUrl );
		return sb.toString();
	}

	/**
	 * Returns a representative URL
	 * @return URL to represent the object
	 */
	public String makeUrl(String vo) {
		StringBuffer url = new StringBuffer();
		url.append("vomss://" + hostname + ":" + httpsPort + "/voms/" + vo + "?/" + vo);

		return url.toString();
	}
}
