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
package se.lnu.eres.fuzzy.functions.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.functions.exceptions.FuzzyOperationException;


public class FuzzyBooleanImpl extends FuzzyNumberImpl implements FuzzyBoolean {

	protected static final Logger Logger =  LogManager.getLogger(FuzzyNumberImpl.class.getSimpleName());
	
	public FuzzyBooleanImpl(LinearPieceWiseFunction function) {
		super(function);
	}

	public FuzzyBooleanImpl() {
		super();
	}

	@Override
	public boolean isFuzzyBoolean() throws FuzzyOperationException, FunctionOperationException {
		ImmutablePair<Double, Double> support = getSupport();
		if (support.getLeft() < 0) {
			return false;
		}
		if (support.getRight() > 1) {
			return false;
		}

		// The following should never happen
		if (support.getLeft() > support.getRight()) {
			return false;
		}

		return true;

	}

	@Override
	public String toString() {
		return "AbstractFuzzyBoolean [function=" + function + "]";
	}

	@Override
	public FuzzyBoolean sanitize() throws FunctionOperationException {
		LinearPieceWiseFunction newFunction = new LinearPiecewiseFunctionImpl();

		// Get the support and if it is less than [0,1], add points.
		ImmutablePair<Double, Double> support = getFunction().getDefinedDomain();
		
		if (support.getLeft() > 0) {
			Logger.info("Left part of support larger than 0. It is: {}", support.getLeft());
			// Points to add because the domain is too narrow.
			// If the value at the support is larger than 0, then there are two points to
			// add:
			// One at the current support with value 0 and another at the (0,0)
			newFunction.addPoint(0, 0);
			if (!getFunctionValuesAt(support.getLeft()).contains(Double.valueOf(0))) {
				//Add also the <x,y> such that x=leftSuppport and y=0
				newFunction.addPoint(support.getLeft(), 0);
			}
			
		}
		
		//Add the rest of the points in the original
		for(ImmutablePair<Double, Double> point : getFunction().getDatapoints()) {
			newFunction.addPoint(point.getLeft(), point.getRight());
		}
		
		if(support.getRight()<1.0) {
			//Points to add becasue the domain did not finish at 1.0.
			
			if(!getFunctionValuesAt(support.getRight()).contains(Double.valueOf(0))) {
				//Add also the value 0 to the x of the prevoius support
				newFunction.addPoint(support.getRight(), 0);
			}
			
			newFunction.addPoint(1, 0);
		}
		return new FuzzyBooleanImpl(newFunction);
	}

}
