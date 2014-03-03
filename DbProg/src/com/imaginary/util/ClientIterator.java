/* $Id: ClientIterator.java,v 1.2 1999/11/06 19:50:59 borg Exp $ */
/* Copyright � 1999 George Reese, All Rights Reserved */
package com.imaginary.util;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;

/**
 * The client portion of the distributed iterator support. This class
 * implements the <CODE>Iterator</CODE> interface for a distributed
 * iterator. Using distributed iterators, you can ship a collection across
 * the network one element at a time, thus transmitting only the data
 * required by the application. Furthermore, by avoiding transmitting
 * the entire collection, you enable access to the initial elements of
 * the collection quicker than would be possible through raw serialization
 * of a collection.
 * <BR>
 * Last modified $Date: 1999/11/06 19:50:59 $
 * @version $Revision: 1.2 $
 * @author George Reese (borg@imaginary.com)
 * @see com.imaginary.util.DistributedIterator
 */
public class ClientIterator implements Iterator, Serializable {
    /**
     * The remote iterator to which this client is referencing.
     * @serial
     */
    private DistributedIterator source = null;

    /**
     * Required constructor for serialization.
     */
    public ClientIterator() {
        super();
    }

    /**
     * Constructs a new <CODE>ClientIterator</CODE> using the named
     * <CODE>DistributedIterator</CODE> as its remote source.
     * @param src the server-based distributed iterator
     */
    public ClientIterator(DistributedIterator src) {
        super();
        source = src;
    }

    /**
     * @return true if more elements are available in the iterator
     */
    public boolean hasNext() {
        try {
            return source.hasNext();
        }
        catch( RemoteException e ) {
            return false;
        }
    }

    /**
     * @return the next element in the iterator
     */
    public Object next() {
        try {
            Object ob = source.next();
            
            return ob;
            //return source.next();
        }
        catch( RemoteException e ) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This operation is unsupported in this implementation.
     * @throws java.lang.UnsupportedOperationException always thrown
     */
    public void remove() {
        try {
            source.remove();
        }
        catch( RemoteException e ) {
        }
    }
}
       
