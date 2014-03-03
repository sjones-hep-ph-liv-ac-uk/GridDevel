package com.imaginary.bank;

import com.imaginary.lwp.Home;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.TransactionException;
import java.rmi.RemoteException;

public interface AccountHome extends Home {
    AccountFacade create(Identifier id, AccountType t, CustomerFacade cust)
        throws RemoteException, TransactionException;
}
