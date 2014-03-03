package com.imaginary.bank;

import com.imaginary.lwp.BaseEntity;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.SequenceGenerator;
import com.imaginary.lwp.SequenceException;
import com.imaginary.lwp.TransactionException;
import java.rmi.RemoteException;

public class AccountEntity extends BaseEntity implements Account {
    private double         balance      = 0.0;
    private CustomerFacade customer     = null;
    private int            number       = 0;
    private AccountType    type         = null;

    public AccountEntity() throws RemoteException {
        super();
    }

    public void create(Identifier id, AccountType t, CustomerFacade cust)
        throws TransactionException {
        prepareCreate(id);
        try {
            number = (int)SequenceGenerator.generateSequence("ACCT_NUM");
        }
        catch( SequenceException e ) {
            throw new TransactionException(e.getMessage());
        }
        type = t;
        customer = cust;
    }
    
    public void credit(Identifier id, double amt)
        throws TransactionException {
        prepareStore(id);
        balance += amt;
    }
    
    public double getBalance(Identifier id) {
        prepareRead(id);
        return balance;
    }

    public CustomerFacade getCustomer(Identifier id) {
        prepareRead(id);
        return customer;
    }
    
    public int getNumber(Identifier id) {
        prepareRead(id);
        return number;
    }
    
    public AccountType getType(Identifier id) {
        prepareRead(id);
        return type;
    }
}
