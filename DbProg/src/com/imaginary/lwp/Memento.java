package com.imaginary.lwp;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Captures the classic memento pattern for Java. The memento pattern
 * decouples a business object from its state so that systems like
 * the lightweight persistence engine can manage storage and retrieval
 * of an object's state to and from a data store.
 * <BR>
 * Last modified $Date$
 * @version $Revision$
 * @author <A href="mailto:george.reese@dasein.org">George Reese</A>
 */
public class Memento implements Serializable {
    /**
     * The bitmask meaning an attribute should not be saved.
     */
    static public final int NOSAVE =
        (Modifier.FINAL | Modifier.STATIC | Modifier.TRANSIENT);

    /**
     * Determines whether or not a given field should be saved.
     * A field should not be saved if it is final, static, or transient.
     * @param f the field to be tested
     * @return true if the field should be saved
     */
    static public boolean isSaved(Field f) {
        int mod = f.getModifiers();
        
        if( (mod & Memento.NOSAVE) == 0 ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * The values representing the state of the object behind this
     * memento.
     * @serial
     */
    private HashMap values = new HashMap();

    /**
     * Constructs a new, empty memento.
     */
    public Memento() {
        super();
    }

    /**
     * Constructs a memento representing the state of the specified
     * object.
     * @param ob the object to be represented
     */
    public Memento(Object ob) {
        super();
        {
            Class cls = ob.getClass();
            
            while( !cls.equals(Object.class) ) {
                Field[] attrs = cls.getDeclaredFields();
                HashMap map = new HashMap();
                
                values.put(cls, map);
                for(int i=0; i<attrs.length; i++) {
                    attrs[i].setAccessible(true);
                    if( isSaved(attrs[i]) ) {
                        try {
                            map.put(attrs[i].getName(), attrs[i].get(ob));
                        }
                        catch( IllegalAccessException e ) {
                            throw new SecurityException(e.getMessage());
                        }
                    }
                }
                cls = cls.getSuperclass();
            }
        }
    }

    /**
     * Provides the value for the attribute of the specified class.
     * @param cls the class in which the attribute is declared
     * @param attr the name of the attribute
     * @return the value of the attribute
     */
    public Object get(Class cls, String attr) {
        HashMap map;

        if( !values.containsKey(cls) ) {
            return null;
        }
        map = (HashMap)values.get(cls);
        return map.get(attr);
    }

    /**
     * Maps the values currently in the memento to the object
     * in question.
     * @param ob the object who should be assigned values from the memento
     * @throws java.lang.NoSuchFieldException the object in question does
     * not have a field for one of the memento values
     */
    public void map(Object ob) throws NoSuchFieldException {
        Iterator keys = values.keySet().iterator();

        while( keys.hasNext() ) {
            Class cls = (Class)keys.next();
            HashMap vals = (HashMap)values.get(cls);
            Iterator attrs = vals.keySet().iterator();

            while( attrs.hasNext() ) {
                String attr = (String)attrs.next();
                Object val = vals.get(attr);
                Field f = cls.getDeclaredField(attr);

                f.setAccessible(true);
                try {
                    f.set(ob, val);
                }
                catch( IllegalAccessException e ) {
                    throw new SecurityException(e.getMessage());
                }
            }
        }
    }

    /**
     * Places the specified value into the memento based on the field's
     * declaring class and name.
     * @param cls the class in which the field is declared
     * @param attr the name of the attribute
     * @param val the value being stored
     */
    public void put(Class cls, String attr, Object val) {
        HashMap map;
        
        if( values.containsKey(cls) ) {
            map = (HashMap)values.get(cls);
        }
        else {
            map = new HashMap();
            values.put(cls, map);
        }
        map.put(attr, val);
    }
}
