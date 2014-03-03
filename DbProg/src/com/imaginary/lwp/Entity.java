package com.imaginary.lwp;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Entity extends Remote {
    String getLastUpdateID() throws RemoteException;

    long getLastUpdateTime() throws RemoteException;
    
    long getObjectID() throws RemoteException;

    BaseFacade getFacade() throws RemoteException;

    boolean isChanged(long luit) throws RemoteException;
}
