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
package se.lnu.eres.fuzzy_assessment_goals.goals.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import se.lnu.eres.fuzzy_assessment_goals.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy_assessment_goals.functions.impl.FunctionPiecewiseImpl;
import se.lnu.eres.fuzzy_assessment_goals.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy_assessment_goals.functions.impl.FuzzyBooleanImpl;
import se.lnu.eres.fuzzy_assessment_goals.goals.Goal;
import se.lnu.eres.fuzzy_assessment_goals.goals.GoalType;

public class GoalImpl implements Goal {

	private final GoalType type;

	private List<Goal> children;

	public GoalImpl(GoalType type, List<Goal> children) {
		super();
		this.type = type;
		this.children = children;
	}

	public GoalImpl(GoalType type) {
		this(type, new ArrayList<Goal>());
	}

	@Override
	public GoalType getType() {
		return type;
	}

	@Override
	public List<Goal> getChildren() {
		return children;
	}

	@Override
	public void addChild(Goal child) {
		children.add(child);

	}

	@Override
	public FuzzyBoolean assessSatisfaction() throws FunctionOperationException {
		if (children.size() == 0) {
			throw new FunctionOperationException(
					"No possible satisfaction assessment because intermediate goal does not have any child");
		}

		FuzzyBoolean partialResult = children.getFirst().assessSatisfaction();
		switch (type) {
		case AND:

			
			// skip the first
			for (int i = 1; i < children.size(); i++) {
				partialResult = assessANDSatisfaction(partialResult, children.get(i).assessSatisfaction());
			}
			return partialResult;
		case OR:

			// skip the first
			for (int i = 1; i < children.size(); i++) {
				partialResult = assessORSatisfaction(partialResult, children.get(i).assessSatisfaction());
			}
			return partialResult;

		default:
			throw new FunctionOperationException("Unknown type of goal");
		}
	}

	private FuzzyBoolean assessORSatisfaction(FuzzyBoolean f1, FuzzyBoolean f2) throws FunctionOperationException {
		
		throw new UnsupportedOperationException("Not implemented yet");
	}

	private FuzzyBoolean assessANDSatisfaction(FuzzyBoolean f1, FuzzyBoolean f2) throws FunctionOperationException {
		//Zadeh's extension principle B(z) = sup {t(B1(x), B2(y))|t(x, y) = z}, 0 ≤ z ≤ 1 (5) x,y∈[0,1], where t-norm is the Min

		//Get x-points of interest from the points of interest of the two fuzzy booleans
		List<Double> xPointsOfInterest =  CollectionUtils.collate(f1.getFunction().getLimitXpoints(), f2.getFunction().getLimitXpoints(),false);
		//Add as point of interest the x values where the fuzzy booleans intersect (the minimum value passes from belonging to f1 to f2).
		/* After this, we known that the full interval is either below or above */ 
		//TODO: find intersections
		xPointsOfInterest=CollectionUtils.collate(xPointsOfInterest, f1.getFunction().findIntersections(f2.getFunction()),false);
		
		LinearPieceWiseFunction resultFunction = new FunctionPiecewiseImpl();
		
		//For each point of interests p
		for(double p : xPointsOfInterest) {
		
		//find the largest value f2(x) such that x>p, and save the minimum between<f2(x),f1(p)>
		double maxYinF2afterP = f2.getLargestValueAfterX(p);
		double minimum1 = Math.min(f1.getFunctionValueAt(p), maxYinF2afterP);
		
		//find the largest value f1(x) such that x>p, and save the minimum between<f1(x),f2(p)>
		double maxYinF1afterP = f1.getLargestValueAfterX(p);
		double minimum2 = Math.min(f2.getFunctionValueAt(p), maxYinF1afterP);
	
		//get the maximum of the two values previously saved as m and save <p,m> in the result list;
		resultFunction.addPoint(p, Math.max(minimum1, minimum2));
		}
		
		//At this point the result interval has several duplicates and is out of order. Clean duplicates and sort.
		resultFunction.getDatapoints().sortByX();
		resultFunction.getDatapoints().retainLargestYforReplicatedX();
		resultFunction.simplifyPiecewiseFunction();
		return new FuzzyBooleanImpl(resultFunction);
	}
}
