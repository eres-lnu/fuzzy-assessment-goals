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

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FuzzyOperationException;
import se.lnu.eres.fuzzy_assessment_goals.functions.FuzzyNumber;

public class FuzzyNumberImpl extends AbstractFuzzyNumber implements FuzzyNumber {

	private static final Logger Logger = LogManager.getLogger(FuzzyNumberImpl.class.getSimpleName());

	protected List<ImmutablePair<Double, Double>> points;

	public FuzzyNumberImpl() {
		super();
	}

	public FuzzyNumberImpl(List<ImmutablePair<Double, Double>> points) {
		super();
		this.points = points;
	}

	static public boolean IsFuzzyNumber(List<ImmutablePair<Double, Double>> points) {

		return (new FuzzyNumberImpl(points)).isFuzzyNumber();

	}

	@Override
	protected boolean monotonicallyDecreasingFromTopValue() {
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
	protected boolean monotonicallyIncreasingUntilReachingTopValue() {
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
	protected double maximumValueInPoints() {
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
	protected double minimumValueInPoints() {
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
	public ImmutablePair<Double, Double> getSupport() throws FuzzyOperationException {
		// The left part is the last element in points that is 0 before increasing
		// The right part is the first element in porints that is 0 after decreasing
		ImmutablePair<Double, Double> previousp = null;
		Double supportLeft = -Double.MAX_VALUE, supportRight;
		for (ImmutablePair<Double, Double> p : points) {
			if (previousp == null) {
				previousp = p;
			} else {
				// Looking for the left part
				if (previousp.getRight() == 0 && p.getRight() > 0) {
					supportLeft = previousp.getLeft();
				}
				// Looking for the right part
				if (previousp.getRight() > 0 && p.getRight() == 0) {
					supportRight = p.getLeft();
					return new ImmutablePair<Double, Double>(supportLeft, supportRight);
				}
				previousp = p;
			}
		}

		throw new FuzzyOperationException("There was not found Support for the fuzzy number " + points.toString());

	}

	@Override
	public ImmutablePair<Double, Double> getCore() throws FuzzyOperationException {
		// The left part is the first element in points that is 1
		// The right part is the last element in points that is 1
		ImmutablePair<Double, Double> previousp = null;
		Double coreLeft = -Double.MAX_VALUE, coreRight;
		for (ImmutablePair<Double, Double> p : points) {
			if (previousp == null) {
				previousp = p;
			} else {
				// Looking for the left part
				if (p.getRight() == 1 && previousp.getRight() < 1) {
					coreLeft = p.getLeft();
				}
				// Looking for the right part
				if (p.getRight() < 1 && previousp.getRight() == 1) {
					coreRight = previousp.getLeft();
					return new ImmutablePair<Double, Double>(coreLeft, coreRight);
				}
				previousp = p;
			}

		}
		throw new FuzzyOperationException("There was not found Core for the fuzzy number " + points.toString());
	}

}
