package com.imaginary.lwp;

import com.imaginary.lwp.jdbc.JDBCGenerator;

public abstract class SequenceGenerator {
    static private long              currentNode = -1L;
    static private SequenceGenerator generator   = null;
    static private long              nextID      = -1L;
    
    static public synchronized long generateSequence(String seq)
        throws SequenceException {
        if( generator == null ) {
            String cname = System.getProperty(LWPProperties.SEQ_GEN);

            if( cname == null ) {
                generator = new JDBCGenerator();
            }
            else {
                try {
                    generator =
                        (SequenceGenerator)Class.forName(cname).newInstance();
                }
                catch( Exception e ) {
                    throw new SequenceException(e.getMessage());
                }
            }
        }
        return generator.generate(seq);
    }

    static public synchronized long nextObjectID() throws SequenceException {
        if( currentNode == -1L || nextID >= 99999L ) {
            currentNode = generateSequence("node");
            if( currentNode < 1 ) {
                nextID = 1;
            }
            else {
                nextID = 0;
            }
        }
        else {
            nextID++;
        }
        return ((currentNode*100000L) + nextID);
    }

    public SequenceGenerator() {
        super();
    }

    public abstract long generate(String seq) throws SequenceException;
}
    
