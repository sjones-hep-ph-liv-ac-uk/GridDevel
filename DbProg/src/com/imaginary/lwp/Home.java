package com.imaginary.lwp;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface Home extends Remote {
    Collection find(Identifier id, SearchCriteria crit)
        throws FindException, RemoteException, TransactionException;

    Entity findByObjectID(Identifier id, long oid)
        throws FindException, PersistenceException, RemoteException;

    void remove(Identifier id, long oid)
        throws RemoteException, TransactionException;
}
