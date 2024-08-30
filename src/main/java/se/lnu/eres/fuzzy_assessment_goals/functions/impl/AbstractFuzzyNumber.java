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

import se.lnu.eres.fuzzy_assessment_goals.functions.FuzzyNumberCheck;

public abstract class AbstractFuzzyNumber implements FuzzyNumberCheck {

	public AbstractFuzzyNumber() {

	}

	@Override
	public boolean isFuzzyNumber() {

		// Condition 1: top y is 1.
		if (maximumValueInPoints() != 1.0) {
			return false;
		}

		// Condition 2: Bottom is 0
		if (minimumValueInPoints() != 0.0) {
			return false;
		}

		// Condition 3: monotonically increasing from [a,b]
		if (!monotonicallyIncreasingUntilReachingTopValue()) {
			return false;
		}

		// Condition 4: monotonically decreasing from [c,d]
		return (monotonicallyDecreasingFromTopValue());
	}

	protected abstract boolean monotonicallyDecreasingFromTopValue();

	protected abstract boolean monotonicallyIncreasingUntilReachingTopValue();

	protected abstract double minimumValueInPoints();

	protected abstract double maximumValueInPoints();

}
