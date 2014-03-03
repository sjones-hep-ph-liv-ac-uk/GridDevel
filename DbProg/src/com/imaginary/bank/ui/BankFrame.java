package com.imaginary.bank.ui;

import com.imaginary.bank.CustomerFacade;
import com.imaginary.swing.WorkerThread;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

public class BankFrame extends JFrame implements TreeSelectionListener {
    private JTextField social, firstName, lastName, custid;
    
    public BankFrame() {
        super("First Imaginary Bank");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        {
            JScrollPane tpane;
            JPanel dpane, p;

            dpane = new JPanel(new BorderLayout());
            {
                GridBagLayout layout = new GridBagLayout();
                GridBagConstraints gbc = new GridBagConstraints();
                JLabel lbl;

                gbc.weightx = 1.0;
                gbc.anchor = GridBagConstraints.NORTHWEST;
                gbc.insets = new Insets(3, 3, 3, 3);
                p = new JPanel(layout);
                lbl = new JLabel(TellerApp.getLabel("LBL_CUST_ID"));
                layout.setConstraints(lbl, gbc);
                p.add(lbl);
                gbc.gridx = 1;
                custid = new JTextField(10);
                custid.setToolTipText(TellerApp.getTooltip("TT_CUST_ID"));
                lbl.setLabelFor(custid);
                layout.setConstraints(custid, gbc);
                p.add(custid);
                
                gbc.gridx = 2;
                lbl = new JLabel(TellerApp.getLabel("LBL_SSN"));
                layout.setConstraints(lbl, gbc);
                p.add(lbl);
                gbc.gridx = 3;
                social = new JTextField(11);
                social.setToolTipText(TellerApp.getTooltip("TT_SSN"));
                lbl.setLabelFor(social);
                layout.setConstraints(social, gbc);
                p.add(social);
                
                gbc.gridy = 1;
                gbc.gridx = 0;
                lbl = new JLabel(TellerApp.getLabel("LBL_FIRST_NAME"));
                layout.setConstraints(lbl, gbc);
                p.add(lbl);
                gbc.gridx = 1;
                firstName = new JTextField(20);
                firstName.setToolTipText(TellerApp.getTooltip("TT_FIRST_NAME"));
                lbl.setLabelFor(firstName);
                layout.setConstraints(firstName, gbc);
                p.add(firstName);
                
                gbc.gridx = 2;
                lbl = new JLabel(TellerApp.getLabel("LBL_LAST_NAME"));
                layout.setConstraints(lbl, gbc);
                p.add(lbl);
                gbc.gridx = 3;
                lastName = new JTextField(10);
                lastName.setToolTipText(TellerApp.getTooltip("TT_LAST_NAME"));
                lbl.setLabelFor(lastName);
                layout.setConstraints(lastName, gbc);
                p.add(lastName);
                dpane.add(p, BorderLayout.NORTH);
            }
            {
                JTree tree = new JTree(new BankModel());

                tree.addTreeSelectionListener(this);
                tpane = new JScrollPane(tree);
            }
            {
                JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                               tpane, dpane);
                
                getContentPane().add(sp);
            }
        }
        pack();
    }

    /**
     * This method listens for selections in the tree and sets the values
     * on the right hand side of the window for any selected customer.
     * This method uses the WorkerThread since any call to get
     * a customer value may trigger a network call if that value
     * is yet cached in the facade.
     */
    public void valueChanged(TreeSelectionEvent evt) {
        TreePath path = evt.getNewLeadSelectionPath();
        final Object ob = path.getLastPathComponent();

        TellerApp.notifyWait();
        if( ob instanceof CustomerNode ) {
            WorkerThread wt = new WorkerThread() {
                    String ssn, fn, ln, cid;

                    public void run() {
                        CustomerFacade cust = ((CustomerNode)ob).getCustomer();

                        try {
                            ssn = cust.getSocialSecurity();
                            fn = cust.getFirstName();
                            ln = cust.getLastName();
                            cid = "" + cust.getObjectID();
                        }
                        catch( RemoteException e ) {
                            ssn = "ERROR";
                            fn = "";
                            ln = "";
                            cid = "";
                        }
                    }

                    public void complete() {
                        try {
                            social.setText(ssn);
                            firstName.setText(fn);
                            lastName.setText(ln);
                            custid.setText(cid);
                        }
                        finally {
                            TellerApp.notifyResume();
                        }
                    }
                };

            WorkerThread.invokeWorker(wt);
        }
        else {
            try {
                social.setText("");
                custid.setText("");
                firstName.setText("");
                lastName.setText("");
            }
            finally {
                TellerApp.notifyResume();
            }
        }
    }
}
