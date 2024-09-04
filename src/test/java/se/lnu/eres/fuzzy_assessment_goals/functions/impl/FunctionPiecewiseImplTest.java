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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.lnu.eres.fuzzy_assessment_goals.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FunctionOperationException;

class FunctionPiecewiseImplTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testCheckMonotonicInContinuousDecreasing() {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		//The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		
		Assertions.assertTrue(!function.isMonotonicallyIncreasing(), "The function is monotonically decreasing, not increasing " + function.toString());
		Assertions.assertTrue(function.isMonotonicallyDecreasing(), "The function should be monotonically decreasing " + function.toString());
		System.out.println(function.toString());
	}
	
	@Test
	void testgetLimitXpoints() {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		//The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		List<Double> xpoints= Arrays.asList(0.0,0.56,1.89,Double.MAX_VALUE);
		Assertions.assertEquals(function.getLimitXpoints(), xpoints);
	}

	
	@Test
	void testGetValueAt() throws FunctionOperationException {
		LinearPieceWiseFunction function = new FunctionPiecewiseImpl();
		//The points in the longitudinal acceleration satisfaction
		function.addPoint(0.0, 1.0);
		function.addPoint(0.56, 1.0);
		function.addPoint(1.89, 0.0);
		function.addPoint(Double.MAX_VALUE, 0.0);
		Assertions.assertEquals(1.0,function.getValueAt(0.56));
		Assertions.assertEquals(1.0,function.getValueAt(0.3));
		Assertions.assertTrue(function.getValueAt(0.6)<1.0,"The value at 0.6 should be lower than 1 because it starts decreasing at 0.56");
		Assertions.assertTrue(function.getValueAt(0.6)>0.8,"The value at 0.6 should be larger than 0.8 because it does not decrease so quickly from 0.56");
		System.out.println("testGetValueAt: value at 0.6 is " + function.getValueAt(0.6));
		Assertions.assertTrue(function.getValueAt(2.0)==0.0,"The value at 2 should be 0 because it reaches the 0 at 1.89");
		
	}
	
	@Test
	void testGetIntersections() throws FunctionOperationException {
		LinearPieceWiseFunction f1 = new FunctionPiecewiseImpl();
		f1.addPoint(0, 1);
		f1.addPoint(7,1);
		f1.addPoint(8, 0);
		f1.addPoint(9, 3);
		f1.addPoint(10, 0);
		
		LinearPieceWiseFunction f2 = new FunctionPiecewiseImpl();
		f2.addPoint(0, 2);
		f2.addPoint(1, 3);
		f2.addPoint(2, 3);
		f2.addPoint(3, 3);
		f2.addPoint(4, 4);
		f2.addPoint(5, 4);
		f2.addPoint(6, 0);
		f2.addPoint(10, 3);
		
		List<Double> intersections = f1.findIntersections(f2);
		
		Assertions.assertEquals(4, intersections.size(), "Intersections contents are: " + intersections.toString());
		System.out.println("Intersections contents are: " + intersections.toString());
	}
	
	
}
