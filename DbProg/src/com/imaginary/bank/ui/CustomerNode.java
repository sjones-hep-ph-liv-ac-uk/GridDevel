package com.imaginary.bank.ui;

import com.imaginary.bank.Customer;
import com.imaginary.bank.AccountFacade;
import com.imaginary.bank.CustomerHome;
import com.imaginary.bank.CustomerFacade;

import com.imaginary.lwp.BaseHome;
import com.imaginary.lwp.FindException;
import com.imaginary.lwp.Identifier;
import com.imaginary.lwp.SearchCriteria;
import com.imaginary.lwp.TransactionException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.tree.TreeNode;

public class CustomerNode implements TreeNode {
    private CustomerFacade customer  = null;
    private ArrayList      children  = null;
    private TreeNode       parent    = null;

    public CustomerNode(TreeNode prnt) {
        super();
        parent = prnt;
    }
    
    public CustomerNode(TreeNode prnt, CustomerFacade cust) {
        super();
        parent = prnt;
        customer = cust;
    }

    public Enumeration children() {
        return new RootNode.IteratorEnumeration(children.iterator());
    }

    public boolean getAllowsChildren() {
        return !isLeaf();
    }
    
    public TreeNode getChildAt(int ind) {
        return (TreeNode)getChildren().get(ind);
    }

    public int getChildCount() {
        return getChildren().size();
    }
    
    private synchronized ArrayList getChildren() {
        if( children == null ) {
            load();
        }
        return children;
    }

    public CustomerFacade getCustomer() {
        return customer;
    }
    
    public int getIndex(TreeNode chld) {
        return getChildren().indexOf(chld);
    }

    public TreeNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        if( parent instanceof AccountNode ) {
            return true;
        }
        else {
            return false;
        }
    }

    private void load() {
        TellerApp.notifyWait();
        try {
            if( customer == null ) {
                CustomerHome home;
                
                children = new ArrayList();
                try {
                    String[] pre = { "firstName", "lastName" };
                    SearchCriteria sc;
                    Iterator it;
                    
                    home = (CustomerHome)BaseHome.getInstance(Identifier.currentIdentifier(),
                                                              Customer.class);
                    sc = new SearchCriteria(pre);
                    it = home.find(Identifier.currentIdentifier(), sc).iterator();
                    while( it.hasNext() ) {
                        CustomerFacade cust = (CustomerFacade)it.next();
                        
                        children.add(new CustomerNode(this, cust));
                    }
                }
                catch( RemoteException e ) {
                    e.printStackTrace();
                    return;
                }
                catch( FindException e ) {
                    e.printStackTrace();
                    return;
                }
                catch( TransactionException e ) {
                    e.printStackTrace();
                    return;
                }
            }
            else {
                Iterator it;
                
                children = new ArrayList();
                try {
                    it = customer.getAccounts().iterator();
                }
                catch( RemoteException e ) {
                    e.printStackTrace();
                    return;
                }
                while( it.hasNext() ) {
                    AccountFacade acct = (AccountFacade)it.next();
                    
                    children.add(new AccountNode(this, acct));
                }
            }
        }
        finally {
            TellerApp.notifyResume();
        }
    }
    
    public String toString() {
        if( customer == null ) {
            return "Customers";
        }
        else {
            try {
                TellerApp.notifyWait();
                return (customer.getLastName() + ", " +
                        customer.getFirstName());
            }
            catch( RemoteException e ) {
                e.printStackTrace();
                return "ERROR";
            }
            finally {
                TellerApp.notifyResume();
            }
        }
    }
}
