package se.lnu.eres.fuzzy.plot;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;

import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.impl.LinearPieceWiseFunctionDataPoints;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Plotter {
	/*
	 * Following tutorials at: -
	 * https://www.codejava.net/java-se/graphics/using-jfreechart-to-draw-xy-line-
	 * chart-with-xydataset Except for the deprecated classes ChartUtils, UIUtils -
	 * https://www.baeldung.com/jfreechart-visualize-data
	 */

	private JFrame frame;
	private ChartPanel chartPanel;
	private JFreeChart lineChart;
	private XYSeriesCollection dataset;
	private XYLineAndShapeRenderer renderer;

	public Plotter(String applicationTitle) {
		frame = new JFrame(applicationTitle);
		// in case of adding several plots to the frame
		frame.setLayout(new FlowLayout());
	}

	public void showExample() {
		dataset = createDatasetDefault();
		lineChart = ChartFactory.createXYLineChart("The-title", "x-axis-title", "y-axis-title", dataset,
				PlotOrientation.VERTICAL, false, true, false);
		XYPlot plot = lineChart.getXYPlot();
		renderer = new XYLineAndShapeRenderer();
		plot.setRenderer(renderer);
		addDefaultRenderedCharacteristics();

		chartDisplayCharacteristics();
	}

	private static final int SIZEX = 300;
	private static final int SIZEY = 150;
	private static final boolean ALL_PLOTS_IN_CENTER = false;

	private static int xloc = 10;
	private static int yloc = 10;

	private void chartDisplayCharacteristics() {
		chartPanel = new ChartPanel(lineChart);
		XYPlot plot = lineChart.getXYPlot();

		plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.WHITE);

		chartPanel.setPreferredSize(new java.awt.Dimension(SIZEX, SIZEY));


		frame.getContentPane().add(chartPanel);

		if (ALL_PLOTS_IN_CENTER) {
			UIUtils.centerFrameOnScreen(frame);
		} else {
			frame.setLocation(xloc, yloc);
			xloc += SIZEX+20;
			if (xloc > 2000) {
				xloc = 10;
				yloc += SIZEY+60;
			}
		}


		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();

	}

	private Shape circle = new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0);
	private Shape circlesmall = new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0);

	private Color currentColor = Color.RED;

	public void addObservationValue(LinearPieceWiseFunction function) {
		addDatasetFromFunction(function, true, false, frame.getTitle() + " goal&obs.");
	}

	public void addRequirementValue(LinearPieceWiseFunction function) {
		currentColor = Color.BLUE;
		addDatasetFromFunction(function, false, true, frame.getTitle() + " goal&obs.");
		currentColor = Color.RED;
	}

	public void addDatasetFromFunction(LinearPieceWiseFunction function) {
		addDatasetFromFunction(function, true, true, frame.getTitle() + " satisf.");
	}

	private int seriesIdx = -1;

	private void addDatasetFromFunction(LinearPieceWiseFunction function, boolean createNewChart, boolean display,
			String name) {

		if (createNewChart) {
			dataset = new XYSeriesCollection();
			renderer = new XYLineAndShapeRenderer();
			lineChart = ChartFactory.createXYLineChart(name, "", "", dataset, PlotOrientation.VERTICAL, false, true,
					false);
			seriesIdx = -1;
		}
		seriesIdx++;

		LinearPieceWiseFunctionDataPoints points = function.getDatapoints();
		XYSeries series = new XYSeries("s" + (seriesIdx + 1));

		dataset.addSeries(series);
		renderer.setSeriesPaint(seriesIdx, currentColor);
		renderer.setSeriesShape(seriesIdx, circlesmall);
		renderer.setSeriesStroke(seriesIdx, new BasicStroke(4.0f));
		series.add(points.get(0).getLeft(), points.get(0).getRight());
		if (points.size() == 1) {
			renderer.setSeriesShape(seriesIdx, circle);
		} else {
			if (points.get(1).getLeft().equals(points.get(0).getLeft())) {
				// If the first one is a discontinuity, it is the actual value
				renderer.setSeriesShape(seriesIdx, circle);
				// And move the x-axis a bit to the left to graphically show better the value
				lineChart.getXYPlot().getDomainAxis().setRange(points.get(0).getLeft() - 0.025,
						points.getLast().getLeft() + 0.025);

			}

			// The rest of points
			for (int i = 1; i < points.size(); i++) {
				// Check if the previous piece continues or if it is a new one
				if (points.get(i).getLeft().equals(points.get(i - 1).getLeft())) {
					// Discontinuity

					// A new series
					seriesIdx++;
					series = new XYSeries("s" + (seriesIdx + 1));

					dataset.addSeries(series);
					// With smallcircles
					renderer.setSeriesPaint(i, currentColor);
					renderer.setSeriesShape(seriesIdx, circlesmall);
					renderer.setSeriesStroke(i, new BasicStroke(4.0f));
					// Unless next one exists and is also a discontinutiy, then big circle
					if (i + 1 < points.size() && points.get(i).getLeft().equals(points.get(i + 1).getLeft())) {
						renderer.setSeriesShape(seriesIdx, circle);
					}
					// If this is the last point of the function and it created a discontinuity,
					// also big circle
					if (i == points.size() - 1) {
						renderer.setSeriesShape(seriesIdx, circle);
					}

				}

				series.add(points.get(i).getLeft(), points.get(i).getRight());

			}
		}

		lineChart.getXYPlot().setRenderer(renderer);
		if (display) {
			chartDisplayCharacteristics();
		}
	}

	private void addDefaultRenderedCharacteristics() {
		// sets paint color for each series
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesPaint(1, Color.RED);
		renderer.setSeriesPaint(2, Color.RED);

		// sets thickness for series (using strokes)
		renderer.setSeriesStroke(0, new BasicStroke(3.0f));
		renderer.setSeriesStroke(1, new BasicStroke(3.0f));
		renderer.setSeriesStroke(2, new BasicStroke(3.0f));

		// sets shapes for series
		Shape squaresmall = new Rectangle2D.Double(-1.0, -1.0, 1.0, 1.0);
		Shape squarebig = new Rectangle2D.Double(-6.0, -6.0, 6.0, 6.0);
		renderer.setSeriesShape(0, squaresmall);
		renderer.setSeriesShape(1, squarebig);
		renderer.setSeriesShape(2, circle);

	}

	public void showPlot() {
		frame.setVisible(true);

	}

	//

	/**
	 * @param proportions is the array with the proportion of healthy nodes in each
	 *                    step
	 * @return a dataset with a line for the current percentage of healthy and
	 *         another line with the average up to the moment
	 */
	private XYSeriesCollection createDatasetDefault() {
		// DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		XYSeriesCollection datasetSeries = new XYSeriesCollection();

		XYSeries series1 = new XYSeries("s1");
		series1.add(1, 5.0);
		series1.add(5.5, 7.0);
		series1.add(10.0, 11.0);

		XYSeries s2 = new XYSeries("s2");
		s2.add(10.0, 6.0);
		s2.add(12.0, 5.0);
		s2.add(14.0, 5.0);

		XYSeries sp = new XYSeries("sp");
		sp.add(10.0, 9.0);

		datasetSeries.addSeries(series1);
		datasetSeries.addSeries(s2);
		datasetSeries.addSeries(sp);

		return datasetSeries;
	}

	public void save(String filename) throws IOException {

		
		try {

			String filepath = "target/images/" + filename + ".png";
			Files.deleteIfExists(Paths.get(filepath).toAbsolutePath());

			OutputStream out = new FileOutputStream(filepath);
			ChartUtils.writeChartAsPNG(out, lineChart, chartPanel.getWidth(), chartPanel.getHeight());

		} catch (IOException ex) {
		}

	}

}
