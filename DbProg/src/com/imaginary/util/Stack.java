/* $Id: Stack.java,v 1.1.1.1 1999/11/06 18:38:04 borg Exp $ */
/* Copyright � 1999-2000 George Reese, All Rights Reserved */
package com.imaginary.util;

/**
 * A generic interface for stacked collections. This interface prescribes
 * methods that let you access objects in a collection based on some rule
 * of order.
 * <BR>
 * Last modified $Date: 1999/11/06 18:38:04 $
 * @version $Revision: 1.1.1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public interface Stack {
    /**
     * @return true if there are no objects on the stack
     */
    boolean isEmpty();

    /**
     * Provides a look at the next object on the stack without removing it.
     * @return the next object on the stack
     */
    Object peek();

    /**
     * Removes the next object on the stack and returns it.
     * @return the next object on the stack
     */
    Object pop();

    /**
     * Places an object on the stack.
     * @param ob the object to be placed on the stack
     * @return the object placed on the stack
     */
    Object push(Object ob);

    /**
     * Provides the location of the specified object on the stack. The number
     * 1 means the first object, 2 the second, and so on.
     * @return the location of the object on the stack or -1 if it is not on
     * the stack
     */
    int search(Object ob);

    /**
     * @return the number of objects on the stack.
     */
    int size();
}
