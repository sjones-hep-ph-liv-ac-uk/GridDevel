package com.imaginary.util;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;

/**
 * Implements the <CODE>DistributedIterator</CODE> interface by referencing
 * a local <CODE>Iterator</CODE>.
 * <BR>
 * Last modified $Date: 1999/11/06 18:38:04 $
 * @version $Revision: 1.1.1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class DistributedIteratorImpl
extends UnicastRemoteObject implements DistributedIterator {
    /**
     * The local iterator that serves as the source for the elements of
     * the distributed iterator.
     */
    private Iterator source = null;

    /**
     * Constructs a new <CODE>DistributedIteratorImpl</CODE> using
     * the specified local iterator as a data source.
     * @param src the local iterator
     * @throws java.rmi.RemoteException could not export the iterator
     */
    public DistributedIteratorImpl(Iterator src) throws RemoteException {
        super();
        source = src;
    }

    /**
     * @return true if more elements are available in the iterator
     */
    public boolean hasNext() {
        return source.hasNext();
    }

    /**
     * @return the next element in the iterator
     */
    public Object next() {
        Object ob = source.next();

        return ob;
        //return source.next();
    }

    /**
     * This operation is unsupported in this implementation.
     * @throws java.lang.UnsupportedOperationException always thrown
     */
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from a " +
                                                "distributed iterator.");
    }
}
