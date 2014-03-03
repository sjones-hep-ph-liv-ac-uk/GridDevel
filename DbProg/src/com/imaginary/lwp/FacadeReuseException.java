/* $Id: FacadeReuseException.java,v 1.1 1999/10/06 03:19:13 borg Exp $ */
/* Copyright � 1999 George Reese, All Rights Reserved */
package com.imaginary.lwp;

/**
 * Represents an attempt to reuse a reference that has already been
 * assigned an entity.
 * <BR>
 * Last modified $Date: 1999/10/06 03:19:13 $
 * @version $Revision: 1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class FacadeReuseException extends RuntimeException {
    /**
     * Empty constructor.
     */
    public FacadeReuseException() {
        super();
    }

    /**
     * Exception with text.
     * @param rsn the reason for the exception
     */
    public FacadeReuseException(String rsn) {
        super(rsn);
    }
}
