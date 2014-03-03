package com.imaginary.bank;

import com.imaginary.lwp.Entity;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.TransactionException;
import java.rmi.RemoteException;

public interface Account extends Entity {
    static public final String BALANCE       = "balance";
    static public final String CUSTOMER      = "customer";
    static public final String NUMBER        = "number";
    static public final String TYPE          = "type";

    void credit(Identifier id, double amt)
        throws RemoteException, TransactionException;
    
    double getBalance(Identifier id) throws RemoteException;

    CustomerFacade getCustomer(Identifier id) throws RemoteException;

    int getNumber(Identifier id) throws RemoteException;
    
    AccountType getType(Identifier id) throws RemoteException;
}
