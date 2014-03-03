package com.imaginary.lwp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class SearchCriteria implements Serializable {
    static final long serialVersionUID = 2581791631479120186L;

    /**
     * The bindings of searchable attributes and their values.
     * @serial
     */
    private ArrayList bindings  = new ArrayList();
    /**
     * Any attributes that should be preloaded with the query if the
     * data store does not automatically pull out all attributes.
     * @serial
     */
    private ArrayList preloads  = new ArrayList();
    /**
     * The fields by which the results should be sorted.
     * @serial
     */
    private ArrayList sorts     = new ArrayList();
    
    public SearchCriteria() {
        super();
    }

    public SearchCriteria(String[] pre) {
        super();
        for(int i=0; i<pre.length; i++) {
            preloads.add(pre[i]);
        }
    }
    
    public SearchCriteria(Iterator pre) {
        super();
        while( pre.hasNext() ) {
            preloads.add(pre.next());
        }
    }
    
    public void addBinding(SearchBinding sb) {
        bindings.add(sb);
    }

    public void addBinding(SearchCriteria sc) {
        bindings.add(new SearchBinding(sc));
    }

    public void addBinding(String fld, Object val) {
        bindings.add(new SearchBinding(fld, val));
    }

    public void addBinding(SearchBoolean sb, String fld,
                           SearchOperator so, Object val) {
        bindings.add(new SearchBinding(sb, fld, so, val));
    }

    public void addSort(String attr) {
        sorts.add(attr);
    }

    public void addSorts(String[] attrs) {
        for(int i=0; i<attrs.length; i++) {
            sorts.add(attrs[i]);
        }
    }

    public void addSorts(Iterator it) {
        while( it.hasNext() ) {
            sorts.add(it.next());
        }
    }
    
    public Iterator bindings() {
        return bindings.iterator();
    }

    public Iterator preloads() {
        return preloads.iterator();
    }

    public Iterator sorts() {
        return sorts.iterator();
    }
}
