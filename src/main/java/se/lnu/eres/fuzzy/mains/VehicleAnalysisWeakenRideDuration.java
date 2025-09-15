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

import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.goals.GoalType;
import se.lnu.eres.fuzzy.goals.LeafGoal;
import se.lnu.eres.fuzzy.goals.impl.GoalWeakeningFunctionImpl;
import se.lnu.eres.fuzzy.goals.impl.WeakenedGoalImpl;

public class VehicleAnalysisWeakenRideDuration extends VehicleRideGoalModelAnalysis {

	private static final double Weaknessing[] = {0, 0.1, 0.2, 0.3};

	public static void main(String[] args) throws FunctionOperationException, IOException {

		for (int i = 0; i < Weaknessing.length; i++) {
			VehicleAnalysisWeakenRideDuration model = new VehicleAnalysisWeakenRideDuration();
			model.setExperimentIndex(i);
			model.createRideSatisfactionModel();
			model.evaluate();
			
		}

	}

	private void setExperimentIndex(int index) {
		experimentWeakeningIndex=index;
		
	}

	private int experimentWeakeningIndex;
	
	@Override
	protected LeafGoal createLeafGoalRideDuration() throws FunctionOperationException, IOException {
		LeafGoal goal = super.createLeafGoalRideDuration();
		// return goal;
		LeafGoal weakenedGoal = new WeakenedGoalImpl(goal, new GoalWeakeningFunctionImpl(GoalType.OR, Weaknessing[experimentWeakeningIndex]));
		return weakenedGoal;
	}
}
