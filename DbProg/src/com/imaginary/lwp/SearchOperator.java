package com.imaginary.lwp;

import java.io.Serializable;

public class SearchOperator implements Serializable {
    static final long serialVersionUID = 5959255794938219548L;

    static public SearchOperator EQUAL         = new SearchOperator(1);
    static public SearchOperator LIKE          = new SearchOperator(2);
    static public SearchOperator NOT_EQUAL     = new SearchOperator(3);
    static public SearchOperator LESS_THAN     = new SearchOperator(4);
    static public SearchOperator LESS_EQUAL    = new SearchOperator(5);
    static public SearchOperator GREATER_THAN  = new SearchOperator(6);
    static public SearchOperator GREATER_EQUAL = new SearchOperator(7);

    /**
     * An internal flag describing which operator this is.
     * @serial
     */
    private int operator = 0;
    
    public SearchOperator() {
        super();
    }

    private SearchOperator(int oper) {
        super();
        operator = oper;
    }

    public boolean equals(Object ob) {
        if( ob instanceof SearchOperator ) {
            SearchOperator op = (SearchOperator)ob;

            return (op.operator == operator);
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return operator;
    }

    public String toString() {
        switch( operator ) {
        case 1:
            {
                return "=";
            }
        case 2:
            {
                return "LIKE";
            }
        case 3:
            {
                return "<>";
            }
        case 4:
            {
                return "<";
            }
        case 5:
            {
                return "<=";
            }
        case 6:
            {
                return ">";
            }
        case 7:
            {
                return ">=";
            }
        default:
            {
                return "UNKNOWN";
            }
        }
    }
}
