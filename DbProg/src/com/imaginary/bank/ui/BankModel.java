package com.imaginary.bank.ui;

import javax.swing.tree.DefaultTreeModel;

public class BankModel extends DefaultTreeModel {
    public BankModel() {
        super(new RootNode());
    }
}
