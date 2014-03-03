package com.imaginary.bank;

import com.imaginary.lwp.BaseHome;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.Transaction;
import com.imaginary.lwp.TransactionException;
import java.rmi.RemoteException;

public class AccountHomeImpl extends BaseHome implements AccountHome {
    public AccountHomeImpl() throws RemoteException {
        super();
    }

    public AccountFacade create(Identifier id, AccountType t,
                                CustomerFacade cust)
        throws TransactionException, RemoteException {
        Transaction trans = Transaction.getCurrent(id);
        AccountEntity acct = new AccountEntity();
        boolean success = false;

        trans.begin();
        try {
            AccountFacade fac;
            
            acct.create(id, t, cust);
            fac = new AccountFacade(acct);
            cust.addAccount(id, fac);
            success = true;
            return fac;
        }
        finally {
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
