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
package se.lnu.eres.fuzzy_assessment_goals.functions;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class FunctionPiecewiseImpl implements LinearPieceWiseFunction, FuzzyNumber {

	private static final Logger Logger = LogManager.getLogger(FunctionPiecewiseImpl.class.getSimpleName());

	private List<ImmutablePair<Double, Double>> points;

	public FunctionPiecewiseImpl() {
		points = new ArrayList<ImmutablePair<Double, Double>>();
	}

	@Override
	public boolean isFuzzyNumber() {

		// Condition 1: top y is 1.
		if (maximumValueInPoints() != 1.0) {
			return false;
		}

		// Condition 2: Bottom is 0
		if (minimumValueInPoints() != 0.0) {
			return false;
		}

		// Condition 3: monotonically increasing from [a,b]
		if (!monotonicallyIncreasingUntilReachingTopValue()) {
			return false;
		}

		// Condition 4: monotonically decreasing from [c,d]
		return (monotonicallyDecreasingFromTopValue());
	}

	private boolean monotonicallyDecreasingFromTopValue() {
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

	private boolean monotonicallyIncreasingUntilReachingTopValue() {
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

	private double maximumValueInPoints() {
		// the maximum must correspond to one of the piece extremes of the function.
		double currentMax = 0.0;
		for (ImmutablePair<Double, Double> p : points) {
			if (p.getRight() > currentMax) {
				currentMax = p.getRight();
			}
		}
		return currentMax;
	}

	private double minimumValueInPoints() {
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

	private boolean isPointsSorted() {
		if (points.size() <= 1) {
			return true;
		}

		for (int i = 0; i < points.size() - 1; i++) {
			if (points.get(i).getLeft() > points.get(i + 1).getLeft()) {
				return false;
			}
		}
		return true;

	}
	
	

	@Override
	public String toString() {
		return "FunctionPiecewiseImpl [points=" + points.toString() + "]";
	}

	@Override
	public FuzzyNumber getFuzzyNumber() {
		return this;
	}

}
