package com.imaginary.lwp;

public class SequenceException extends PersistenceException {
    public SequenceException() {
        super();
    }

    public SequenceException(String rsn) {
        super(rsn);
    }
}
