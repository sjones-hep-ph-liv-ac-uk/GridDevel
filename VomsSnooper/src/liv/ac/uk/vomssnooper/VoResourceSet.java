package liv.ac.uk.vomssnooper;

/**
 * Represents the set of resources required by a VO
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-11-28
 */

public class VoResourceSet {
	private String ramPerCore64;
	private String ramPerCore32;
	private String scratch;
	private String maxCpu;
	private String maxWall;
	private String other ;
	
	/**
	 * 
	 * Constructor with all attribs
	 * @param ramPerCore64 ram per core on 64 bit systems
	 * @param ramPerCore32 ram per core on 32 bit system (defunct)
	 * @param scratch scratch space
	 * @param maxCpu max cpu time
	 * @param maxWall max wallclock time
	 * @param other any other resources, freetext
	 */
	public VoResourceSet(String ramPerCore64, String ramPerCore32, String scratch, String maxCpu, String maxWall, String other) {
		super();
		this.ramPerCore64 = ramPerCore64;
		this.ramPerCore32 = ramPerCore32;
		this.scratch = scratch;
		this.maxCpu = maxCpu;
		this.maxWall = maxWall;
		this.other = other;
	}
	
	/**
	 * Simple constructor
	 */
	public VoResourceSet() {
		super();
		this.ramPerCore64 = null;
		this.ramPerCore32 = null;
		this.scratch = null;
		this.maxCpu = null;
		this.maxWall = null;
		this.other = null;
	}
	

	/**
	 * Getter
	 * @return
	 */
	public String getRamPerCore64() {
		return ramPerCore64;
	}

	/**
	 * @param ramPerCore64
	 */
	public void setRamPerCore64(String ramPerCore64) {
		this.ramPerCore64 = ramPerCore64;
	}

	/**
	 * Getter
	 * @return
	 */
	public String getRamPerCore32() {
		return ramPerCore32;
	}

	/**
	 * Setter
	 * @param ramPerCore32
	 */
	public void setRamPerCore32(String ramPerCore32) {
		this.ramPerCore32 = ramPerCore32;
	}

	/**
	 * Getter
	 * @return
	 */
	public String getScratch() {
		return scratch;
	}

	/**
	 * Setter
	 * @param scratch
	 */
	public void setScratch(String scratch) {
		this.scratch = scratch;
	}

	/**
	 * Getter
	 * @return
	 */
	public String getMaxCpu() {
		return maxCpu;
	}

	/**
	 * Setter
	 * @param maxCpu
	 */
	public void setMaxCpu(String maxCpu) {
		this.maxCpu = maxCpu;
	}

	/**
	 * Getter
	 * @return
	 */
	public String getMaxWall() {
		return maxWall;
	}

	/**
	 * Setter
	 * @param maxWall
	 */
	public void setMaxWall(String maxWall) {
		this.maxWall = maxWall;
	}

	/**
	 * Getter
	 * @return
	 */
	public String getOther() {
		return other;
	}

	/**
	 * Setter
	 * @param other
	 */
	public void setOther(String other) {
		this.other = other;
	}
	
	/**
	 * Setter
	 * @param other
	 */
	public void appendToOther(String other) {
		if (this.other == null) {
			this.other = "";
		}
		this.other = this.other + other;
	}
	
}
