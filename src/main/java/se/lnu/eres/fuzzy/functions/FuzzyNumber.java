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
package se.lnu.eres.fuzzy.functions;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.functions.exceptions.FuzzyOperationException;

public interface FuzzyNumber extends FuzzyNumberCheck {

	ImmutablePair<Double, Double> getSupport() throws FuzzyOperationException;

	ImmutablePair<Double, Double> getCore() throws FuzzyOperationException;

	LinearPieceWiseFunction getFunction();

	void setFunction(LinearPieceWiseFunction function);

	Double getFunctionValueAt(double leftXpoint) throws FunctionOperationException;

	/**
	 * This method allows discontinuities on point x where the y value is different
	 * when the x is approached left, right, or exactly the point.
	 * 
	 * @param leftXpoint
	 * @return The points y for y=f(x)
	 * @throws FunctionOperationException
	 */
	List<Double> getFunctionValuesAt(double leftXpoint) throws FunctionOperationException;

	double getLargestValueAfterX(double p, boolean strictAfterX) throws FunctionOperationException;

	double getLargestValueBeforX(double p) throws FunctionOperationException;

}
