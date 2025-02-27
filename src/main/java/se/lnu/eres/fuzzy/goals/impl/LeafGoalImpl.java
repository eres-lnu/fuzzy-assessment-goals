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

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.math.DoubleMath;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.FuzzyNumber;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.functions.impl.LinearPiecewiseFunctionImpl;
import se.lnu.eres.fuzzy.functions.impl.FuzzyBooleanImpl;
import se.lnu.eres.fuzzy.functions.impl.LinearPieceWiseFunctionDataPoints;
import se.lnu.eres.fuzzy.goals.Goal;
import se.lnu.eres.fuzzy.goals.GoalType;
import se.lnu.eres.fuzzy.goals.LeafGoal;
import se.lnu.eres.fuzzy.goals.LeafGoalType;

public class LeafGoalImpl implements LeafGoal {

	private static final Logger Logger = LogManager.getLogger(LeafGoalImpl.class.getSimpleName());

	private final LeafGoalType type;
	private FuzzyNumber truthValue;
	private FuzzyNumber lastObservation = null;
	private String name;

	public LeafGoalImpl(LeafGoalType type, FuzzyNumber function) {
		this(type, function, "Default leaf goal");

	}

	public LeafGoalImpl(LeafGoalType type, FuzzyNumber truthValue, String name) {
		super();
		this.type = type;
		this.truthValue = truthValue;
		this.name = name;
		Logger.info("Cronstructing Leaf goal named {}", name);
	}

	@Override
	public LinearPieceWiseFunction getObservation() throws FunctionOperationException {
		if(lastObservation!=null) {
			return lastObservation.getFunction();
		}
		throw new FunctionOperationException("Observation was not set. Impossible to find its function");
	}
	
	@Override
	public LinearPieceWiseFunction getGoalTruthValue() throws FunctionOperationException {
		if(truthValue!=null) {
			return truthValue.getFunction();
		}
		throw new FunctionOperationException("Truth value was null. Impossible to find its function");
	}
	
	@Override
	public LeafGoalType getLeafType() {
		return type;
	}

	@Override
	public boolean isCorrectType() {
		switch (type) {
		case LB:
			// Monotonically increasing. It should start with y=0 and finish with y=1, but
			// we leave that check out
			return truthValue.getFunction().isMonotonicallyIncreasing();

		case UB:
			// Monotonically decreasing. It should start with y=1 and finish with y=0, but
			// we leave that check out
			return truthValue.getFunction().isMonotonicallyDecreasing();
		// break;
		default: // ( INT, MIN, MAX)
			// TODO: Continue with the rest. check that the type corresponds to the goal
			// satisfaction function!
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Uses Zadeh's extension principle B(y) = sup{O(x)|μG (x) = y}, (0 ≤ y ≤ 1) to
	 * create the satisfaction function
	 * 
	 * @throws FunctionOperationException
	 */
	@Override
	public FuzzyBoolean assessSatisfactionFromObservation(FuzzyNumber observation) throws FunctionOperationException {

		// Zadeh's principle B(y) = sup{O(x)|μG (x) = y}, (0 ≤ y ≤ 1)

		// Points of interest: the combination of the points of the observation and the
		// truth values
		// Using the assumption that the functions are piecewise functions composed of
		// linear functions

		List<Double> xPointsOfInterest = CollectionUtils.collate(truthValue.getFunction().getLimitXpoints(),
				observation.getFunction().getLimitXpoints(), false);

		Logger.debug("Points of interest in x-axis are: {}", xPointsOfInterest);
		// TODO: First iteration considers only LB or UB goals, that are monotically
		// increasing/decreasing and the same Y value cannot
		// happen for x values that are in different pieces in the function.

		double leftXpoint = 0;
		boolean assignedLeftXpoint = false;
		LinearPieceWiseFunctionDataPoints resultInterval = new LinearPieceWiseFunctionDataPoints();
		LinearPieceWiseFunctionDataPoints resultZeroLengthIntervals = new LinearPieceWiseFunctionDataPoints();
		for (double rightXPoint : xPointsOfInterest) {
			if (!assignedLeftXpoint) {
				leftXpoint = rightXPoint;
				assignedLeftXpoint = true;
			} else {
				Logger.debug("Calculatiung result interval for <leftXpiont,rightXPoint,observation>=<{},{},{}>",
						leftXpoint, rightXPoint, observation);
				LinearPieceWiseFunctionDataPoints newinterval = calculateResultInInterval(leftXpoint, rightXPoint,
						observation);
				// Add to intervals or zeroLengthIntervals depending on the interval length
				if (DoubleMath.fuzzyEquals(newinterval.getFirst().getLeft(), newinterval.getLast().getLeft(),
						LinearPieceWiseFunction.TOLERANCE)) {
					// Zero length
					resultZeroLengthIntervals.add(newinterval.getFirst());
					resultZeroLengthIntervals.add(newinterval.getLast());
				} else {
					resultInterval.addAll(newinterval);
				}
				Logger.info(
						"Calculation finished. Current result interval is: {} and zero length intervals contain the points: {}",
						resultInterval.toString(), resultZeroLengthIntervals.toString());
				leftXpoint = rightXPoint;
			}
		}

		/*
		 * At this point the result intervals may have duplicates (max point of an
		 * interval corresponds with minimum of another interval) and may be out of
		 * order. Clean duplicates and sort respecting intervals.
		 */

		resultInterval.sortByXRespectingIntervalCouples();

		// The Y values should match now because the single points are in a different
		// DataPoints structure, but they are replicated
		// resultInterval.retainLargestYforReplicatedX();

		// Same for the single points. Sort and keep only the largest (to satisfy part
		// of the "sup" in Zadeh's extension principle)
		resultZeroLengthIntervals.sortByX();
		
		resultZeroLengthIntervals.removeElementsExistingInDataset(resultInterval);
		
		resultZeroLengthIntervals.retainLargestYforReplicatedX();
		resultZeroLengthIntervals.removePointsWhoseYValueisTheSmallest(resultInterval);

		/*
		 * The result may be a non-continuous function, being a single points left in
		 * discontinuity (e.g., the full satisfaction of a requirement or the full
		 * dissatisfaction). Add the single points by merging the zero value intervals
		 * with the rest of points and then sort the result
		 * 
		 */
		Logger.debug("Assessment of zero lenght intervals after processing is: {}", resultZeroLengthIntervals.toString());
		resultInterval.addAllSortedInEvenPositions(resultZeroLengthIntervals); // To in odd positions to avoid putting
																				// the single point in between the two
																				// points of an interval
		Logger.info("Assessment of intervals ans single points finished. Result after merging with zero intervals is: {}", resultInterval.toString());
		resultInterval.removeDuplicatedNeighborPoints();

		Logger.info("Assessment of leaf goal '{}' finished. Result is is: {}", name, resultInterval.toString());
		return new FuzzyBooleanImpl(new LinearPiecewiseFunctionImpl(resultInterval));

	}

	private LinearPieceWiseFunctionDataPoints calculateResultInInterval(double leftXpoint, double rightXpoint,
			FuzzyNumber observation) throws FunctionOperationException {
		/*
		 * It calculates the satisfaction function between points leftXpoint and
		 * rightXPoint
		 */
		double minY = Math.min(truthValue.getFunctionValueAt(leftXpoint), truthValue.getFunctionValueAt(rightXpoint));
		double maxY = Math.max(truthValue.getFunctionValueAt(leftXpoint), truthValue.getFunctionValueAt(rightXpoint));
		// the Y in the input become the X in the output

		// returns and interval of the observation
		LinearPieceWiseFunction intervalFunction = new LinearPiecewiseFunctionImpl();
		intervalFunction.addPoint(leftXpoint, truthValue.getFunctionValueAt(leftXpoint));
		intervalFunction.addPoint(rightXpoint, truthValue.getFunctionValueAt(rightXpoint));
		Logger.debug("Method calculateResultInInterval: Calculated interval function is {}", intervalFunction);
		LinearPieceWiseFunction inverseIntervalFunction = intervalFunction.getInverse();
		Logger.debug("Method calculateResultInInterval: Calculated inverse of interval function is {}",
				inverseIntervalFunction);

		// Calculate B(y)
		LinearPieceWiseFunctionDataPoints result = new LinearPieceWiseFunctionDataPoints();

		// Case y=0;
		if (DoubleMath.fuzzyEquals(minY, 0, LinearPieceWiseFunction.TOLERANCE)) {
			Logger.debug("Case Min y=0");
			result.add(new ImmutablePair<Double, Double>(minY,
					getFullDisatisfactionValueDependingOnCase(observation, leftXpoint, rightXpoint)));
		} else {
			// Apply B(y)= O(truthValue^(−1)(y))
			Logger.debug("Case Min NOT y=0");
			result.add(new ImmutablePair<Double, Double>(minY,
					observation.getFunctionValueAt(inverseIntervalFunction.getValueAt(minY))));
		}
		if (DoubleMath.fuzzyEquals(maxY, 1, LinearPieceWiseFunction.TOLERANCE)) {
			// case y=1
			Logger.debug("Case Max y=1");
			result.add(new ImmutablePair<Double, Double>(maxY,
					getFullsatisfactionValueDependingOnCase(observation, leftXpoint, rightXpoint)));
		} else {// Apply B(y)= O(truthValue^(−1)(y))
			Logger.debug("Case Max NOT y=1");
			result.add(new ImmutablePair<Double, Double>(maxY,
					observation.getFunctionValueAt(inverseIntervalFunction.getValueAt(maxY))));
		}

		Logger.debug("The result function is {}", result.toString());
		return result;

	}

	private Double getFullsatisfactionValueDependingOnCase(FuzzyNumber observation, double leftXpoint,
			double rightXpoint) throws FunctionOperationException {
		
		switch (type) {
		case LB:
			return observation.getFunctionValueAt(rightXpoint);
		case UB:
			return observation.getFunctionValueAt(leftXpoint);
		default: // ( INT, MIN, MAX)
			throw new UnsupportedOperationException();
		}
	}

	private Double getFullDisatisfactionValueDependingOnCase(FuzzyNumber observation, double leftXpoint,
			double rightXpoint) throws FunctionOperationException {

		switch (type) {
		case LB:
			return observation.getFunctionValueAt(leftXpoint);
		case UB:
			return observation.getFunctionValueAt(rightXpoint);
		default: // ( INT, MIN, MAX)
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public GoalType getType() {
		throw new UnsupportedOperationException("Operation exclusive for intermediate nodes. This is a leaf goal");
	}

	@Override
	public List<Goal> getChildren() {
		throw new UnsupportedOperationException("Operation exclusive for intermediate nodes. This is a leaf goal");
	}

	@Override
	public void addChild(Goal child) {
		throw new UnsupportedOperationException("Operation exclusive for intermediate nodes. This is a leaf goal");

	}

	@Override
	public FuzzyBoolean assessSatisfaction() throws FunctionOperationException {
		if (lastObservation == null) {
			throw new FunctionOperationException(
					"Impossible to assessSatisfactio() without parameters because the observation null");
		}
		return assessSatisfactionFromObservation(lastObservation);
	}

	@Override
	public void setObservation(FuzzyNumber observation) {
		lastObservation = observation;

	}

	public String getName() {
		return name;
	}





}
