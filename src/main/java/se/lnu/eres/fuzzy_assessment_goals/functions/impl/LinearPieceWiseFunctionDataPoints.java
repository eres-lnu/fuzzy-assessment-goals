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

import com.google.common.math.DoubleMath;

import se.lnu.eres.fuzzy_assessment_goals.functions.LinearPieceWiseFunction;
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
		for (ImmutablePair<Double, Double> point : points) {
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
		for (ImmutablePair<Double, Double> p : datapoints) {
			xPoints.add(p.getLeft());
		}
		return xPoints;
	}

	/**
	 * @param point
	 * @return The first interval containing the x value of the point. Therefore, it
	 *         does not return the correct value if it is a point where the function
	 *         is not continuous
	 * @throws FunctionOperationException
	 */
	public LinearPieceWiseFunctionDataPoints getIntervalContaining(double point) throws FunctionOperationException {

		ImmutablePair<Double, Double> left = null;
		boolean isLeftSet = false;

		for (ImmutablePair<Double, Double> right : datapoints) {
			if (!isLeftSet) {
				left = right;
				isLeftSet = true;
			} else {
				if (left.getLeft() <= point && right.getLeft() >= point) {
					Logger.debug(
							"Possible problem with the Varargs in the constructor. Calling with <point,left,right>=<{},{},{}>",
							point, left, right);
					return new LinearPieceWiseFunctionDataPoints(left, right);
				}
				left = right;
			}
		}

		throw new FunctionOperationException(
				"Interval for point " + point + " was not found in dataset=" + datapoints.toString());
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

	/**
	 * This function assumes that the datapoints represent intervals in couples such
	 * as: interval1= [datapoint0, datapoint1], interval2= [datapoint2,datapoint3]
	 * ...
	 */
	public void sortByXRespectingIntervalCouples() {
		// TODO Auto-generated method stub

		List<ImmutablePair<Double, Double>> newdatapoints = new ArrayList<ImmutablePair<Double, Double>>();
		
		while(datapoints.size()>0) {
			newdatapoints.addAll(removeLowestXStartingIntervalFromDatapoints());
		}
		datapoints=newdatapoints;
	}

	private List<ImmutablePair<Double, Double>> removeLowestXStartingIntervalFromDatapoints() {
		int indexSmallestBeginningOfInterval=0;
		double smallestX=Double.MAX_VALUE;
		for(int i=0; i<datapoints.size(); i++) {
			if((i%2)==0) {//Beggining of interval
				if(datapoints.get(i).getLeft()<smallestX) {
					smallestX=datapoints.get(i).getLeft();
					indexSmallestBeginningOfInterval=i;
				}
			}
		}
		List<ImmutablePair<Double, Double>> result = new ArrayList<ImmutablePair<Double, Double>>();
		result.addLast(remove(indexSmallestBeginningOfInterval+1));
		result.addFirst(remove(indexSmallestBeginningOfInterval));
		return result;
		
		
	}

	class XPointsComparator implements Comparator<ImmutablePair<Double, Double>> {

		@Override
		public int compare(ImmutablePair<Double, Double> o1, ImmutablePair<Double, Double> o2) {
			// Compares its two arguments for order. Returns a negative integer, zero, or a
			// positive integer as the first argument is less than, equal to, or greater
			// than the second.
			if (o1.getLeft() < o2.getLeft()) {
				return -1;
			}
			if (o1.getLeft().equals(o2.getLeft())) {
				return 0;
			} else
				return 1;
		}

	}

	public void retainLargestYforReplicatedX() {
		// It asumes that the elements are sorted by X
		List<ImmutablePair<Double, Double>> newDatapoints = new ArrayList<ImmutablePair<Double, Double>>();

		int datapointsLength = datapoints.size();

		while (datapointsLength > 0) {
			double currentProcessingX = datapoints.getLast().getLeft();
			double maxY = getMaximumYforCurrentX(currentProcessingX);

			// Not good in terms of time complexity, to use addFist when the type is an
			// ArrayList.
			newDatapoints.addFirst(new ImmutablePair<Double, Double>(currentProcessingX, maxY));
			removeXvaluesFromLast(currentProcessingX);

			datapointsLength = datapoints.size();
		}

		datapoints = newDatapoints;

	}

	private void removeXvaluesFromLast(double currentProcessingX) {
		// Removing from the last because it gives less room to make mess with the
		// indices when iterating.
		while (datapoints.size() > 0 && DoubleMath.fuzzyEquals(datapoints.getLast().getLeft(), currentProcessingX,
				LinearPieceWiseFunction.TOLERANCE)) {
			datapoints.removeLast();
		}

	}

	private double getMaximumYforCurrentX(double currentProcessingX) {
		double maxY = -Double.MAX_VALUE;

		for (int i = datapoints.size() - 1; i >= 0; i--) {
			if (datapoints.get(i).getLeft().equals(currentProcessingX)) {
				if (datapoints.get(i).getRight() > maxY) {
					maxY = datapoints.get(i).getRight();
				}
			} else {
				return maxY;
			}

		}
		return maxY;
	}

	public void removeIntermediatePoitnsForLinearFunctions() throws FunctionOperationException {
		// This function assumes that the datapoints are sorted
		if (datapoints.size() < 3) {
			return;
		}
		ImmutablePair<Double, Double> left, middle, right;

		left = datapoints.get(0);
		middle = datapoints.get(1);

		int i = 2;
		while (i < datapoints.size()) {
			right = datapoints.get(i);
			double yFromFunction = (new FunctionPiecewiseImpl(new LinearPieceWiseFunctionDataPoints(left, right)))
					.getValueAt(middle.getLeft());
			Logger.info("Trying to remove intermediate point {} between values {} and {}. ", middle, left, right);

			if (DoubleMath.fuzzyEquals(yFromFunction, middle.getRight(), LinearPieceWiseFunction.TOLERANCE)) {
				Logger.info("Removing point because the Y at point {} is {} , which is equal to {}", middle.getLeft(),
						yFromFunction, middle.getRight());
				datapoints.remove(i - 1);
				middle = right;
			} else {
				Logger.info("Leaving point because the Y at point {} is {} , which is NOT equal to {}",
						middle.getLeft(), yFromFunction, middle.getRight());
				left = middle;
				middle = right;
				i++;
			}
		}

	}

	public void removeDuplicatedPoints() {
		int size = datapoints.size();
		for (int i = 0; i < size; i++) {
			for (int j = size - 1; j > i; j--) {
				// Use the relaxed comparator with some tolerance
				// if (datapoints.get(i).equals(datapoints.get(j))) {
				if (myEqualDatapoints(datapoints.get(i), datapoints.get(j))) {
					remove(j);
					size--;
				}
			}

		}

	}

	/**
	 * Removes point in position i only if the i-1 has the same <x,y> value
	 */
	public void removeDuplicatedNeighborPoints() {
		int size = datapoints.size();
		for (int i = size - 1; i > 0; i--) {
			// Use the relaxed comparator with some tolerance
			if (myEqualDatapoints(datapoints.get(i), datapoints.get(i - 1))) {
				// if(datapoints.get(i).equals(datapoints.get(i-1))) {
				remove(i);
			}
		}

	}

	private boolean myEqualDatapoints(ImmutablePair<Double, Double> p1, ImmutablePair<Double, Double> p2) {

		if (!DoubleMath.fuzzyEquals(p1.getLeft(), p2.getLeft(), LinearPieceWiseFunction.TOLERANCE)) {
			return false;
		}
		return DoubleMath.fuzzyEquals(p1.getRight(), p2.getRight(), LinearPieceWiseFunction.TOLERANCE);
	}

	/**
	 * Add in even positions to avoid putting the single point in between the two
	 * points of an interval. This method should be called only when the Datapoints
	 * in the function are duplicated to indicate intervals, such as: interval1=
	 * [datapoint0, datapoint1], interval2= [datapoint2,datapoint3] ...
	 * 
	 * A single datapoint in points2 should not break any interval. That means that
	 * its left value (the x value) should be between a point in position 0 or
	 * (2n-1) and 2n for n in 1,2,3, and NOT between a point in position 2n and 2n+1
	 * for n in 0,1,2...
	 * 
	 * @param the DatPoints to merge
	 */
	public void addAllSortedInEvenPositions(LinearPieceWiseFunctionDataPoints points2) {
		for (ImmutablePair<Double, Double> p : points2.getDatapoints().reversed()) { // From largest to smalles
			// from the end of the datapoints to keep parity in the next positions to treat
			boolean added = false;
			for (int i = datapoints.size() - 1; (i >= 0) && (!added); i--) {
				if (datapoints.get(i).getLeft() <= p.getLeft()) {
					// Position found: add on the righ if n is odd and on the left (same position i
					// because the rest are moved to the right) if n is even.
					if ((i % 2) == 1) {
						datapoints.add(i + 1, p);
					} else {
						datapoints.add(i, p);
					}

					added = true;
				}
			}
		}
	}

	/**
	 * This method removes the point <x,y> if it finds in the dataset a points
	 * <x,y_y> and all y_y are larger than y
	 * 
	 * @param d2 The second datapoints
	 */
	public void removePointsWhoseYValueisTheSmallest(LinearPieceWiseFunctionDataPoints d2) {
		int size = datapoints.size();
		for (int i = size - 1; i >= 0; i--) {
			if (d2.valueXisAnIntervalExtreme(datapoints.get(i).getLeft())) {
				ImmutablePair<Double, Double> smallest = d2.findLowestIntervalExtremeForX(datapoints.get(i).getLeft());
				if (smallest.getRight() > datapoints.get(i).getRight()) {// the point in this function was the smallest
					remove(i);
				}

			}
		}

	}

	private boolean valueXisAnIntervalExtreme(double x) {
		for (ImmutablePair<Double, Double> point : getDatapoints()) {
			if (DoubleMath.fuzzyEquals(point.getLeft(), x, LinearPieceWiseFunction.TOLERANCE)) {
				return true;
			}
		}
		return false;
	}

	private ImmutablePair<Double, Double> findLowestIntervalExtremeForX(Double left) {
		ImmutablePair<Double, Double> smallestY = new ImmutablePair<Double, Double>(left, Double.MAX_VALUE);
		for (ImmutablePair<Double, Double> point : getDatapoints()) {
			if (DoubleMath.fuzzyEquals(left, point.getLeft(), LinearPieceWiseFunction.TOLERANCE)
					&& point.getRight() < smallestY.getRight()) {
				smallestY = point;
			}
		}
		return smallestY;
	}

}
