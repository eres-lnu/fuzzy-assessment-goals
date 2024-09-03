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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.lnu.eres.fuzzy_assessment_goals.functions.exceptions.FunctionOperationException;

public class LinearPieceWiseFunctionDataPoints implements Iterable<ImmutablePair<Double, Double>> {

	private static final Logger Logger = LogManager.getLogger(LinearPieceWiseFunctionDataPoints.class.getSimpleName());
	
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

	public LinearPieceWiseFunctionDataPoints(ImmutablePair<Double, Double>... points) {
		this();
		for(ImmutablePair<Double, Double> point : points) {
			datapoints.add(point);
		}
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

	public List<Double> getXpoints() {
		List<Double> xPoints = new ArrayList<Double>();
		for(ImmutablePair<Double, Double> p : datapoints) {
			xPoints.add(p.getLeft());
		}
		return xPoints;
	}

	public LinearPieceWiseFunctionDataPoints getIntervalContaining(double point) throws FunctionOperationException {
		
		ImmutablePair<Double, Double> left=null;
		boolean isLeftSet=false;
		
		for(ImmutablePair<Double, Double> right : datapoints) {
			if(!isLeftSet) {
				left=right;
				isLeftSet=true;
			}
			else {
				if(left.getLeft()<=point && right.getLeft()>=point) {
					Logger.info("Possible problem with the Varargs in the constructor. Calling with <point,left,right>=<{},{},{}>", point,left,right);
					return new LinearPieceWiseFunctionDataPoints(left,right);
				}
				left=right;
			}
		}
		
		throw new FunctionOperationException("Interval for point " + point + " was not found in dataset=" + datapoints.toString());
	}

	public void addAll(LinearPieceWiseFunctionDataPoints additionalData) {
		datapoints.addAll(additionalData.datapoints);
		
	}

	@Override
	public String toString() {
		return "LinearPieceWiseFunctionDataPoints [datapoints=" + datapoints.toString() + "]";
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LinearPieceWiseFunctionDataPoints other = (LinearPieceWiseFunctionDataPoints) obj;
		return Objects.equals(datapoints, other.datapoints);
	}

	public void sortByX() {
		Collections.sort(datapoints, new XPointsComparator());
		
	}
	
	class XPointsComparator implements Comparator<ImmutablePair<Double,Double>>{

		@Override
		public int compare(ImmutablePair<Double, Double> o1, ImmutablePair<Double, Double> o2) {
			//Compares its two arguments for order. Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
			if(o1.getLeft()<o2.getLeft()) {return -1;}
			if(o1.getLeft().equals(o2.getLeft())) {return 0;}
			else return 1;
		}
		
	}

	public void retainLargestYforReplicatedX() {
		//It asumes that the elements are sorted by X
		List<ImmutablePair<Double, Double>> newDatapoints = new ArrayList<ImmutablePair<Double, Double>>();
		
		int datapointsLength=datapoints.size();
		
		while(datapointsLength>0) {
			double currentProcessingX = datapoints.getLast().getLeft();
			double maxY = getMaximumYforCurrentX(currentProcessingX);
			
			//Not good in terms of time complexity, to use addFist when the type is an ArrayList.
			newDatapoints.addFirst(new ImmutablePair<Double,Double>(currentProcessingX,maxY));
			removeXvaluesFromLast(currentProcessingX);
			
			datapointsLength=datapoints.size();
		}
		
		datapoints=newDatapoints;
		
	}

	private void removeXvaluesFromLast(double currentProcessingX) {
		//Removing from the last because it gives less room to make mess with the indices when iterating.
		while(datapoints.size()>0 && datapoints.getLast().getLeft().equals(currentProcessingX)) {
			datapoints.removeLast();
		}
		
	}

	private double getMaximumYforCurrentX(double currentProcessingX) {
		double maxY = -Double.MAX_VALUE;
		
		for(int i = datapoints.size()-1; i>=0; i--) {
			if(datapoints.get(i).getLeft().equals(currentProcessingX)) {
				if(datapoints.get(i).getRight()>maxY) {
					maxY=datapoints.get(i).getRight();
				}
			}
			else {return maxY;}
			
		} 
		return maxY;
	}
		
		

	
	

}
