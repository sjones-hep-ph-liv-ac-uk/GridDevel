package com.imaginary.bank.ui;

import com.imaginary.lwp.AuthenticationException;
import com.imaginary.lwp.Identifier;

import com.imaginary.util.LifoStack;

import java.awt.Cursor;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class TellerApp {
    static public final String LABELS   = "com.imaginary.bank.ui.labels";
    static public final String TOOLTIPS = "com.imaginary.bank.ui.tooltips";

    static private LifoStack      cursors  = new LifoStack();
    static private BankFrame      frame    = null;
    static private Locale         locale   = Locale.getDefault();
    static private ResourceBundle labels   = null;
    static private ResourceBundle tooltips = null;

    static public String getLabel(String code) {
        if( labels == null ) {
            return code;
        }
        else {
            try {
                return labels.getString(code);
            }
            catch( MissingResourceException e ) {
                e.printStackTrace();
                return code;
            }
        }
    }
    
    static public String getTooltip(String code) {
        if( tooltips == null ) {
            return code;
        }
        else {
            try {
                return tooltips.getString(code);
            }
            catch( MissingResourceException e ) {
                e.printStackTrace();
                return code;
            }
        }
    }

    static private void loadBundles() {
        try {
            tooltips = ResourceBundle.getBundle(TOOLTIPS, locale);
            labels = ResourceBundle.getBundle(LABELS,locale);
        }
        catch( MissingResourceException e ) {
            e.printStackTrace();
        }
    }
    
    static public void main(String[] args) {
        loadBundles();
        try {
            Identifier.login("oreilly", "oreilly");
        }
        catch( AuthenticationException e ) {
            e.printStackTrace();
            return;
        }
        frame = new BankFrame();
        frame.setVisible(true);
    }

    static public void notifyResume() {
        Cursor c;

        if( cursors.isEmpty() ) {
            c = Cursor.getDefaultCursor();
        }
        else {
            c = (Cursor)cursors.pop();
        }
        frame.setCursor(c);
    }
    
    static public void notifyWait() {
        cursors.push(frame.getCursor());
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    static public void setLocale(Locale loc) {
        locale = loc;
        loadBundles();
    }
}
