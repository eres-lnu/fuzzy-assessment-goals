/**
 * Copyright 2024 EReS research Lab - Linnaeus University
 * Contact: https://lnu.se/en/research/research-groups/engineering-resilient-systems-eres/
 * 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Contributors: 
 * 		Diego Perez
 */
package se.lnu.eres.fuzzy.plot;


import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.impl.LinearPiecewiseFunctionImpl;

class PlotterTest {



	@AfterAll
	static void closeScanner() throws InterruptedException {
		//During the automatic tests, the next line gives a bit of time to look at the chart.
		Thread.sleep(1000);

	}

	@Test
	void TestSimple() throws IOException {
		Plotter p = new Plotter("Simple default");
		p.showExample();
		p.showPlot();
		p.save("simple");

		Assertions.assertNotNull(p);
	}

	@Test
	void TestLeafGoal() {
		Plotter p = new Plotter("Leaf goal paper");
		double[] x = new double[] { 0, 0.74436, 0.894736, 1.0 };
		double[] y = new double[] { 0, 0, 1.0, 0.3 };
		LinearPieceWiseFunction f = new LinearPiecewiseFunctionImpl();
		for (int i = 0; i < x.length; i++) {
			f.addPoint(x[i], y[i]);
		}
		p.addDatasetFromFunction(f);
		p.showPlot();


		Assertions.assertNotNull(p);
	}

	@Test
	void TestLeafGoalDiscontinuous() {
		Plotter p = new Plotter("Discontinuous function with three points");
		double resultXY[][] = new double[][] { { 0.0, 0.68965 }, { 0.5, 0.86206 }, { 0.5, 1.0 }, { 0.5, 0.333333 },
				{ 0.55, 0.0 }, { 1, 0 } };

		LinearPieceWiseFunction f = new LinearPiecewiseFunctionImpl();
		for (int i = 0; i < resultXY.length; i++) {
			f.addPoint(resultXY[i][0], resultXY[i][1]);
		}
		p.addDatasetFromFunction(f);
		p.showPlot();

		

		Assertions.assertNotNull(p);
	}

	@Test
	void TestLeafGoalFullDissatisfaction() {
		Plotter p = new Plotter("Full dissatisfaction");
		double xy[][] = new double[][] { { 0, 1 }, { 0, 0 }, { 1, 0 } };

		LinearPieceWiseFunction f = new LinearPiecewiseFunctionImpl();
		for (int i = 0; i < xy.length; i++) {
			f.addPoint(xy[i][0], xy[i][1]);
		}
		p.addDatasetFromFunction(f);
		p.showPlot();

		

		Assertions.assertNotNull(p);
	}
	
	@Test
	void TestSinglePoint() {
		Plotter p = new Plotter("Single point");
		double xy[][] = new double[][] { { 0.0, 1 }};

		LinearPieceWiseFunction f = new LinearPiecewiseFunctionImpl();
		for (int i = 0; i < xy.length; i++) {
			f.addPoint(xy[i][0], xy[i][1]);
		}
		p.addDatasetFromFunction(f);
		p.showPlot();

		

		Assertions.assertNotNull(p);
	}

	@Test
	void TestTwoPoints() {
		Plotter p = new Plotter("Two points");
		double xy[][] = new double[][] { { 0.0, 1 }, { 0.0, 0 }};

		LinearPieceWiseFunction f = new LinearPiecewiseFunctionImpl();
		for (int i = 0; i < xy.length; i++) {
			f.addPoint(xy[i][0], xy[i][1]);
		}
		p.addDatasetFromFunction(f);
		p.showPlot();

		

		Assertions.assertNotNull(p);
	}
	
	
	@Test
	void TestLeafGoalFullSatisfaction() {
		Plotter p = new Plotter("Full satisfaction");
		double xy[][] = new double[][] { { 0.0, 0 }, { 1.0, 0 }, { 1, 1 } };

		LinearPieceWiseFunction f = new LinearPiecewiseFunctionImpl();
		for (int i = 0; i < xy.length; i++) {
			f.addPoint(xy[i][0], xy[i][1]);
		}
		p.addDatasetFromFunction(f);
		p.showPlot();

		
		

		Assertions.assertNotNull(p);
	}

	@Test
	void TestManySameDiscontinuity() {
		Plotter p = new Plotter("Many discontinuities at a point");
		double xy[][] = new double[][] { { 0.0, 0 }, { 0.5, 0 }, { 0.5, 0.2 }, { 0.5, 0.4 }, { 0.5, 0.7 }, { 0.5, 1 },
				{ 1, 1 } };

		LinearPieceWiseFunction f = new LinearPiecewiseFunctionImpl();
		for (int i = 0; i < xy.length; i++) {
			f.addPoint(xy[i][0], xy[i][1]);
		}
		p.addDatasetFromFunction(f);
		p.showPlot();

		
		Assertions.assertNotNull(p);
	}

}
