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


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FuzzyNumberConversionException;
import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FuzzyOperationException;
import se.lnu.eres.fuzzy_assessment_goals.functions.impl.FunctionPiecewiseImpl;

class FunctionPiecewiseImplTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testSuccessFuzzyNumber() throws FuzzyNumberConversionException {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		function.addPoint(0, 0);
		function.addPoint(5, 0);
		function.addPoint(7, 1);
		function.addPoint(9, 0);
		function.addPoint(Double.MAX_VALUE, 0);
		
		Assertions.assertTrue(function.isFuzzyNumber(), "The function should be a fuzzy number but it isn't + " + function.toString());

	}

	
	@Test
	void testFailFuzzyNumberForMembershipMoreThan1() {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		function.addPoint(0, 0);
		function.addPoint(5, 0);
		function.addPoint(7, 1.2);
		function.addPoint(9, 0);
		function.addPoint(Double.MAX_VALUE, 0);
		
		Assertions.assertFalse(function.isFuzzyNumber(), "The function should not be a fuzzy number because it has a membership higher than 1, but it is: + " + function.toString());
	}
	
	@Test
	void testFailFuzzyNumberForNoMonotonicallyIncreasing() {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		function.addPoint(0, 0);
		function.addPoint(5, 0);
		function.addPoint(5.5, 0.9);
		function.addPoint(6, 0.6); // This has decreased;
		function.addPoint(7, 1);
		function.addPoint(9, 0);
		function.addPoint(Double.MAX_VALUE, 0);
		
		Assertions.assertFalse(function.isFuzzyNumber(), "The function should not be a fuzzy number because it has a membership higher than 1, but it is: + " + function.toString());

		
	}
	
	@Test
	void testSupport() throws FuzzyOperationException, FuzzyNumberConversionException  {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		function.addPoint(0, 0);
		function.addPoint(5, 0);
		function.addPoint(7, 1);
		function.addPoint(9, 0);
		function.addPoint(Double.MAX_VALUE, 0);
		Assertions.assertTrue(function.isFuzzyNumber(), "The function should be a fuzzy number but it isn't + " + function.toString());
		FuzzyNumber fn = function.getFuzzyNumber(); 
		Assertions.assertEquals(new ImmutablePair<Double,Double>(5.0,9.0), fn.getSupport());
		
		

	}
	
	@Test
	void testCore() throws FuzzyOperationException, FuzzyNumberConversionException  {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		function.addPoint(0, 0);
		function.addPoint(5, 0);
		function.addPoint(7, 1);
		function.addPoint(9, 0);
		function.addPoint(Double.MAX_VALUE, 0);
		Assertions.assertTrue(function.isFuzzyNumber(), "The function should be a fuzzy number but it isn't + " + function.toString());
		FuzzyNumber fn = function.getFuzzyNumber(); 
		Assertions.assertEquals(new ImmutablePair<Double,Double>(7.0,7.0), fn.getCore());
		
		

	}
	
	
}
