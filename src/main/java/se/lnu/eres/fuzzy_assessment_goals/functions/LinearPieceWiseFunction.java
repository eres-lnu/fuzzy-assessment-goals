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

import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FuzzyNumberConversionException;

public interface LinearPieceWiseFunction extends FuzzyNumberCheck{

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

	Double getValueAt(double leftXpoint) throws FunctionOperationException;
}
