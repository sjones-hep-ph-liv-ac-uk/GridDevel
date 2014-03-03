package com.imaginary.bank.ui;

import com.imaginary.bank.Account;
import com.imaginary.bank.AccountFacade;
import com.imaginary.bank.AccountHome;
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

public class AccountNode implements TreeNode {
    private AccountFacade account  = null;
    private ArrayList     children = null;
    private TreeNode      parent   = null;

    public AccountNode(TreeNode prnt) {
        super();
        parent = prnt;
    }
    
    public AccountNode(TreeNode prnt, AccountFacade acct) {
        super();
        parent = prnt;
        account = acct;
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

    public int getIndex(TreeNode chld) {
        return getChildren().indexOf(chld);
    }

    public TreeNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        if( parent instanceof CustomerNode ) {
            return true;
        }
        else {
            return false;
        }
    }

    private void load() {
        TellerApp.notifyWait();
        try {
            if( account == null ) {
                AccountHome home;
                
                children = new ArrayList();
                try {
                    String[] pre = { "number" };
                    SearchCriteria sc;
                    Iterator it;
                    
                    home = (AccountHome)BaseHome.getInstance(Identifier.currentIdentifier(),
                                                             Account.class);
                    sc = new SearchCriteria(pre);
                    it = home.find(Identifier.currentIdentifier(), sc).iterator();
                    while( it.hasNext() ) {
                        AccountFacade acct = (AccountFacade)it.next();
                        
                        children.add(new AccountNode(this, acct));
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
                CustomerFacade cust;
                
                children = new ArrayList();
                try {
                    cust = account.getCustomer();
                }
                catch( RemoteException e ) {
                    e.printStackTrace();
                    return;
                }
                children.add(new CustomerNode(this, cust));
            }
        }
        finally {
            TellerApp.notifyResume();
        }
    }
    
    public String toString() {
        if( account == null ) {
            return "Accounts";
        }
        else {
            TellerApp.notifyWait();
            try {
                return ("" + account.getNumber());
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
