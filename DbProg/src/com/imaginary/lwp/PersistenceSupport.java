package com.imaginary.lwp;

import java.util.Collection;

public interface PersistenceSupport {
    public abstract void create(Transaction trans, Memento mem)
        throws PersistenceException;

    public abstract Collection find(Transaction trans, SearchCriteria sc)
        throws FindException;

    public abstract void load(Transaction trans, Memento mem, long oid)
        throws PersistenceException;

    public abstract void remove(Transaction trans, long oid)
        throws PersistenceException;
    
    public abstract void store(Transaction trans, Memento mem)
        throws PersistenceException;
}
