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
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.math.DoubleMath;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.goals.Goal;
import se.lnu.eres.fuzzy.goals.GoalType;

class GoalAndImpl extends AbstractGoal implements Goal {

	private static final Logger Logger = LogManager.getLogger(GoalAndImpl.class.getSimpleName());
	
	public GoalAndImpl(List<Goal> children) {
		super(GoalType.AND, children);
	}

	public GoalAndImpl() {
		this(new ArrayList<Goal>());
	}

	@Override
	protected double getLargestValueOfInterestFromFunction(FuzzyBoolean f, double p) throws FunctionOperationException {
		//find the largest value f(x) such that x>=p, 
		return f.getLargestValueAfterX(p,true);
	}

	@Override
	protected double getLargestValueOfInterestFromFunction(FuzzyBoolean f, double p, boolean approachFromLeft)
			throws FunctionOperationException {
		//find the largest value f(x) such that x>p, may be a discontinuity point 
		return f.getLargestValueAfterX(p,approachFromLeft);
	}
	
	@Override
	protected List<Double> setMinimums(List<Double> f1ValuesAtP, double maxYOfInterestInF2,
			double maxYOfInterestInF2ApproachingFromRight) {
		List<Double> minimums = f1ValuesAtP.stream().map(d -> Math.min(d, maxYOfInterestInF2))
				.collect(Collectors.toList());
		// If both were discontinuity points, set the last of minimums using
		// maxYOfInterestInF2ApproachingFromRight

		// It is a discontinuity point in f1 if the size of the list is >1
		// IT is a discontinuity point in f2 if the value is different depending on
		// whether it is approached from the right

		if (minimums.size() > 1 && (!DoubleMath.fuzzyEquals(maxYOfInterestInF2ApproachingFromRight, maxYOfInterestInF2,
				LinearPieceWiseFunction.TOLERANCE))) { // Both are discontinuity

			Logger.debug(
					"The minimums of a discontinuous function are going to change. Input was f:{}, maximum values in f2 were: {}, {}",
					f1ValuesAtP.toString(), maxYOfInterestInF2, maxYOfInterestInF2ApproachingFromRight);
			double lastMinimum =minimums.removeLast();
			Logger.debug("The last value of the list of minimus was:{}", lastMinimum);
			
			minimums.addLast(Math.min(f1ValuesAtP.get( f1ValuesAtP.size() - 1), maxYOfInterestInF2ApproachingFromRight));

		}
		return minimums;

	}

}
