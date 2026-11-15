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

import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.impl.LinearPiecewiseFunctionImpl;
import se.lnu.eres.fuzzy.goals.Goal;
import se.lnu.eres.fuzzy.goals.LeafGoal;
import se.lnu.eres.fuzzy.plot.Plotter;

/**
 * Visual test (it needs a graphical environment) that shows some of the goals of
 * one of the complete trees used in the balanced stress tests. The tree is built
 * exactly as in the stress tests (same builder and same seed). It shows:
 * - For a range of leaves (by default, the leaves with index 210 to 219, counting
 *   from 0 and from left to right): a plot with the satisfaction function (goal)
 *   and the observation of each leaf.
 * - The plot of the satisfaction of all the ancestors of those leaves, from the
 *   lowest level up to the root.
 *
 * The windows stay open until any of them is closed, which closes all the other
 * windows and finishes the test. Run it with, for example:
 * mvn test -Pstress -Dtest=BalancedTreeVisualTest -Dvisual.children=4
 *
 * Options (system properties): visual.children (default 4), visual.leaves (default 500),
 * visual.first (default 210) and visual.last (default 219).
 */
class BalancedTreeVisualTest {

	private static final int CHILDREN = Integer.getInteger("visual.children", 4);
	private static final int LEAVES = Integer.getInteger("visual.leaves", 500);
	private static final int FIRST_LEAF = Integer.getInteger("visual.first", 210);
	private static final int LAST_LEAF = Integer.getInteger("visual.last", 219);

	@Test
	void showLeavesAndTheirAncestors() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "A graphical environment is needed");
		// The test configuration logs at debug level in the goals
		Configurator.setRootLevel(Level.WARN);
		for (String name : new String[] { "LeafGoalImpl", "AbstractGoal", "GoalAndImpl", "GoalOrImpl",
				"FuzzyNumberImpl", "LinearPiecewiseFunctionImpl", "LinearPieceWiseFunctionDataPoints" }) {
			Configurator.setLevel(name, Level.WARN);
		}

		Goal root = AbstractGoalTreeStressTest.buildCompleteTree(CHILDREN, LEAVES,
				new Random(AbstractGoalTreeStressTest.SEED));

		// Level order traversal. The nodes are visited in the same order as they were
		// created, so the leaves are in the order of their index
		List<Goal> nodes = new ArrayList<>();
		Map<Goal, Goal> parents = new IdentityHashMap<>();
		Map<Goal, Integer> levels = new IdentityHashMap<>();
		nodes.add(root);
		levels.put(root, 0);
		for (int i = 0; i < nodes.size(); i++) {
			Goal node = nodes.get(i);
			if (node instanceof LeafGoal) {
				continue;
			}
			for (Goal child : node.getChildren()) {
				nodes.add(child);
				parents.put(child, node);
				levels.put(child, levels.get(node) + 1);
			}
		}
		List<LeafGoal> leaves = new ArrayList<>();
		for (Goal node : nodes) {
			if (node instanceof LeafGoal) {
				leaves.add((LeafGoal) node);
			}
		}
		Assertions.assertEquals(LEAVES, leaves.size());
		Assertions.assertTrue(0 <= FIRST_LEAF && FIRST_LEAF <= LAST_LEAF && LAST_LEAF < leaves.size(),
				"The leaf indexes must be in [0," + (leaves.size() - 1) + "]");

		System.out.println("Tree with " + LEAVES + " leaves and " + CHILDREN + " children per goal, "
				+ (nodes.size() - leaves.size()) + " intermediate goals and "
				+ Collections.max(levels.values()) + " levels below the root");

		// Plots of the leaves: the goal (satisfaction function) and the observation
		Set<Goal> ancestors = new LinkedHashSet<>();
		for (int i = FIRST_LEAF; i <= LAST_LEAF; i++) {
			LeafGoal leaf = leaves.get(i);
			Plotter plotter = new Plotter("Leaf " + i);
			LinearPieceWiseFunction observation = leaf.getObservation();
			LinearPieceWiseFunction truthValue = leaf.getGoalTruthValue();
			double xEnd = Math.max(1.0, Math.max(largestFiniteX(observation), largestFiniteX(truthValue)));
			plotter.addObservationValue(finiteCopy(observation, xEnd));
			plotter.addRequirementValue(finiteCopy(truthValue, xEnd));
			plotter.showPlot();
			for (Goal parent = parents.get(leaf); parent != null; parent = parents.get(parent)) {
				ancestors.add(parent);
			}
		}

		// Plots of the satisfaction of the ancestors, from the lowest level to the root
		List<Goal> sortedAncestors = new ArrayList<>(ancestors);
		Collections.sort(sortedAncestors, (a, b) -> levels.get(b) - levels.get(a));
		System.out.println("Showing the satisfaction of " + sortedAncestors.size() + " ancestors");
		for (Goal ancestor : sortedAncestors) {
			String name = "Level " + levels.get(ancestor) + " " + ancestor.getType() + " (node "
					+ nodes.indexOf(ancestor) + ")";
			FuzzyBoolean result = ancestor.assessSatisfaction();
			Plotter plotter = new Plotter(name);
			plotter.addDatasetFromFunction(result.getFunction());
			plotter.showPlot();
			System.out.println("  " + name + ": " + ancestor.getChildren().size() + " children, "
					+ result.getFunction().getDatapoints().size() + " points");
		}

		System.out.println("Close any of the windows to close all of them and finish the test");
		waitUntilAnyWindowIsClosedAndCloseAll();
	}

	/**
	 * The functions of the leaves end with a point at x=Double.MAX_VALUE, which
	 * makes the axes of the plot fail ("Must be finite"). It returns a copy where
	 * that point is moved to xEnd, with the same y value.
	 */
	private static LinearPieceWiseFunction finiteCopy(LinearPieceWiseFunction function, double xEnd) {
		LinearPieceWiseFunction copy = new LinearPiecewiseFunctionImpl();
		double lastX = Double.NaN;
		for (ImmutablePair<Double, Double> point : function.getDatapoints()) {
			if (point.getLeft() < Double.MAX_VALUE) {
				copy.addPoint(point.getLeft(), point.getRight());
				lastX = point.getLeft();
			} else if (Double.isNaN(lastX) || lastX < xEnd) {
				copy.addPoint(xEnd, point.getRight());
				lastX = xEnd;
			}
		}
		return copy;
	}

	private static double largestFiniteX(LinearPieceWiseFunction function) {
		double largest = 0.0;
		for (ImmutablePair<Double, Double> point : function.getDatapoints()) {
			if (point.getLeft() < Double.MAX_VALUE) {
				largest = Math.max(largest, point.getLeft());
			}
		}
		return largest;
	}

	/**
	 * Waits until any of the windows that are open is closed, and then closes all
	 * the rest.
	 */
	private static void waitUntilAnyWindowIsClosedAndCloseAll() throws Exception {
		// Give time to the windows to be displayed
		Thread.sleep(1000);
		CountDownLatch oneClosed = new CountDownLatch(1);
		SwingUtilities.invokeAndWait(() -> {
			for (Window window : Window.getWindows()) {
				if (window.isShowing()) {
					window.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent e) {
							oneClosed.countDown();
						}
					});
				}
			}
		});
		oneClosed.await();
		SwingUtilities.invokeAndWait(() -> {
			for (Window window : Window.getWindows()) {
				window.dispose();
			}
		});
	}

}
