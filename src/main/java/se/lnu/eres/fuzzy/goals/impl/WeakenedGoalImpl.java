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

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.FuzzyNumber;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.goals.Goal;
import se.lnu.eres.fuzzy.goals.LeafGoal;
import se.lnu.eres.fuzzy.goals.LeafGoalType;

public class WeakenedGoalImpl extends LeafGoalImpl implements Goal {



	private LeafGoal baseGoal;
	
	public WeakenedGoalImpl(LeafGoal goal, FuzzyNumber weakeningFunction) {
		super(LeafGoalType.LB, weakeningFunction);
		baseGoal=goal;
		
	}

	@Override
	public FuzzyBoolean assessSatisfaction() throws FunctionOperationException {
		return assessSatisfactionFromObservation(baseGoal.assessSatisfaction()).sanitize();
	}

	@Override
	public LinearPieceWiseFunction getObservation() throws FunctionOperationException {
		
		return baseGoal.getObservation();
	}

	@Override
	public LinearPieceWiseFunction getGoalTruthValue() throws FunctionOperationException {
		// TODO Auto-generated method stub
		return baseGoal.getGoalTruthValue();
	}

	
	
}
