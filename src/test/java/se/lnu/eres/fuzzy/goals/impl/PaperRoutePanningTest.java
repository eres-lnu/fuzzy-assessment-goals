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


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.FuzzyNumber;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.functions.impl.FuzzyNumberImpl;
import se.lnu.eres.fuzzy.functions.impl.LinearPiecewiseFunctionImpl;
import se.lnu.eres.fuzzy.goals.Goal;
import se.lnu.eres.fuzzy.goals.GoalType;
import se.lnu.eres.fuzzy.goals.LeafGoal;
import se.lnu.eres.fuzzy.goals.LeafGoalType;
import se.lnu.eres.fuzzy.plot.Plotter;

class PaperRoutePanningTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void PapeerRoutePlanningTest() throws FunctionOperationException, InterruptedException {
		Goal comfort = GoalFactory.CreateGoal(GoalType.AND);

		LeafGoal longitudinalAcceleration = createLeafGoalLongitudinalAcceleration();
		LeafGoal lateralAcceleration = createLeafGoalLateralAcceleration();
		LeafGoal bumping = createLeafGoalBumpming();

		comfort.addChild(longitudinalAcceleration);
		comfort.addChild(lateralAcceleration);
		comfort.addChild(bumping);

		Goal rideSatisfaction = GoalFactory.CreateGoal(GoalType.OR);

		LeafGoal rideDuration = createLeafGoalRideDuration();

		rideSatisfaction.addChild(rideDuration);
		rideSatisfaction.addChild(comfort);

		Goal rootSystemSatisfaction = GoalFactory.CreateGoal(GoalType.AND);

		LeafGoal fuelConsumption = createLeafGoalFuelConsumption();

		rootSystemSatisfaction.addChild(fuelConsumption);
		rootSystemSatisfaction.addChild(rideSatisfaction);

		FuzzyBoolean result = rootSystemSatisfaction.assessSatisfaction();

		Plotter p = new Plotter("Route planning");
		p.addDatasetFromFunction(result.getFunction());
		p.showPlot();
		
		Thread.sleep(5000);

	}
	
	
	@Test
	void PaperBumpingLeafGoalTest() throws FunctionOperationException, InterruptedException {


		LeafGoal bumping = createLeafGoalBumpming();
		FuzzyBoolean result = bumping.assessSatisfaction();
		Plotter p = new Plotter("Route planning");
		p.addDatasetFromFunction(result.getFunction());
		p.showPlot();

		Thread.sleep(5000);

		


		

	}
	
	@Test
	void PapeerComfortGoalTest() throws FunctionOperationException, InterruptedException {
		Goal comfort = GoalFactory.CreateGoal(GoalType.AND);

		LeafGoal longitudinalAcceleration = createLeafGoalLongitudinalAcceleration();
		LeafGoal lateralAcceleration = createLeafGoalLateralAcceleration();
		LeafGoal bumping = createLeafGoalBumpming();

		comfort.addChild(longitudinalAcceleration);
		comfort.addChild(lateralAcceleration);
		comfort.addChild(bumping);
		Thread.sleep(10000);

		FuzzyBoolean result = comfort.assessSatisfaction();

		Plotter p = new Plotter("Route planning");
		p.addDatasetFromFunction(result.getFunction());
		p.showPlot();
		

	}

	private LeafGoal createLeafGoalLongitudinalAcceleration() throws FunctionOperationException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		// The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue, "Longitudinal acceleration");

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();

		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.5, 0.0);
		observationFunction.addPoint(0.7, 1.0);
		observationFunction.addPoint(0.9, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		plotIntermediateResult(goal, "Longitudinal acceleration");

		return goal;
	}

	private LeafGoal createLeafGoalLateralAcceleration() throws FunctionOperationException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		// The points in the lateral acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(1.0, 0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue, "Lateral acceleration");

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.1, 0.0);
		observationFunction.addPoint(0.15, 1.0);
		observationFunction.addPoint(0.2, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		plotIntermediateResult(goal, "Lateral acceleration");
		return goal;
	}

	private LeafGoal createLeafGoalBumpming() throws FunctionOperationException, InterruptedException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();

		function.addPoint(0.0, 1.0);
		//function.addPoint(0.1, 1.0);
		function.addPoint(1.0, 0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue, "Bumping");

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		observationFunction.addPoint(0.0, 1.0);
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		plotIntermediateResult(goal, "Bumping");
		//Thread.sleep(5000);
		return goal;
	}

	private LeafGoal createLeafGoalRideDuration() throws FunctionOperationException {
		// Full sastifaction (at x=0);
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();

		function.addPoint(0.0, 1.0);
		function.addPoint(15, 1);
		function.addPoint(20, 0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue, "Ride duration");

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(10, 0.0);
		observationFunction.addPoint(13, 1.0);
		observationFunction.addPoint(17, 1.0);
		observationFunction.addPoint(19, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		plotIntermediateResult(goal, "Ride Duration");
		return goal;
	}

	private LeafGoal createLeafGoalFuelConsumption() throws FunctionOperationException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();

		function.addPoint(0.0, 1.0);
		function.addPoint(2.0, 0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue, "Fuel consumption");

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.3, 0.0);
		observationFunction.addPoint(1, 1);
		observationFunction.addPoint(1.5, 1);
		observationFunction.addPoint(1.8, 0.5);
		observationFunction.addPoint(2.2, 0.5);
		observationFunction.addPoint(3.5, 0.0);
		observationFunction.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		goal.setObservation(observation);
		plotIntermediateResult(goal, "Fuel Consumption");
		return goal;
	}

	private void plotIntermediateResult(LeafGoal goal, String name) throws FunctionOperationException {
		FuzzyBoolean result = goal.assessSatisfaction();
		Plotter p = new Plotter(name);
		p.addDatasetFromFunction(result.getFunction());
		p.showPlot();

	}
	


}
