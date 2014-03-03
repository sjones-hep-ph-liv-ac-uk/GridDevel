package com.imaginary.lwp;

/**
 * The base exception class for all persistence-related problems.
 * <BR>
 * Last modified $Date: 1999/10/05 21:43:04 $
 * @version $Revision: 1.1.1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class PersistenceException extends Exception {
    /**
     * Empty constructor for serialization and nothing else.
     */
    public PersistenceException() {
        super();
    }
    
    /**
     * Constructs a new exception for the specified reason.
     * @param rsn the reason message for the exception
     */
    public PersistenceException(String rsn) {
        super(rsn);
    }
}
