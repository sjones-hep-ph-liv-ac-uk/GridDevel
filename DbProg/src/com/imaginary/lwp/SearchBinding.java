package com.imaginary.lwp;

import java.io.Serializable;

public class SearchBinding implements Serializable {
    static final long serialVersionUID = -5110219124763741587L;

    /**
     * The name of the field being searched on.
     * @serial
     */
    private String         field         = null;
    /**
     * The boolean for the search, i.e. AND or OR.
     * @serial
     */
    private SearchBoolean  searchBoolean = SearchBoolean.AND;
    /**
     * The operator joining the field and the value in question.
     * @serial
     */
    private SearchOperator operator      = SearchOperator.EQUAL;
    /**
     * The value to which the field should be related for this query.
     * @serial
     */
    private Object         value         = null;
    
    public SearchBinding(SearchCriteria crit) {
        super();
        value = crit;
    }

    public SearchBinding(String fld, Object val) {
        super();
        field = fld;
        value = val;
    }

    public SearchBinding(SearchBoolean sb, String fld, SearchOperator oper,
                         Object val) {
        this(fld, val);
        searchBoolean = sb;
        operator = oper;
    }
    
    public SearchBoolean getBoolean() {
        return searchBoolean;
    }
    
    public String getField() {
        return field;
    }

    public SearchOperator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }
}
