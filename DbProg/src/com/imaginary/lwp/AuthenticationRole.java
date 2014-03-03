package com.imaginary.lwp;

/**
 * A role used for authentication.
 * <BR>
 * Last modified $Date: 1999/11/07 19:32:24 $
 * @version $Revision: 1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class AuthenticationRole {
    /**
     * The implementation-specific credentials that support this role.
     * In its simplest form, this could be a role name. In a more complex
     * form, it could be tied to the java.security package.
     */
    private Object credentials = null;

    /**
     * Constructs a new role using the specified credentials.
     * @param cred the credentials to use for the role
     */
    public AuthenticationRole(Object cred) {
        super();
        credentials = cred;
    }

    /**
     * @return the role credentials
     */
    public Object getCredentials() {
        return credentials;
    }
}
