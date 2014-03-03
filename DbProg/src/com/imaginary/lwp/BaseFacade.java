package com.imaginary.lwp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class BaseFacade implements Serializable {
    private           HashMap               cache          = new HashMap();
    private           Entity                entity         = null;
    private           Home                  home           = null;
    private transient ArrayList             listeners      = new ArrayList();
    private           String                lastUpdateID   = null;
    private           long                  lastUpdateTime = -1L;
    private           long                  objectID       = -1L;
    
    public BaseFacade() {
        super();
    }
    
    public BaseFacade(long oid) {
        super();
        objectID = oid;
    }

    public BaseFacade(Entity ent) throws RemoteException {
        super();
        entity = ent;
        objectID = entity.getObjectID();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if( listeners == null ) {
            listeners = new ArrayList();
        }
        listeners.add(l);
    }

    public void addPropertyChangeListener(String p, PropertyChangeListener l) {
        if( listeners == null ) {
            listeners = new ArrayList();
        }
        listeners.add(l);
    }
    
    public void assign(long oid) {
        if( objectID != -1L ) {
            throw new FacadeReuseException("Facade already assigned.");
        }
        else {
            objectID = oid;
        }
    }
    
    public void assign(long oid, Entity ent) {
        assign(oid);
        entity = ent;
    }

    public void assign(long oid, HashMap vals) {
        assign(oid);
    }

    protected boolean contains(String attr) {
        return cache.containsKey(attr);
    }

    public boolean equals(Object ob) {
        if( ob instanceof BaseFacade ) {
            BaseFacade ref = (BaseFacade)ob;

            return (ref.getObjectID() == getObjectID());
        }
        else {
            return false;
        }
    }
    
    protected void firePropertyChange() {
        firePropertyChange(new PropertyChangeEvent(this, null, null, null));
    }

    protected void firePropertyChange(PropertyChangeEvent evt) {
        Iterator it;

        if( listeners == null ) {
            return;
        }
        it = listeners.iterator();
        while( it.hasNext() ) {
            PropertyChangeListener l = (PropertyChangeListener)it.next();

            l.propertyChange(evt);
        }
    }
    
    protected Object get(String attr) {
        return cache.get(attr);
    }

    public Entity getEntity() throws RemoteException {
        if( entity == null ) {
            reconnect();
        }
        return entity;
    }
    
    public String getLastUpdateID() throws RemoteException {
        if( lastUpdateID == null ) {
            try {
                lastUpdateID = getEntity().getLastUpdateID();
            }
            catch( RemoteException e ) {
                reconnect();
                lastUpdateID = getEntity().getLastUpdateID();
            }
        }
        return lastUpdateID;
    }

    public long getLastUpdateTime() throws RemoteException {
        if( lastUpdateTime == -1L ) {
            try {
                lastUpdateTime = getEntity().getLastUpdateTime();
            }
            catch( RemoteException e ) {
                reconnect();
                lastUpdateTime = getEntity().getLastUpdateTime();
            }
        }
        return lastUpdateTime;
    }
    
    public long getObjectID() {
        return objectID;
    }

    public int hashCode() {
        Long l = new Long(getObjectID());

        return l.hashCode();
    }
    
    public boolean hasListeners(String prop) {
        if( listeners == null ) {
            return false;
        }
        if( listeners.size() > 0 ) {
            return true;
        }
        else {
            return false;
        }
    }
    
    protected void put(String attr, Object val) {
        cache.put(attr, val);
    }

    protected void reconnect() throws RemoteException {
        final BaseFacade ref = this;
        Thread t;

        if( home == null ) {
            String url = System.getProperty(LWPProperties.RMI_URL);
            ObjectServer svr;

            try {
                svr = (ObjectServer)Naming.lookup(url);
            }
            catch( MalformedURLException e ) {
                throw new RemoteException(e.getMessage());
            }
            catch( NotBoundException e ) {
                throw new RemoteException(e.getMessage());
            }
            try {
                Identifier id = Identifier.currentIdentifier();

                if( id == null ) {
                    id = Identifier.getServerID();
                }
                home = (Home)svr.lookup(id, getClass().getName());
            }
            catch( LookupException e ) {
                throw new RemoteException(e.getMessage());
            }
        }
        try {
            Identifier id = Identifier.currentIdentifier();
            
            entity = home.findByObjectID(id, objectID);
            lastUpdateID = entity.getLastUpdateID();
            lastUpdateTime = entity.getLastUpdateTime();
        }
        catch( PersistenceException e ) {
            throw new RemoteException(e.getMessage());
        }
        catch( FindException e ) {
            throw new RemoteException(e.getMessage());
        }
        catch( RemoteException e ) {
            e.printStackTrace();
            String url = System.getProperty(LWPProperties.RMI_URL);
            ObjectServer svr;
            
            try {
                svr = (ObjectServer)Naming.lookup(url);
            }
            catch( MalformedURLException salt ) {
                throw new RemoteException(salt.getMessage());
            }
            catch( NotBoundException salt ) {
                throw new RemoteException(salt.getMessage());
            }
            try {
                Identifier id = Identifier.currentIdentifier();

                if( id == null ) {
                    id = Identifier.getServerID();
                }
                home = (Home)svr.lookup(id, getClass().getName());
                entity = home.findByObjectID(id, objectID);
                lastUpdateID = entity.getLastUpdateID();
                lastUpdateTime = entity.getLastUpdateTime();
            }
            catch( LookupException salt ) {
                throw new RemoteException(salt.getMessage());
            }
            catch( PersistenceException salt ) {
                throw new RemoteException(salt.getMessage());
            }
            catch( FindException salt ) {
                throw new RemoteException(salt.getMessage());
            }
        }
        t = new Thread() {
            public void run() {
                while( true ) {
                    synchronized( ref ) {
                        if( entity == null ) {
                            return;
                        }
                        try {
                            if( lastUpdateTime == -1L ) {
                                lastUpdateTime = entity.getLastUpdateTime();
                            }
                            if( entity.isChanged(lastUpdateTime) ) {
                                lastUpdateTime = entity.getLastUpdateTime();
                                lastUpdateID = entity.getLastUpdateID();
                                firePropertyChange();
                            }
                        }
                        catch( RemoteException e ) {
                            // this will force a reload
                            entity = null;
                            return;
                        }
                    }
                    try { Thread.sleep(30000); }
                    catch( InterruptedException e ) { }
                }
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if( listeners == null ) {
            return;
        }
        listeners.remove(l);
    }

    public void removePropertyChangeListener(String p,
                                             PropertyChangeListener l) {
        if( listeners == null ) {
            return;
        }
        listeners.remove(l);
    }

    public synchronized void reset() {
        cache.clear();
        lastUpdateTime = -1L;
        lastUpdateID = null;
    }
}
