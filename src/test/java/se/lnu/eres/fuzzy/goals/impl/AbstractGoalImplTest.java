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
package se.lnu.eres.fuzzy.goals.impl;



import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.google.common.math.DoubleMath;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.FuzzyNumber;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.impl.FunctionPiecewiseImpl;
import se.lnu.eres.fuzzy.functions.impl.FuzzyNumberImpl;

import se.lnu.eres.fuzzy.goals.LeafGoal;
import se.lnu.eres.fuzzy.goals.LeafGoalType;

abstract class AbstractGoalImplTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	protected LeafGoal createLeafGoalFuelConsumption() {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(1.0, 0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new FunctionPiecewiseImpl();
		// The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in
		// f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.1, 0.0);
		observationFunction.addPoint(0.15, 1.0);
		observationFunction.addPoint(0.2, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		return goal;
	}

	protected LeafGoal createLeafGoalRideSatisfaction() {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new FunctionPiecewiseImpl();
		// The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in
		// f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.5, 0.0);
		observationFunction.addPoint(0.7, 1.0);
		observationFunction.addPoint(0.9, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		return goal;

	}


	protected LeafGoal createLeafDiscontinuousResult(double startInc, double peakAt, double finishDec) {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.5, 1.0);
		function.addPoint(1.0, 0.5);
		function.addPoint(1.5, 0.5);
		function.addPoint(2.0, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new FunctionPiecewiseImpl();
		// The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in
		// f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(startInc, 0.0);
		observationFunction.addPoint(peakAt, 1.0);
		observationFunction.addPoint(finishDec, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		return goal;

	}

	protected LeafGoal createLeafGoalFuelConsumptionTriangularGivenTopValueAndWidth(double topValueAt, double width) {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(1.0, 0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new FunctionPiecewiseImpl();
		// The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in
		// f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(topValueAt - (width / 2.0), 0.0);
		observationFunction.addPoint(topValueAt, 1.0);
		observationFunction.addPoint(topValueAt + (width / 2.0), 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		return goal;
	}
	
	protected void checkFuzzyEquals(double[][] resultXY, FuzzyBoolean result) {
		Assertions.assertEquals(resultXY.length, result.getFunction().getDatapoints().size());
		for (int i = 0; i < resultXY.length; i++) {
			Assertions
					.assertTrue(
							DoubleMath.fuzzyEquals(resultXY[i][0], result.getFunction().getDatapoints().get(i).getLeft(),
									LinearPieceWiseFunction.TOLERANCE),
							"Unexpected result for X value at position " + i);

			Assertions
					.assertTrue(
							DoubleMath.fuzzyEquals(resultXY[i][1], result.getFunction().getDatapoints().get(i).getRight(),
									LinearPieceWiseFunction.TOLERANCE),
							"Unexpected result for Y value at position " + i);

		}
		
	}

}
