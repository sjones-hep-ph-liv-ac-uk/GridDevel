package com.imaginary.lwp;

/**
 * The base exception class for all configuration-related problems.
 * <BR>
 * Last modified $Date$
 * @version $Revision$
 * @author George Reese (borg@imaginary.com)
 */
public class ConfigurationException extends RuntimeException {
    /**
     * Empty constructor for serialization and nothing else.
     */
    public ConfigurationException() {
        super();
    }
    
    /**
     * Constructs a new exception for the specified reason.
     * @param rsn the reason message for the exception
     */
    public ConfigurationException(String rsn) {
        super(rsn);
    }
}
