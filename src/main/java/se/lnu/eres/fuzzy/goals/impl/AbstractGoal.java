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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.functions.impl.LinearPiecewiseFunctionImpl;
import se.lnu.eres.fuzzy.functions.impl.FuzzyBooleanImpl;
import se.lnu.eres.fuzzy.goals.Goal;
import se.lnu.eres.fuzzy.goals.GoalType;

public abstract class AbstractGoal implements Goal {

	private static final Logger Logger = LogManager.getLogger(AbstractGoal.class.getSimpleName());
	private static final String NL = System.getProperty("line.separator");
	private final GoalType type;

	private List<Goal> children;

	public AbstractGoal(GoalType type, List<Goal> children) {
		super();
		this.type = type;
		this.children = children;
	}

	public AbstractGoal(GoalType type) {
		this(type, new ArrayList<Goal>());
	}

	@Override
	public GoalType getType() {
		return type;
	}

	@Override
	public List<Goal> getChildren() {
		return children;
	}

	@Override
	public void addChild(Goal child) {
		children.add(child);

	}

	@Override
	public FuzzyBoolean assessSatisfaction() throws FunctionOperationException {
		if (children.size() == 0) {
			throw new FunctionOperationException(
					"No possible satisfaction assessment because intermediate goal does not have any child");
		}

		FuzzyBoolean partialResult = children.getFirst().assessSatisfaction();
		// skip the first
		for (int i = 1; i < children.size(); i++) {
			partialResult = assessPartialSatisfactionAllowingDiscontinuous(partialResult,
					children.get(i).assessSatisfaction());
			Logger.debug("Partial result at iteration i={} is {}", i, partialResult.getFunction().toString());
		}
		return partialResult;

	}

	private FuzzyBoolean assessPartialSatisfactionAllowingDiscontinuous(FuzzyBoolean f1, FuzzyBoolean f2)
			throws FunctionOperationException {
		// Zadeh's extension principle B(z) = sup {t(B1(x), B2(y))|t(x, y) = z}, 0 ≤ z ≤
		// 1 (5) x,y∈[0,1], where t-norm is the Min

		Logger.debug("Starting assessment of partial satisfaction. Fuzzy booleans are: {}   f1: {} {}   f2: {}", NL,
				f1.toString(), NL, f2.toString());

		// Get x-points of interest from the points of interest of the two fuzzy
		// booleans
		List<Double> xPointsOfInterest = CollectionUtils.collate(f1.getFunction().getLimitXpoints(),
				f2.getFunction().getLimitXpoints(), false);
		// Add as point of interest the x values where the fuzzy booleans intersect (the
		// minimum value passes from belonging to f1 to f2).
		/* After this, we known that the full interval is either below or above */
		Logger.debug("The points of interests for X are: {}", xPointsOfInterest.toString());
		// TODO: find intersections
		xPointsOfInterest = CollectionUtils.collate(xPointsOfInterest,
				f1.getFunction().findIntersections(f2.getFunction()), false);
		Logger.debug("The points of interests after adding the intersections between functions are: {}",
				xPointsOfInterest.toString());

		LinearPieceWiseFunction resultFunction = new LinearPiecewiseFunctionImpl();

		// For each point of interests p
		for (double p : xPointsOfInterest) {
			Logger.debug("Starting loop for point of intestest {}", p);
			// here it depends whether the goal is of type AND or OR. Find the largest value
			// f2(x) such that x=>p or x<=p,
			double maxYOfInterestInF2 = getLargestValueOfInterestFromFunction(f2, p, true);
			// f1(p) may have multiple values:
			List<Double> f1ValuesAtP = f1.getFunctionValuesAt(p);
			Logger.debug("   For point {}, the maxYOfInterestInF2={} and the f1 values at the point are:{}", p,
					maxYOfInterestInF2, f1ValuesAtP.toString());
			// Save the minimums between<f2(x),severalf1(p)>
			// List<Double> minimums1 = new ArrayList<Double>();
			// f1ValuesAtP.forEach((d) -> {minimums1.add( Math.min(d,
			// maxYOfInterestInF2));}) ;
			// with streams
			List<Double> minimums1 = setMinimums(f1ValuesAtP, maxYOfInterestInF2,
					getLargestValueOfInterestFromFunction(f2, p, false));

			// here it depends whether the goal is of type AND or OR. Find the largest value
			// f1(x) such that x=>p or x<=p,
			double maxYOfInterestInF1 = getLargestValueOfInterestFromFunction(f1, p, true);
			// f2(p) may have multiple values
			List<Double> f2ValuesAtP = f2.getFunctionValuesAt(p);
			// Save the minimums between<several2(x),f1(p)>
			// with streams
			List<Double> minimums2 = setMinimums(f2ValuesAtP, maxYOfInterestInF1,
					getLargestValueOfInterestFromFunction(f1, p, false));

			Logger.debug("The minimum values for Y found for x={} have been: f1:{} and f2:{}", p, minimums1.toString(),
					minimums2.toString());

			for (int i = 0; i < Math.max(minimums1.size(), minimums2.size()); i++) {
				resultFunction.addPoint(p, Math.max(minimums1.get(Math.min(minimums1.size() - 1, i)),
						minimums2.get(Math.min(minimums2.size() - 1, i))));
				Logger.debug(
						"Added point to result function for the point of interest p={}. Now the result function looks like: {}",
						p, resultFunction.toString());
			}
		}

		Logger.debug("Satisfaction result points calculated. Now sorting datapoints of {}", resultFunction);
		// At this point the result interval has several duplicates and is out of order.
		// Clean duplicates and sort.
		resultFunction.getDatapoints().sortByX();
		// resultFunction.getDatapoints().retainLargestYforReplicatedX();
		Logger.debug("Satisfaction result points calculated an sorted. Now removing dupulicates from {}", resultFunction);
		resultFunction.simplifyPiecewiseFunction();
		return new FuzzyBooleanImpl(resultFunction);
	}

	/**
	 * Returns the largest value of interest from function "f", which is the largest
	 * value after x-axis=p. If the p is approached from left, the maximum value at
	 * p is considered. If the p is approached from the right and it is a
	 * discontinuity point, only the rightmost value of of p in the function is
	 * considered.
	 * 
	 * @param f
	 * @param p
	 * @param approachFromLeft
	 * @return
	 * @throws FunctionOperationException
	 */
	protected abstract double getLargestValueOfInterestFromFunction(FuzzyBoolean f, double p, boolean approachFromLeft)
			throws FunctionOperationException;

	protected abstract double getLargestValueOfInterestFromFunction(FuzzyBoolean f1, double p)
			throws FunctionOperationException;

	protected abstract List<Double> setMinimums(List<Double> f1ValuesAtP, double maxYOfInterestInF2,
			double maxYOfInterestInF2ApproachingFromRight);
}
