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
package se.lnu.eres.fuzzy.stress;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Complete trees (all levels but the last are full, and the last is filled from
 * left to right) with a varying number of leaves and of children per
 * intermediate goal. The type of each intermediate goal (AND/OR) is random.
 * 
 * The number of children per goal can be overridden with
 * -Dperf.balanced.branching=2,4,6,8,10
 */
class GoalTreeBalancedStressTest extends AbstractGoalTreeStressTest {

	private static final int[] BRANCHING = parseInts(System.getProperty("perf.balanced.branching", "2,4,6,8,10"));

	@Test
	void completeTreesWithRandomAndOr() throws Exception {
		printHeader("Complete trees with random AND/OR goals");
		for (int branching : BRANCHING) {
			for (int leaves : leafCounts()) {
				final int k = branching;
				final int n = leaves;
				measure("children=" + k, k, n, () -> buildCompleteTree(k, n, new Random(SEED)));
			}
		}
	}

}
