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


import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.lnu.eres.fuzzy.functions.FuzzyNumber;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.functions.exceptions.FuzzyOperationException;

public class FuzzyNumberImpl extends AbstractFuzzyNumber implements FuzzyNumber {

	private static final Logger Logger = LogManager.getLogger(FuzzyNumberImpl.class.getSimpleName());



	public FuzzyNumberImpl() {
		super();
	}

	public FuzzyNumberImpl(LinearPieceWiseFunction function) {
		super(function);

	}


	@Override
	public void setFunction(LinearPieceWiseFunction function) {
		this.function=function;
		
	}
	
	static public boolean IsFuzzyNumber(LinearPieceWiseFunction function) {

		return (new FuzzyNumberImpl(function)).isFuzzyNumber();

	}


	@Override
	public ImmutablePair<Double, Double> getSupport() {
		//TODO: Extend in case that the fuzzy number should use something different the pieceWise
		LinearPieceWiseFunctionDataPoints points = function.getDatapoints();
		// The left part is the last element in points that is 0 before increasing
		// The right part is the first element in prints that is 0 after decreasing
		ImmutablePair<Double, Double> previousp = null;
		Double supportLeft = points.getFirst().getLeft();
		Double supportRight = points.getLast().getLeft();
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

		return new ImmutablePair<Double, Double>(supportLeft, supportRight);

	}

	@Override
	public ImmutablePair<Double, Double> getCore() throws FuzzyOperationException {
		//TODO: Extend in case that the fuzzy number should use something different the pieceWise
		LinearPieceWiseFunctionDataPoints points = function.getDatapoints();
		// The left part is the first element in points that is 1
		// The right part is the last element in points that is 1
		ImmutablePair<Double, Double> previousp = null;
		Double coreLeft = -Double.MAX_VALUE, coreRight = Double.MAX_VALUE;
		boolean definedCoreLeft = false, definedCoreRight = false;

		for (ImmutablePair<Double, Double> p : points) {
			if (previousp == null) {
				if (p.getRight() == 1) {
					coreLeft = p.getLeft();
					definedCoreLeft = true;
				}
				previousp = p;
			} else {
				// Looking for the left part
				if (p.getRight() == 1 && previousp.getRight() < 1) {
					coreLeft = p.getLeft();
					definedCoreLeft = true;
				}
				// Looking for the right part
				if (p.getRight() < 1 && previousp.getRight() == 1) {
					coreRight = previousp.getLeft();
					definedCoreRight = true;

				}
				previousp = p;
			}

		}
		// Maybe it finished with 1: FuzzBool(1)=1
		if (points.getLast().getRight() == 1) {
			coreRight = points.getLast().getLeft();
			definedCoreRight = true;
		}

		if (definedCoreRight && definedCoreLeft) {
			return new ImmutablePair<Double, Double>(coreLeft, coreRight);
		}

		Logger.warn("There was not found a Core for the following fuzzy number {}", points.toString());
		throw new FuzzyOperationException("There was not found Core for the fuzzy number " + points.toString());
	}

	@Override
	public LinearPieceWiseFunction getFunction() {
		return function;
	}

	@Override
	public Double getFunctionValueAt(double leftXpoint) throws FunctionOperationException {
		return function.getValueAt(leftXpoint);
	}
	
	@Override
	public List<Double> getFunctionValuesAt(double xpoint) throws FunctionOperationException {
	
		return function.getValuesAt(xpoint);
	}

	@Override
	public double getLargestValueAfterX(double p, boolean approachFromLeft) throws FunctionOperationException {
		return function.getLargestValueAfterX(p,approachFromLeft);
	}

	
	@Override
	public double getLargestValueBeforX(double p, boolean approachFromLeft) throws FunctionOperationException {
		return function.getLargestValueBeforeX(p, approachFromLeft);
	}

	@Override
	public double getLargestValueBetween(double leftXpoint, double rightXpoint, boolean extremesDiscarded) throws FunctionOperationException {
		return function.getLargestValueBetween(leftXpoint, rightXpoint, extremesDiscarded);
	}



}
