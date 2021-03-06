package com.basingwerk.utilisation;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.jfree.data.time.Second;
import org.jfree.ui.RefineryUtilities;

@SuppressWarnings("serial")
public class ShowUsage extends JFrame {

	private File dataFile;
	private DateTimePicker startDateTimePicker;
	private DateTimePicker endDateTimePicker;

	public ShowUsage() {
		initUI();
	}

	private void initUI() {
		JButton selectFileButton = new JButton("Select file");

    final JTextField fileField = new JTextField("<selected file>");
    fileField.setMaximumSize(new Dimension(99999,selectFileButton.getMaximumSize().height));
    fileField.setEditable(false);
    
		selectFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int result = chooser.showOpenDialog(null);

				if (result == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					if (file.canRead() != true) {
						JOptionPane.showMessageDialog( null, "File not found.");
					}
					else {
					  dataFile = file;
					  fileField.setText(file.getPath());
					}
				}
				else if (result == JFileChooser.CANCEL_OPTION) {
					JOptionPane.showMessageDialog( null, "Operation cancelled.");
				  dataFile = null;
				}
			}
		});

		JLabel startLabel = new JLabel("Start: ");

		JLabel endLabel = new JLabel("End:");

		
		JLabel useDotsLabel = new JLabel("Use dots");
		final JCheckBox useDotsCheckbox = new JCheckBox();
		
		JLabel ccLabel = new JLabel("Cluster CPUs");
		JLabel trLabel = new JLabel("Total running");
		JLabel tqLabel = new JLabel("Total queued");
		JLabel mrLabel = new JLabel("Multi running ");
		JLabel mqLabel = new JLabel("Multi queued");
		
		final JCheckBox ccCheckbox = new JCheckBox();
		ccCheckbox.setSelected(true);
		ccCheckbox.setEnabled(false);
		final JCheckBox trCheckbox = new JCheckBox();
		trCheckbox.setSelected(true);
		final JCheckBox tqCheckbox = new JCheckBox();
		tqCheckbox.setSelected(true);
		final JCheckBox mrCheckbox = new JCheckBox();
		mrCheckbox.setSelected(true);
		final JCheckBox mqCheckbox = new JCheckBox();
		mqCheckbox.setSelected(true);
		
		JButton plotButton = new JButton("Plot");

		plotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				if (dataFile != null) {

					Date sd = startDateTimePicker.getDate();
					Date ed = endDateTimePicker.getDate();
					Second startSec = new Second(sd);
					Second endSec = new Second(ed);

					final UsagePlotter plotter = new UsagePlotter("Usage", dataFile, startSec, endSec, useDotsCheckbox.isSelected(),							
					trCheckbox.isSelected(),tqCheckbox.isSelected(),ccCheckbox.isSelected(),
					mrCheckbox.isSelected(),mqCheckbox.isSelected());
					
					plotter.pack();
					RefineryUtilities.centerFrameOnScreen(plotter);
					plotter.setVisible(true);
				}
				else {
					JOptionPane.showMessageDialog( null, "Select a datafile first.");
				}
			}
		});

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		startDateTimePicker = new DateTimePicker();
    startDateTimePicker.setFormats(sdf);
		startDateTimePicker.setTimeFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM));
		Date dateNow = new Date();
		Date dateBefore = new Date(dateNow.getTime() - 2 * 24 * 3600 * 1000);
		startDateTimePicker.setDate(dateBefore);

		endDateTimePicker = new DateTimePicker();
    endDateTimePicker.setFormats(sdf);
		endDateTimePicker.setTimeFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM));
		Date endDate = new Date();
		endDateTimePicker.setDate(endDate);

		Container pane = getContentPane();
		GroupLayout layout = new GroupLayout(pane);
		pane.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
    // Horizontal
		Group horz = layout.createSequentialGroup();

		Group column1TopDown = layout.createParallelGroup( GroupLayout.Alignment.LEADING);
		column1TopDown.addComponent(selectFileButton);
		column1TopDown.addComponent(startLabel);
		column1TopDown.addComponent(endLabel);
		column1TopDown.addComponent(useDotsLabel);

		column1TopDown.addComponent(ccLabel);
		column1TopDown.addComponent(trLabel);
		column1TopDown.addComponent(tqLabel);
		column1TopDown.addComponent(mrLabel);
		column1TopDown.addComponent(mqLabel);		
		
		column1TopDown.addComponent(plotButton);
		horz.addGroup(column1TopDown);	
		Group column2TopDown = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		column2TopDown.addComponent(fileField);
		column2TopDown.addComponent(startDateTimePicker);
		column2TopDown.addComponent(endDateTimePicker);
		column2TopDown.addComponent(useDotsCheckbox);
		
		column2TopDown.addComponent(ccCheckbox);
		column2TopDown.addComponent(trCheckbox);
		column2TopDown.addComponent(tqCheckbox);
		column2TopDown.addComponent(mrCheckbox);
		column2TopDown.addComponent(mqCheckbox);
		
		
    // missing
		horz.addGroup(column2TopDown);
		layout.setHorizontalGroup(horz);

    // Vertical		
		Group virt = layout.createSequentialGroup();
		
		Group row1LeftToRight = layout.createParallelGroup( GroupLayout.Alignment.LEADING);
		row1LeftToRight.addComponent(selectFileButton);
		row1LeftToRight.addComponent(fileField);
		virt.addGroup(row1LeftToRight);

		Group row2LeftToRight = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		row2LeftToRight.addComponent(startLabel);
		row2LeftToRight.addComponent(startDateTimePicker);
		virt.addGroup(row2LeftToRight);
		layout.setVerticalGroup(virt);
		
		Group row3LeftToRight = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		row3LeftToRight.addComponent(endLabel);
		row3LeftToRight.addComponent(endDateTimePicker);
		virt.addGroup(row3LeftToRight);
		layout.setVerticalGroup(virt);

		Group row4LeftToRight = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		row4LeftToRight.addComponent(useDotsLabel);
		row4LeftToRight.addComponent(useDotsCheckbox);
		virt.addGroup(row4LeftToRight);
		layout.setVerticalGroup(virt);

		Group row4dLeftToRight = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		row4dLeftToRight.addComponent(ccLabel);
		row4dLeftToRight.addComponent(ccCheckbox);
		virt.addGroup(row4dLeftToRight);
		layout.setVerticalGroup(virt);
		
		Group row4bLeftToRight = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		row4bLeftToRight.addComponent(trLabel);
		row4bLeftToRight.addComponent(trCheckbox);
		virt.addGroup(row4bLeftToRight);
		layout.setVerticalGroup(virt);

		Group row4cLeftToRight = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		row4cLeftToRight.addComponent(tqLabel);
		row4cLeftToRight.addComponent(tqCheckbox);
		virt.addGroup(row4cLeftToRight);
		layout.setVerticalGroup(virt);

		Group row4eLeftToRight = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		row4eLeftToRight.addComponent(mrLabel);
		row4eLeftToRight.addComponent(mrCheckbox);
		virt.addGroup(row4eLeftToRight);
		layout.setVerticalGroup(virt);
		
		Group row4fLeftToRight = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		row4fLeftToRight.addComponent(mqLabel);
		row4fLeftToRight.addComponent(mqCheckbox);
		virt.addGroup(row4fLeftToRight);
		layout.setVerticalGroup(virt);
		
		Group row5LeftToRight = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		row5LeftToRight.addComponent(plotButton);
		// missing
		virt.addGroup(row5LeftToRight);
		layout.setVerticalGroup(virt);

		setTitle("Arc/Condor usage plot");
		setSize(300, 300);
		setLocationRelativeTo(null);

	}

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				ShowUsage su = new ShowUsage();
				su.setVisible(true);
			}
		});
	}
}
