package com.imaginary.lwp;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * An abstract representation of a data storage transaction. This class
 * manages the life cycle of a data storage transaction. Applications can
 * get a transaction instance by calling <CODE>getCurrent</CODE>. The
 * transaction does not begin, however, until the <CODE>begin</CODE> method
 * is called by an application.
 * <BR>
 * Last modified $Date: 1999/11/20 17:33:19 $
 * @version $Revision: 1.7 $
 * @author George Reese (borg@imaginary.com)
 */
public abstract class Transaction {
    static private HashMap transactions = new HashMap();

    static public Transaction getCurrent(Identifier id) {
        Transaction trans;
        String cname;

        if( id == null ) {
            cname = System.getProperty(LWPProperties.XACTION);
            try {
                trans = (Transaction)Class.forName(cname).newInstance();
                trans.userID = id;
            }
            catch( Exception e ) {
                throw new ConfigurationException(e.getMessage());
            }
        }            
        synchronized( transactions ) {
            if( transactions.containsKey(id) ) {
                trans = (Transaction)transactions.get(id);
                return trans;
            }
            cname = System.getProperty(LWPProperties.XACTION);
            try {
                trans = (Transaction)Class.forName(cname).newInstance();
                trans.userID = id;
            }
            catch( Exception e ) {
                throw new ConfigurationException(e.getMessage());
            }
            transactions.put(id, trans);
        }
        return trans;
    }

    private long       timestamp = -1L;
    private HashSet    toCreate  = new HashSet();
    private HashSet    toRemove  = new HashSet();
    private HashSet    toStore   = new HashSet();
    private Identifier userID    = null;
    
    public Transaction() {
        super();
    }

    public synchronized final void begin() throws TransactionException {
        if( timestamp == -1L ) {
            timestamp = (new Date()).getTime();
        }
    }

    public abstract void commit() throws TransactionException;
    
    public synchronized final void end() throws TransactionException {
        try {
            Iterator obs;

            obs = toRemove.iterator();
            while( obs.hasNext() ) {
                BaseEntity p = (BaseEntity)obs.next();

                p.remove(this);
            }
            obs = toCreate.iterator();
            while( obs.hasNext() ) {
                BaseEntity p = (BaseEntity)obs.next();

                p.create(this);
            }
            obs = toStore.iterator();
            while( obs.hasNext() ) {
                BaseEntity p = (BaseEntity)obs.next();
                
                p.store(this);
            }
            commit();
            obs = toRemove.iterator();
            while( obs.hasNext() ) {
                BaseEntity p = (BaseEntity)obs.next();

                p.commit(this);
            }
            obs = toCreate.iterator();
            while( obs.hasNext() ) {
                BaseEntity p = (BaseEntity)obs.next();

                p.commit(this);
            }
            obs = toStore.iterator();
            while( obs.hasNext() ) {
                BaseEntity p = (BaseEntity)obs.next();
                
                p.commit(this);
            }
            toCreate.clear();
            obs = toRemove.iterator();
            while( obs.hasNext() ) {
                BaseEntity p = (BaseEntity)obs.next();
                
                //p.invalidate();
            }
            toRemove.clear();
            toStore.clear();
            Transaction.transactions.remove(userID);
        }
        catch( TransactionException e ) {
            Transaction trans;
            Iterator obs;

            e.printStackTrace();
            rollback();
            Transaction.transactions.remove(userID);
            // use a different transaction to reload everyone
            trans = Transaction.getCurrent(userID);
            obs = toRemove.iterator();
            while( obs.hasNext() ) {
                BaseEntity ob = (BaseEntity)obs.next();

                try {
                    ob.reload(trans);
                }
                catch( Exception disaster ) {
                    // remove it from the cache or something
                }
            }
            obs = toStore.iterator();
            while( obs.hasNext() ) {
                BaseEntity ob = (BaseEntity)obs.next();

                try {
                    ob.reload(trans);
                }
                catch( Exception disaster ) {
                    // remove it from the cache or something
                }
            }
            throw e;
        }
        catch( Exception e ) {
            rollback();
            throw new TransactionException(e.getMessage());
        }
        finally {
            timestamp = -1L;
        }
    }

    public boolean equals(Object ob) {
        if( !(ob instanceof Transaction) ) {
            return false;
        }
        else {
            Transaction trans = (Transaction)ob;

            return trans.userID.equals(userID);
        }
    }
    
    public synchronized final Identifier getIdentifier() {
        return userID;
    }

    public synchronized final long getTimestamp() {
        return timestamp;
    }

    synchronized final void prepareCreate(BaseEntity ob) {
        if( toCreate.contains(ob) ) {
            return;
        }
        toCreate.add(ob);
    }

    public synchronized final boolean isInProcess() {
        return (timestamp != -1L);
    }
    
    synchronized final void prepareRemove(BaseEntity ob) {
        if( toRemove.contains(ob) ) {
            return;
        }
        if( toCreate.contains(ob) ) {
            toCreate.remove(ob);
            return;
        }
        if( toStore.contains(ob) ) {
            toStore.remove(ob);
        }
        toRemove.add(ob);
    }
    
    synchronized final void prepareStore(BaseEntity ob) {
        if( toStore.contains(ob) || toCreate.contains(ob) ) {
            return;
        }
        if( toRemove.contains(ob) ) {
            return;
        }
        toStore.add(ob);
    }
    
    public abstract void rollback() throws TransactionException;
}
