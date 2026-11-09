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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.functions.exceptions.FuzzyOperationException;
import se.lnu.eres.fuzzy.functions.impl.FuzzyBooleanImpl;
import se.lnu.eres.fuzzy.functions.impl.LinearPiecewiseFunctionImpl;
import se.lnu.eres.fuzzy.goals.Goal;
import se.lnu.eres.fuzzy.goals.GoalType;

/**
 * Regression test for two discontinuities closer than TOLERANCE. Each of them
 * returned also the values of the other one, so the points at their x were
 * duplicated in every assessment and the number of points grew exponentially
 * with the number of children (found in the wide AND stress test with 900
 * leaves).
 */
@Timeout(30)
class AbstractGoalCloseDiscontinuitiesTest {

	private static final double FIRST_JUMP = 0.30016204937066976;
	private static final double SECOND_JUMP = 0.3002245555535085;
	private static final int CHILDREN = 8;

	/** Partial result found in the stress test, with two jumps 6.25e-5 apart */
	private static FuzzyBoolean closeDiscontinuities() {
		return fuzzyBoolean(0.0, 0.0, 0.15117730170211752, 0.0, 0.2978015167791316, 0.9818016860143487, FIRST_JUMP,
				0.9986971240932767, FIRST_JUMP, 1.0, SECOND_JUMP, 0.9986971240932767, SECOND_JUMP, 1.0,
				0.3003708389478377, 0.9934077515666458, 0.301215593066413, 0.9604979530247213, 0.3260147224780699,
				0.0, 1.0, 0.0);
	}

	/** Triangle far from the discontinuities */
	private static FuzzyBoolean triangle() {
		return fuzzyBoolean(0.0, 0.0, 0.5542300462839537, 0.0, 0.645745750744908, 1.0, 0.7372614552058624, 0.0,
				1.0, 0.0);
	}

	private static FuzzyBoolean fuzzyBoolean(double... xy) {
		LinearPieceWiseFunction f = new LinearPiecewiseFunctionImpl();
		for (int i = 0; i < xy.length; i += 2) {
			f.addPoint(xy[i], xy[i + 1]);
		}
		return new FuzzyBooleanImpl(f);
	}

	/** Child goal whose satisfaction is given */
	private static Goal fixed(FuzzyBoolean satisfaction) {
		return new Goal() {
			@Override
			public GoalType getType() {
				return GoalType.AND;
			}

			@Override
			public List<Goal> getChildren() {
				return new ArrayList<Goal>();
			}

			@Override
			public void addChild(Goal child) {
				throw new UnsupportedOperationException();
			}

			@Override
			public FuzzyBoolean assessSatisfaction() {
				return satisfaction;
			}
		};
	}

	private static Goal goalWithManyChildren(GoalType type) throws FunctionOperationException {
		Goal goal = GoalFactory.CreateGoal(type);
		goal.addChild(fixed(closeDiscontinuities()));
		for (int i = 1; i < CHILDREN; i++) {
			goal.addChild(fixed(triangle()));
		}
		return goal;
	}

	private static long pointsAt(FuzzyBoolean f, double x) {
		return f.getFunction().getDatapoints().getXpoints().stream().filter(p -> p == x).count();
	}

	@Test
	void andDoesNotDuplicatePointsAtCloseDiscontinuities() throws FunctionOperationException, FuzzyOperationException {
		FuzzyBoolean result = goalWithManyChildren(GoalType.AND).assessSatisfaction();

		Assertions.assertTrue(pointsAt(result, FIRST_JUMP) <= 3, "Points at first jump: " + result);
		Assertions.assertTrue(pointsAt(result, SECOND_JUMP) <= 3, "Points at second jump: " + result);
		Assertions.assertTrue(result.getFunction().getDatapoints().size() <= 15, "Result: " + result);
		Assertions.assertTrue(result.isFuzzyBoolean());
	}

	@Test
	void andResultDoesNotDependOnTheNumberOfEqualChildren() throws FunctionOperationException {
		Goal two = GoalFactory.CreateGoal(GoalType.AND);
		two.addChild(fixed(closeDiscontinuities()));
		two.addChild(fixed(triangle()));

		Assertions.assertEquals(two.assessSatisfaction().getFunction().getDatapoints(),
				goalWithManyChildren(GoalType.AND).assessSatisfaction().getFunction().getDatapoints());
	}
}
