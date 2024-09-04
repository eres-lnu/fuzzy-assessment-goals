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
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		return getY(interval, leftXpoint);

	}

	private Double getY(LinearPieceWiseFunctionDataPoints interval, double leftXpoint) {

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
		rightXforf2 = xPointsOfInterest.removeFirst();
		int i = 1;
		while (i < points.size() &&xPointsOfInterest.size()>0) {
			f1right = points.get(i);
			
			// Maybe there are more intersections
			leftXforf2 = rightXforf2;
			rightXforf2 = xPointsOfInterest.removeFirst();


			
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
			
			
			// if we reach that the rightXforf2 is out of our f1 interval, advance the
			// interval in f1

			if (rightXforf2 >= f1right.getLeft()) {
				Logger.info(
						"It is moment to advance to the next interval in f1 because rightXforf2={} and f1right.getLeft()={}",
						rightXforf2, f1right.getLeft());
				f1left = f1right;
				i++;
			}

		}
		return result;
	}

	@Override
	public boolean existsIntersetionBetween(Double left, Double right, ImmutablePair<Double, Double> f1left,
			ImmutablePair<Double, Double> f1right) throws FunctionOperationException {
		// To intersect at some point, either the leftY of this is lower and the rightY
		// is larger or the leftY of this is larger and the rightY is lower than the
		// points passed.

		// If the left is larger and the right lower
		if (getValueAt(left) >= f1left.getRight() && getValueAt(right) <= f1right.getRight()) {
			return true;
		}

		// If the left is lower and the right larger
		if (getValueAt(left) <= f1left.getRight() && getValueAt(right) >= f1right.getRight()) {
			return true;
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
		Logger.info("Calculating intersection of <x1,y1>=<{},{}> <x2,y2>=<{},{}>, <xl,yl>=<{},{}> ,<xr,yr>=<{},{}>", x1, y1, x2, y2, xl, yl, xr, yr);

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
		Logger.info("The calculated b are b1={} b2={}, numerator={}, and denominator={} ", b1, b2, numerator,denominator);
		if (denominator != 0.0) {
			return (numerator / denominator);
		} else {
			return x2;
		}
	}

	@Override
	public double getLargestValueAfterX(double p) throws FunctionOperationException {
		double max= getValueAt(p);
		for(ImmutablePair<Double,Double> point : points) {
			if(point.getLeft()>p) {
				//The x value is eligible
				if(point.getRight()>max) {
					max=point.getRight();
				}
			}
		}
		return max;
		
	}

	@Override
	public void simplifyPiecewiseFunction() throws FunctionOperationException {
		points.removeIntermediatePoitnsForLinearFunctions();
		
	}

}
