/* $Id: JDBCTransactionImpl.java,v 1.1 1999/11/07 19:32:31 borg Exp $ */
/* Copyright � 1998-1999 George Reese, All Rights Reserved */
package com.imaginary.lwp.jdbc;

import com.imaginary.lwp.Transaction;
import com.imaginary.lwp.TransactionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Implements the <CODE>Transaction</CODE> interface for support
 * of JDBC transactions.
 * <BR>
 * Last modified $Date: 1999/11/07 19:32:31 $
 * @version $Revision: 1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class JDBCTransactionImpl
extends Transaction implements JDBCTransaction {
    static public Connection getJDBCConnection() throws SQLException {
        String url = System.getProperty("imaginary.lwp.jdbcURL");
        String uid = System.getProperty("imaginary.lwp.user");
        String pw = System.getProperty("imaginary.lwp.password");
        Properties p = new Properties();
        
        if( uid != null ) {
            p.put("user", uid);
        }
        if( pw != null ) {
            p.put("password", pw);
        }
        return DriverManager.getConnection(url, p);
    }
    
    private Connection connection = null;

    /**
     * Constructs a new transaction.
     */
    public JDBCTransactionImpl() {
        super();
    }
    
    /**
     * Sends a commit to the connection currently in use.
     * @throws com.imaginary.lwp.TransactionException the commit failed
     */
    public void commit() throws TransactionException {
        try {
            if( connection == null ) {
                return;
            }
            if(  connection.isClosed() ) {
                throw new TransactionException("Invalid transactional state.");
            }
            connection.commit();
            connection.close();
            connection = null;
        }
        catch( SQLException e ) {
            throw new TransactionException("Database error: " +
                                           e.getMessage());
        }
    }

    /**
     * Provides a JDBC <CODE>Connection</CODE> object to the persistence
     * handler implementing a persistence for a business object. This
     * method finds the connection by loading a <CODE>DataSource</CODE>
     * from a JNDI directory and asking the data source for the
     * connection. The data source name should be provided in the
     * <B>imaginary.lwp.dataSouceName</B> system property.
     * @return the JDBC <CODE>Connection</CODE>
     * @throws java.sql.SQLException an error occurred creating the
     * connection from the data source
     */
    public Connection getConnection() throws SQLException {
        if( connection == null ) {
            connection = getJDBCConnection();
            try {
                connection.setAutoCommit(false);
            }
            catch( SQLException e ) {
            }
        }
        return connection;
    }

    /**
     * Tells the current connection to rollback.
     */
    public void rollback() {
        try {
            if( connection == null ) {
                return;
            }
            if(  connection.isClosed() ) {
                throw new NullPointerException();
            }
            connection.rollback();
            connection.close();
            connection = null;
        }
        catch( SQLException e ) {
           e.printStackTrace();
        }
    }
}
