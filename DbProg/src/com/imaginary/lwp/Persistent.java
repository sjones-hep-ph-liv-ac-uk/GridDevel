package com.imaginary.lwp;

public interface Persistent {
    String getLastUpdateID();

    long getLastUpdateTime();
    
    long getObjectID();
 
    void create(Transaction trans) throws PersistenceException;
    
    void load(Transaction trans, long oid) throws PersistenceException;

    void reload(Transaction trans) throws PersistenceException;

    void remove(Transaction trans) throws PersistenceException;
    
    void store(Transaction trans) throws PersistenceException;   
}    
