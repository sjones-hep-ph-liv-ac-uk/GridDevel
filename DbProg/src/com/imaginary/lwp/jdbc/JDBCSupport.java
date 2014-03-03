package com.imaginary.lwp.jdbc;

import com.imaginary.lwp.BaseFacade;
import com.imaginary.lwp.FindException;
import com.imaginary.lwp.PersistenceSupport;
import com.imaginary.lwp.SearchBinding;
import com.imaginary.lwp.SearchCriteria;
import com.imaginary.lwp.Transaction;
import com.imaginary.util.DistributedList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Persistence support for JDBC-based persistence.
 * <BR>
 * Last modified $Date: 1999/11/20 17:33:19 $
 * @version $Revision: 1.2 $
 * @author George Reese (borg@imaginary.com)
 */
public abstract class JDBCSupport implements PersistenceSupport {
    /**
     * Binds the search bindings to the statement in progress.
     * @param stmt the statement being set up
     * @param ind the index to start binding at
     * @param bindings the bindings to bind
     * @throws com.imaginary.lwp.FindException
     * @throws java.sql.SQLException an error occurred binding the bindings
     * to the statement
     */
    private void bind(PreparedStatement stmt, int ind, Iterator bindings)
        throws FindException, SQLException  {
        while( bindings.hasNext() ) {
            SearchBinding bdg = (SearchBinding)bindings.next();
            Object val = bdg.getValue();
            
            if( val instanceof SearchCriteria ) {
                SearchCriteria sc = (SearchCriteria)val;

                bind(stmt, ind, sc.bindings());
            }
            else if( val instanceof BaseFacade ) {
                BaseFacade ref = (BaseFacade)val;

                stmt.setLong(ind++, ref.getObjectID());
            }
            else {
                stmt.setObject(ind++, val);
            }
        }
    }

    /**
     * Executes a search for objects meeting the specified criteria
     * using the specified transaction.
     * @param tr the transaction to use for the find operation
     * @param sc the search criteria to base the find on
     * @return an iterator of matching objects
     * @throws com.imaginary.lwp.FindException an error occurred
     * searching for objects meeting the search criteria
     */
    public Collection find(Transaction tr, SearchCriteria sc)
        throws FindException {
        Iterator bindings = sc.bindings();
        DistributedList list = new DistributedList();
        String sql = getFindSQL(sc);

        try {
            JDBCTransaction trans;
            Connection conn;

            trans = (JDBCTransaction)tr;
            try {
                conn = trans.getConnection();
            }
            catch( Exception e ) {
                e.printStackTrace();
                return null;
            }
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSetMetaData meta;
            ResultSet rs;
            int cc;

            bind(stmt, 1, bindings);
            rs = stmt.executeQuery();
            meta = rs.getMetaData();
            cc = meta.getColumnCount();
            while( rs.next() ) {
                HashMap map = new HashMap();
                long oid = rs.getLong(1);
                String cls = rs.getString(2);
                
                for(int i=3; i<=cc; i++) {
                    String tbl = meta.getTableName(i).toUpperCase();
                    String name = meta.getColumnLabel(i).toUpperCase();
                    Object val = rs.getObject(i);

                    if( tbl.equals("") ) {
                        tbl = getPrimaryTable().toUpperCase();
                    }
                    name = tbl + "." + name;
                    if( rs.wasNull() ) {
                        val = null;
                    }
                    map.put(name, val);
                }
                list.add(getFacade(oid, cls, map));
            }
            return list;
        }
        catch( SQLException e ) {
            throw new FindException("Database error: " + e.getMessage());
        }
    }

    /**
     * Provides the reference object for objects supported by this
     * persistence support object.
     * @param oid the object ID of the desired object
     * @param cls the reference class name
     * @param vals the initial cache values
     * @return an instance of the reference class pointing to the specified
     * object
     * @throws com.imaginary.lwp.FindException the specified class could not
     * be loaded
     */
    public final BaseFacade getFacade(long oid, String cls, HashMap vals)
        throws FindException {
        try {
            BaseFacade ref;

            ref = (BaseFacade)Class.forName(cls).newInstance();
            ref.assign(oid, vals);
            return ref;
        }
        catch( Exception e ) {
            throw new FindException("Database error: " + e.getMessage());
        }
    }

    /**
     * Special method for building a <CODE>SELECT</CODE> statement that
     * will perform a search using the named search critieria.
     * @param sc the search criteria to build SQL from
     * @return the SQL that performs the select
     * @throws com.imaginary.lwp.FindException the SQL could not be built
     */
    protected String getFindSQL(SearchCriteria sc) throws FindException {
        StringBuffer sql = new StringBuffer("SELECT ");
        ArrayList tables = new ArrayList();
        String where, order;
        Iterator it;
        
        sql.append(getPrimaryTable() + "." + mapField("objectID"));
        sql.append(", " + getPrimaryTable() + ".CRT_CLASS");
        tables.add(getPrimaryTable());
        it = sc.preloads();
        while( it.hasNext() ) {
            String fld = mapField((String)it.next());
            int i = fld.indexOf(".");
            String tbl;

            if( i != -1 ) {
                tbl = fld.substring(0, i);
                if( !tables.contains(tbl) ) {
                    tables.add(tbl);
                }
            }
            sql.append(", ");
            sql.append(fld);
        }
        where = getWhere(sc.bindings(), tables);
        order = getOrder(sc.sorts(), tables);
        it = tables.iterator();
        sql.append(" FROM ");
        while( it.hasNext() ) {
            sql.append((String)it.next());
            if( it.hasNext() ) {
                sql.append(", ");
            }
        }
        if( where.length() > 0 ) {
            sql.append(" WHERE ");
            sql.append("(" + where + ")");
        }
        else if( tables.size() > 1 ) {
            sql.append(" WHERE ");
        }
        it = tables.iterator();
        while( it.hasNext() ) {
            String tbl = (String)it.next();
            JDBCJoin join;
            
            if( tbl.equals(getPrimaryTable()) ) {
                continue;
            }
            join = getJoin(tbl);
            sql.append(" AND " + join.toString() + " ");
        }
        if( order.length() > 0 ) {
            sql.append(" ORDER BY " + order);
        }
        return sql.toString();
    }

    /**
     * Given a table, this method needs to provide a portion of a
     * <CODE>WHERE</CODE> clause that supports joining to the specified
     * table.
     * @param tbl the table to join to
     * @return the join object that represents a join for the primary
     * table to the specified table
     * @throws com.imaginary.lwp.FindException a join could not be constructed
     */
    protected abstract JDBCJoin getJoin(String tbl) throws FindException;

    /**
     * Provides the <CODE>ORDER BY</CODE> clause to support ordering of
     * the results.
     * @param sorts the sort criteria from the search criteria object
     * @param a pass by reference thing where any new tables that need
     * to be joined to are added to this list
     * @return a string with the <CODE>ORDER BY</CODE> clause
     * @throws com.imaginary.lwp.FindException the clause could not be
     * built
     */
    private String getOrder(Iterator sorts, ArrayList tables)
        throws FindException {
        StringBuffer order = null;

        if( !sorts.hasNext() ) {
            return "";
        }
        do {
            String col = (String)sorts.next();
            int i;

            if( order == null ) {
                order = new StringBuffer();
            }
            else {
                order.append(", ");
            }
            col = mapField(col);
            order.append(col);
            i = col.indexOf(".");
            if( i != -1 ) {
                String tbl = col.substring(0, i);

                if( !tables.contains(tbl) ) {
                    tables.add(tbl);
                }
            }
        } while( sorts.hasNext() );
        return order.toString();
    }

    /**
     * Implemented by subclasses to provide the name of the primary
     * table for storing objects supported by this class.
     * @return the name of the primary table
     */
    protected abstract String getPrimaryTable();

    /**
     * Provides the <CODE>WHERE</CODE> clause to support a find.
     * @param bindings the search bindings from the search criteria object
     * @param a pass by reference thing where any new tables that need
     * to be joined to are added to this list
     * @return a string with the <CODE>WHERE</CODE> clause
     * @throws com.imaginary.lwp.FindException the clause could not be
     * built
     */
    private String getWhere(Iterator bindings, ArrayList tables)
        throws FindException {
        StringBuffer where = null;

        if( !bindings.hasNext() ) {
            return "";
        }
        do {
            SearchBinding bdg = (SearchBinding)bindings.next();
            Object val = bdg.getValue();
            String fld = bdg.getField();

            if( where == null ) {
                where = new StringBuffer();
            }
            else {
                where.append(" " + bdg.getBoolean().toString() + " ");
            }
            if( val instanceof SearchCriteria ) {
                SearchCriteria sc = (SearchCriteria)val;

                where.append("(");
                where.append(getWhere(sc.bindings(), tables));
                where.append(")");
            }
            else {
                int i;
                
                fld = mapField(fld);
                where.append(fld);
                i = fld.indexOf(".");
                if( i != -1 ) {
                    String tbl = fld.substring(0, i);

                    if( !tables.contains(tbl) ) {
                        tables.add(tbl);
                    }
                }
                where.append(" " + bdg.getOperator().toString() + " ?");
            }
        } while( bindings.hasNext() );
        if( where == null ) {
            return "";
        }
        else {
            return where.toString();
        }
    }

    /**
     * Maps a field from the supported object's attributes to a database
     * field.
     * @param fld the Java object.attribute for the field to map
     * @return the database table to map the field to
     * @throws com.imaginary.lwp.FindException the field could not be mapped
     */
    protected abstract String mapField(String fld) throws FindException;
}
