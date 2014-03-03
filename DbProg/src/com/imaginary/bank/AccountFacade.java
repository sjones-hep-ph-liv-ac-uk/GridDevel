package com.imaginary.bank;

import com.imaginary.lwp.BaseFacade;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.TransactionException;
import java.rmi.RemoteException;

public class AccountFacade extends BaseFacade {
    public AccountFacade() {
        super();
    }

    public AccountFacade(long oid) {
        super(oid);
    }
    
    public AccountFacade(Account acct) throws RemoteException {
        super(acct);
    }
    
    public void credit(double amt)
        throws RemoteException, TransactionException {
        credit(Identifier.currentIdentifier(), amt);
    }

    public void credit(Identifier id, double amt)
        throws RemoteException, TransactionException {
        Account acct;
        
        try {
            acct = (Account)getEntity();
            acct.credit(id, amt);
        }
        catch( RemoteException e ) {
            reconnect();
            acct = (Account)getEntity();
            acct.credit(id, amt);
        }
    }
    
    public double getBalance() throws RemoteException {
        return getBalance(Identifier.currentIdentifier());
    }

    public double getBalance(Identifier id) throws RemoteException {
        if( contains(Account.BALANCE) ) {
            Double d = (Double)get(Account.BALANCE);
            
            if( d == null ) {
                return 0.0;
            }
            else {
                return d.doubleValue();
            }
        }
        else {
            Account acct;
            double bal;
            
            try {
                acct = (Account)getEntity();
                bal = acct.getBalance(id);
            }
            catch( RemoteException e ) {
                reconnect();
                acct = (Account)getEntity();
                bal = acct.getBalance(id);
            }
            put(Account.BALANCE, new Double(bal));
            return bal;
        }
    }

    public CustomerFacade getCustomer() throws RemoteException {
        return getCustomer(Identifier.currentIdentifier());
    }

    public CustomerFacade getCustomer(Identifier id) throws RemoteException {
        if( contains(Account.CUSTOMER) ) {
            return (CustomerFacade)get(Account.CUSTOMER);
        }
        else {
            CustomerFacade cust;
            Account acct;
            
            try {
                acct = (Account)getEntity();
                cust = acct.getCustomer(id);
            }
            catch( RemoteException e ) {
                reconnect();
                acct = (Account)getEntity();
                cust = acct.getCustomer(id);
            }
            put(Account.CUSTOMER, cust);
            return cust;
        }
    }

    public int getNumber() throws RemoteException {
        return getNumber(Identifier.currentIdentifier());
    }

    public int getNumber(Identifier id) throws RemoteException {
        if( contains(Account.NUMBER) ) {
            Integer num = (Integer)get(Account.NUMBER);
            
            if( num == null ) {
                return 0;
            }
            else {
                return num.intValue();
            }
        }
        else {
            Account acct;
            int num;
            
            try {
                acct = (Account)getEntity();
                num = acct.getNumber(id);
            }
            catch( RemoteException e ) {
                reconnect();
                acct = (Account)getEntity();
                num = acct.getNumber(id);
            }
            put(Account.NUMBER, new Integer(num));
            return num;
        }
    }
    
    public AccountType getType() throws RemoteException {
        return getType(Identifier.currentIdentifier());
    }

    public AccountType getType(Identifier id) throws RemoteException {
        if( contains(Account.TYPE) ) {
            return (AccountType)get(Account.TYPE);
        }
        else {
            Account acct;
            AccountType t;
            
            try {
                acct = (Account)getEntity();
                t = acct.getType(id);
            }
            catch( RemoteException e ) {
                reconnect();
                acct = (Account)getEntity();
                t = acct.getType(id);
            }
            put(Account.TYPE, t);
            return t;
        }
    }
}
