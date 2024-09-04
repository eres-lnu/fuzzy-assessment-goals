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
package se.lnu.eres.fuzzy_assessment_goals.goals.impl;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.lnu.eres.fuzzy_assessment_goals.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy_assessment_goals.functions.FuzzyNumber;
import se.lnu.eres.fuzzy_assessment_goals.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy_assessment_goals.functions.impl.FunctionPiecewiseImpl;
import se.lnu.eres.fuzzy_assessment_goals.functions.impl.FuzzyNumberImpl;
import se.lnu.eres.fuzzy_assessment_goals.goals.GoalType;
import se.lnu.eres.fuzzy_assessment_goals.goals.Goal;
import se.lnu.eres.fuzzy_assessment_goals.goals.LeafGoal;
import se.lnu.eres.fuzzy_assessment_goals.goals.LeafGoalType;

class GoalImplTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testAssessSatisfaction() throws FunctionOperationException {
		//An AND goal with two leaf goals as children.
		
		LeafGoal lg1 = createLeafGoalRideSatisfaction();
		LeafGoal lg2 = createLeafGoalFuelConsumption();
		
		Goal g = new GoalImpl(GoalType.AND);
		
		g.addChild(lg1);
		g.addChild(lg2);
		
		FuzzyBoolean result = g.assessSatisfaction();
		Assertions.assertEquals(6, result.getFunction().getDatapoints().size(), "Expected 6 elements but the result of goal satisfaction is:" + result.toString());
		System.out.println("Result of goal satisfaction is:" + result.toString());
		
		
		
	}

	private LeafGoal createLeafGoalFuelConsumption() {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		//The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(1.0, 0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);
		
		LinearPieceWiseFunction observationFunction = new FunctionPiecewiseImpl();
		//The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.1, 0.0);
		observationFunction.addPoint(0.15, 1.0);
		observationFunction.addPoint(0.2, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);
		
		goal.setObservation(observation);
		return goal;
	}

	private LeafGoal createLeafGoalRideSatisfaction() {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		//The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);
		
		LinearPieceWiseFunction observationFunction = new FunctionPiecewiseImpl();
		//The points in the Observation O2. Triangle with Max in f(0.7)=1 and mins in f(0.5)=f(0.9)=0
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.5, 0.0);
		observationFunction.addPoint(0.7, 1.0);
		observationFunction.addPoint(0.9, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);
		
		goal.setObservation(observation);
		return goal;
		
	}

}
