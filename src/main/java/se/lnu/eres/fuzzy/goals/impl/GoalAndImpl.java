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

import java.util.ArrayList;
import java.util.List;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.goals.Goal;
import se.lnu.eres.fuzzy.goals.GoalType;

class GoalAndImpl extends AbstractGoal implements Goal {

	public GoalAndImpl(List<Goal> children) {
		super(GoalType.AND, children);
	}

	public GoalAndImpl() {
		this(new ArrayList<Goal>());
	}

	@Override
	protected double getLargestValueOfInterestFromFunction(FuzzyBoolean f, double p) throws FunctionOperationException {
		//find the largest value f(x) such that x>=p, 
		return f.getLargestValueAfterX(p,false);
	}

	@Override
	protected double getLargestValueOfInterestFromFunction(FuzzyBoolean f, double p, boolean approachFromLeft)
			throws FunctionOperationException {
		return f.getLargestValueAfterX(p,true);
	}

}
