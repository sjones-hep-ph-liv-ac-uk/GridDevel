/* $Id: JDBCJoin.java,v 1.1 1999/11/07 19:32:31 borg Exp $ */
/* Copyright � 1999 George Reese, All Rights Reserved */
package com.imaginary.lwp.jdbc;

import java.io.Serializable;

/**
 * Represents a join between two tables. The join is constructed with two
 * <CODE>&lt;TABLE&gt;.&lt;COLUMN&gt;</CODE> strings. For example, if
 * a <CODE>BOOK</CODE> table is joined to a <CODE>AUTHOR</CODE> table,
 * this object might be constructed as:
 * <PRE>
 * JDBCJoin join = new JDBCJoin("BOOK.AUTHOR", "AUTHOR.AUTHOR_ID");
 * </PRE>
 * The result is a join that looks like:
 * <PRE>
 * BOOK.AUTHOR = AUTHOR.AUTHOR_ID
 * </PRE>
 * <BR>
 * Last modified $Date: 1999/11/07 19:32:31 $
 * @version $Revision: 1.1 $
 * @author George Reese (borg@imaginary.com)
 */
public class JDBCJoin implements Serializable {
    /**
     * The first table in the join.
     * @serial
     */
    private String first = null;
    /*
     * The second table in the join.
     * @serial
     */
    private String second  = null;

    /**
     * Constructor required by serialization.
     */
    public JDBCJoin() {
        super();
    }

    /**
     * Constructs a new join that joins using the first field to the
     * second field.
     * @param f the first field
     * @param s the second field
     */
    public JDBCJoin(String f, String s) {
        super();
        first = f;
        second = s;
    }

    /**
     * Converts the join into SQL.
     * @return the SQL for the join
     */
    public String toString() {
        return (first + " = " + second);
    }
}
