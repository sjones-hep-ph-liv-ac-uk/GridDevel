package com.imaginary.lwp;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Iterator;

public interface ObjectServer extends Remote {
    Identifier login(String uid, String pw)
        throws AuthenticationException, RemoteException;
    
    Identifier login(String uid, String pw, AuthenticationRole r)
        throws AuthenticationException, RemoteException;
    
    Home lookup(Identifier id, String cname)
        throws LookupException, RemoteException;

    Session startSession(Identifier id, String cname)
        throws LookupException, RemoteException;
}
