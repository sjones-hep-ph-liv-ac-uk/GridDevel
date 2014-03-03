package com.imaginary.bank;

import com.imaginary.lwp.BaseFacade;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.TransactionException;

import java.rmi.RemoteException;
import java.util.Collection;

public class CustomerFacade extends BaseFacade {
    /**
     * Default constructor.
     */
    public CustomerFacade() {
        super();
    }

    /**
     * Constructs an instance of <CODE>CustomerFacade</CODE> having the 
     * specified <CODE>objectID</CODE>.
     * @param oid the object ID of the referenced bean
     */
    public CustomerFacade(long oid) {
        super(oid);
    }

    /**
     * Constructs an instance of <CODE>CustomerFacade</CODE> referencing the 
     * specified remote interface.
     * @param ent the remote interface of the referenced entity
     */
    public CustomerFacade(Customer ent) throws RemoteException {
        super(ent);
    }

    public void addAccount(AccountFacade acct)
        throws RemoteException, TransactionException {
        addAccount(Identifier.currentIdentifier(), acct);
    }

    public void addAccount(Identifier id, AccountFacade acct)
        throws RemoteException, TransactionException {
        Customer cust;

        reset();
        try {
            cust = (Customer)getEntity();
            cust.addAccount(id, acct);
        }
        catch( RemoteException e ) {
            reconnect();
            cust = (Customer)getEntity();
            cust.addAccount(id, acct);
        }
    }
    
    public Collection getAccounts() throws RemoteException {
        return getAccounts(Identifier.currentIdentifier());
    }
    
    public Collection getAccounts(Identifier id) throws RemoteException {
        Collection val;

        if( contains(Customer.ACCOUNTS) ) {
            val = (Collection)get(Customer.ACCOUNTS);
            return val;
        }
        try {
            Customer ref;

            ref = (Customer)getEntity();
            val = ref.getAccounts(id);
        }
        catch( RemoteException e ) {
            Customer ref;
            reconnect();
            ref = (Customer)getEntity();
            val = ref.getAccounts(id);
        }
        put(Customer.ACCOUNTS, val);
        return val;
    }

    public String getFirstName() throws RemoteException {
        return getFirstName(Identifier.currentIdentifier());
    }
    
    /**
     * Delegates to <CODE>getFirstName</CODE> in the associated entity.
     * @param a see remote interface
     * @return an instance of String
     * @throws RemoteException
     */
    public String getFirstName(Identifier id) throws RemoteException {
        String val;

        if( contains(Customer.FIRST_NAME) ) {
            val = (String)get(Customer.FIRST_NAME);
            return val;
        }
        try {
            Customer ref;

            ref = (Customer)getEntity();
            val = ref.getFirstName(id);
        }
        catch( RemoteException e ) {
            Customer ref;
            reconnect();
            ref = (Customer)getEntity();
            val = ref.getFirstName(id);
        }
        put(Customer.FIRST_NAME, val);
        return val;
    }

    public String getLastName() throws RemoteException {
        return getLastName(Identifier.currentIdentifier());
    }
    
    /**
     * Delegates to <CODE>getLastName</CODE> in the associated entity.
     * @param a see remote interface
     * @return an instance of String
     * @throws RemoteException
     */
    public String getLastName(Identifier id) throws RemoteException {
        String val;

        if( contains(Customer.LAST_NAME) ) {
            val = (String)get(Customer.LAST_NAME);
            return val;
        }
        try {
            Customer ref;

            ref = (Customer)getEntity();
            val = ref.getLastName(id);
        }
        catch( RemoteException e ) {
            Customer ref;
            reconnect();
            ref = (Customer)getEntity();
            val = ref.getLastName(id);
        }
        put(Customer.LAST_NAME, val);
        return val;
    }

    public String getSocialSecurity() throws RemoteException {
        return getSocialSecurity(Identifier.currentIdentifier());
    }
    
    /**
     * Delegates to <CODE>getSocialSecurity</CODE> in the associated entity.
     * @param a see remote interface
     * @return an instance of String
     * @throws RemoteException
     */
    public String getSocialSecurity(Identifier id) throws RemoteException {
        String val;

        if( contains(Customer.SOCIAL_SECURITY) ) {
            val = (String)get(Customer.SOCIAL_SECURITY);
            return val;
        }
        try {
            Customer ref;

            ref = (Customer)getEntity();
            val = ref.getSocialSecurity(id);
        }
        catch( RemoteException e ) {
            Customer ref;
            reconnect();
            ref = (Customer)getEntity();
            val = ref.getSocialSecurity(id);
        }
        put(Customer.SOCIAL_SECURITY, val);
        return val;
    }
}
