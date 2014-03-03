package com.imaginary.lwp;

import com.imaginary.lwp.jdbc.JDBCAuthenticator;
import com.imaginary.util.PropertyReader;
import java.io.File;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;

public class ObjectServerImpl
extends UnicastRemoteObject implements ObjectServer {
    static public void main(String[] args) {
        try {
            String bundle = System.getProperty(LWPProperties.PROPS_BUNDLE);
            Thread t;

            t = new Thread() {
                public void run() {
                    Identifier id = Identifier.getServerID();
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
            if( bundle != null ) {
                PropertyReader r = new PropertyReader();

                r.read(bundle);
            }
            else {
                String fname = System.getProperty(LWPProperties.PROPS_FILE);

                if( fname != null ) {
                    PropertyReader r = new PropertyReader();

                    r.read(new File(fname));
                }
            }
            Naming.rebind(System.getProperty(LWPProperties.RMI_URL),
                          new ObjectServerImpl());
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }

    private Authenticator authenticator = null;
    private HashMap       homes         = new HashMap();
    
    private ObjectServerImpl() throws RemoteException {
        super();
        {
            String acl = System.getProperty(LWPProperties.AUTHENTICATOR);

            try {
                authenticator =(Authenticator)Class.forName(acl).newInstance();
            }
            catch( Exception e ) {
                authenticator = new JDBCAuthenticator();
            }
        }
    }

    private boolean isAuthenticated(Identifier id) {
        return Identifier.isAuthenticated(id);
    }

    public Identifier login(String uid, String pw)
        throws AuthenticationException, RemoteException {
        authenticator.authenticate(uid, pw);
        return new Identifier(uid);
    }

    public Identifier login(String uid, String pw, AuthenticationRole r)
        throws AuthenticationException, RemoteException {
        authenticator.authenticate(uid, pw, r);
        return new Identifier(uid, r);
    }

    public Home lookup(Identifier id, String cname)
        throws LookupException, RemoteException {
        String hname;
        Home home;
        int len;

        if( !isAuthenticated(id) ) {
            return null;
        }
        len = cname.length();
        if( len > 4 ) {
            if( cname.substring(len-4).equals("Impl") ) {
                hname = cname.substring(0, len-4);
            }
            else if( len > 6 ) {
                if( cname.substring(len-6).equals("Facade") ) {
                    hname = cname.substring(0, len-6);
                }
                else {
                    hname = cname;
                }
            }
            else {
                hname = cname;
            }
        }
        else {
            hname = cname;
        }
        hname = hname + "HomeImpl";
        synchronized( homes ) {
            if( homes.containsKey(hname) ) {
                return (Home)homes.get(hname);
            }
            try {
                home = (Home)Class.forName(hname).newInstance();
            }
            catch( ClassCastException e ) {
                throw new LookupException(e.getMessage());
            }
            catch( ClassNotFoundException e ) {
                throw new LookupException(e.getMessage());
            }
            catch( IllegalAccessException e ) {
                throw new LookupException(e.getMessage());
            }
            catch( InstantiationException e ) {
                throw new LookupException(e.getMessage());
            }
            homes.put(hname, home);
            return home;
        }
    }
    
    public Session startSession(Identifier id, String cname)
        throws LookupException, RemoteException {
        String sname;
        int len;
        
        if( !isAuthenticated(id) ) {
            return null;
        }
        len = cname.length();
        if( len > 7 ) {
            if( cname.substring(len-7).equals("Session") ) {
                sname = cname.substring(0, len-7);
            }
            else {
                sname = cname;
            }
        }
        else {
            sname = cname;
        }
        sname = sname + "Session";
        try {
            return (Session)Class.forName(sname).newInstance();
        }
        catch( ClassCastException e ) {
            throw new LookupException(e.getMessage());
        }
        catch( ClassNotFoundException e ) {
            throw new LookupException(e.getMessage());
        }
        catch( IllegalAccessException e ) {
            throw new LookupException(e.getMessage());
        }
        catch( InstantiationException e ) {
            throw new LookupException(e.getMessage());
        }
    }
}
