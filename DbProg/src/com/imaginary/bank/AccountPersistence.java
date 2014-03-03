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

public class AccountPersistence extends JDBCSupport {
    static private final String CREATE =
        "INSERT INTO ACCOUNT (ACCOUNT_ID, CUSTOMER_ID, ACCT_TYPE, BALANCE, " +
        "ACCT_NUMBER, CRT_CLASS, LUID, LUTS) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public void create(Transaction trans, Memento mem)
        throws PersistenceException {
        PreparedStatement stmt = null;

        try {
            Connection conn = ((JDBCTransaction)trans).getConnection();

            stmt = conn.prepareStatement(CREATE);
            {
                Long l = (Long)mem.get(BaseEntity.class, "objectID");
                stmt.setLong(1, l.longValue());
            }
            {
                CustomerFacade cust;

                cust = (CustomerFacade)mem.get(AccountEntity.class,
                                               Account.CUSTOMER);
                stmt.setLong(2, cust.getObjectID());
            }
            {
                AccountType type;

                type = (AccountType)mem.get(AccountEntity.class, Account.TYPE);
                stmt.setString(3, type.getCode());
            }
            {
                Double d = (Double)mem.get(AccountEntity.class, Account.BALANCE);

                stmt.setDouble(4, d.doubleValue());
            }
            {
                Integer num = (Integer)mem.get(AccountEntity.class, Account.NUMBER);

                stmt.setInt(5, num.intValue());
            }
            {
                stmt.setString(6, "com.imaginary.bank.AccountEntity");
            }
            {
                String luid = trans.getIdentifier().getUserID();

                stmt.setString(7, luid);
            }
            {
                stmt.setLong(8, trans.getTimestamp());
            }
            if( stmt.executeUpdate() != 1 ) {
                throw new PersistenceException("No row added!");
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

    protected JDBCJoin getJoin(String tbl) throws FindException {
        if( tbl.equals("CUSTOMER") ) {
            return new JDBCJoin("ACCOUNT.CUSTOMER_ID",
                                "CUSTOMER.CUSTOMER_ID");
        }
        else {
            return null;
        }
    }
    
    protected String getPrimaryTable() {
        return "ACCOUNT";
    }
    
    static private final String SELECT =
        "SELECT ACCT_TYPE, CUSTOMER_ID, ACCT_NUMBER, BALANCE, " +
        "LUID, LUTS " +
        "FROM ACCOUNT " +
        "WHERE ACCOUNT_ID = ?";

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
                String str = rs.getString(1);
                AccountType t;
                
                if( rs.wasNull() ) {
                    t = null;
                }
                else {
                    if( str.trim().equals("CHK") ) {
                        t = AccountType.CHECKING;
                    }
                    else {
                        t = AccountType.SAVINGS;
                    }
                }
                mem.put(AccountEntity.class, Account.TYPE, t);
            }
            {
                long l = rs.getLong(2);
                CustomerFacade cust;

                if( rs.wasNull() ) {
                    cust = null;
                }
                else {
                    cust = new CustomerFacade(rs.getLong(2));
                }
                mem.put(AccountEntity.class, Account.CUSTOMER, cust);
            }
            {
                int num = rs.getInt(3);

                if( rs.wasNull() ) {
                    mem.put(AccountEntity.class, Account.NUMBER, null);
                }
                else {
                    mem.put(AccountEntity.class, Account.NUMBER,
                            new Integer(num));
                }
            }
            {
                double bal = rs.getDouble(4);
                Double d = null;
                
                if( !rs.wasNull() ) {
                    d = new Double(bal);
                }
                mem.put(AccountEntity.class, Account.BALANCE, d);
            }
            {
                String luid = rs.getString(5);

                mem.put(BaseEntity.class, "lastUpdateID", luid);
            }
            {
                long l = rs.getLong(6);

                mem.put(BaseEntity.class, "lastUpdateTime", new Long(l));
            }
            if( rs.next() ) {
                throw new PersistenceException("Multiple rows matching: "
                                               + oid);
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
            return "ACCOUNT_ID";
        }
        else if( fld.equals(Account.CUSTOMER) ) {
            return "CUSTOMER_ID";
        }
        else if( fld.equals(Account.TYPE) ) {
            return "ACCT_TYPE";
        }
        else if( fld.equals(Account.NUMBER) ) {
            return "ACCT_NUMBER";
        }
        else if( fld.equals(Account.BALANCE) ) {
            return "BALANCE";
        }
        else {
            return fld;
        }
    }
        
    static private final String REMOVE =
        "DELETE FROM ACCOUNT WHERE ACCOUNT_ID = ?";
    
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
        "UPDATE ACCOUNT " +
        "SET CUSTOMER_ID = ?, " +
        "ACCT_TYPE = ?, " +
        "ACCT_NUMBER = ?, " +
        "BALANCE = ?, " +
        "LUID = ?, " +
        "LUTS = ? " +
        "WHERE ACCOUNT_ID = ? AND LUID = ? AND LUTS = ?";

    public void store(Transaction trans, Memento mem)
        throws PersistenceException {
        PreparedStatement stmt = null;

        try {
            Connection conn = ((JDBCTransaction)trans).getConnection();
            int count;
            
            stmt = conn.prepareStatement(UPDATE);
            {
                CustomerFacade cust;

                cust = (CustomerFacade)mem.get(AccountEntity.class,Account.CUSTOMER);
                stmt.setLong(1, cust.getObjectID());
            }
            {
                AccountType type;

                type = (AccountType)mem.get(AccountEntity.class, Account.TYPE);
                stmt.setString(2, type.getCode());
            }
            {
                Integer num = (Integer)mem.get(AccountEntity.class, Account.NUMBER);

                stmt.setInt(3, num.intValue());
            }
            {
                Double d = (Double)mem.get(AccountEntity.class, Account.BALANCE);

                stmt.setDouble(4, d.doubleValue());
            }
            stmt.setString(5, trans.getIdentifier().getUserID());
            stmt.setLong(6, trans.getTimestamp());
            {
                Long l = (Long)mem.get(BaseEntity.class, "objectID");

                stmt.setLong(7, l.longValue());
            }
            {
                String luid = (String)mem.get(BaseEntity.class,
                                              "lastUpdateID");

                stmt.setString(8, luid);
            }
            {
                Long l = (Long)mem.get(BaseEntity.class, "lastUpdateTime");

                stmt.setLong(9, l.longValue());
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
