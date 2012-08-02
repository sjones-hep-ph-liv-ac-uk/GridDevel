package liv.ac.uk.vomssnooper;

/**
 * Represents one FQAN
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-04-24
 */

public class Fqan {

  private String groupType;
	private String fqanExpr;
	private String description;
	private Boolean isUsed;
	
	
	public Fqan()
	{
		groupType = "dummy";
		fqanExpr = "dummy";
		description = "dummy";
		isUsed= false;
	}
	
	public Boolean getIsUsed() {
		return isUsed;
	}
	public void setIsUsed(Boolean isUsed) {
		this.isUsed = isUsed;
	}
	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public String getFqanExpr() {
		return fqanExpr;
	}

	public void setFqanExpr(String fqanExpr) {
		this.fqanExpr = fqanExpr;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

