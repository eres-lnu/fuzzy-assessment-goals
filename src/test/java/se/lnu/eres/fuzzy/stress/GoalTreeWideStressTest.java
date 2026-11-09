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

import se.lnu.eres.fuzzy.goals.GoalType;

/**
 * A single AND (or OR) goal that has all the leaves as direct children.
 */
class GoalTreeWideStressTest extends AbstractGoalTreeStressTest {

	@Test
	void wideAndGoal() throws Exception {
		printHeader("Wide trees: one AND goal with many leaves");
		for (int leaves : leafCounts()) {
			final int n = leaves;
			measure("AND", n, n, () -> buildWide(GoalType.AND, n, new Random(SEED)));
		}
	}

	@Test
	void wideOrGoal() throws Exception {
		printHeader("Wide trees: one OR goal with many leaves");
		for (int leaves : leafCounts()) {
			final int n = leaves;
			measure("OR", n, n, () -> buildWide(GoalType.OR, n, new Random(SEED)));
		}
	}

}
