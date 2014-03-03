package com.imaginary.bank;

import com.imaginary.lwp.BaseSession;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.Transaction;
import com.imaginary.lwp.TransactionException;
import java.rmi.RemoteException;

public class AccountTransactionSession
extends BaseSession implements AccountTransaction {
    public AccountTransactionSession() throws RemoteException {
        super();
    }
    
    public void deposit(Identifier id, AccountFacade acct, double amt)
        throws InsufficientFundsException, RemoteException,
               TransactionException {
        Transaction trans = Transaction.getCurrent(id);
        boolean proc = trans.isInProcess();
        boolean success = false;

        if( amt < 0.0 ) {
            withdraw(id, acct, -amt);
        }
        else {
            if( !proc ) {
                trans.begin();
            }
            try {
                acct.credit(id, amt);
                success = true;
            }
            finally {
                if( success ) {
                    if( !proc ) {
                        try { trans.end(); }
                        catch( TransactionException e ) { }
                    }
                }
                else {
                    trans.rollback();
                }
            }
        }
    }

    public void transfer(Identifier id, AccountFacade src, AccountFacade targ,
                         double amt)
        throws InsufficientFundsException, RemoteException,
               TransactionException {
        Transaction trans = Transaction.getCurrent(id);
        boolean success = false;

        if( amt < 0.0 ) {
            AccountFacade tmp = targ;
            
            amt = -amt;
            targ = src;
            src = tmp;
        }
        trans.begin();
        try {
            withdraw(id, src, amt);
            deposit(id, targ, amt);
            success = true;
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

    public void withdraw(Identifier id, AccountFacade acct, double amt)
        throws InsufficientFundsException, RemoteException,
               TransactionException {
        Transaction trans = Transaction.getCurrent(id);
        boolean proc = trans.isInProcess();
        boolean success = false;

        if( amt < 0.0 ) {
            deposit(id, acct, -amt);
        }
        else {
            double bal;
            
            if( !proc ) {
                trans.begin();
            }
            try {
                bal = acct.getBalance();
                if( bal < amt ) {
                    throw new InsufficientFundsException();
                }
                acct.credit(id, -amt);
                success = true;
            }
            finally {
                if( success ) {
                    if( !proc ) {
                        try { trans.end(); }
                        catch( TransactionException e ) { }
                    }
                }
                else {
                    trans.rollback();
                }
            }
        }
   }
}
