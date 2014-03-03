package com.imaginary.lwp;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * A client token for identifying itself to the server. When a user
 * logs in to the system successfully, the client is provided with
 * an <CODE>Identifier</CODE> instance that it passes back to the
 * server any time it is involved in a transaction. The server then
 * uses that identifier to validate access to the resource in question.
 * <BR>
 * Last modified $Date: 1999/11/20 17:33:19 $
 * @version $Revision: 1.4 $
 * @author George Reese (borg@imaginary.com)
 */
public class Identifier implements Serializable {
    /**
     * A class that keeps track of an authenticated ID with the last time
     * it was touched.
     */
    private class AuthenticationMonitor {
        /**
         * The authenticated ID
         */
        public Identifier id;
        /**
         * The time it was last touched.
         */
        public long       lastTouched;
    };
    
    /**
     * A list of already authenticated people.
     */
    static private HashMap      authenticated = new HashMap();
    /**
     * Stores the current identifiers for a client.
     */
    static private HashMap      identifiers   = new HashMap();
    /**
     * The random key generator.
     */
    static private SecureRandom randomizer    = null;
    /**
     * The server's ID.
     */
    static private Identifier   serverID      = null;
    
    /**
     * Provides a client application with its identifier so that
     * it can pass it to a transactional method.
     * @return the current client identifier
     */
    static public Identifier currentIdentifier() {
        return currentIdentifier((AuthenticationRole)null);
    }

    /**
     * @param cred a credentials object to use for the role
     * @return the current identifier for the role with the specified
     * credentials
     */
    static public Identifier currentIdentifier(Object cred) {
        return (Identifier)identifiers.get(new AuthenticationRole(cred));
    }
    
    /**
     * @param r the role whose identifier is being sought
     * @return the current identifier for the specified role
     */
    static public Identifier currentIdentifier(AuthenticationRole r) {
        return (Identifier)identifiers.get(r);
    }

    /**
     * Generates a secure, random long used for key generation.
     * @return a random long
     */
    static private long getRandomNumber() {
        byte[] value = new byte[60];
        long l = 0;

        if( randomizer == null ) {
            randomizer = new SecureRandom();
        }            
        randomizer.nextBytes(value);
        for(int i=0; i<60; i++) {
            l = l + (value[i]<<i);
        }
        return l;
    }

    static public Identifier getServerID() {
        if( serverID == null ) {
            serverID = new Identifier("LWPSERVER");
        }
        return serverID;
    }
    
    /**
     * Looks through the list of authenticated users for
     * any authentication matching the specified identifier.
     * @param id the identifier being validated
     * @return true if the id was created by this server
     */
    static boolean isAuthenticated(Identifier id) {
        synchronized( authenticated ) {
            Iterator it;
            HashMap map;

            
            if( id == null ) {
                System.out.println("ID was null.");
                return false;
            }
            if( id.userID.equals("LWPSERVER") ) {
                if( id.key == getServerID().key ) {
                    return true;
                }
                else {
                    return false;
                }
            }
            if( !authenticated.containsKey(id.userID) ) {
                return false;
            }
            map = (HashMap)authenticated.get(id.userID);
            it = map.entrySet().iterator();
            while( it.hasNext() ) {
                Map.Entry ent = (Map.Entry)it.next();
                AuthenticationMonitor mon;
                
                mon = (AuthenticationMonitor)ent.getValue();
                if( mon.id.key == id.key ) {
                    mon.lastTouched = (new Date()).getTime();
                    return true;
                }
            }
            return false;
        }
    }
    
    /**
     * Authenticates the specified user ID against the specified password.
     * This method finds the server and sends the user ID and password
     * to it for authentication.  If the password does not match the
     * currently stored password, then an exception is thrown. Otherwise
     * it will store the identifier the server hands back. This method
     * authenticates for a default role.
     * @param uid the user ID of the person using the system
     * @param pw the password of the user to use for authentication
     * @throws com.imaginary.lwp.AuthenticationException the login attempt
     * failed
     */
    static public Identifier login(String uid, String pw)
        throws AuthenticationException {
        return login(uid, pw, null);
    }

    /**
     * Authenticates the specified user ID against the specified password.
     * This method finds the server and sends the user ID and password
     * to it for authentication.  If the password does not match the
     * currently stored password, then an exception is thrown. Otherwise
     * it will store the identifier the server hands back.
     * @param uid the user ID of the person using the system
     * @param pw the password of the user to use for authentication
     * @param r the role under which the user is being authenticated
     * @throws com.imaginary.lwp.AuthenticationException the login attempt
     * failed
     */
    static public Identifier login(String uid, String pw, AuthenticationRole r)
        throws AuthenticationException {
        String url = System.getProperty(LWPProperties.RMI_URL);
        ObjectServer server;

        try {
            Identifier id;

            server = (ObjectServer)Naming.lookup(url);
            id = server.login(uid, pw, r);
            if( id != null ) {
                identifiers.put(r, id);
            }
            return id;
        }
        catch( MalformedURLException e ) {
            throw new AuthenticationException(e);
        }
        catch( NotBoundException e ) {
            throw new AuthenticationException(e);
        }
        catch( RemoteException e ) {
            throw new AuthenticationException(e);
        }
    }

    /**
     * A thread that goes through the list of authenticated users and
     * throws out people who have not touched the system in a while.
     */
    static void monitor() {
        Thread t = new Thread() {
            public void run() {
                ArrayList uids = new ArrayList();
                
                while( true ) {
                    Iterator keys;
                    
                    try { Thread.sleep(600000); }
                    catch( InterruptedException e ) { }
                    synchronized( authenticated ) {
                        Iterator it = authenticated.keySet().iterator();

                        while( it.hasNext() ) {
                            uids.add(it.next());
                        }
                    }
                    keys = uids.iterator();
                    while( keys.hasNext() ) {
                        String uid = (String)keys.next();
                        long time = (new Date()).getTime();
                        
                        try { Thread.sleep(1000); }
                        catch( InterruptedException e ) { }
                        synchronized( authenticated ) {
                            if( authenticated.containsKey(uid) ) {
                                HashMap map = (HashMap)authenticated.get(uid);
                                Iterator roles = map.keySet().iterator();

                                while( roles.hasNext() ) {
                                    AuthenticationRole r;
                                    AuthenticationMonitor mon;
                                    long diff;
                                    
                                    r = (AuthenticationRole)roles.next();
                                    mon = (AuthenticationMonitor)map.get(r);
                                    diff = time - mon.lastTouched;
                                    // 30 minutes
                                    if( diff > 1800000 ) {
                                        map.remove(r);
                                        if( map.size() < 1 ) {
                                            authenticated.remove(uid);
                                        }
                                    }
                                }
                            }
                        }       
                    }            
                }
            }
        };

        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    /**
     * This implementation currently only verifies that the user is
     * authenticated.
     */
    static boolean validateCreate(Identifier id, BaseEntity ent) {
        return isAuthenticated(id);
    }

    static boolean validateRead(Identifier id, BaseEntity ent) {
        return isAuthenticated(id);
    }
    
    /**
     * This implementation currently only verifies that the user is
     * authenticated.
     */
    static boolean validateRemove(Identifier id, BaseEntity ent) {
        return isAuthenticated(id);
    }

    /**
     * This implementation currently only verifies that the user is
     * authenticated.
     */
    static boolean validateStore(Identifier id, BaseEntity ent) {
        return isAuthenticated(id);
    }
    
    /**
     * A token that makes sure this identifier works only for this
     * session.
     * @serial
     */
    private long               key    = -1L;
    /**
     * The user ID associated with this user.
     * @serial
     */
    private String             userID = null;

    /**
     * Empty constructor required by serialization.
     */
    public Identifier() {
        super();
    }
    
    /**
     * Constructs an identifier associated with a specific system user
     * under a default role.
     * @param uid the user ID of the person this identifier represents
     */
    Identifier(String uid) {
        this(uid, null);
    }

    /**
     * Constructs an identifier associated with the specified user under
     * the specified role.
     * @param uid the user ID this identifier represents
     * @param r the role under which this UID is authenticated
     */
    Identifier(String uid, AuthenticationRole r) {
        synchronized( authenticated ) {
            if( authenticated.containsKey(uid) ) {
                HashMap map = (HashMap)authenticated.get(uid);
                
                if( map.containsKey(r) ) {
                    AuthenticationMonitor mon;

                    mon = (AuthenticationMonitor)map.get(r);
                    key = mon.id.key;
                    userID = mon.id.userID;
                    mon.lastTouched = (new Date()).getTime();
                }
                else {
                    AuthenticationMonitor mon = new AuthenticationMonitor();

                    key = getRandomNumber();
                    if( uid.equals("guest") ) {
                        userID = "guest" + key;
                        if( userID.length() > 15 ) {
                            userID = userID.substring(0, 14);
                        }
                    }
                    else {
                        userID = uid;
                    }
                    mon.id = this;
                    mon.lastTouched = (new Date()).getTime();
                    map.put(r, mon);
                }
            }
            else {
                AuthenticationMonitor mon = new AuthenticationMonitor();
                HashMap map = new HashMap();
                
                key = getRandomNumber();
                if( uid.equals("guest") ) {
                    userID = "guest" + key;
                    if( userID.length() > 15 ) {
                        userID = userID.substring(0, 14);
                    }
                }
                else {
                    userID = uid;
                }
                mon.id = this;
                mon.lastTouched = (new Date()).getTime();
                map.put(r, mon);
                authenticated.put(uid, map);
            }
        }
    }
    
    /**
     * @param the object to compare to
     * @return true if the object is an <CODE>Identifier</CODE> and it
     * shares the same key as this object
     */
    public boolean equals(Object ob) {
        if( ob instanceof Identifier ) {
            Identifier id = (Identifier)ob;

            if( key != id.key ) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * @return the user ID associated with this identifier
     */
    public String getUserID() {
        return userID;
    }

    /**
     * A hash code based on the key.
     */
    public int hashCode() {
        return (new Long(key)).hashCode();
    }

    /**
     * @return the return value from <CODE>toString()</CODE>
     * @see #toString()
     */
    public String toLocaleString(Locale loc) {
        return toString();
    }
    
    /**
     * @return a human-readable version of this identifier
     */
    public String toString() {
        return userID;
    }
}
