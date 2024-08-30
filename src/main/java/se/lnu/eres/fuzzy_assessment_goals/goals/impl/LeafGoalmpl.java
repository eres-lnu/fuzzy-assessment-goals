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

import se.lnu.eres.fuzzy_assessment_goals.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy_assessment_goals.functions.FuzzyNumber;
import se.lnu.eres.fuzzy_assessment_goals.goals.LeafGoal;
import se.lnu.eres.fuzzy_assessment_goals.goals.LeafGoalType;

public class LeafGoalmpl implements LeafGoal {

	private final LeafGoalType type;
	private FuzzyNumber truthValue;
	
	
	public LeafGoalmpl(LeafGoalType type, FuzzyNumber function) {
		this.type=type;
		this.truthValue=function;
		
		
	}

	@Override
	public LeafGoalType getType() {
		return type;
	}

	@Override
	public boolean isCorrectType() {
		switch (type) {
        case LB:
            //Monotonically increasing. It should start with y=0 and finish with y=1, but we leave that check out
        	return truthValue.getFunction().isMonotonicallyIncreasing();
           
        case UB:
        	//Monotonically decreasing. It should start with y=1 and finish with y=0, but we leave that check out
        	return truthValue.getFunction().isMonotonicallyDecreasing();
            //break;
        default: //( INT, MIN, MAX)
        	//TODO: Continue with the rest. check that the type corresponds to the goal satisfaction function!
        	throw new UnsupportedOperationException();
    }
		
	
	}

	@Override
	public FuzzyBoolean assessSatisfaction(FuzzyNumber observation) {
		
		//Zadeh's principle B(y) = sup{O(x)|μG (x) = y}, (0 ≤ y ≤ 1)
		
		//Points of interest: the combination of the points of the observation and the truth values
		
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
