package com.imaginary.bank;

import java.io.Serializable;

public class AccountType implements Serializable {
    static public final AccountType CHECKING = new AccountType("CHK");
    static public final AccountType SAVINGS  = new AccountType("SAV");

    private String code = null;
    
    private AccountType(String t) {
        super();
        code = t;
    }

    public boolean equals(Object ob) {
        if( !(ob instanceof AccountType) ) {
            return false;
        }
        else {
            AccountType at = (AccountType)ob;

            return at.code.equals(code);
        }
    }
    
    public String getCode() {
        return code;
    }

    public int hashCode() {
        return code.hashCode();
    }
    
    public String toString() {
        if( code.equals("CHK") ) {
            return "CHECKING";
        }
        else {
            return "SAVINGS";
        }
    }
}
