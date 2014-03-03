/* $Id: JDBCGenerator.java,v 1.1 1999/11/07 19:32:30 borg Exp $ */
/* Copyright � 1999 George Reese, All Rights Reserved */
package com.imaginary.lwp.jdbc;

import com.imaginary.lwp.SequenceException;
import com.imaginary.lwp.SequenceGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A JDBC-based sequence generator that implements LWP's
 * <CODE>SequenceGenerator</CODE> interface. To use this sequence
 * generator, your database must have the following data model:
 * <PRE>
 * CREATE TABLE ORA_SEQGEN (
 *     NAME           VARCHAR(25)     NOT NULL PRIMARY KEY,
 *     NEXT_SEQ       BIGINT          NOT NULL DEFAULT 1,
 *     LUTS           BIGINT          NOT NULL);
 * 
 * CREATE UNIQUE INDEX SEQGEN_IDX ON ORA_SEQGEN(NAME, LUTS);
 * </PRE>
 * <BR>
 * Last modified $Date: 1999/11/07 19:32:30 $
 * @version $Revision: 1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class JDBCGenerator extends SequenceGenerator {
    /**
     * The SQL to insert a new sequence number in the table.
     */
    static public final String INSERT =
        "INSERT INTO ORA_SEQGEN (NAME, NEXT_SEQ, LUTS) " +
        "VALUES(?, ?, ?)";

    /**
     * Selects the next sequence number from the database.
     */
    static public final String SELECT = 
        "SELECT NEXT_SEQ, LUTS " +
        "FROM ORA_SEQGEN " +
        "WHERE NAME = ?";

    /**
     * The SQL to one-up the current sequence number.
     */
    static public final String UPDATE =
        "UPDATE ORA_SEQGEN " +
        "SET NEXT_SEQ = ?, " +
        "LUTS = ? " +
        "WHERE NAME = ? " +
        "AND LUTS = ?";

    /**
     * Creates a new sequence.
     * @param conn the JDBC connection to use
     * @param seq the sequence name
     * @throws java.sql.SQLException a database error occurred
     */
    private void createSequence(Connection conn, String seq)
        throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(INSERT);

        stmt.setString(1, seq);
        stmt.setLong(2, 1L);
        stmt.setLong(3, (new java.util.Date()).getTime());
        stmt.executeUpdate();
    }

    /**
     * Generates a sequence for the specified sequence in accordance with
     * the <CODE>SequenceGenerator</CODE> interface.
     * @param seq the name of the sequence to generate
     * @return the next value in the sequence
     * @throws com.imaginary.lwp.SequenceException an error occurred
     * generating the sequence
     */
    public synchronized long generate(String seq) throws SequenceException {
        Connection conn = null;
        
        try {
            PreparedStatement stmt;
            ResultSet rs;
            long nid, lut, tut;
            
            conn = JDBCTransactionImpl.getJDBCConnection();
            stmt = conn.prepareStatement(SELECT);
            stmt.setString(1, seq);
            rs = stmt.executeQuery();
            if( !rs.next() ) {
                try {
                    createSequence(conn, seq);
                }
                catch( SQLException e ) {
                    String state = e.getSQLState();

                    // if a duplicate was found, retry sequence generation
                    if( state.equalsIgnoreCase("SQL0803N") ) {
                        return generate(seq);
                    }
                    throw new SequenceException("Database error: " +
                                                e.getMessage());
                }
                return 0L;
            }
            nid = rs.getLong(1);
            lut = rs.getLong(2);
            tut = (new java.util.Date()).getTime();
            if( tut == lut ) {
                tut++;
            }
            stmt = conn.prepareStatement(UPDATE);
            stmt.setLong(1, nid+1);
            stmt.setLong(2, tut);
            stmt.setString(3, seq);
            stmt.setLong(4, lut);
            try {
                stmt.executeUpdate();
                conn.commit();
            }
            catch( SQLException e ) {
                String state = e.getSQLState();

                // someone else grabbed the row,
                // we need to try again
                if( state.equals("SQL0100W") ) {
                    return generate(seq);
                }
                throw new SequenceException("Database error: " +
                                            e.getMessage());
            }
            return nid;
        }
        catch( SQLException e ) {
            throw new SequenceException("Database error: " +
                                        e.getMessage());
        }
        finally {
            if( conn != null ) {
                try { conn.close(); }
                catch( SQLException e ) { }
            }
        }
    }
}
