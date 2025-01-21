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
import org.junit.jupiter.api.Test;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;

import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;

import se.lnu.eres.fuzzy.goals.Goal;
import se.lnu.eres.fuzzy.goals.GoalType;
import se.lnu.eres.fuzzy.goals.LeafGoal;

class GoalImplANDTest extends AbstractGoalImplTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testAssessAndSatisfaction() throws FunctionOperationException {
		// An AND goal with two leaf goals as children.

		LeafGoal lg1 = createLeafGoalRideSatisfaction();
		LeafGoal lg2 = createLeafGoalFuelConsumption();

		Goal g = GoalFactory.CreateGoal(GoalType.AND);

		g.addChild(lg1);
		g.addChild(lg2);

		FuzzyBoolean result = g.assessSatisfaction();
		Assertions.assertEquals(6, result.getFunction().getDatapoints().size(),
				"Expected 6 elements but the result of goal satisfaction is:" + result.toString());
		System.out.println("Result of goal satisfaction is:" + result.toString());

	}

	@Test
	void testAssessAndSatisfactionDiscontinuousComponentLessSatisfied() throws FunctionOperationException {

		LeafGoal lg1 = createLeafDiscontinuousResult(0.9, 1.1, 4.0);
		LeafGoal lg2 = createLeafGoalFuelConsumptionTriangularGivenTopValueAndWidth(0.3, 0.2);

		Goal g = GoalFactory.CreateGoal(GoalType.AND);

		g.addChild(lg1);
		g.addChild(lg2);

		FuzzyBoolean result = g.assessSatisfaction();
		Assertions.assertEquals(6, result.getFunction().getDatapoints().size(),
				"Expected 6 elements but the result of goal satisfaction is:" + result.toString());
		System.out.println("Result of goal satisfaction is:" + result.toString());

	}

	@Test
	void testAssessAndSatisfactionDiscontinuousComponentLessSatisfiedSwitchLeafOrder()
			throws FunctionOperationException {

		LeafGoal lg1 = createLeafDiscontinuousResult(0.9, 1.1, 4.0);
		LeafGoal lg2 = createLeafGoalFuelConsumptionTriangularGivenTopValueAndWidth(0.3, 0.2);

		Goal g = GoalFactory.CreateGoal(GoalType.AND);

		g.addChild(lg2);
		g.addChild(lg1);

		FuzzyBoolean result = g.assessSatisfaction();
		Assertions.assertEquals(6, result.getFunction().getDatapoints().size(),
				"Expected 6 elements but the result of goal satisfaction is:" + result.toString());
		System.out.println("Result of goal satisfaction is:" + result.toString());

	}

	@Test
	void testAssessAndSatisfactionDiscontinuousComponentMoreSatisfied() throws FunctionOperationException {

		LeafGoal lg1 = createLeafDiscontinuousResult(0.9, 1.1, 4.0);
		LeafGoal lg2 = createLeafGoalFuelConsumptionTriangularGivenTopValueAndWidth(0.8, 0.2);

		Goal g = GoalFactory.CreateGoal(GoalType.AND);

		g.addChild(lg1);
		g.addChild(lg2);

		FuzzyBoolean result = g.assessSatisfaction();
		Assertions.assertEquals(5, result.getFunction().getDatapoints().size(),
				"Expected 6 elements but the result of goal satisfaction is:" + result.toString());

		double resultXY[][] = new double[][] { { 0.0, 0.68965 }, { 0.175, 0.75 }, { 0.2, 1 }, { 0.3, 0 }, { 1, 0 } };
		checkFuzzyEquals(resultXY, result);
		System.out.println("Result of goal satisfaction is:" + result.toString());

	}

	@Test
	void testAssessAndSatisfactionDiscontinuousComponentMoreSatisfiedSwitchLeafOrder()
			throws FunctionOperationException {

		LeafGoal lg1 = createLeafDiscontinuousResult(0.9, 1.1, 4.0);
		LeafGoal lg2 = createLeafGoalFuelConsumptionTriangularGivenTopValueAndWidth(0.8, 0.2);

		Goal g = GoalFactory.CreateGoal(GoalType.AND);

		g.addChild(lg2);
		g.addChild(lg1);

		FuzzyBoolean result = g.assessSatisfaction();
		Assertions.assertEquals(5, result.getFunction().getDatapoints().size(),
				"Expected 6 elements but the result of goal satisfaction is:" + result.toString());

		double resultXY[][] = new double[][] { { 0.0, 0.68965 }, { 0.175, 0.75 }, { 0.2, 1 }, { 0.3, 0 }, { 1, 0 } };
		checkFuzzyEquals(resultXY, result);
		System.out.println("Result of goal satisfaction is:" + result.toString());

	}

	@Test
	void testAssessAndSatisfactionDoubleDiscontinuousTriplePoint() throws FunctionOperationException {

		LeafGoal lg1 = createLeafDiscontinuousResult(0.9, 1.1, 4.0);
		LeafGoal lg2 = createLeafDiscontinuousResult(0.95, 1.1, 3.0);

		Goal g = GoalFactory.CreateGoal(GoalType.AND);

		g.addChild(lg1);
		g.addChild(lg2);

		FuzzyBoolean result = g.assessSatisfaction();
		Assertions.assertEquals(6, result.getFunction().getDatapoints().size(),
				"Expected 6 elements but the result of goal satisfaction is:" + result.toString());
		System.out.println("Result of goal satisfaction is:" + result.toString());
		double resultXY[][] = new double[][] { { 0.0, 0.68965 }, { 0.5, 0.86206 }, { 0.5, 1.0 }, { 0.5, 0.333333 },
				{ 0.55, 0.0 }, { 1, 0 } };
		checkFuzzyEquals(resultXY, result);
		System.out.println("Result of goal satisfaction is:" + result.toString());

	}

}
