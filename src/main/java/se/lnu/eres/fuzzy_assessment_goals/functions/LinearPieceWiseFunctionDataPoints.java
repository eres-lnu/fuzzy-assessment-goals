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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class LinearPieceWiseFunctionDataPoints implements Iterable<ImmutablePair<Double, Double>> {

	private List<ImmutablePair<Double, Double>> datapoints;

	public List<ImmutablePair<Double, Double>> getDatapoints() {
		return datapoints;
	}

	public LinearPieceWiseFunctionDataPoints() {
		super();
		datapoints = new ArrayList<ImmutablePair<Double, Double>>();
	}

	public LinearPieceWiseFunctionDataPoints(List<ImmutablePair<Double, Double>> datapoints) {
		super();
		this.datapoints = datapoints;
	}

	public void add(ImmutablePair<Double, Double> point) {
		datapoints.add(point);
		
	}

	public int size() {
		return datapoints.size();
	}

	public ImmutablePair<Double, Double> get(int i) {
	
		return datapoints.get(i);
	}

	public ImmutablePair<Double, Double> remove(int i) {
		return datapoints.remove(i);
	}

	public void add(int i, ImmutablePair<Double, Double> point) {
		datapoints.add(i, point);
		
	}

	@Override
	public Iterator<ImmutablePair<Double, Double>> iterator() {
		return datapoints.iterator();
	}

	public ImmutablePair<Double, Double> getFirst() {
		return datapoints.getFirst();
	}

	public ImmutablePair<Double, Double> getLast() {
		return datapoints.getLast();
	}
	

}
