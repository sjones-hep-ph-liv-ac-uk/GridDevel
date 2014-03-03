package com.imaginary.lwp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public abstract class BaseEntity
extends UnicastRemoteObject implements Entity, Persistent {
    static private HashMap supporters = new HashMap();

    static PersistenceSupport getPersistenceSupport(String cname) {
        synchronized( supporters ) {
            if( supporters.containsKey(cname) ) {
                return (PersistenceSupport)supporters.get(cname);
            }
            else {
                PersistenceSupport sp;
                
                try {
                    String hcls, prop;

                    prop = LWPProperties.HNDLR_PREFIX + cname;
                    hcls = System.getProperty(prop);
                    sp = (PersistenceSupport)Class.forName(hcls).newInstance();
                    supporters.put(cname, sp);
                    return sp;
                }
                catch( Exception e ) {
                    throw new ConfigurationException(e.getMessage());
                }
            }
        }
    }
    
    private transient PersistenceSupport handler        = null;
    private transient Transaction        lock           = null;
    private transient long               lastTouched    = -1L;
    private           String             lastUpdateID   = null;
    private           long               lastUpdateTime = -1L;
    private           long               objectID       = -1L;
    
    public BaseEntity() throws RemoteException {
        super();
        handler = getPersistenceSupport(getClass().getName());
        lastTouched = System.currentTimeMillis();
    }

    public synchronized void commit(Transaction trans) {
        lastUpdateID = trans.getIdentifier().getUserID();
        lastUpdateTime = trans.getTimestamp();
        lock = null;
    }
    
    public synchronized final void create(Transaction trans)
        throws PersistenceException {
        handler.create(trans, new Memento(this));
    }

    public boolean equals(Object target) {
        if( !Entity.class.isAssignableFrom(target.getClass()) ) {
            return false;
        }
        else {
            Entity ent = (Entity)target;

            try {
                long oid = ent.getObjectID();

                if( oid == objectID ) {
                    return true;
                }
                else {
                    return false;
                }
            }
            catch( RemoteException e ) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public synchronized long getLastTouched() {
        return lastTouched;
    }
    
    public synchronized String getLastUpdateID() {
        return lastUpdateID;
    }
    
    public synchronized long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public long getObjectID() {
        return objectID;
    }

    public BaseFacade getFacade() {
        String cname = getFacadeClass();

        try {
            BaseFacade ref;

            ref = (BaseFacade)Class.forName(cname).newInstance();
            ref.assign(objectID, this);
            return ref;
        }
        catch( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    public String getFacadeClass() {
        String cname = getClass().getName();
        int len = cname.length();
        
        if( cname.substring(len-4).equals("Impl") ) {
            return (cname.substring(0, len-4) + "Facade");
        }
        else {
            return cname + "Facade";
        }
    }
    
    public int hashCode() {
        return (new Long(objectID)).hashCode();
    }
    
    public synchronized boolean isChanged(long luit) {
        lastTouched = System.currentTimeMillis();
        if( luit == lastUpdateTime ) {
            return false;
        }
        else {
            return true;
        }
    }

    public synchronized final void load(Transaction trans, long oid)
        throws PersistenceException {
        Memento mem = new Memento();

        handler.load(trans, mem, oid);
        try {
            mem.map(this);
        }
        catch( NoSuchFieldException e ) {
            e.printStackTrace();
            throw new PersistenceException(e.getMessage());
        }
    }

    private void lock(Transaction trans) throws TransactionException {
        if( lock == null ) {
            lock = trans;
        }
        else {
            if( !lock.equals(trans) ) {
                throw new TransactionException("Attempt to access " +
                                               getClass().getName() + " by " +
                                               trans.getIdentifier().getUserID() +
                                               "denied due to lock held by " +
                                               lock.getIdentifier().getUserID());
            }
        }
    }
    
    protected synchronized final void prepareCreate(Identifier id)
        throws TransactionException {
        Transaction trans = Transaction.getCurrent(id);

        lock(trans);
        if( !Identifier.validateCreate(id, this) ) {
            throw new SecurityException("Illegal create attempt on class " +
                                        getClass().getName() + " by " +
                                        id.getUserID());
        }
        try {
            objectID = SequenceGenerator.nextObjectID();
        }
        catch( PersistenceException e ) {
            throw new TransactionException("Failed to generate new objectID.");
        }
        trans.prepareCreate(this);
    }

    protected synchronized final void prepareRead(Identifier id) {
        if( !Identifier.validateRead(id, this) ) {
            throw new SecurityException("Illegal read attempt on class " +
                                        getClass().getName() + " by " +
                                        id.getUserID());
        }
    }

    protected synchronized final void prepareRemove(Identifier id)
        throws TransactionException {
        Transaction trans = Transaction.getCurrent(id);
        
        lock(trans);
        if( !Identifier.validateRemove(id, this) ) {
            throw new SecurityException("Illegal delete attempt on class " +
                                        getClass().getName() + " by " +
                                        id.getUserID());
        }
        trans.prepareRemove(this);
    }
    
    protected synchronized final void prepareStore(Identifier id)
        throws TransactionException {
        Transaction trans = Transaction.getCurrent(id);

        lock(trans);
        if( !Identifier.validateStore(id, this) ) {
            throw new SecurityException("Illegal update attempt on class " +
                                        getClass().getName() + " by " +
                                        id.getUserID());
        }
        trans.prepareStore(this);
    }
    
    public synchronized void reload(Transaction trans)
        throws PersistenceException {
        lastUpdateID = "unknown";
        lastUpdateTime = -1L;
        lock = null;
        load(trans, objectID);
    }

    public synchronized final void remove(Transaction trans)
        throws PersistenceException {
        handler.remove(trans, objectID);
    }
    
    public synchronized final void store(Transaction trans)
        throws PersistenceException {
        handler.store(trans, new Memento(this));
    }
}
