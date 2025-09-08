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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.lnu.eres.fuzzy.functions.FuzzyNumber;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FuzzyOperationException;
import se.lnu.eres.fuzzy.functions.impl.FuzzyNumberImpl;
import se.lnu.eres.fuzzy.functions.impl.LinearPiecewiseFunctionImpl;
import se.lnu.eres.fuzzy.goals.GoalType;
import se.lnu.eres.fuzzy.goals.LeafGoal;
import se.lnu.eres.fuzzy.goals.LeafGoalType;

public class GoalWeakeningFunctionImpl extends FuzzyNumberImpl {
	private static final Logger Logger = LogManager.getLogger(GoalWeakeningFunctionImpl.class.getSimpleName());

	public GoalWeakeningFunctionImpl(GoalType type, double alpha) {
		super(createFunction(type, alpha));
	}

	private static LinearPieceWiseFunction createFunction(GoalType type, double alpha) {

		LinearPieceWiseFunction function = new LinearPiecewiseFunctionImpl();
		switch (type) {
		case AND:
			//function.addPoint(-Double.MAX_VALUE, 1.0-alpha);
			//Starts at 1-alpha and at x=alpha, reaches 1
			function.addPoint(0.0,1.0-alpha);
			function.addPoint(0.0,1.0-alpha);
			function.addPoint(alpha, 1.0);
			function.addPoint(1.0, 1.0);
			break;
		case OR:
			//start at 0 and remains like that until x=alpha
			//Then it grows linearly with slope=1, which means that reaches x=1 with value 1-alpha
			function.addPoint(0.0, 0.0);
			function.addPoint(alpha, 0.0);
			function.addPoint(1.0, 1.0-alpha);
			//and it remains at that value, without ever reaching one
			function.addPoint(Double.MAX_VALUE, 1.0-alpha);
			
			break;
		default:
			Logger.error("The type of the Goal was not ANd or OR. It was {}", type);
		}
		return function;

	}

	@Override
	public boolean isFuzzyNumber() throws FuzzyOperationException {
		throw new FuzzyOperationException("IsFuzzyNumber not implemented for the Goal Weakening Function");
	}

}
