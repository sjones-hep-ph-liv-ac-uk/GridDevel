package com.imaginary.lwp;

/**
 * Represents a failure to authenticate. There are two scenarios which
 * can cause an authentication failure:
 * <UL>
 * <LI> Invalid credentials </LI>
 * <LI> System failure </LI>
 * </UL>
 * You can find out which kind of failure this represents by checking
 * the <CODE>getType</CODE> method.
 * <BR>
 * Last modified $Date: 1999/10/06 03:19:10 $
 * @version $Revision: 1.2 $
 * @author George Reese (borg@imaginary.com)
 */
public class AuthenticationException extends Exception {
    /**
     * Represents a credential-based failure.
     */
    static public final short CREDENTIAL = 1;
    /**
     * Represents a system-based failure.
     */
    static public final short SYSTEM     = 2;

    /**
     * The failure type.
     */
    private short type = CREDENTIAL;
    
    /**
     * Constructs an authentication exception either for serialization
     * or for the default credential-based exception.
     */
    public AuthenticationException() {
        super();
    }

    /**
     * Constructs an authentication exception having the specified
     * message. The authentication type is credential.
     * @param rsn the reason for the exception
     */
    public AuthenticationException(String rsn) {
        super(rsn);
    }

    /**
     * Constructs an authentication exception having the specified
     * message. The authentication type is as specified.
     * @param rsn the reason for the exception
     * @param t the exception type
     */
    public AuthenticationException(String rsn, short t) {
        super(rsn);
        type = t;
    }

    /**
     * Constructs a system-based authentication exception caused
     * by the specified exception.
     * @param cse the cause of the exception
     */
    public AuthenticationException(Exception cse) {
        super(cse.getMessage());
        type = SYSTEM;
    }

    /**
     * Constructs a system-based authentication exception caused by
     * the specified exception.
     * @param reason the reason for the exception
     * @param cse the exception that caused this exception
     */
    public AuthenticationException(String reason, Exception cse) {
        super(reason);
        type = SYSTEM;
    }

    /**
     * @return the exception type
     */
    public short getType() {
        return type;
    }
}
