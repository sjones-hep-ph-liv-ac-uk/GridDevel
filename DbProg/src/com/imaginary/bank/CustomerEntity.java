package com.imaginary.bank;

import com.imaginary.lwp.BaseEntity;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.TransactionException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

public class CustomerEntity extends BaseEntity implements Customer {
    private ArrayList accounts       = new ArrayList();
    private String    firstName      = null;
    private String    lastName       = null;
    private String    socialSecurity = null;

    public CustomerEntity() throws RemoteException {
        super();
    }

    public void addAccount(Identifier id, AccountFacade acct)
        throws TransactionException {
        prepareStore(id);
        accounts.add(acct);
    }
    
    public void create(Identifier id, String fn, String ln, String ssn)
        throws TransactionException {
        prepareCreate(id);
        firstName = fn;
        lastName = ln;
        socialSecurity = ssn;
    }

    public Collection getAccounts(Identifier id) {
        prepareRead(id);
        return accounts;
    }
    
    public String getFirstName(Identifier id) {
        prepareRead(id);
        return firstName;
    }

    public String getLastName(Identifier id) {
        prepareRead(id);
        return lastName;
    }

    public String getSocialSecurity(Identifier id) {
        prepareRead(id);
        return socialSecurity;
    }
}
