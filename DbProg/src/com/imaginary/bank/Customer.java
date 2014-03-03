package com.imaginary.bank;

import com.imaginary.lwp.Entity;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.TransactionException;

import java.rmi.RemoteException;
import java.util.Collection;

public interface Customer extends Entity {
    static public final String ACCOUNTS         = "accounts";
    static public final String FIRST_NAME       = "firstName";
    static public final String LAST_NAME        = "lastName";
    static public final String SOCIAL_SECURITY  = "socialSecurity";

    public void addAccount(Identifier id, AccountFacade acct)
        throws RemoteException, TransactionException;
    
    public Collection getAccounts(Identifier id) throws RemoteException;
    
    public String getFirstName(Identifier id) throws RemoteException;
    
    public String getLastName(Identifier id) throws RemoteException;

    public String getSocialSecurity(Identifier id) throws RemoteException;
}    
