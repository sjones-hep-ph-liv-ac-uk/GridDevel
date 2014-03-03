package com.imaginary.lwp;

/**
 * The base exception class for all transaction-related problems.
 * <BR>
 * Last modified $Date$
 * @version $Revision$
 * @author George Reese (borg@imaginary.com)
 */
public class TransactionException extends Exception {
    /**
     * Empty constructor for serialization and nothing else.
     */
    public TransactionException() {
        super();
    }
    
    /**
     * Constructs a new exception for the specified reason.
     * @param rsn the reason message for the exception
     */
    public TransactionException(String rsn) {
        super(rsn);
    }
}
