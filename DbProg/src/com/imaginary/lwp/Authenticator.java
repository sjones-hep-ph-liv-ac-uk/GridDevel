/* $Id: Authenticator.java,v 1.2 1999/11/07 19:32:25 borg Exp $ */
/* Copyright � 1999 George Reese, All Rights Reserved */
package com.imaginary.lwp;

/**
 * Authenticates a user ID/password pair. Different applications may provide
 * their own authenticator and specify them in their LWP configuration
 * file using the &quot;imaginary.lwp.authenticator&quot; property.
 * <BR>
 * Last modified $Date: 1999/11/07 19:32:25 $
 * @version $Revision: 1.2 $
 * @author George Reese (borg@imaginary.com)
 */
public interface Authenticator {
    /**
     * Authenticates the specified user ID against the specified
     * password.
     * @param uid the user ID to authenticate
     * @param pw the password to use for authentication
     * @throws com.imaginary.lwp.AuthenticationException the
     * user ID failed to authenticate against the specified password
     */
    void authenticate(String uid, String pw) throws AuthenticationException;

    /**
     * Authenticates the specified user ID against the specified
     * password.
     * @param uid the user ID to authenticate
     * @param pw the password to use for authentication
     * @param r the role to authenticate for
     * @throws com.imaginary.lwp.AuthenticationException the
     * user ID failed to authenticate against the specified password
     */
    void authenticate(String uid, String pw, AuthenticationRole r)
        throws AuthenticationException;
}
