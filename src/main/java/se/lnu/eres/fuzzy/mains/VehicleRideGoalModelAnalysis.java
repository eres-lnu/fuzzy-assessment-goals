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
package se.lnu.eres.fuzzy.mains;

import java.io.IOException;

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
import se.lnu.eres.fuzzy.goals.impl.GoalFactory;
import se.lnu.eres.fuzzy.goals.impl.LeafGoalImpl;
import se.lnu.eres.fuzzy.plot.Plotter;

public class VehicleRideGoalModelAnalysis {

	public static void main(String[] args) throws FunctionOperationException, IOException {

		VehicleRideGoalModelAnalysis model = new VehicleRideGoalModelAnalysis();
		model.evaluate();

	}

	private void evaluate() throws FunctionOperationException, IOException {

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

		Plotter p = new Plotter("Root");
		p.addDatasetFromFunction(result.getFunction());
		p.showPlot();
		p.save("Root");

		// Show intermediate non-leaf goals results
		FuzzyBoolean comfortResult = comfort.assessSatisfaction();
		Plotter pc = new Plotter("Comfort");
		pc.addDatasetFromFunction(comfortResult.getFunction());
		pc.showPlot();
		pc.save("Comfort");

		FuzzyBoolean rideResult = rideSatisfaction.assessSatisfaction();
		Plotter pr = new Plotter("Ride");
		pr.addDatasetFromFunction(rideResult.getFunction());
		pr.showPlot();
		pr.save("Ride");

	}

	private LeafGoal createLeafGoalLongitudinalAcceleration() throws FunctionOperationException, IOException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		// The goal and observation functions for the longitudinal acceleration
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(2, 0.0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();

		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.5, 0.0);
		observationFunction.addPoint(0.7, 1.0);
		observationFunction.addPoint(0.9, 0.0);
		observationFunction.addPoint(2, 0.0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);
		

		goal.setObservation(observation);
		plotIntermediateResult(goal, "Longitudinal acceleration");

		return goal;
	}

	private LeafGoal createLeafGoalLateralAcceleration() throws FunctionOperationException, IOException {
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		// The goal and observation functions for the lateral acceleration
		function.addPoint(0.0, 1.0);
		function.addPoint(1.0, 0);
		function.addPoint(1.2, 0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(0.1, 0.0);
		observationFunction.addPoint(0.15, 1.0);
		observationFunction.addPoint(0.2, 0.0);
		observationFunction.addPoint(1.2, 0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);
		
		goal.setObservation(observation);
		plotIntermediateResult(goal, "Lateral acceleration");
		return goal;
	}

	private LeafGoal createLeafGoalBumpming() throws FunctionOperationException, IOException {
		// The goal and observation functions for the bumping. 
		//Fully satisfied goal 
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();

		function.addPoint(0.0, 1.0);
		function.addPoint(0.05, 1.0);
		function.addPoint(1.0, 0);
		function.addPoint(1.2, 0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		observationFunction.addPoint(0.0, 1.0);
		observationFunction.addPoint(0.03, 0.0);
		observationFunction.addPoint(1.2, 0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);
		

		goal.setObservation(observation);
		plotIntermediateResult(goal, "Bumping");
		return goal;
	}

	private LeafGoal createLeafGoalRideDuration() throws FunctionOperationException, IOException {
		// Full sastifaction (at x=0);
		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();

		function.addPoint(0.0, 1.0);
		function.addPoint(15, 1);
		function.addPoint(20, 0);
		function.addPoint(22, 0);
		FuzzyNumber goalTruthValue = new FuzzyNumberImpl(function);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, goalTruthValue);

		LinearPieceWiseFunction observationFunction = new LinearPiecewiseFunctionImpl();
		observationFunction.addPoint(0.0, 0.0);
		observationFunction.addPoint(10, 0.0);
		observationFunction.addPoint(13, 1.0);
		observationFunction.addPoint(17, 1.0);
		observationFunction.addPoint(19, 0.0);
		observationFunction.addPoint(22, 0);
		FuzzyNumber observation = new FuzzyNumberImpl(observationFunction);

		
		
		goal.setObservation(observation);
		plotIntermediateResult(goal, "Ride Duration");
		return goal;
	}

	private LeafGoal createLeafGoalFuelConsumption() throws FunctionOperationException, IOException {
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
		plotIntermediateResult(goal, "Fuel Consumption");
		return goal;
	}

	private void plotRequirementAndObservation(Plotter p, LinearPieceWiseFunction requirement, LinearPieceWiseFunction observationFunction) throws IOException {
		p.addObservationValue(observationFunction);
		p.addRequirementValue(requirement);
		p.showPlot();
		
	}

	private void plotIntermediateResult(LeafGoal goal, String name) throws FunctionOperationException, IOException {
		FuzzyBoolean result = goal.assessSatisfaction();
		Plotter p = new Plotter(name);
		plotRequirementAndObservation(p,  goal.getGoalTruthValue(), goal.getObservation());
		p.addDatasetFromFunction(result.getFunction());
		p.showPlot();
		p.save(name);

	}

}
