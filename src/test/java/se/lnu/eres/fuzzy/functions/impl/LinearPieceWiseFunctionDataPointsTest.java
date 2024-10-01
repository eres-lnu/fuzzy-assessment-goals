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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;

class LinearPieceWiseFunctionDataPointsTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testIntervalContainint() throws FunctionOperationException {
		LinearPieceWiseFunctionDataPoints points = new LinearPieceWiseFunctionDataPoints();
		//The points in the longitudinal acceleration satisfaction
		points.add(new ImmutablePair<Double,Double>(0.0, 1.0));
		points.add(new ImmutablePair<Double,Double>(0.56, 1.0));
		points.add(new ImmutablePair<Double,Double>(1.89, 0.0));
		points.add(new ImmutablePair<Double,Double>(Double.MAX_VALUE, 0.0));
		
		
		Assertions.assertEquals(points.getIntervalContaining(0.3), new LinearPieceWiseFunctionDataPoints(new ImmutablePair<Double,Double>(0.0, 1.0),new ImmutablePair<Double,Double>(0.56, 1.0)));
	}

}
