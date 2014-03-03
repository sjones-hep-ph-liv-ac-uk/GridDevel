/* $Id: LifoStack.java,v 1.1.1.1 1999/11/06 18:38:04 borg Exp $ */
/* Copyright � 1999 George Reese, All Rights Reserved */
package com.imaginary.util;

// from the J2SE
import java.util.ArrayList;

/**
 * An unsynchronized LIFO stack. This class provides easy access to pushing
 * and popping objects to and from a stack where the rule is that the last
 * object in is the first object out. As with most Java collections, this
 * class is wholly unsynchronized.
 * <BR>
 * Last modified $Date: 1999/11/06 18:38:04 $
 * @version $Revision: 1.1.1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class LifoStack extends ArrayList implements Stack {
    /**
     * Constructs an empty LIFO stack.
     */
    public LifoStack() {
        super();
    }

    /**
     * Provides a look at the last object placed on the stack, since it
     * will be the first one out. This method does not change the contents
     * of the stack. Because this class is unsynchronized, applications
     * using this class are responsible for making sure that a
     * <CODE>peek()</CODE> followed by a <CODE>pop()</CODE> returns the
     * same value.
     * @return the object on the top of the stack
     */
    public Object peek() {
        int last = size() - 1;
        Object ob;

        if( last == -1 ) {
            return null;
        }
        ob = get(last);
        return ob;
    }

    /**
     * Pops the last object placed on the stack off of it and returns it.
     * @return the last object placed on the stack
     */
    public Object pop() {
        int last = size() - 1;
        Object ob;

        if( last == -1 ) {
            return null;
        }
        ob = get(last);
        remove(last);
        return ob;
    }

    /**
     * Pushes a new object onto the stack.
     * @param ob the new object
     * @return the new object
     */
    public Object push(Object ob) {
        add(ob);
        return ob;
    }

    /**
     * Searches the stack for the specified object. Returns the location
     * of the object with respect to the top of the stack or -1.
     * @param ob the object being sought
     * @return the index of the object on the stack or -1.
     */
    public int search(Object ob) {
        int i = lastIndexOf(ob);

        if( i == -1 ) {
            return -1;
        }
        else {
            return (size()-i);
        }
    }
}
