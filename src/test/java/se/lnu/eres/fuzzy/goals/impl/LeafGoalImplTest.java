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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.math.DoubleMath;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.FuzzyNumber;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.functions.impl.LinearPiecewiseFunctionImpl;
import se.lnu.eres.fuzzy.functions.impl.FuzzyNumberImpl;
import se.lnu.eres.fuzzy.goals.LeafGoal;
import se.lnu.eres.fuzzy.goals.LeafGoalType;

class LeafGoalImplTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testAssessSatisfaction() throws FunctionOperationException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		// The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in
		// f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.5, 0.0);
		observationFunction.addPoint(0.7, 1.0);
		observationFunction.addPoint(0.9, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		FuzzyBoolean result = goal.assessSatisfaction();
		Assertions.assertEquals(4, result.getFunction().getDatapoints().size());
		for (ImmutablePair<Double, Double> point : result.getFunction().getDatapoints()) {
			Assertions.assertTrue(Double.isFinite(point.getLeft()),
					"There is an X point in the result whose value is not finite, its value is: " + point.getLeft());
			Assertions.assertTrue(Double.isFinite(point.getRight()),
					"There is an Y point in the result whose value is not finite, its value is: " + point.getRight());
		}

		System.out.println("Result of goal satisfaction is:" + result.toString());

		double[] x = new double[]{0, 0.74436, 0.894736, 1.0};
		double[] y = new double[]{0, 0, 1.0, 0.3};
		for(int i=0; i<x.length;i++) {
			Assertions.assertTrue(DoubleMath.fuzzyEquals(x[i], result.getFunction().getDatapoints().get(i).getLeft(), LinearPieceWiseFunction.TOLERANCE));
			Assertions.assertTrue(DoubleMath.fuzzyEquals(y[i], result.getFunction().getDatapoints().get(i).getRight(), LinearPieceWiseFunction.TOLERANCE));
		}
		
	}

	@Test
	void testAssessFullSatisfaction() throws FunctionOperationException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		// The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in
		// f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.1, 0.0);
		observationFunction.addPoint(0.2, 1.0);
		observationFunction.addPoint(0.3, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		FuzzyBoolean result = goal.assessSatisfaction();
		Assertions.assertEquals(3, result.getFunction().getDatapoints().size());
		for (ImmutablePair<Double, Double> point : result.getFunction().getDatapoints()) {
			Assertions.assertTrue(Double.isFinite(point.getLeft()),
					"There is an X point in the result whose value is not finite, its value is: " + point.getLeft());
			Assertions.assertTrue(Double.isFinite(point.getRight()),
					"There is an Y point in the result whose value is not finite, its value is: " + point.getRight());
		}
		Assertions.assertEquals(result.getFunction().getDatapoints().get(0), new ImmutablePair<Double,Double>(0.0,0.0));
		Assertions.assertEquals(result.getFunction().getDatapoints().get(1), new ImmutablePair<Double,Double>(1.0,0.0));
		Assertions.assertEquals(result.getFunction().getDatapoints().get(2), new ImmutablePair<Double,Double>(1.0,1.0));

		System.out.println("Result of goal satisfaction is:" + result.toString());

	}

	@Test
	void testAssessFullUnSatisfaction() throws FunctionOperationException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		// The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in
		// f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(2, 0.0);
		observationFunction.addPoint(3, 1.0);
		observationFunction.addPoint(4, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		FuzzyBoolean result = goal.assessSatisfaction();
		Assertions.assertEquals(3, result.getFunction().getDatapoints().size(), " the result contains: " + result.getFunction().toString());
		for (ImmutablePair<Double, Double> point : result.getFunction().getDatapoints()) {
			Assertions.assertTrue(Double.isFinite(point.getLeft()),
					"There is an X point in the result whose value is not finite, its value is: " + point.getLeft());
			Assertions.assertTrue(Double.isFinite(point.getRight()),
					"There is an Y point in the result whose value is not finite, its value is: " + point.getRight());
		}

		System.out.println("Result of goal satisfaction is:" + result.toString());
		Assertions.assertEquals(result.getFunction().getDatapoints().get(0), new ImmutablePair<Double,Double>(0.0,1.0));
		Assertions.assertEquals(result.getFunction().getDatapoints().get(1), new ImmutablePair<Double,Double>(0.0,0.0));
		Assertions.assertEquals(result.getFunction().getDatapoints().get(2), new ImmutablePair<Double,Double>(1.0,0.0));

	}

	@Test
	void testAssessFullUncertainSatisfaction() throws FunctionOperationException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		// The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in
		// f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.1, 0.0);
		observationFunction.addPoint(0.2, 1.0);
		observationFunction.addPoint(2.0, 1.0);
		observationFunction.addPoint(3.0, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		FuzzyBoolean result = goal.assessSatisfaction();
		Assertions.assertEquals(2, result.getFunction().getDatapoints().size());
		for (ImmutablePair<Double, Double> point : result.getFunction().getDatapoints()) {
			Assertions.assertTrue(Double.isFinite(point.getLeft()),
					"There is an X point in the result whose value is not finite, its value is: " + point.getLeft());
			Assertions.assertTrue(Double.isFinite(point.getRight()),
					"There is an Y point in the result whose value is not finite, its value is: " + point.getRight());
		}

		System.out.println("Result of goal satisfaction is:" + result.toString());


	}
	
	
	@Test
	void testAssessPointDiscontinuityInMiddleSmallIncrementObservation() throws FunctionOperationException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.5, 1.0);
		function.addPoint(1.0, 0.5);
		function.addPoint(1.5, 0.5);
		function.addPoint(2.0, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		// The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in
		// f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.6, 0.0);
		observationFunction.addPoint(1.2, 1.0);
		observationFunction.addPoint(5.0, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		FuzzyBoolean result = goal.assessSatisfaction();
		Assertions.assertEquals(6, result.getFunction().getDatapoints().size(), " the result contains: " + result.getFunction().toString());
		for (ImmutablePair<Double, Double> point : result.getFunction().getDatapoints()) {
			Assertions.assertTrue(Double.isFinite(point.getLeft()),
					"There is an X point in the result whose value is not finite, its value is: " + point.getLeft());
			Assertions.assertTrue(Double.isFinite(point.getRight()),
					"There is an Y point in the result whose value is not finite, its value is: " + point.getRight());
		}

		System.out.println("Result of goal satisfaction is:" + result.toString());


	}

	
	@Test
	void testAssessPointDiscontinuityInMiddleFastIncrementObservation() throws FunctionOperationException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.5, 1.0);
		function.addPoint(1.0, 0.5);
		function.addPoint(1.5, 0.5);
		function.addPoint(2.0, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		// The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in
		// f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.9, 0.0);
		observationFunction.addPoint(1.1, 1.0);
		observationFunction.addPoint(20.0, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		FuzzyBoolean result = goal.assessSatisfaction();
		Assertions.assertEquals(6, result.getFunction().getDatapoints().size(), " the result contains: " + result.getFunction().toString());
		for (ImmutablePair<Double, Double> point : result.getFunction().getDatapoints()) {
			Assertions.assertTrue(Double.isFinite(point.getLeft()),
					"There is an X point in the result whose value is not finite, its value is: " + point.getLeft());
			Assertions.assertTrue(Double.isFinite(point.getRight()),
					"There is an Y point in the result whose value is not finite, its value is: " + point.getRight());
		}

		System.out.println("Result of goal satisfaction is:" + result.toString());


	}
	
}
