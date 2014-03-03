package com.imaginary.bank;

import com.imaginary.lwp.BaseHome;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.Transaction;
import com.imaginary.lwp.TransactionException;
import java.rmi.RemoteException;

public class CustomerHomeImpl extends BaseHome implements CustomerHome {
    public CustomerHomeImpl() throws RemoteException {
        super();
    }

    public CustomerFacade create(Identifier id, String fn, String ln,
                                 String ssn)
        throws TransactionException, RemoteException {
        Transaction trans = Transaction.getCurrent(id);
        CustomerEntity cust = new CustomerEntity();
        boolean success = false;

        trans.begin();
        try {
            cust.create(id, fn, ln, ssn);
            success = true;
            return new CustomerFacade(cust);
        }
        finally {
            if( success ) {
                System.out.println("Ending transaction...");
                try { trans.end(); }
                catch( TransactionException e ) { }
            }
            else {
                trans.rollback();
            }
        }
    }
}
