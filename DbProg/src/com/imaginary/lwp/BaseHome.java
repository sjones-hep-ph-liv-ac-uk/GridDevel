package com.imaginary.lwp;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public abstract class BaseHome extends UnicastRemoteObject implements Home {
    static public Home getInstance(Class cls) throws RemoteException {
        return getInstance(Identifier.currentIdentifier(), cls);
    }

    static public Home getInstance(Identifier id, Class cls)
        throws RemoteException {
        String url = System.getProperty(LWPProperties.RMI_URL);
        ObjectServer svr;

        try {
            svr = (ObjectServer)Naming.lookup(url);
            return (Home)svr.lookup(id, cls.getName());
        }
        catch( Exception e ) {
            String msg = e.getMessage();

            if( msg == null ) {
                msg = "";
            }
            e.printStackTrace();
            throw new RemoteException(msg);
        }
    }

    private HashMap            cache    = new HashMap();
    private HashMap            marked   = new HashMap();
    private PersistenceSupport support  = null;
    
    public BaseHome() throws RemoteException {
        super();
        {
            String cname = getClass().getName();

            // Take FrogHomeImpl and make it FrogEntity
            cname = cname.substring(0, cname.length()-8) + "Entity";
            support = BaseEntity.getPersistenceSupport(cname);
        }
        Thread t = new Thread() {
            public void run() {
                sweep();
            }
        };

        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    protected void cache(BaseEntity ob) {
        synchronized( cache ) {
            cache.put(new Long(ob.getObjectID()), ob);
        }
    }
    
    public Collection find(Identifier id, SearchCriteria sc)
        throws FindException, TransactionException {
        Transaction trans = Transaction.getCurrent(null);
        Collection results;

        trans.begin();
        results = support.find(trans, sc);
        trans.end();
        return results;
    }
    
    public final Entity findByObjectID(Identifier id,  long oid)
        throws FindException, RemoteException {
        Long lid = new Long(oid);
        BaseEntity ent;

        synchronized( cache ) {
            String cname = null;
            
            if( cache.containsKey(lid) ) {
                return (Entity)cache.get(lid);
            }
            if( marked.containsKey(lid) ) {
                ent = (BaseEntity)marked.get(lid);
                marked.remove(lid);
                cache.put(lid, ent);
                return ent;
            }
            try {
                Transaction trans = Transaction.getCurrent(null);

                trans.begin();
                cname = getClass().getName();
                cname = cname.substring(0, cname.length()-8) + "Entity";
                ent = (BaseEntity)Class.forName(cname).newInstance();
                ent.load(trans, oid);
                cache.put(lid, ent);
                trans.end();
                return ent;
            }
            catch( PersistenceException e ) {
                e.printStackTrace();
                throw new FindException(e.getMessage());
            }
            catch( TransactionException e ) {
                e.printStackTrace();
                throw new FindException(e.getMessage());
            }
            catch( ClassCastException e ) {
                throw new FindException("Specified class is not an entity: " +
                                        cname);
            }
            catch( ClassNotFoundException e ) {
                throw new FindException("Specified class could not be found " +
                                        cname);
            }
            catch( InstantiationException e ) {
                throw new FindException("Specified entity class could not " +
                                        "be loaded: " + cname);
            }
            catch( IllegalAccessException e ) {
                throw new FindException("Specified entity class does not " +
                                        "have an accessible constructor: " +
                                        cname);
            }
        }
    }

    public void remove(Identifier id, long oid)
        throws RemoteException, TransactionException {
        Transaction trans = Transaction.getCurrent(id);
        boolean proc = trans.isInProcess();
        boolean success = false;

        if( !proc ) {
            trans.begin();
        }
        try {
            synchronized( cache ) {
                Long lid = new Long(oid);
                
                if( cache.containsKey(lid) ) {
                    BaseEntity ent = (BaseEntity)cache.get(lid);

                    try {
                        ent.remove(trans);
                    }
                    catch( PersistenceException e ) {
                        throw new TransactionException("Persistence error: " +
                                                       e.getMessage());
                    }
                    cache.remove(lid);
                    success = true;
                }
            }
            if( !success ) {
                // TODO: add security check here for deleting objects
                try {
                    support.remove(trans, oid);
                }
                catch( PersistenceException e ) {
                    throw new TransactionException("Persistence error: " +
                                                   e.getMessage());
                }
                success = true;
            }
        }
        finally {
            if( !proc ) {
                if( success ) {
                    try { trans.end(); }
                    catch( TransactionException e ) { }
                }
                else {
                    trans.rollback();
                }
            }
        }
    }
    
    private void sweep() {
        while( true ) {
            HashMap copy;
            Iterator it;
            long t;
            int i;
            
            try { Thread.sleep(3600000); }
            catch( InterruptedException e ) { }
            i = 0;
            copy = new HashMap();
            synchronized( cache ) {
                copy.putAll(marked);
            }
            it = copy.keySet().iterator();
            while( it.hasNext() ) {
                Long l = (Long)it.next();
                BaseEntity ent = (BaseEntity)copy.get(l);

                t = System.currentTimeMillis();
                if( false ) {
                    //if( !ent.isValid() ) { FIXME
                    synchronized( cache ) {
                        marked.remove(l);
                        i++;
                    }
                }
                else if( (t - ent.getLastTouched()) > 60000 ) {
                    //ent.invalidate(); FIXME
                    synchronized( cache ) {
                        marked.remove(l);
                        i++;
                    }
                }
            }
            System.out.println("\t" + i + " objects invalidated.");
            try { Thread.sleep(1500); }
            catch( InterruptedException e ) { }
            synchronized( cache ) {
                System.out.println("\tMarking " + cache.size() + " objects.");
                marked.putAll(cache);
                cache.clear();
            }
            System.out.println("Sweep complete.");
        }
    }
}
