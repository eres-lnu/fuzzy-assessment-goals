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

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import se.lnu.eres.fuzzy.functions.FuzzyBoolean;
import se.lnu.eres.fuzzy.functions.FuzzyNumber;
import se.lnu.eres.fuzzy.functions.LinearPieceWiseFunction;
import se.lnu.eres.fuzzy.functions.exceptions.FunctionOperationException;
import se.lnu.eres.fuzzy.functions.impl.FuzzyNumberImpl;
import se.lnu.eres.fuzzy.functions.impl.LinearPiecewiseFunctionImpl;
import se.lnu.eres.fuzzy.goals.Goal;
import se.lnu.eres.fuzzy.goals.GoalType;
import se.lnu.eres.fuzzy.goals.LeafGoal;
import se.lnu.eres.fuzzy.goals.LeafGoalType;
import se.lnu.eres.fuzzy.goals.impl.GoalFactory;
import se.lnu.eres.fuzzy.goals.impl.LeafGoalImpl;
import se.lnu.eres.fuzzy.plot.Plotter;

/**
 * Stress tests that measure execution time and memory usage when assessing the
 * satisfaction of large goal trees.
 *
 * These tests are excluded from the default build (see the surefire configuration
 * in the pom.xml). Run them with: mvn test -Pstress. The sizes of the trees and the number of
 * repetitions can be overridden with system properties, e.g.
 * -Dperf.balanced.depth=7 -Dperf.repetitions=10 (see the constants below).
 *
 * Memory is reported in two ways:
 * - allocated: bytes allocated by the thread during the assessment (reliable, not affected by the GC).
 * - retained: heap that remains in use after the assessment, while the result is
 *   still referenced (approximate, based on System.gc()).
 */
@Tag("performance")
class GoalTreeStressTest {

	private static final int REPETITIONS = Integer.getInteger("perf.repetitions", 5);
	private static final int BALANCED_BRANCHING = Integer.getInteger("perf.balanced.branching", 3);
	private static final int[] BALANCED_DEPTHS = parseInts(System.getProperty("perf.balanced.depths", "2,3,4,5,6"));
	private static final int[] CHAIN_DEPTHS = parseInts(System.getProperty("perf.chain.depths", "10,50,100,200"));
	private static final int[] WIDE_SIZES = parseInts(System.getProperty("perf.wide.sizes", "10,50,100,200"));
	/** Upper limit for one single assessment, to detect blow-ups instead of hanging */
	private static final long TIMEOUT_SECONDS = Long.getLong("perf.timeout.seconds", 300);
	private static final long SEED = 42;
	private static final String ERRORS_IMAGES_FOLDER = "target/images/test-errors/";

	/** Title of the scenario being run, used to name the error outputs */
	private String scenario = "";

	private static final com.sun.management.ThreadMXBean THREAD_BEAN = (com.sun.management.ThreadMXBean) ManagementFactory
			.getThreadMXBean();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// The test configuration logs at debug level in the goals, which would dominate
		// the measurements
		Configurator.setRootLevel(Level.WARN);
		for (String name : new String[] { "LeafGoalImpl", "AbstractGoal", "GoalAndImpl", "GoalOrImpl",
				"FuzzyNumberImpl", "LinearPiecewiseFunctionImpl", "LinearPieceWiseFunctionDataPoints" }) {
			Configurator.setLevel(name, Level.WARN);
		}
		// Warm up the JIT with a small tree
		for (int i = 0; i < 3; i++) {
			assess(buildBalanced(3, 2, new Random(SEED)));
		}
	}

	@Test
	void balancedTreesAlternatingAndOr() throws Exception {
		printHeader("Balanced trees (branching=" + BALANCED_BRANCHING + ", alternating AND/OR)");
		for (int depth : BALANCED_DEPTHS) {
			final int d = depth;
			measure("depth=" + d, countBalancedLeaves(BALANCED_BRANCHING, d),
					() -> buildBalanced(BALANCED_BRANCHING, d, new Random(SEED)));
		}
	}

	@Test
	void deepChainTrees() throws Exception {
		printHeader("Deep chains (each intermediate goal has one leaf and one intermediate child)");
		for (int depth : CHAIN_DEPTHS) {
			final int d = depth;
			measure("depth=" + d, d + 1, () -> buildChain(d, new Random(SEED)));
		}
	}

	@Test
	void wideAndGoal() throws Exception {
		printHeader("Wide trees: one AND goal with many leaves");
		for (int size : WIDE_SIZES) {
			final int n = size;
			measure("leaves=" + n, n, () -> buildWide(GoalType.AND, n, new Random(SEED)));
		}
	}

	@Test
	void wideOrGoal() throws Exception {
		printHeader("Wide trees: one OR goal with many leaves");
		for (int size : WIDE_SIZES) {
			final int n = size;
			measure("leaves=" + n, n, () -> buildWide(GoalType.OR, n, new Random(SEED)));
		}
	}

	// ------------------------------------------------------------------ measuring

	private interface TreeBuilder {
		Goal build() throws FunctionOperationException;
	}

	private void measure(String label, int leaves, TreeBuilder builder) throws Exception {
		// The tree is rebuilt each repetition so that nothing is cached between runs
		long[] timesNs = new long[REPETITIONS];
		long[] allocated = new long[REPETITIONS];
		long[] retained = new long[REPETITIONS];
		int resultPoints = 0;

		for (int r = 0; r < REPETITIONS; r++) {
			Goal root = builder.build();
			gc();
			long heapBefore = usedHeap();
			long start = System.nanoTime();

			FuzzyBoolean result = Assertions.assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT_SECONDS),
					() -> root.assessSatisfaction());

			timesNs[r] = System.nanoTime() - start;
			allocated[r] = measureAllocation(root);
			gc();
			retained[r] = usedHeap() - heapBefore;
			resultPoints = result.getFunction().getDatapoints().size();

			Assertions.assertNotNull(result);
			checkFuzzyBoolean(result, label, leaves);
			// keep result reachable until the retained heap has been measured
			Assertions.assertNotNull(root);
		}

		System.out.println(String.format("%-14s leaves=%-7d result points=%-6d time(ms) median=%-9.2f min=%-9.2f max=%-9.2f"
				+ " | allocated(MB) median=%-8.2f | retained(MB) median=%-8.2f", label, leaves, resultPoints,
				ms(median(timesNs)), ms(min(timesNs)), ms(max(timesNs)), mb(median(allocated)), mb(median(retained))));
	}

	/**
	 * Fails the test if the result is not a fuzzy boolean. Before failing, it
	 * prints the points of the result and saves a plot of it in
	 * target/images/test-errors/ to help debugging.
	 */
	private void checkFuzzyBoolean(FuzzyBoolean result, String label, int leaves) throws Exception {
		if (result.isFuzzyBoolean()) {
			return;
		}
		String name = (scenario + "_" + label).replaceAll("[^A-Za-z0-9._=-]+", "_");
		System.out.println("RESULT IS NOT A FUZZY BOOLEAN. Scenario: " + scenario + ", " + label + ", leaves=" + leaves);
		System.out.println("Points (x, y) of the result:");
		for (ImmutablePair<Double, Double> point : result.getFunction().getDatapoints()) {
			System.out.println("   (" + point.getLeft() + ", " + point.getRight() + ")");
		}
		try {
			System.out.println("Support: " + result.getSupport() + "  Core: " + result.getCore());
		} catch (Exception e) {
			System.out.println("Support/Core could not be calculated: " + e);
		}
		// Plotter.save does not create folders and ignores IO errors
		Files.createDirectories(Paths.get(ERRORS_IMAGES_FOLDER));
		Plotter plotter = new Plotter(name);
		plotter.addDatasetFromFunction(result.getFunction());
		plotter.save("test-errors/" + name);
		System.out.println("Plot saved in " + ERRORS_IMAGES_FOLDER + name + ".png");
		Assertions.fail("Result is not a fuzzy boolean (" + scenario + ", " + label + "). See the points above and "
				+ ERRORS_IMAGES_FOLDER + name + ".png");
	}

	/**
	 * Allocation is measured by re-running the assessment directly in the current
	 * thread, because assertTimeoutPreemptively executes in a different thread.
	 */
	private long measureAllocation(Goal root) throws FunctionOperationException {
		long before = THREAD_BEAN.getThreadAllocatedBytes(Thread.currentThread().getId());
		root.assessSatisfaction();
		return THREAD_BEAN.getThreadAllocatedBytes(Thread.currentThread().getId()) - before;
	}

	private static FuzzyBoolean assess(Goal g) {
		try {
			return g.assessSatisfaction();
		} catch (FunctionOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void gc() throws InterruptedException {
		for (int i = 0; i < 3; i++) {
			System.gc();
			Thread.sleep(50);
		}
	}

	private static long usedHeap() {
		Runtime rt = Runtime.getRuntime();
		return rt.totalMemory() - rt.freeMemory();
	}

	// ------------------------------------------------------------- tree builders

	private static Goal buildBalanced(int branching, int depth, Random rnd) throws FunctionOperationException {
		return buildBalanced(branching, depth, GoalType.AND, rnd);
	}

	private static Goal buildBalanced(int branching, int depth, GoalType type, Random rnd)
			throws FunctionOperationException {
		Goal goal = GoalFactory.CreateGoal(type);
		GoalType childType = type == GoalType.AND ? GoalType.OR : GoalType.AND;
		for (int i = 0; i < branching; i++) {
			if (depth <= 1) {
				goal.addChild(createLeaf(rnd));
			} else {
				goal.addChild(buildBalanced(branching, depth - 1, childType, rnd));
			}
		}
		return goal;
	}

	private static int countBalancedLeaves(int branching, int depth) {
		return (int) Math.pow(branching, depth);
	}

	private static Goal buildChain(int depth, Random rnd) throws FunctionOperationException {
		Goal root = GoalFactory.CreateGoal(GoalType.AND);
		Goal current = root;
		for (int i = 0; i < depth; i++) {
			current.addChild(createLeaf(rnd));
			if (i < depth - 1) {
				Goal next = GoalFactory.CreateGoal(i % 2 == 0 ? GoalType.OR : GoalType.AND);
				current.addChild(next);
				current = next;
			} else {
				current.addChild(createLeaf(rnd));
			}
		}
		return root;
	}

	private static Goal buildWide(GoalType type, int leaves, Random rnd) throws FunctionOperationException {
		Goal root = GoalFactory.CreateGoal(type);
		for (int i = 0; i < leaves; i++) {
			root.addChild(createLeaf(rnd));
		}
		return root;
	}

	/**
	 * Leaf goal with an upper-bound satisfaction function and a triangular
	 * observation. The location and width of the observation vary randomly (but
	 * deterministically, given the seed) so that the partial results are different
	 * and the number of points to process grows realistically.
	 */
	private static LeafGoal createLeaf(Random rnd) {
		LinearPieceWiseFunction truth = new LinearPiecewiseFunctionImpl();
		truth.addPoint(0.0, 1.0);
		truth.addPoint(1.0, 0.0);
		truth.addPoint(Double.MAX_VALUE, 0.0);
		FuzzyNumber truthValue = new FuzzyNumberImpl(truth);

		double top = 0.3 + rnd.nextDouble() * 0.4;
		double width = 0.05 + rnd.nextDouble() * 0.25;
		LinearPieceWiseFunction obs = new LinearPiecewiseFunctionImpl();
		obs.addPoint(0.0, 0.0);
		obs.addPoint(top - width / 2.0, 0.0);
		obs.addPoint(top, 1.0);
		obs.addPoint(top + width / 2.0, 0.0);
		obs.addPoint(Double.MAX_VALUE, 0.0);

		LeafGoal goal = new LeafGoalImpl(LeafGoalType.UB, truthValue);
		goal.setObservation(new FuzzyNumberImpl(obs));
		return goal;
	}

	// ----------------------------------------------------------------- utilities

	private void printHeader(String title) {
		scenario = title;
		System.out.println();
		System.out.println("=== " + title + " (" + REPETITIONS + " repetitions) ===");
	}

	private static int[] parseInts(String csv) {
		return Arrays.stream(csv.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
	}

	private static long median(long[] values) {
		long[] sorted = values.clone();
		Arrays.sort(sorted);
		return sorted[sorted.length / 2];
	}

	private static long min(long[] values) {
		return Arrays.stream(values).min().getAsLong();
	}

	private static long max(long[] values) {
		return Arrays.stream(values).max().getAsLong();
	}

	private static double ms(long ns) {
		return ns / 1_000_000.0;
	}

	private static double mb(long bytes) {
		return bytes / (1024.0 * 1024.0);
	}
}
