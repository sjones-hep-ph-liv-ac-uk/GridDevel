package com.imaginary.bank;

import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.Session;
import com.imaginary.lwp.TransactionException;
import java.rmi.RemoteException;

public interface AccountTransaction extends Session {
    void deposit(Identifier id, AccountFacade acct, double amt)
        throws InsufficientFundsException, RemoteException,
               TransactionException;

    void transfer(Identifier id, AccountFacade src, AccountFacade targ,
                  double amt)
        throws InsufficientFundsException, RemoteException,
               TransactionException;

    void withdraw(Identifier id, AccountFacade acct, double amt)
        throws InsufficientFundsException, RemoteException,
               TransactionException;
}
