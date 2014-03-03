/* $Id: FifoStack.java,v 1.1.1.1 1999/11/06 18:38:04 borg Exp $ */
/* Copyright � 1999-2000 George Reese, All Rights Reserved */
package com.imaginary.util;

// from the J2SE
import java.util.ArrayList;

/**
 * An unsynchronized FIFO stack. This class provides easy access to pushing
 * and popping objects to and from a stack where the rule is that the first
 * object in is the first object out. As with most Java collections, this
 * class is wholly unsynchronized.
 * <BR>
 * Last modified $Date: 1999/11/06 18:38:04 $
 * @version $Revision: 1.1.1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class FifoStack extends ArrayList implements Stack {
    /**
     * Constructs an empty FIFO stack.
     */
    public FifoStack() {
        super();
    }

    /**
     * Provides a look at the first object placed on the stack, since it
     * will be the first one out. This method does not change the contents
     * of the stack. Because this class is unsynchronized, applications
     * using this class are responsible for making sure that a
     * <CODE>peek()</CODE> followed by a <CODE>pop()</CODE> returns the
     * same value.
     * @return the first object on the top of the stack
     */
    public Object peek() {
        Object ob;

        if( size() == 0 ) {
            return null;
        }
        ob = get(0);
        return ob;
    }

    /**
     * Pops the first object placed on the stack off of it and returns it.
     * @return the first object placed on the stack
     */
    public Object pop() {
        Object ob;

        if( size() == 0 ) {
            return null;
        }
        ob = get(0);
        remove(0);
        return ob;
    }

    /**
     * Pushes a new object onto the end of stack.
     * @param ob the new object
     * @return the new object
     */
    public Object push(Object ob) {
        add(ob);
        return ob;
    }

    /**
     * Searches the stack for the specified object. Returns the location
     * of the object with respect to the first object on the stack or -1.
     * @param ob the object being sought
     * @return the index of the object on the stack or -1.
     */
    public int search(Object ob) {
        int i = indexOf(ob);

        if( i == -1 ) {
            return -1;
        }
        else {
            return (i+1);
        }
    }
}

