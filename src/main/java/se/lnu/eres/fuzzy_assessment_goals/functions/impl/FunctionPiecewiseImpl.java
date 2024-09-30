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
package se.lnu.eres.fuzzy_assessment_goals.functions.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.math.DoubleMath;

import se.lnu.eres.fuzzy_assessment_goals.functions.FuzzyNumber;
import se.lnu.eres.fuzzy_assessment_goals.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FuzzyNumberConversionException;

public class FunctionPiecewiseImpl implements LinearPieceWiseFunction {

	private static final Logger Logger = LogManager.getLogger(FunctionPiecewiseImpl.class.getSimpleName());

	protected LinearPieceWiseFunctionDataPoints points;

	public FunctionPiecewiseImpl() {
		points = new LinearPieceWiseFunctionDataPoints();
	}

	public FunctionPiecewiseImpl(LinearPieceWiseFunctionDataPoints points) {
		super();
		this.points = points;
	}

	@Override
	public void addPoint(double x, double y) {
		points.add(new ImmutablePair<Double, Double>(x, y));
		sortLastPoint();
	}

	private void sortLastPoint() {
		// Only last point can be out of order
		if (points.size() > 1) {
			if (points.get(points.size() - 1).getLeft() < points.get(points.size() - 2).getLeft()) {
				// it is out of order. It needs to sort
				ImmutablePair<Double, Double> lastElement = points.remove(points.size() - 1);
				points.add(findSortedIndexInPoints(lastElement.getLeft(), 0, points.size() - 1), lastElement);
			}
		}

	}

	private int findSortedIndexInPoints(double value, int start, int end) {

		if (start == end) {
			return start;
		}
		// Binary search a in the sorted array
		if (points.get((start + end) / 2).getLeft() > value) {// it is on the left side
			return findSortedIndexInPoints(value, start, ((start + end) / 2));
		} else { // it is on the right side
			return findSortedIndexInPoints(value, ((start + end) / 2) + 1, end);
		}
	}

	@Override
	public FuzzyNumber getFuzzyNumber() throws FuzzyNumberConversionException {
		if (FuzzyNumberImpl.IsFuzzyNumber(this)) {
			return new FuzzyNumberImpl(this);
		}
		Logger.debug(
				"Trying to converte a PieceWise function to a Fuzzy number, but the function does not satisfy the Fuzzy Number characteristics {}",
				points.toString());
		throw new FuzzyNumberConversionException("Function does not correspond to fuzzy number" + points.toString());
	}

	@Override
	public boolean isFuzzyNumber() {
		return FuzzyNumberImpl.IsFuzzyNumber(this);
	}

	@Override
	public boolean monotonicallyDecreasingFromTopValue() {
		if (points.size() < 2) {
			return true;
		}
		ImmutablePair<Double, Double> previousp = null;
		boolean topReached = false;
		for (ImmutablePair<Double, Double> p : points) {
			if (previousp == null) {
				previousp = p;
			} else {

				if (!topReached) {
					if (previousp.getRight() >= 1.0) {
						topReached = true;
					} else {
						previousp = p;
					}
				} else {// top was already reached
					if (previousp.getRight() < p.getRight()) {
						// not decreasing!
						return false;
					} else {
						previousp = p;
					}
				}

			}
		}
		return true;

	}

	@Override
	public boolean monotonicallyIncreasingUntilReachingTopValue() {
		if (points.size() < 2) {
			return true;
		}
		ImmutablePair<Double, Double> previousp = null;
		for (ImmutablePair<Double, Double> p : points) {
			if (previousp == null) {
				previousp = p;
			} else {
				if (p.getRight() < 1.0) {// still increasing
					if (previousp.getRight() > p.getRight()) {
						return false;
					}
					previousp = p;
				} else {
					return true;
				}

			}

		}

		// It should not reach here because there should exist a position with value 1!
		Logger.warn(
				"The execution should not have reached this point. A Funtion has been traversed and it has not reached any position x such that f(x)>=1. The function is {}",
				points.toString());
		return true;
	}

	@Override
	public double maximumValueInPoints() {
		// the maximum must correspond to one of the piece extremes of the function.
		double currentMax = 0.0;
		for (ImmutablePair<Double, Double> p : points) {
			if (p.getRight() > currentMax) {
				currentMax = p.getRight();
			}
		}
		return currentMax;
	}

	@Override
	public double minimumValueInPoints() {
		// the minimum must correspond to one of the piece extremes of the function.
		double currentMin = 1.0;
		for (ImmutablePair<Double, Double> p : points) {
			if (p.getRight() < currentMin) {
				currentMin = p.getRight();
			}
		}
		return currentMin;
	}

	@Override
	public boolean isMonotonicallyIncreasing() {
		if (points.size() < 2) {
			return true;
		}
		ImmutablePair<Double, Double> previousp = null;
		for (ImmutablePair<Double, Double> p : points) {
			if (previousp == null) {
				previousp = p;
			} else {
				if (previousp.getRight() > p.getRight()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isMonotonicallyDecreasing() {
		if (points.size() < 2) {
			return true;
		}
		ImmutablePair<Double, Double> previousp = null;
		for (ImmutablePair<Double, Double> p : points) {
			if (previousp == null) {
				previousp = p;
			} else {
				if (previousp.getRight() < p.getRight()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public LinearPieceWiseFunctionDataPoints getDatapoints() {
		return points;
	}

	@Override
	public List<Double> getLimitXpoints() {
		return points.getXpoints();
	}

	@Override
	public Double getValueAt(double leftXpoint) throws FunctionOperationException {
		Logger.debug("Looking for value at {} in function {}", leftXpoint, points);
		LinearPieceWiseFunctionDataPoints interval = points.getIntervalContaining(leftXpoint);
		Logger.debug("The interval of interest is {}", interval);
		return GetY(interval, leftXpoint);

	}

	@Override
	public List<Double> getValuesAt(double xpoint) throws FunctionOperationException {
		Logger.debug("Looking for values at {} in function {}", xpoint, points);
		if(isDiscontinuousAtX(xpoint)) {
			return getDiscontinuousYs(xpoint);
		}
		else {
			Logger.debug("It is not a discontinuity point");
			return Arrays.asList(getValueAt(xpoint));
		}
		
	}
	


	private static Double GetY(LinearPieceWiseFunctionDataPoints interval, double leftXpoint) {

		Logger.debug("getY: finding f({}) in interval {}. The x values x1 and x2 are: <{},{}>", leftXpoint, interval,
				interval.getFirst().getLeft(), interval.getLast().getLeft());
		/*
		 * Handle the special case that is the discontinuity. The derivative would give
		 * infinite. In tat case, it is assumed that the Y value on the right (the one
		 * that will continue the function for) larger X values, is used.
		 */
		if (interval.getFirst().getLeft().equals(interval.getLast().getLeft())) {
			Logger.debug("getY: the f({}) in interval {} is {} (interval had 0 lenght)", leftXpoint, interval,
					interval.getLast().getRight());
			return interval.getLast().getRight();
		}

		// Now the normal case
		double leftX = interval.getFirst().getLeft();
		double leftY = interval.getFirst().getRight();
		double rightX = interval.getLast().getLeft();
		double rightY = interval.getLast().getRight();
		double result = leftY + (leftXpoint - leftX) * ((rightY - leftY) / (rightX - leftX));
		Logger.debug("getY: the f({}) in interval {} is {}", leftXpoint, interval, result);
		return result;
	}
	
	
	/**
	 * The list of Y in case that it is a discontinuity point. It cannot be in the middle of an interval, it must be in the extremes. 
	 * @param xpoint
	 * @return
	 */
	private List<Double> getDiscontinuousYs(double xpoint) {
		List<Double> result = new ArrayList<Double>();
		for (ImmutablePair<Double, Double> point : points) {
			if(DoubleMath.fuzzyEquals(point.getLeft(), xpoint, TOLERANCE)) {
				result.add(point.getRight());
			}
		}
		return result;
			
	}

	@Override
	public LinearPieceWiseFunction getInverse() throws UnsupportedOperationException {
		if (points.size() > 2) {
			throw new UnsupportedOperationException(
					"This method only works for liinear functions defined by two points (x1,y1), (x2,y2)");
		}
		LinearPieceWiseFunction result = new FunctionPiecewiseImpl();
		if (points.getLast().getRight() >= points.getFirst().getRight()) {
			// non decreasing function (x1,y1),(x2,y2) -> returns (y1,x1),(y2,x2)
			result.addPoint(points.getFirst().getRight(), points.getFirst().getLeft());
			result.addPoint(points.getLast().getRight(), points.getLast().getLeft());
		} else {
			// decreasing function (x1,y1),(x2,y2) -> returns (y2,x2), (y1,x1)
			result.addPoint(points.getLast().getRight(), points.getLast().getLeft());
			result.addPoint(points.getFirst().getRight(), points.getFirst().getLeft());

		}
		return result;

	}

	@Override
	public String toString() {
		return "FunctionPiecewiseImpl [points=" + points.toString() + "]";
	}

	@Override
	public List<Double> findIntersections(LinearPieceWiseFunction f2) throws FunctionOperationException {

		ImmutablePair<Double, Double> f1left, f1right;
		Double leftXforf2, rightXforf2;
		List<Double> result = new ArrayList<Double>();
		if (points.size() == 0 || f2.getDatapoints().size() == 0 || points.size() + f2.getDatapoints().size() < 3) {
			// No points of intersection possible
			return result;
		}
		// special case that this function has only one point and it is on the line of
		// the other function
		if (points.size() == 1) {
			if (f2.getValueAt(points.getFirst().getLeft()) == points.getFirst().getRight()) {
				result.add(points.getFirst().getLeft());
			}
			return result;
		}

		// At this point the current function has at least two points
		// Get x-points of interest from the points of interest of the two fuzzy
		// booleans
		List<Double> xPointsOfInterest = CollectionUtils.collate(getLimitXpoints(), f2.getLimitXpoints(), false);
		Logger.info("The xPointsOfInterest merged are: {}", xPointsOfInterest.toString());
		f1left = points.getFirst();
		leftXforf2 = xPointsOfInterest.removeFirst();
		rightXforf2 = xPointsOfInterest.removeFirst();
		int i = 1;
		while (i < points.size() && xPointsOfInterest.size() >= 0) {
			f1right = points.get(i);

			// Maybe there are more intersections
			//leftXforf2 = rightXforf2;
			//rightXforf2 = xPointsOfInterest.removeFirst();

			// Maybe we are in one fo the

			Logger.info(
					"finding intersection betwen <x1,y1>={}, <x2,y2>={}, leftXforIntervalInF2={} and rightXforIntervalInF2={}",
					f1left, f1right, leftXforf2, rightXforf2);
			if (f2.existsIntersetionBetween(leftXforf2, rightXforf2, f1left, f1right)) {
				Logger.info("Yes, they intersect, now calculating the intersection point");
				// Possibly adding a duplicate if the intersection point corresponds with one
				// of the picewise function points because it will be added when exploring the
				// intervals before and after the point.
				result.add(f2.getIntersectionPointX(leftXforf2, rightXforf2, f1left, f1right));
				Logger.info("  And the intersection point was {}", result.getLast());

			} else {
				Logger.info("No, they did not intersect");
			}

			if (rightXforf2 >= f1right.getLeft()) { // if we reach that the rightXforf2 is out of our f1 interval,
													// advance the
				// interval in f1
				Logger.info(
						"It is moment to advance to the next interval in f1 because rightXforf2={} and f1right.getLeft()={}",
						rightXforf2, f1right.getLeft());
				f1left = f1right;
				i++;
			} else {// advance interval in f2
					// This is to avoid that the interval in f2 is advanced always because a new
					// interval in f1 may match the current interval in f2
				Logger.info(
						"It is moment to advance to the next interval in f2 because rightXforf2={} and f1right.getLeft()={}",rightXforf2, f1right.getLeft());
				leftXforf2 = rightXforf2;
				rightXforf2 = xPointsOfInterest.removeFirst();
			}

		}
		return result;
	}

	@Override
	public boolean existsIntersetionBetween(Double left, Double right, ImmutablePair<Double, Double> f2left,
			ImmutablePair<Double, Double> f2right) throws FunctionOperationException {
		// Calculate whether it exists an effective interval for intersection. The
		// maximum of lefts and miinimum of rights X
		double effectiveXleft = Math.max(left, f2left.getLeft());
		double effectiveXright = Math.min(right, f2right.getLeft());

		// If the intervals are disjoint, there is no intersection
		if (effectiveXright < effectiveXleft) {
			return false;
		}

		// If the interavl contains a single point, it is a more difficult case because
		// it could be a discontinuity point in any of the intervals, "this" or the
		// interval in the argument points. Or it could be also the only intersection
		// point.
		if (effectiveXright == effectiveXleft) {
			if (left.equals(right) || (f2left.getLeft().equals(f2right.getLeft()))) {
				return handleIntervalExistenceInSinglePointInterval(effectiveXleft, f2left, f2right);
			} else {
				// legit intersection in a single point, no discontinuity of one of the
				// functions
				LinearPieceWiseFunctionDataPoints f2 = new LinearPieceWiseFunctionDataPoints(f2left, f2right);
				return DoubleMath.fuzzyEquals(getValueAt(effectiveXright), GetY(f2, effectiveXright),
						LinearPieceWiseFunction.TOLERANCE);
			}
		}

		// Now the intervals are not disjoint.
		// To intersect at some point, either the effectiveLeftY of this is lower and
		// the effectiveRightY
		// is larger or the leftY of this is larger and the rightY is lower than the
		// interval formed by the points passed.
		LinearPieceWiseFunctionDataPoints f2 = new LinearPieceWiseFunctionDataPoints(f2left, f2right);

		// If the left is larger and the right lower
		if (getValueAt(effectiveXleft) >= GetY(f2, effectiveXleft)
				&& getValueAt(effectiveXright) <= GetY(f2, effectiveXright)) {
			return true;
		}

		// If the left is lower and the right larger
		if (getValueAt(effectiveXleft) <= GetY(f2, effectiveXleft)
				&& getValueAt(effectiveXright) >= GetY(f2, effectiveXright)) {
			return true;
		}

		return false;
	}

	private boolean handleIntervalExistenceInSinglePointInterval(double effectiveX,
			ImmutablePair<Double, Double> f2left, ImmutablePair<Double, Double> f2right)
			throws FunctionOperationException {
		if (isDiscontinuousAtX(effectiveX)) {
			double minYDiscontinuous = getMinYinDiscontinuityAtX(effectiveX);
			double maxYDiscontinuous = getMaxYinDiscontinuityAtX(effectiveX);
			double f2YatEffectiveX = GetY(new LinearPieceWiseFunctionDataPoints(f2left, f2right), effectiveX);
			// true if the f2YatTheEffectiveX is between the minimum and maximum
			return f2YatEffectiveX >= minYDiscontinuous && f2YatEffectiveX <= maxYDiscontinuous;
		} else {// the discontinuous is the f2left f2right
			double minYDiscontinuous = Math.min(f2left.getRight(), f2right.getRight());
			double maxYDiscontinuous = Math.max(f2left.getRight(), f2right.getRight());
			double yAtEffectiveX = getValueAt(effectiveX);
			return yAtEffectiveX >= minYDiscontinuous && yAtEffectiveX <= maxYDiscontinuous;

		}
	}

	private double getMaxYinDiscontinuityAtX(double xvalue) {
		double maxYforX = -Double.MAX_VALUE;
		for (ImmutablePair<Double, Double> point : points) {
			if (point.getLeft() == xvalue && point.getRight() > maxYforX) {
				maxYforX = point.getRight();
			}
		}
		return maxYforX;
	}

	private double getMinYinDiscontinuityAtX(double xvalue) {
		double minYforX = Double.MAX_VALUE;
		for (ImmutablePair<Double, Double> point : points) {
			if (point.getLeft() == xvalue && point.getRight() < minYforX) {
				minYforX = point.getRight();
			}
		}
		return minYforX;

	}

	private boolean isDiscontinuousAtX(double xvalue) {
		boolean firstAlreadyFound = false;
		for (ImmutablePair<Double, Double> point : points) {
			if (DoubleMath.fuzzyEquals(point.getLeft(),xvalue,TOLERANCE)) {
				if (firstAlreadyFound) {
					return true;
				}
				firstAlreadyFound = true;
			}

		}
		return false;
	}

	@Override
	public Double getIntersectionPointX(Double x1, Double x2, ImmutablePair<Double, Double> fleft,
			ImmutablePair<Double, Double> fright) throws FunctionOperationException {
		// TODO find the intersection point where two linear functions for which we have
		// two coordinates (x1,y1) and (x2,y2).
		/*
		 * Follow the formula that, the function in y=Ax+b is ((y2-y1)/(x2-x1))*x +
		 * y1/(x1(y2-y1)/(x2-x1))
		 */
		Double y1, y2, xr, xl, yr, yl;
		y1 = getValueAt(x1);
		y2 = getValueAt(x2);
		xl = fleft.getLeft();
		yl = fleft.getRight();
		xr = fright.getLeft();
		yr = fright.getRight();
		Logger.info("Calculating intersection of <x1,y1>=<{},{}> <x2,y2>=<{},{}>, <xl,yl>=<{},{}> ,<xr,yr>=<{},{}>", x1,
				y1, x2, y2, xl, yl, xr, yr);

		double slope1 = (y2 - y1) / (x2 - x1);
		double slope2 = (yr - yl) / (xr - xl);

		Logger.info("The slope of functions is: Slope1={} and Slope2={}", slope1, slope2);

		// This works if x1 and xl are different from 0. Otherwise the "b" in y1=Ax1+b
		// is directly equal to the "y1"
		double b1, b2 = 0;
		if (x1 == 0.0) {
			b1 = y1;
		} else {
			b1 = y1 - (x1 * slope1);
		}

		if (xl == 0.0) {
			b2 = yl;
		} else {
			b2 = yl - (xl * slope2);
		}

		double numerator = b1 - b2;

		// This can be 0 if both lines are exactly one over the other. Not impossible.
		double denominator = slope2 - slope1;
		// If the lines are exactly one over the other, return the rightmost x
		Logger.info("The calculated b are b1={} b2={}, numerator={}, and denominator={} ", b1, b2, numerator,
				denominator);
		if (denominator != 0.0) {
			return (numerator / denominator);
		} else {
			return x2;
		}
	}

	@Override
	public double getLargestValueAfterX(double p) throws FunctionOperationException {
		double max = getValueAt(p);
		for (ImmutablePair<Double, Double> point : points) {
			if (point.getLeft() > p) {
				// The x value is eligible
				if (point.getRight() > max) {
					max = point.getRight();
				}
			}
		}
		return max;

	}

	@Override
	public void simplifyPiecewiseFunction() throws FunctionOperationException {
		points.removeIntermediatePoitnsForLinearFunctions();

	}

	@Override
	public double getLargestValueBeforeX(double p) throws FunctionOperationException {
		double max = getValueAt(p);
		for (ImmutablePair<Double, Double> point : points) {
			if (point.getLeft() < p) {
				// The x value is eligible
				if (point.getRight() > max) {
					max = point.getRight();
				}
			} else {
				// Assuming that the function has the points sorted,
				// Once the x is too large, it will be too large also in the next points
				return max;
			}
		}
		return max;
	}



}
