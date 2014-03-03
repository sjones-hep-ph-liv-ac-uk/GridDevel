package com.imaginary.lwp;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public abstract class BaseSession
extends UnicastRemoteObject implements Session {
    static public Session getInstance(Identifier id, Class cls) 
        throws RemoteException {
        String url = System.getProperty(LWPProperties.RMI_URL);
        ObjectServer svr;

        try {
            svr = (ObjectServer)Naming.lookup(url);
            return (Session)svr.startSession(id, cls.getName());
        }
        catch( Exception e ) {
            String msg = e.getMessage();

            if( msg == null ) {
                msg = "";
            }
            e.printStackTrace();
            throw new RemoteException(msg);
        }
    }
    
    public BaseSession() throws RemoteException {
        super();
    }
}
