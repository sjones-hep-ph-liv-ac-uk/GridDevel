/* $Id: DistributedIterator.java,v 1.1.1.1 1999/11/06 18:38:04 borg Exp $ */
/* Copyright � 1999 George Reese, All Rights Reserved */
package com.imaginary.util;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Wraps an <CODE>Iterator</CODE> so that it can act as a distributed
 * iterator. A distributed iterator is an iterator where the collection
 * is stored on a server and elements are transmitted across the network
 * one element at a time on demand. This contrasts with serialization of
 * the collection, where the entire collection is transmitted across the
 * network at once.
 * <P>
 * If you have a collection whose elements you want to make available
 * across the network using the distributed iterator paradigm, you
 * retrieve an iterator for the collection and wrap it with a
 * <CODE>DistributedIterator</CODE> implementation. You then pass the
 * distributed iterator to a <CODE>ClientIterator</CODE> and pass
 * that across the network. Consider the following RMI method that
 * returns a distributed iterator for its remote method <CODE>cats()</CODE>:
 * <PRE>
 * private ArrayList cats;
 * 
 * public Iterator cats() throws RemoteException {
 *     DistributedIterator dist = new DistributedIteratorImpl(cats.iterator());
 *     ClientIterator it = new ClientIterator(dist);
 *
 *     return it;
 * }
 * </PRE>
 * The result of this method is that an empty iterator is sent across
 * the network to the client. That empty iterator knows how to retrieve
 * each cat from the <CODE>cats ArrayList</CODE> from the server on demand
 * as the client application calls for them. If the client only asks for
 * the first cat, only the first cat is ever sent across the network.
 * If the collection of cats contains 1 million cats, the client does
 * not need to wait on that entire collection to be transmitted across
 * the network before it can access the first cat.
 * <BR>
 * Last modified $Date: 1999/11/06 18:38:04 $
 * @version $Revision: 1.1.1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public interface DistributedIterator extends Remote {
    /**
     * @return true if more elements are available in the iterator
     */
    boolean hasNext() throws RemoteException;
    
    /**
     * @return the next element in the iterator
     */
    Object next() throws RemoteException;

    /**
     * This operation is unsupported in this implementation.
     * @throws java.lang.UnsupportedOperationException always thrown
     */
    void remove() throws RemoteException;
}
