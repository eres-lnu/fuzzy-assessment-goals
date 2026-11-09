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
 * Chains in which each intermediate goal has a leaf and the next intermediate
 * goal as children (the last one has two leaves). A chain with N leaves has N-1
 * nested intermediate goals, whose type (AND/OR) is random.
 * 
 * The assessment is recursive, so very deep chains may exceed the default stack
 * size of the JVM (StackOverflowError). That would be the maximum depth
 * supported with the default settings.
 */
class GoalTreeDeepChainStressTest extends AbstractGoalTreeStressTest {

	@Test
	void deepChainsWithRandomAndOr() throws Exception {
		printHeader("Deep chains with random AND/OR goals");
		for (int leaves : leafCounts()) {
			final int n = leaves;
			measure("chain", 2, n, () -> buildChain(n, new Random(SEED)));
		}
	}

}
