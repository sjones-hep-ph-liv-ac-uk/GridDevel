package liv.ac.uk.vomssnooper;

/**
 * Represents one individual contact
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-04-24
 */

public class IndividualContact {

	private String name;
	private String role;
	private String email;
	private String dn;

	/**
	 * Constructor
	 * 
	 * @param name Name of contact
	 * @param role Role of contact
	 * @param email email of contact
	 * @param dn dn of contact
	 */
	public IndividualContact(String name, String role, String email, String dn) {
		super();
		this.name = name;
		this.role = role;
		this.email = email;
		this.dn = dn;
	}

	/**
	 * Returns a string
	 * 
	 * @return a string representative of the object
	 */
	public String toString() {
		String res = "";
		res = res.concat((name.compareTo("Not available") != 0 ? name + ", " : "null, "));
		res = res.concat((role.compareTo("Not available") != 0 ? role + ", " : "null, "));
		res = res.concat((email.compareTo("Not available") != 0 ? email + ", " : "null, "));
		res = res.concat((dn.compareTo("Not available") != 0 ? dn + ", " : "null, "));

		return res;
	}

	/**
	 * Get a field -name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set a field - name
	 * 
	 * @return
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get a field - role
	 * 
	 * @return
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Set a field - role
	 * 
	 * @return
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Get a field - email
	 * 
	 * @return
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Set a field - email
	 * 
	 * @return
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Get a field - dn
	 * 
	 * @return
	 */
	public String getDn() {
		return dn;
	}

	/**
	 * Set a field - dn
	 * 
	 * @return
	 */
	public void setDn(String dn) {
		this.dn = dn;
	}
}
