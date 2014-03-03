/* $Id: JDBCAuthenticator.java,v 1.1 1999/11/07 19:32:30 borg Exp $ */
/* Copyright � 1999 George Reese, All Rights Reserved */
package com.imaginary.lwp.jdbc;

import com.imaginary.lwp.Authenticator;
import com.imaginary.lwp.AuthenticationException;
import com.imaginary.lwp.AuthenticationRole;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implements the <CODE>Authenticator</CODE> interface to authenticate
 * a user ID/password against values stored in a database. This class
 * expects the following table structure:
 * <TABLE>
 * <TR>
 * <TH><CODE>LWP_USER</CODE></TH>
 * </TR>
 * <TR>
 * <TD><CODE>USER_ID (VARCHAR(25))</CODE></TD>
 * </TR>
 * <TR>
 * <TD><CODE>PASSWORD (VARCHAR(25))</CODE></TD>
 * </TR>
 * </TABLE>
 * If you want a more complex authentication scheme, you should
 * write your own <CODE>Authenticator</CODE> implementation.
 * <P>
 * This implementation ignores all role information and just authenticates
 * base on UID/PW.
 * <BR>
 * Last modified $Date: 1999/11/07 19:32:30 $
 * @version $Revision: 1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class JDBCAuthenticator implements Authenticator {
    /**
     * The SQL SELECT statement.
     */
    static public final String SELECT =
        "SELECT PASSWORD FROM LWP_USER WHERE USER_ID = ?";
    
    /**
     * Authenticates the specified user ID against the specified
     * password.
     * @param uid the user ID to authenticate
     * @param pw the password to use for authentication
     * @throws com.imaginary.lwp.AuthenticationException the
     * user ID failed to authenticate against the specified password
     */
    public void authenticate(String uid, String pw)
        throws AuthenticationException {
        Connection conn = null;
        
        try {
            PreparedStatement stmt;
            String actual;
            ResultSet rs;
            
            conn = JDBCTransactionImpl.getJDBCConnection();
            stmt = conn.prepareStatement(SELECT);
            stmt.setString(1, uid);
            rs = stmt.executeQuery();
            if( !rs.next() ) {
                throw new AuthenticationException("Invalid user ID or " +
                                                  "password.");
            }
            actual = rs.getString(1);
            if( rs.wasNull() ) {
                throw new AuthenticationException("No password specified for "+
                                                  uid);
            }
            if( !actual.equals(pw) ) {
                throw new AuthenticationException("Invalid user ID or " +
                                                  "password.");
            }
            conn.commit();
        }
        catch( SQLException e ) {
            e.printStackTrace();
            throw new AuthenticationException(e);
        }
        finally {
            if( conn != null ) {
                try { conn.close(); }
                catch( SQLException e ) { }
            }
        }
    }
    
    /**
     * Authenticates the specified user ID against the specified
     * password.
     * @param uid the user ID to authenticate
     * @param pw the password to use for authentication
     * @param r this is ignored
     * @throws com.imaginary.lwp.AuthenticationException the
     * user ID failed to authenticate against the specified password
     */
    public void authenticate(String uid, String pw, AuthenticationRole r)
        throws AuthenticationException {
        authenticate(uid, pw);
    }
}
