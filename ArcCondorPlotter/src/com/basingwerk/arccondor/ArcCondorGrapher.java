package com.basingwerk.arccondor;


import java.awt.*;
import java.awt.event.*;
import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JPanel; 
import javax.swing.JFrame;
import javax.swing.Box;
import javax.swing.BoxLayout;

public class ArcCondorGrapher extends JPanel {
    public ArcCondorGrapher() {
        super(new GridLayout(1,0));

        Border paneEdge = BorderFactory.createEmptyBorder(0,10,10,10);

        JPanel simpleBorders = new JPanel();
        simpleBorders.setBorder(paneEdge);
        simpleBorders.setLayout(new BoxLayout(simpleBorders,
                                              BoxLayout.Y_AXIS));
//        JPanel matteBorders = new JPanel();
//        matteBorders.setBorder(paneEdge);
//        matteBorders.setLayout(new BoxLayout(matteBorders,
//                                              BoxLayout.Y_AXIS));

//        ImageIcon icon = createImageIcon("images/wavy.gif",
//                                         "wavy-line border icon"); //20x22
//        Border border = BorderFactory.createMatteBorder(-1, -1, -1, -1, icon);
//        if (icon != null) {
//            addCompForBorder(border,
//                             "matte border (-1,-1,-1,-1,icon)",
//                             matteBorders);
//        } else {
//            addCompForBorder(border,
//                             "matte border (-1,-1,-1,-1,<null-icon>)",
//                             matteBorders);
//        }
//        border = BorderFactory.createMatteBorder(1, 5, 1, 1, Color.red);
//        addCompForBorder(border,
//                         "matte border (1,5,1,1,Color.red)",
//                         matteBorders);
//
//        border = BorderFactory.createMatteBorder(0, 20, 0, 0, icon);
//        if (icon != null) {
//            addCompForBorder(border,
//                             "matte border (0,20,0,0,icon)",
//                             matteBorders);
//        } else {
//            addCompForBorder(border,
//                             "matte border (0,20,0,0,<null-icon>)",
//                             matteBorders);
//        }

        //Third pane: titled borders
        JPanel titledBorders = new JPanel();
        titledBorders.setBorder(paneEdge);
        titledBorders.setLayout(new BoxLayout(titledBorders,
                                              BoxLayout.Y_AXIS));
        TitledBorder titled;

        titled = BorderFactory.createTitledBorder("title");
        addCompForBorder(titled,
                         "default titled border"
                         + " (default just., default pos.)",
                         titledBorders);

        addCompForTitledBorder(titled,
                               "titled line border"
                                   + " (centered, default pos.)",
                               TitledBorder.CENTER,
                               TitledBorder.DEFAULT_POSITION,
                               titledBorders);

        addCompForTitledBorder(titled,
                               "titled lowered etched border"
                                   + " (right just., default pos.)",
                               TitledBorder.RIGHT,
                               TitledBorder.DEFAULT_POSITION,
                               titledBorders);

        addCompForTitledBorder(titled,
                               "titled lowered bevel border"
                                   + " (default just., above top)",
                               TitledBorder.DEFAULT_JUSTIFICATION,
                               TitledBorder.ABOVE_TOP,
                               titledBorders);

        addCompForTitledBorder(titled, "titled empty border"
                               + " (default just., bottom)",
                               TitledBorder.DEFAULT_JUSTIFICATION,
                               TitledBorder.BOTTOM,
                               titledBorders);

        JPanel compoundBorders = new JPanel();
        compoundBorders.setBorder(paneEdge);
        compoundBorders.setLayout(new BoxLayout(compoundBorders,
                                              BoxLayout.Y_AXIS));
        Border redline = BorderFactory.createLineBorder(Color.red);

        Border compound;
        addCompForBorder(titled, 
                         "titled compound border"
                         + " (centered, below bottom)",
                         compoundBorders);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Usage", null, simpleBorders, null);
        tabbedPane.addTab("Null", null, simpleBorders, null);
        tabbedPane.setSelectedIndex(0);

        add(tabbedPane);
    }

    void addCompForTitledBorder(TitledBorder border,
                                String description,
                                int justification,
                                int position,
                                Container container) {
        border.setTitleJustification(justification);
        border.setTitlePosition(position);
        addCompForBorder(border, description,
                         container);
    }

    void addCompForBorder(Border border,
                          String description,
                          Container container) {
        JPanel comp = new JPanel(new GridLayout(1, 1), false);
        JLabel label = new JLabel(description, JLabel.CENTER);
        comp.add(label);
        comp.setBorder(border);

        container.add(Box.createRigidArea(new Dimension(0, 10)));
        container.add(comp);
    }


    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path,
                                               String description) {
        java.net.URL imgURL = ArcCondorGrapher.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the 
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("BorderDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        ArcCondorGrapher newContentPane = new ArcCondorGrapher();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}