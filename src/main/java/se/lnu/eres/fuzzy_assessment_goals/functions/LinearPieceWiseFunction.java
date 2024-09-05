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

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FuzzyNumberConversionException;
import se.lnu.eres.fuzzy_assessment_goals.functions.impl.LinearPieceWiseFunctionDataPoints;

public interface LinearPieceWiseFunction extends FuzzyNumberCheck {

	public static final double TOLERANCE = 0.0001;

	void addPoint(double x, double y);

	FuzzyNumber getFuzzyNumber() throws FuzzyNumberConversionException;

	double maximumValueInPoints();

	double minimumValueInPoints();

	boolean monotonicallyIncreasingUntilReachingTopValue();

	boolean monotonicallyDecreasingFromTopValue();

	/**
	 * @return whether the function is monotonically increasing
	 */
	boolean isMonotonicallyIncreasing();

	default boolean isMonotonicallyDecreasing() {
		throw new UnsupportedOperationException();
	}

	LinearPieceWiseFunctionDataPoints getDatapoints();

	List<Double> getLimitXpoints();

	Double getValueAt(double x) throws FunctionOperationException;

	LinearPieceWiseFunction getInverse() throws UnsupportedOperationException;

	List<Double> findIntersections(LinearPieceWiseFunction function) throws FunctionOperationException;

	/**
	 * Given a sequence of points in the function <x1,y1>,<x2,y2>,<x3,y3>... this
	 * function removes the datapoints <x_{i+1},y{i+1}> that satisfy
	 * f(x_{i+1})=y_{i+1} begin f() the linear function created by points
	 * <xi,yi>,<x_{i+2},y_{i+2}> that are between x1 and x3
	 * @throws FunctionOperationException 
	 */
	void simplifyPiecewiseFunction() throws FunctionOperationException;

	Double getIntersectionPointX(Double leftLocal, Double rightLocal, ImmutablePair<Double, Double> fleft,
			ImmutablePair<Double, Double> fright) throws FunctionOperationException;

	boolean existsIntersetionBetween(Double leftXforf2, Double rightXforf2, ImmutablePair<Double, Double> f1left,
			ImmutablePair<Double, Double> f1right) throws FunctionOperationException;

	double getLargestValueAfterX(double p) throws FunctionOperationException;

	double getLargestValueBeforeX(double p) throws FunctionOperationException;
}
