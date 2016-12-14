package com.basingwerk.utilisation;


import java.awt.Color;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

@SuppressWarnings("serial")
public class UsagePlotter extends ApplicationFrame {
	private Boolean useDots;
	private Boolean plotTotalRunning;
	private Boolean plotTotalQueued;
	private Boolean plotClusterCpus;
	private Boolean plotMultiRunning;
	private Boolean plotMultiQueued;

	private File logFile;
	private Second startSecond;
	private Second endSecond;

	public UsagePlotter(final String title, File lf) {
		this(title, lf, new Second(0, 0, 0, 1, 1, 2000), new Second(0, 0, 0, 1, 1, 2100));
	}

	public void windowClosing(WindowEvent e) {
		// System.out.print("WindowListener method called: windowClosing.");
	}

	public UsagePlotter(final String title, File lf, Second ss, Second es) {
		this(title, lf, ss, es, false, true, true, true, true, true);
	}

	public UsagePlotter(final String title, File lf, Second ss, Second es, Boolean ud, Boolean tr, Boolean tq, Boolean cc,
			Boolean mr, Boolean mq) {

		super(title);

		this.useDots = ud;

		this.plotTotalRunning = tr;
		this.plotTotalQueued = tq;
		this.plotClusterCpus = cc;
		this.plotMultiRunning = mr;
		this.plotMultiQueued = mq;

		this.startSecond = ss;
		this.endSecond = es;
		
		int dotSize = 3;

		// Get the file to use
		this.logFile = lf;

		// Set the title
		final String chartTitle = "ARC/Condor Cluster Multicore Usage";

		// Make datasets
		//final XYDataset totalCpus = getFields("Cluster CPUs", "Running", new int[] { 7 }, 1.0);
		final XYDataset totalCpus = getClusterSize("Unislots (avg)");
		
		final XYDataset totalRunning = getFields("Total running", "Running", new int[] { 8 }, 1.0);
		final XYDataset totalQueued = getFields("Total queued", "Queued", new int[] { 7, 8 }, 1.0);
		final XYDataset totalMulticore = getFields("Multicores running", "Running", new int[] { 9 }, 1.0);
		final XYDataset mcQueued = getFields("Multicore queued", "Queued", new int[] { 7, }, 1.0);

		// Make a new chart for that dataset
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle, "Date", "Running unislots", totalCpus, true, true,
				false);

		// Get the plot from that chart
		final XYPlot plot = chart.getXYPlot();

		// Set the date axis along the bottom
		final DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("dd-MMM-yyyy"));

		// Set the second axis (right)
		final NumberAxis axis2 = new NumberAxis("Queued unislots");
		axis2.setAutoRangeIncludesZero(false);
		plot.setRangeAxis(1, axis2);

		AbstractXYItemRenderer xyr = new StandardXYItemRenderer();
		if (useDots) {
			XYDotRenderer xydotrenderer = new XYDotRenderer();
			xydotrenderer.setDotHeight(dotSize);
			xydotrenderer.setDotWidth(dotSize);
			xyr = xydotrenderer;
		}
		
		// ---------------------------------------------------
		// Apply another dataset for total queued jobs
		if (this.plotTotalQueued) {
			plot.setDataset(1, totalQueued);

			xyr.setSeriesPaint(0, Color.black);
			plot.setRenderer(1, xyr);
			plot.mapDatasetToRangeAxis(1, 1);
		}

		// ---------------------------------------------------
		// Apply another dataset
		if (this.plotTotalRunning) {
			plot.setDataset(2, totalRunning);
			xyr = new StandardXYItemRenderer();
			if (useDots) {
				XYDotRenderer xydotrenderer = new XYDotRenderer();
				xydotrenderer.setDotHeight(dotSize);
				xydotrenderer.setDotWidth(dotSize);
				xyr = xydotrenderer;
			}
			xyr.setSeriesPaint(0, Color.cyan);
			plot.setRenderer(2, xyr);
		}

		// //---------------------------------------------------
		// // Apply another dataset for multicore queued jobs
		if (this.plotMultiQueued) {
			plot.setDataset(3, mcQueued);
			xyr = new StandardXYItemRenderer();
			if (useDots) {
				XYDotRenderer xydotrenderer = new XYDotRenderer();
				xydotrenderer.setDotHeight(dotSize);
				xydotrenderer.setDotWidth(dotSize);
				xyr = xydotrenderer;
			}
			xyr.setSeriesPaint(0, Color.magenta);
			plot.setRenderer(3, xyr);
			plot.mapDatasetToRangeAxis(3, 1);
		}
		
		
		// ---------------------------------------------------
		// Apply another dataset
		if (this.plotMultiRunning) {
			plot.setDataset(4, totalMulticore);

			xyr = new StandardXYItemRenderer();
			if (useDots) {
				XYDotRenderer xydotrenderer = new XYDotRenderer();
				xydotrenderer.setDotHeight(dotSize);
				xydotrenderer.setDotWidth(dotSize);
				xyr = xydotrenderer;
			}
			xyr.setSeriesPaint(0, Color.blue);
			plot.setRenderer(4, xyr);
		}


		// ---------------------------------------------------
		// Fit the chart in a panel and show it
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1000, 500));
		setContentPane(chartPanel);
	}

	private XYDataset getFields(String title, String key, int[] js, double multiplier) {
		final TimeSeries s1 = new TimeSeries(title, Second.class);
		Pattern pattern = null;
		if (key.compareTo("Queued") == 0) {
			pattern = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})\\s(\\d{2}):(\\d{2}):(\\d{2}).*Queueing.*\\=\\s*(\\d+).*\\=\\s*(\\d+)");
		}
		else {
			pattern = Pattern
					.compile("(\\d{4})(\\d{2})(\\d{2})\\s(\\d{2}):(\\d{2}):(\\d{2}).*Running.*\\=\\s*(\\d+).*\\=\\s*(\\d+).*\\=\\s*(\\d+).*\\=\\s*(\\d+)");
		}

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(this.logFile));
			String line = null;
			// 20140901 12:08:08 : Running report: TotalCpus = 96, RequestedCPUs = 68, MultiCoreTotal = 3, SingleCoreTotal = 44
			// 20140901 12:08:08 : Queueing report : MultiCoreTotal = 0, SingleCoreTotal = 0

			while ((line = reader.readLine()) != null) {

				try {
					Matcher matcher = pattern.matcher(line);

					if (matcher.find()) {
						Integer hour = new Integer(matcher.group(4));
						Integer min = new Integer(matcher.group(5));
						Integer sec = new Integer(matcher.group(6));
						Integer year = new Integer(matcher.group(1));
						Integer month = new Integer(matcher.group(2));
						Integer day = new Integer(matcher.group(3));

						Second timestamp = new Second(sec, min, hour, day, month, year);

						if ((timestamp.compareTo(this.startSecond) >= 0) & (timestamp.compareTo(this.endSecond) <= 0)) {

							Double value = 0.0;
							for (Integer i : js) {
								value = value + (new Float(matcher.group(i)) * multiplier);
							}
							s1.add(timestamp, value);
						}
					}
				}
				catch (Exception e) {
					System.err.print("Log file contains weird data\n" + line + e.toString());
				}
			}
		}
		catch (Exception e) {
			System.err.print("Log file not found\n");
		}
		finally {
			try {
				reader.close();
			}
			catch (IOException e) {
			}
		}

		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(s1);

		return dataset;
	}
	
	
	private XYDataset getClusterSize(String title ) {
		final TimeSeries s1 = new TimeSeries(title, Second.class);
		Pattern pattern = null;
		pattern = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})\\s(\\d{2}):(\\d{2}):(\\d{2}).*Running.*\\=\\s*(\\d+).*\\=\\s*(\\d+).*\\=\\s*(\\d+).*\\=\\s*(\\d+)");

		BufferedReader reader = null;
    Integer count = 0;
    Double totalValue = 0.0;
    Second firstTime = null;
    Second lastTime = null;
    Second validTimestamp = null;
    
    
		try {
			reader = new BufferedReader(new FileReader(this.logFile));
			String line = null;
			// 20140901 12:08:08 : Running report: TotalCpus = 96, RequestedCPUs = 68, MultiCoreTotal = 3, SingleCoreTotal = 44
			// 20140901 12:08:08 : Queueing report : MultiCoreTotal = 0, SingleCoreTotal = 0

			while ((line = reader.readLine()) != null) {

				try {
					Matcher matcher = pattern.matcher(line);

					if (matcher.find()) {
						Integer hour = new Integer(matcher.group(4));
						Integer min = new Integer(matcher.group(5));
						Integer sec = new Integer(matcher.group(6));
						Integer year = new Integer(matcher.group(1));
						Integer month = new Integer(matcher.group(2));
						Integer day = new Integer(matcher.group(3));
						
				    Second timestamp = new Second(sec, min, hour, day, month, year);

						if ((timestamp.compareTo(this.startSecond) >= 0) & (timestamp.compareTo(this.endSecond) <= 0)) {
							count = count + 1;
							totalValue = totalValue + (new Float(matcher.group(7)));
							validTimestamp = timestamp;
						if (count == 1) {
								firstTime = timestamp;
							}
						}
					}
				}
				catch (Exception e) {
					System.err.print("Log file contains weird data\n" + line + e.toString());
				}
			}
		}
		catch (Exception e) {
			System.err.print("Log file not found\n");
		}
		finally {
			try {
				reader.close();
			}
			catch (IOException e) {
			}
		}
		lastTime = validTimestamp;
		
	  double avg = totalValue / count;
		s1.add(firstTime, avg);
		s1.add(lastTime, avg);

		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(s1);

		return dataset;
	}
}
