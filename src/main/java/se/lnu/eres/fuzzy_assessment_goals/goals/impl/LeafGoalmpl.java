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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import se.lnu.eres.fuzzy_assessment_goals.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy_assessment_goals.functions.FuzzyNumber;
import se.lnu.eres.fuzzy_assessment_goals.functions.LinearPieceWiseFunctionDataPoints;
import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FunctionOperationException;
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

	/**
	 * Uses Zadeh's extension principle B(y) = sup{O(x)|μG (x) = y}, (0 ≤ y ≤ 1) to create the satisfaction function
	 * @throws FunctionOperationException 
	 */
	@Override
	public FuzzyBoolean assessSatisfaction(FuzzyNumber observation) throws FunctionOperationException {
		
		//Zadeh's principle B(y) = sup{O(x)|μG (x) = y}, (0 ≤ y ≤ 1)
		
		//Points of interest: the combination of the points of the observation and the truth values
		//Using the assumption that the functions are piecewise functions composed of linear functions
		
		List<Double> xPointsOfInterest =  CollectionUtils.collate(truthValue.getFunction().getLimitXpoints(), observation.getFunction().getLimitXpoints());
		
		//TODO: First iteration considers only LB or UB goals, that are monotically increasing/decreasing and the same Y value cannot 
		// happen for x values that are in different pieces in the function. 
		
		double leftXpoint=0;
		boolean assignedLeftXpoint=false;
		LinearPieceWiseFunctionDataPoints resultInterval = new LinearPieceWiseFunctionDataPoints();
		for(double rightXPoint : xPointsOfInterest) {
			if(!assignedLeftXpoint) {
				leftXpoint=rightXPoint;
				assignedLeftXpoint=true;
			}
			else {
				resultInterval=	calculateResultInInterval(leftXpoint, rightXPoint, observation);
				leftXpoint=rightXPoint;
			}
		}
		
		throw new UnsupportedOperationException();
	}

	private LinearPieceWiseFunctionDataPoints calculateResultInInterval(double leftXpoint, double rightXpoint,
			FuzzyNumber observation) throws FunctionOperationException {
		/*
		 * It calculates the satisfaction function between points leftXpoint and rightXPoint
		 */
		double minY = Math.min(truthValue.getFunctionValueAt(leftXpoint), truthValue.getFunctionValueAt(rightXpoint));
		double maxY = Math.max(truthValue.getFunctionValueAt(leftXpoint), truthValue.getFunctionValueAt(rightXpoint));
		//the Y in the input become the X in the output
		
		//TODO:Continue here
		throw new UnsupportedOperationException("not implemented yet");

	}

}
