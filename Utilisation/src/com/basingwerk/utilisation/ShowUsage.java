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
import java.util.Locale;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jfree.data.time.Second;
import org.jfree.ui.RefineryUtilities;

public class ShowUsage extends JFrame {

	private File theLogFile;
	private DateTimePicker startDateTimePicker;
	private DateTimePicker endDateTimePicker;

	public ShowUsage() {
		initUI();
	}

	private void initUI() {
		JButton selectFileButton = new JButton("Select file");

    final JTextField fileField = new JTextField("<selected file>");
    fileField.setMaximumSize(new Dimension(99999,selectFileButton.getMaximumSize().height));
    
		selectFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int result = chooser.showOpenDialog(null);

				if (result == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					theLogFile = file;
					fileField.setText(file.getPath());
				}
				else if (result == JFileChooser.CANCEL_OPTION) {
					fileField.setText("Operation cancelled");
				}
			}
		});

		JLabel startLabel = new JLabel("Start: ");

		JLabel endLabel = new JLabel("End:");

		JButton plotButton = new JButton("Plot");

		plotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				if (theLogFile != null) {

					Date sd = startDateTimePicker.getDate();
					Date ed = endDateTimePicker.getDate();
					Second startSec = new Second(sd);
					Second endSec = new Second(ed);

					final UsagePlotter demo = new UsagePlotter("Usage", theLogFile, startSec, endSec);
					demo.pack();
					RefineryUtilities.centerFrameOnScreen(demo);
					demo.setVisible(true);
					demo.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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

		Group pgh1 = layout.createParallelGroup( GroupLayout.Alignment.LEADING);
		pgh1.addComponent(selectFileButton);
		pgh1.addComponent(startLabel);
		pgh1.addComponent(endLabel);
		pgh1.addComponent(plotButton);
		horz.addGroup(pgh1);	
		Group pgh2 = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		pgh2.addComponent(fileField);
		pgh2.addComponent(startDateTimePicker);
		pgh2.addComponent(endDateTimePicker);
    // missing
		horz.addGroup(pgh2);
		layout.setHorizontalGroup(horz);

    // Vertical		
		Group virt = layout.createSequentialGroup();
		
		Group pgv1 = layout.createParallelGroup( GroupLayout.Alignment.LEADING);
		pgv1.addComponent(selectFileButton);
		pgv1.addComponent(fileField);
		virt.addGroup(pgv1);
		Group pgv2 = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		pgv2.addComponent(startLabel);
		pgv2.addComponent(startDateTimePicker);
		virt.addGroup(pgv2);
		layout.setVerticalGroup(virt);
		Group pgv3 = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		pgv3.addComponent(endLabel);
		pgv3.addComponent(endDateTimePicker);
		virt.addGroup(pgv3);
		layout.setVerticalGroup(virt);
		Group pgv4 = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
		pgv4.addComponent(plotButton);
		// missing
		virt.addGroup(pgv4);
		layout.setVerticalGroup(virt);

		setTitle("Arc/Condor usage plot");
		setSize(300, 200);
		setLocationRelativeTo(null);

	}

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				ShowUsage su = new ShowUsage();
//				su.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				su.setVisible(true);
			}
		});
	}
}
