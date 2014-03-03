package com.imaginary.lwp;

import java.io.Serializable;

public class SearchBoolean implements Serializable {
    static public       SearchBoolean AND              = new SearchBoolean(1);
    static public       SearchBoolean OR               = new SearchBoolean(2);
    static        final long          serialVersionUID = 7487212559751152791L;

    /**
     * An internal flag describing how this binding relates to the
     * previous binding in a search criteria.
     * @serial
     */
    private int searchBoolean = 0;
    
    public SearchBoolean() {
        super();
    }

    private SearchBoolean(int sb) {
        super();
        searchBoolean = sb;
    }

    public boolean equals(Object ob) {
        if( ob instanceof SearchBoolean ) {
            SearchBoolean op = (SearchBoolean)ob;

            return (op.searchBoolean == searchBoolean);
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return searchBoolean;
    }

    public String toString() {
        switch( searchBoolean ) {
        case 1:
            {
                return "AND";
            }
        case 2:
            {
                return "OR";
            }
        default:
            {
                return "UNKNOWN";
            }
        }
    }
}
