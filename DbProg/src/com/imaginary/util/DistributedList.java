/* $Id: DistributedList.java,v 1.1.1.1 1999/11/06 18:38:04 borg Exp $ */
/* Copyright � 1999 George Reese, All Rights Reserved */
package com.imaginary.util;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Specializes in providing access to a list of objects via distributed
 * iterators. Because enterprise applications may be accessing huge result
 * sets, clients need the ability to get access to those results without
 * downloading the entire result set at once. Thus, instead of storing
 * lists as an <CODE>ArrayList</CODE>, an application stores them
 * as a <CODE>DistributedList</CODE>. This class provides a specialized
 * <CODE>iterator()</CODE> that returns a <CODE>DistributedIterator</CODE>.
 * <BR>
 * Last modified $Date: 1999/11/06 18:38:04 $
 * @version $Revision: 1.1.1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class DistributedList extends ArrayList {
    /**
     * @return a <CODE>DistributedIterator</CODE> that provides the
     * elements of the list on demand instead of all at once
     */
    public Iterator iterator() {
        try {
            DistributedIteratorImpl di;
            
            di = new DistributedIteratorImpl(super.iterator());
            return new ClientIterator(di);
        }
        catch( RemoteException e ) {
            throw new NullPointerException(e.getMessage());
        }
    }
}
