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
 */
package se.lnu.eres.fuzzy.goals.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;

class AbstractGoalSnapTest {

	@Test
	void snapsRoundingErrorsAroundZero() {
		Assertions.assertEquals(0.0, AbstractGoal.snapToBounds(-4.884376478091286E-16));
		Assertions.assertEquals(0.0, AbstractGoal.snapToBounds(3e-16));
		Assertions.assertEquals(0.0, AbstractGoal.snapToBounds(-LinearPieceWiseFunction.SNAP_TOLERANCE));
	}

	@Test
	void snapsRoundingErrorsAroundOne() {
		Assertions.assertEquals(1.0, AbstractGoal.snapToBounds(1.0000000000000002));
		Assertions.assertEquals(1.0, AbstractGoal.snapToBounds(0.9999999999999999));
	}

	@Test
	void doesNotChangeValuesOutsideSnapTolerance() {
		Assertions.assertEquals(0.5, AbstractGoal.snapToBounds(0.5));
		Assertions.assertEquals(0.00005, AbstractGoal.snapToBounds(0.00005));
		Assertions.assertEquals(0.99995, AbstractGoal.snapToBounds(0.99995));
		Assertions.assertEquals(Double.MAX_VALUE, AbstractGoal.snapToBounds(Double.MAX_VALUE));
	}

	@Test
	void snapToleranceIsFarBelowTolerance() {
		Assertions.assertTrue(LinearPieceWiseFunction.SNAP_TOLERANCE * 1000 <= LinearPieceWiseFunction.TOLERANCE);
	}
}
