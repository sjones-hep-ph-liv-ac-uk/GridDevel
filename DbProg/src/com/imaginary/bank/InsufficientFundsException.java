package com.imaginary.bank;

public class InsufficientFundsException extends Exception {
    public InsufficientFundsException() {
        super();
    }

    public InsufficientFundsException(String rsn) {
        super(rsn);
    }
}
