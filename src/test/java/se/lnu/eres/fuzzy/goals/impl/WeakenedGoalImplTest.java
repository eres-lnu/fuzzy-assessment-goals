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

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.math.DoubleMath;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.FuzzyNumber;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.functions.impl.FuzzyNumberImpl;
import se.lnu.eres.fuzzy.functions.impl.LinearPiecewiseFunctionImpl;
import se.lnu.eres.fuzzy.goals.GoalType;
import se.lnu.eres.fuzzy.goals.LeafGoal;
import se.lnu.eres.fuzzy.goals.LeafGoalType;

class WeakenedGoalImplTest {
	
	protected static final Logger Logger =  LogManager.getLogger(WeakenedGoalImplTest.class.getSimpleName());


	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testAssessSatisfactionLongitudinalAcc()  {
		Logger.info("Starting the testAssessSatisfactionLongitudinalAcc");
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
			FuzzyBoolean result;
			Logger.trace("Right before trying the the assessment of the goal without weakening");
			try {
				result = goal.assessSatisfaction();
			} catch (FunctionOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail("The goal assessment produced an exception");
				return;
			}
			Assertions.assertEquals(4, result.getFunction().getDatapoints().size());
			for (ImmutablePair<Double, Double> point : result.getFunction().getDatapoints()) {
				Assertions.assertTrue(Double.isFinite(point.getLeft()),
						"There is an X point in the result whose value is not finite, its value is: " + point.getLeft());
				Assertions.assertTrue(Double.isFinite(point.getRight()),
						"There is an Y point in the result whose value is not finite, its value is: " + point.getRight());
			}

			Logger.debug("Result of goal satisfaction is:" + result.toString());

			double[] x = new double[]{0, 0.74436, 0.894736, 1.0};
			double[] y = new double[]{0, 0, 1.0, 0.3};
			for(int i=0; i<x.length;i++) {
				Assertions.assertTrue(DoubleMath.fuzzyEquals(x[i], result.getFunction().getDatapoints().get(i).getLeft(), LinearPieceWiseFunction.TOLERANCE), "expected/observed"+x[i]+"/"+result.getFunction().getDatapoints().get(i).getLeft());
				Assertions.assertTrue(DoubleMath.fuzzyEquals(y[i], result.getFunction().getDatapoints().get(i).getRight(), LinearPieceWiseFunction.TOLERANCE), "expected/observed"+y[i]+"/"+ result.getFunction().getDatapoints().get(i).getRight());
			}
			
			LeafGoal weakenedGoal = new WeakenedGoalImpl(goal, new GoalWeakeningFunctionImpl(GoalType.AND, 0.8 ));
			Logger.trace("Right before trying the the assessment of weakened goal");
			try {
				FuzzyNumber weakenedResult = weakenedGoal.assessSatisfaction();
				System.out.println("Weakened Result of goal satisfaction is:" + weakenedResult.toString());
			} catch (FunctionOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail("The weakened goal assessment produced an exception");
			}
			
			
		}
	
	@Test
	void testAssessSatisfactionFuelConsumption() throws FunctionOperationException  {
		Logger.trace("Starting the testAssessSatisfactionFuelConsumption");
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();

		function.addPoint(0.0, 1.0);
		function.addPoint(5.0, 0);
		function.addPoint(6.0, 0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();

		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.3, 0.0);
		observationFunction.addPoint(0.4, 1);
		observationFunction.addPoint(0.42, 1);
		observationFunction.addPoint(0.45, 0.5);
		observationFunction.addPoint(0.5, 0.5);
		observationFunction.addPoint(2.5, 0.0);
		observationFunction.addPoint(6.0, 0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);
		
		goal.setObservation(observation);
		// plotIntermediateResult(goal, "Fuel Consumption");
		
			FuzzyBoolean result;
			Logger.trace("Right before trying the the assessment of the goal without weakening");
			try {
				result = goal.assessSatisfaction();
			} catch (FunctionOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail("The goal assessment produced an exception");
				return;
			}
			Assertions.assertEquals(8, result.getFunction().getDatapoints().size());
			for (ImmutablePair<Double, Double> point : result.getFunction().getDatapoints()) {
				Assertions.assertTrue(Double.isFinite(point.getLeft()),
						"There is an X point in the result whose value is not finite, its value is: " + point.getLeft());
				Assertions.assertTrue(Double.isFinite(point.getRight()),
						"There is an Y point in the result whose value is not finite, its value is: " + point.getRight());
			}

			Logger.debug("Result of goal satisfaction is:" + result.toString());

			
			LeafGoal weakenedGoal = new WeakenedGoalImpl(goal, new GoalWeakeningFunctionImpl(GoalType.AND, 0.8));
			FuzzyBoolean weakenedSanitizedResult;
			Logger.trace("Right before trying the the assessment of weakened goal");
			try {
				weakenedSanitizedResult = weakenedGoal.assessSatisfaction();
				System.out.println("Weakened Result of goal satisfaction is:" + weakenedSanitizedResult.toString());
			} catch (FunctionOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail("The weakened goal assessment produced an exception");
				return;
			}
			
			double[] x = new double[]{0.2, 0.7, 1.0, 1.0};
			double[] y = new double[]{0, 0, 0.375, 1.0};
			
			
			
			checkSanitize(x, y, weakenedSanitizedResult);
			
		}

	private void checkValuesFromPosition(double[] x, double[] y, FuzzyBoolean weakenedResult, int startPosition) {
		
		for(int i=0; i<x.length;i++) {
			Assertions.assertTrue(DoubleMath.fuzzyEquals(x[i], weakenedResult.getFunction().getDatapoints().get(startPosition+i).getLeft(), LinearPieceWiseFunction.TOLERANCE), "expected/observed"+x[i]+"/"+weakenedResult.getFunction().getDatapoints().get(startPosition+i).getLeft());
			Assertions.assertTrue(DoubleMath.fuzzyEquals(y[i], weakenedResult.getFunction().getDatapoints().get(startPosition+i).getRight(), LinearPieceWiseFunction.TOLERANCE), "expected/observed"+y[i]+"/"+ weakenedResult.getFunction().getDatapoints().get(startPosition+i).getRight());
		}
	}

	private void checkSanitize(double[] x, double[] y, FuzzyBoolean weakenedResult) throws FunctionOperationException {
		FuzzyBoolean sanitizedWeakenedResult = weakenedResult.sanitize();
		Logger.trace("Sanitized weakened result is: " + sanitizedWeakenedResult.toString());
		Assertions.assertEquals(0, sanitizedWeakenedResult.getFunction().getDatapoints().get(0).getLeft());
		Assertions.assertEquals(0, sanitizedWeakenedResult.getFunction().getDatapoints().get(0).getRight());
		checkValuesFromPosition(x, y, sanitizedWeakenedResult, 1);
	}
	

}
