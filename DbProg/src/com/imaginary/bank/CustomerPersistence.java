package com.imaginary.bank;

import com.imaginary.lwp.BaseEntity;
import com.imaginary.lwp.FindException;
import com.imaginary.lwp.Memento;
import com.imaginary.lwp.PersistenceException;
import com.imaginary.lwp.Transaction;
import com.imaginary.lwp.jdbc.JDBCJoin;
import com.imaginary.lwp.jdbc.JDBCSupport;
import com.imaginary.lwp.jdbc.JDBCTransaction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class CustomerPersistence extends JDBCSupport {
    static private final String CREATE =
        "INSERT INTO CUSTOMER (CUSTOMER_ID, FIRST_NAME, LAST_NAME, " +
        "SOCIAL_SECURITY, CRT_CLASS, LUID, LUTS) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    public void create(Transaction trans, Memento mem)
        throws PersistenceException {
        PreparedStatement stmt = null;

        try {
            Connection conn = ((JDBCTransaction)trans).getConnection();
            int count;
            
            stmt = conn.prepareStatement(CREATE);
            {
                Long l = (Long)mem.get(BaseEntity.class, "objectID");

                System.out.println("objectID: " + l);
                stmt.setLong(1, l.longValue());
            }
            {
                String fn = (String)mem.get(CustomerEntity.class,
                                            Customer.FIRST_NAME);

                System.out.println("First name: " + fn);
                stmt.setString(2, fn);
            }
            {
                String ln = (String)mem.get(CustomerEntity.class,
                                            Customer.LAST_NAME);

                stmt.setString(3, ln);
            }
            {
                String ssn = (String)mem.get(CustomerEntity.class,
                                             Customer.SOCIAL_SECURITY);

                stmt.setString(4, ssn);
            }
            {
                stmt.setString(5, "com.imaginary.bank.CustomerEntity");
            }
            {
                String luid = trans.getIdentifier().getUserID();

                stmt.setString(6, luid);
            }
            {
                stmt.setLong(7, trans.getTimestamp());
            }
            count = stmt.executeUpdate();
            if( count != 1 ) {
                throw new PersistenceException("No row added!");
            }
        }
        catch( SQLException e ) {
            System.err.println("Bad SQL: " + e.getMessage());
            e.printStackTrace();
            throw new PersistenceException("Database error: " +
                                           e.getMessage());
        }
        finally {
            if( stmt != null ) {
                try { stmt.close(); }
                catch( SQLException e ) { }
            }
        }
    }

    protected JDBCJoin getJoin(String tbl) throws FindException {
        if( tbl.equals("ACCOUNT") ) {
            return new JDBCJoin("CUSTOMER.CUSTOMER_ID",
                                "ACCOUNT.CUSTOMER_ID");
        }
        else {
            return null;
        }
    }
    
    protected String getPrimaryTable() {
        return "CUSTOMER";
    }
    
    static private final String SELECT =
        "SELECT FIRST_NAME, LAST_NAME, SOCIAL_SECURITY, LUID, LUTS " +
        "FROM CUSTOMER " +
        "WHERE CUSTOMER_ID = ?";

    static private final String LOAD_ACCOUNTS =
        "SELECT ACCOUNT_ID FROM ACCOUNT WHERE CUSTOMER_ID = ?";
    
    public void load(Transaction trans, Memento mem, long oid)
        throws PersistenceException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        mem.put(BaseEntity.class, "objectID", new Long(oid));
        try {
            Connection conn = ((JDBCTransaction)trans).getConnection();

            stmt = conn.prepareStatement(SELECT);
            stmt.setLong(1, oid);
            rs = stmt.executeQuery();
            if( !rs.next() ) {
                throw new PersistenceException("No such objectID: " + oid);
            }
            {
                String fn = rs.getString(1);

                mem.put(CustomerEntity.class, Customer.FIRST_NAME, fn);
            }
            {
                String ln = rs.getString(2);

                mem.put(CustomerEntity.class, Customer.LAST_NAME, ln);
            }
            {
                String ssn = rs.getString(3);

                mem.put(CustomerEntity.class, Customer.SOCIAL_SECURITY, ssn);
            }
            {
                String luid = rs.getString(4);

                mem.put(BaseEntity.class, "lastUpdateID", luid);
            }
            {
                long l = rs.getLong(5);

                mem.put(BaseEntity.class, "lastUpdateTime", new Long(l));
            }
            if( rs.next() ) {
                throw new PersistenceException("Multiple rows matching: "
                                               + oid);
            }
            rs.close();
            rs = null;
            {
                ArrayList accts = new ArrayList();
                
                stmt = conn.prepareStatement(LOAD_ACCOUNTS);
                stmt.setLong(1, oid);
                rs = stmt.executeQuery();
                while( rs.next() ) {
                    long aid = rs.getLong(1);
                    
                    accts.add(new AccountFacade(aid));
                }
                mem.put(CustomerEntity.class, Customer.ACCOUNTS, accts);
            }
        }
        catch( SQLException e ) {
            throw new PersistenceException("Database error: " +
                                           e.getMessage());
        }
        finally {
            if( rs != null ) {
                try { rs.close(); }
                catch( SQLException e ) { }
            }
            if( stmt != null ) {
                try { stmt.close(); }
                catch( SQLException e ) { }
            }
        }
    }

    protected String mapField(String fld) {
        if( fld.equals("objectID") ) {
            return "CUSTOMER_ID";
        }
        else if( fld.equals(Customer.FIRST_NAME) ) {
            return "FIRST_NAME";
        }
        else if( fld.equals(Customer.LAST_NAME) ) {
            return "LAST_NAME";
        }
        else if( fld.equals(Customer.SOCIAL_SECURITY) ) {
            return "SOCIAL_SECURITY";
        }
        else {
            return fld;
        }
    }
        
    static private final String REMOVE =
        "DELETE FROM CUSTOMER WHERE CUSTOMER_ID = ?";
    
    public void remove(Transaction trans, long oid)
        throws PersistenceException {
        PreparedStatement stmt = null;

        try {
            Connection conn = ((JDBCTransaction)trans).getConnection();
            int count;
            
            stmt = conn.prepareStatement(REMOVE);
            stmt.setLong(1, oid);
            count = stmt.executeUpdate();
            if( count < 1 ) {
                throw new PersistenceException("No rows removed!");
            }
            else if( count > 1 ) {
                throw new PersistenceException("Too many rows removed!");
            }
        }
        catch( SQLException e ) {
            throw new PersistenceException("Database error: " +
                                           e.getMessage());
        }
        finally {
            if( stmt != null ) {
                try { stmt.close(); }
                catch( SQLException e ) { }
            }
        }
        
    }
    
    static private final String UPDATE =
        "UPDATE CUSTOMER " +
        "SET FIRST_NAME = ?, " +
        "LAST_NAME = ?, " +
        "SOCIAL_SECURITY = ?, " +
        "LUID = ?, " +
        "LUTS = ? " +
        "WHERE CUSTOMER_ID = ? AND LUID = ? AND LUTS = ?";

    public void store(Transaction trans, Memento mem)
        throws PersistenceException {
        PreparedStatement stmt = null;

        try {
            Connection conn = ((JDBCTransaction)trans).getConnection();
            int count;
            
            stmt = conn.prepareStatement(UPDATE);
            {
                String fn = (String)mem.get(CustomerEntity.class,
                                            Customer.FIRST_NAME);

                stmt.setString(1, fn);
            }
            {
                String ln = (String)mem.get(CustomerEntity.class,
                                            Customer.LAST_NAME);

                stmt.setString(2, ln);
            }
            {
                String ssn = (String)mem.get(CustomerEntity.class,
                                             Customer.SOCIAL_SECURITY);

                stmt.setString(3, ssn);
            }
            stmt.setString(4, trans.getIdentifier().getUserID());
            stmt.setLong(5, trans.getTimestamp());
            {
                Long l = (Long)mem.get(BaseEntity.class, "objectID");

                stmt.setLong(6, l.longValue());
            }
            {
                String luid = (String)mem.get(BaseEntity.class,"lastUpdateID");

                stmt.setString(7, luid);
            }
            {
                Long l = (Long)mem.get(BaseEntity.class, "lastUpdateTime");

                stmt.setLong(8, l.longValue());
            }
            count = stmt.executeUpdate();
            if( count < 1 ) {
                throw new PersistenceException("No rows matching object.");
            }
            else if( count > 1 ) {
                throw new PersistenceException("Too many rows updated: " +
                                               count);
            }
        }
        catch( SQLException e ) {
            throw new PersistenceException("Database error: " +
                                           e.getMessage());
        }
        finally {
            if( stmt != null ) {
                try { stmt.close(); }
                catch( SQLException e ) { }
            }
        }
    }
}
