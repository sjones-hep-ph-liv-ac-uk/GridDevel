/* $Id: JDBCTransaction.java,v 1.1 1999/11/07 19:32:31 borg Exp $ */
/* Copyright � 1998-1999 George Reese, All Rights Reserved */
package com.imaginary.lwp.jdbc;

import com.imaginary.lwp.TransactionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Prescribes methods specific to JDBC transactions.
 * <BR>
 * Last modified $Date: 1999/11/07 19:32:31 $
 * @version $Revision: 1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public interface JDBCTransaction {
    /**
     * Commits the transaction.
     * @throws com.imaginary.lwp.TransactionException a database error occurred
     */
    void commit() throws TransactionException;

    /**
     * Provides a JDBC connection.
     * @return the JDBC connection for this transaction
     * @throws java.sql.SQLException a database error occurred
     */
    Connection getConnection() throws SQLException;

    /**
     * Rolls back the transaction.
     * @throws com.imaginary.lwp.TransactionException a database error occurred
     */
    void rollback() throws TransactionException;
}
