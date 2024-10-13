package se.lnu.eres.fuzzy.plot;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
//import org.jfree.ui.ApplicationFrame;
//import org.jfree.ui.RefineryUtilities;

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

public class Plotter {

	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private ChartPanel chartPanel;
	private JFreeChart lineChart;
	private XYSeriesCollection dataset;
	private XYLineAndShapeRenderer renderer;

	public Plotter(String applicationTitle) {
		// super(applicationTitle);
		frame = new JFrame(applicationTitle);

		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		dataset = createDatasetDefault();
		lineChart = ChartFactory.createXYLineChart("The-title", "x-axis-title", "y-axis-title", dataset,
				PlotOrientation.VERTICAL, false, true, false);

		XYPlot plot = lineChart.getXYPlot();
		renderer = new XYLineAndShapeRenderer();
		plot.setRenderer(renderer);
		addDefaultRenderedCharacteristics();

		chartDisplayCharacteristics();

	}

	private void chartDisplayCharacteristics() {
		chartPanel = new ChartPanel(lineChart);
		XYPlot plot = lineChart.getXYPlot();

		plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.WHITE);

		chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));

		// frame.setSize(800, 600);
		frame.setContentPane(chartPanel);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.pack();
		UIUtils.centerFrameOnScreen(frame);

	}

	private Shape circle = new Ellipse2D.Double(-6.0, -6.0, 6.0, 6.0);
	private Shape circlesmall = new Ellipse2D.Double(-1.0, -1.0, 1.0, 1.0);

	public void addDatasetFromFunction(LinearPieceWiseFunction function) {

		dataset = new XYSeriesCollection();
		renderer = new XYLineAndShapeRenderer();

		LinearPieceWiseFunctionDataPoints points = function.getDatapoints();
		XYSeries series = new XYSeries("s1");
		int seriesIdx = 0;
		dataset.addSeries(series);
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesShape(seriesIdx, circlesmall);
		renderer.setSeriesStroke(0, new BasicStroke(4.0f));
		series.add(points.get(0).getLeft(), points.get(0).getRight());
		if (points.size() == 1) {
			renderer.setSeriesShape(seriesIdx, circle);
		} else {// If the first one is a discontinuity, it is the actual value
			if (points.get(1).getLeft().equals(points.get(0).getLeft())) {
				renderer.setSeriesShape(seriesIdx, circle);
			}

			// The rest of points
			for (int i = 1; i < points.size(); i++) {
				// Check if the previous piece continues or if it is a new one
				if (points.get(i).getLeft().equals(points.get(i - 1).getLeft())) {
					// Discontinuity

					// A new series
					series = new XYSeries("s" + (i + 1));
					seriesIdx++;
					dataset.addSeries(series);
					// With smallcircles
					renderer.setSeriesPaint(i, Color.RED);
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

		lineChart = ChartFactory.createXYLineChart("", "", "", dataset, PlotOrientation.VERTICAL, true, true, false);
		lineChart.getXYPlot().setRenderer(renderer);
		chartDisplayCharacteristics();
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

//		Shape squaresmall = new Rectangle2D.Double(-1.0, -1.0, 1.0, 1.0);
//		Shape squarebig = new Rectangle2D.Double(-6.0, -6.0, 6.0, 6.0);
//		renderer.setSeriesShape(0, squaresmall);
//		renderer.setSeriesShape(1, squarebig);
//		renderer.setSeriesShape(2, circle);
//		System.out.println("the shape of the first series is: " + renderer.getSeriesShape(1).toString());

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

//		int width = 640; /* Width of the image */
//		int height = 480; /* Height of the image */

//		File filejpeg = new File("target/" + filename+".jpeg" );
//		System.out.println("Chart bounds are: " + chartPanel.getBounds().toString());
//		ChartUtilities.saveChartAsPNG(filepng, lineChart, chartPanel.getWidth(), chartPanel.getHeight());
//		ChartUtilities.saveChartAsJPEG(filejpeg, lineChart, chartPanel.getWidth(), chartPanel.getHeight());

//		Rectangle rec = chartPanel.getBounds();
//		BufferedImage img = new BufferedImage(rec.width, rec.height, BufferedImage.TYPE_INT_ARGB);
//		Graphics g = img.getGraphics();
//		chartPanel.paint(g);
//		File filepng = new File("target/" + filename + ".png");
//		ImageIO.write(img, "png", filepng);
//		
		try {

			OutputStream out = new FileOutputStream("target/" + filename + ".png");
			ChartUtils.writeChartAsPNG(out, lineChart, chartPanel.getWidth(), chartPanel.getHeight());

		} catch (IOException ex) {
		}

	}

}
