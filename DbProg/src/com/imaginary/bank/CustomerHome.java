package com.imaginary.bank;

import com.imaginary.lwp.Home;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.TransactionException;
import java.rmi.RemoteException;

public interface CustomerHome extends Home {
    CustomerFacade create(Identifier id, String fn, String ln, String ssn)
        throws RemoteException, TransactionException;
}
