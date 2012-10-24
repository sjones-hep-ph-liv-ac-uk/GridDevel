package liv.ac.uk.vomssnooper;

/**
 * Represents one FQAN
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-04-24
 */

public class Fqan {

	private String groupType; // type
	private String fqanExpr; // fqan
	private String description; // desc
	private Boolean isUsed; // whether used

	/**
	 * Constructor
	 */
	public Fqan() {
		groupType = "dummy";
		fqanExpr = "dummy";
		description = "dummy";
		isUsed = false;
	}

	/**
	 * Is this used
	 * 
	 * @return
	 */
	public Boolean getIsUsed() {
		return isUsed;
	}

	/**
	 * Set whether used
	 * 
	 * @param isUsed
	 */
	public void setIsUsed(Boolean isUsed) {
		this.isUsed = isUsed;
	}

	/**
	 * get the type
	 * 
	 * @return
	 */
	public String getGroupType() {
		return groupType;
	}

	/**
	 * set the type
	 * 
	 * @param groupType type string
	 */
	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	/**
	 * get the fqan
	 * 
	 * @return
	 */
	public String getFqanExpr() {
		return fqanExpr;
	}

	/**
	 * Set the fqan
	 * 
	 * @param fqanExpr fqan
	 */
	public void setFqanExpr(String fqanExpr) {
		this.fqanExpr = fqanExpr;
	}

	/**
	 * Get the desc
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the desc
	 * 
	 * @param description desc
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
