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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;

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
 * Common code for the stress tests that measure execution time and memory usage
 * when assessing the satisfaction of large goal trees.
 *
 * These tests are excluded from the default build (see the surefire
 * configuration in the pom.xml). Run them with: mvn test -Pstress.
 *
 * Settings that can be overridden with system properties (see the constants below):
 * -Dperf.repetitions, -Dperf.timeout.seconds, -Dperf.leaves.from, -Dperf.leaves.to and
 * -Dperf.leaves.step.
 *
 * Memory is reported in two ways:
 * - allocated: bytes allocated by the thread during the assessment, including
 *   temporary objects (not affected by the GC).
 * - retained: heap that remains in use after the assessment and a GC, while the
 *   result is still referenced (approximate).
 *
 * Besides printing a table in the console, each test class writes its results to
 * target/stress-results/&lt;ClassName&gt;.csv
 */
@Tag("performance")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractGoalTreeStressTest {

	protected static final int REPETITIONS = Integer.getInteger("perf.repetitions", 5);
	protected static final int LEAVES_FROM = Integer.getInteger("perf.leaves.from", 100);
	protected static final int LEAVES_TO = Integer.getInteger("perf.leaves.to", 1000);
	protected static final int LEAVES_STEP = Integer.getInteger("perf.leaves.step", 100);
	/** Upper limit for one single assessment, to detect blow-ups instead of hanging */
	private static final long TIMEOUT_SECONDS = Long.getLong("perf.timeout.seconds", 900);
	protected static final long SEED = 42;
	private static final String ERRORS_IMAGES_FOLDER = "target/images/test-errors/";
	private static final String RESULTS_FOLDER = "target/stress-results";
	private static final String CSV_HEADER = "scenario,children_per_node,leaves,depth,repetitions,time_median_ms,"
			+ "time_min_ms,time_max_ms,allocated_median_mb,retained_median_mb,result_points";

	private static final com.sun.management.ThreadMXBean THREAD_BEAN = (com.sun.management.ThreadMXBean) ManagementFactory
			.getThreadMXBean();

	/** Title of the scenario being run, used to name the error outputs and in the csv */
	private String scenario = "";
	private Path csvFile;

	@BeforeAll
	void setUpBeforeClass() throws Exception {
		// The test configuration logs at debug level in the goals, which would dominate
		// the measurements
		Configurator.setRootLevel(Level.WARN);
		for (String name : new String[] { "LeafGoalImpl", "AbstractGoal", "GoalAndImpl", "GoalOrImpl",
				"FuzzyNumberImpl", "LinearPiecewiseFunctionImpl", "LinearPieceWiseFunctionDataPoints" }) {
			Configurator.setLevel(name, Level.WARN);
		}
		// Warm up the JIT with small trees of each shape
		for (int i = 0; i < 3; i++) {
			assess(buildCompleteTree(3, 27, new Random(SEED)));
			assess(buildChain(20, new Random(SEED)));
			assess(buildWide(GoalType.AND, 20, new Random(SEED)));
			assess(buildWide(GoalType.OR, 20, new Random(SEED)));
		}

		Files.createDirectories(Paths.get(RESULTS_FOLDER));
		csvFile = Paths.get(RESULTS_FOLDER, getClass().getSimpleName() + ".csv");
		Files.write(csvFile, (CSV_HEADER + System.lineSeparator()).getBytes(),
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	// ------------------------------------------------------------------ measuring

	protected interface TreeBuilder {
		Goal build() throws FunctionOperationException;
	}

	/**
	 * Assesses the tree several times and prints and saves the statistics.
	 *
	 * @param label            short description of the experiment, for the console
	 * @param childrenPerNode  number of children of the intermediate nodes (for
	 *                         information in the results)
	 * @param leaves           number of leaves of the tree
	 * @param builder          creates the tree. It is called once per repetition so
	 *                         that nothing is cached between runs
	 */
	protected void measure(String label, int childrenPerNode, int leaves, TreeBuilder builder) throws Exception {
		long[] timesNs = new long[REPETITIONS];
		long[] allocated = new long[REPETITIONS];
		long[] retained = new long[REPETITIONS];
		int resultPoints = 0;
		int depth = 0;

		for (int r = 0; r < REPETITIONS; r++) {
			Goal root = builder.build();
			if (r == 0) {
				depth = depth(root);
			}
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
			// keep root reachable until the retained heap has been measured
			Assertions.assertNotNull(root);
		}

		System.out.println(String.format(Locale.ROOT,
				"%-22s leaves=%-6d depth=%-5d result points=%-6d time(ms) median=%-10.2f min=%-10.2f max=%-10.2f"
						+ " | allocated(MB) median=%-9.2f | retained(MB) median=%-9.2f",
				label, leaves, depth, resultPoints, ms(median(timesNs)), ms(min(timesNs)), ms(max(timesNs)),
				mb(median(allocated)), mb(median(retained))));

		String row = String.format(Locale.ROOT, "\"%s\",%d,%d,%d,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%d", scenario,
				childrenPerNode, leaves, depth, REPETITIONS, ms(median(timesNs)), ms(min(timesNs)), ms(max(timesNs)),
				mb(median(allocated)), mb(median(retained)), resultPoints);
		Files.write(csvFile, (row + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
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
	 * Allocation is measured by re-running the assessment and reading the allocated
	 * bytes of the thread that runs it (the one of assertTimeoutPreemptively), so
	 * that this second run also has the timeout.
	 */
	private long measureAllocation(Goal root) {
		return Assertions.assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT_SECONDS), () -> {
			long before = THREAD_BEAN.getThreadAllocatedBytes(Thread.currentThread().getId());
			root.assessSatisfaction();
			return THREAD_BEAN.getThreadAllocatedBytes(Thread.currentThread().getId()) - before;
		});
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

	/** Number of levels of intermediate goals below the root (a leaf has depth 0) */
	private static int depth(Goal goal) {
		if (goal instanceof LeafGoal) {
			return 0;
		}
		int max = 0;
		for (Goal child : goal.getChildren()) {
			max = Math.max(max, depth(child));
		}
		return max + 1;
	}

	// ------------------------------------------------------------- tree builders

	/**
	 * Complete tree with exactly the given number of leaves, in which every
	 * intermediate goal has "branching" children, except possibly one at the
	 * frontier of the last level. All levels but the last are full and the last
	 * level is filled from left to right (as in a heap). The type of each
	 * intermediate goal (AND or OR) is chosen randomly.
	 */
	protected static Goal buildCompleteTree(int branching, int leaves, Random rnd) throws FunctionOperationException {
		if (branching < 2 || leaves < 2) {
			throw new IllegalArgumentException("Complete trees need branching >= 2 and at least 2 leaves");
		}
		// Smallest number of nodes whose complete tree has the required leaves
		int nodes = 1;
		while (leavesInCompleteTree(nodes, branching) < leaves) {
			nodes++;
		}
		Goal[] tree = new Goal[nodes];
		for (int i = 0; i < nodes; i++) {
			boolean isLeaf = (long) branching * i + 1 >= nodes;
			tree[i] = isLeaf ? createLeaf(rnd) : GoalFactory.CreateGoal(randomType(rnd));
		}
		for (int i = 0; i < nodes; i++) {
			for (int c = 1; c <= branching; c++) {
				long child = (long) branching * i + c;
				if (child < nodes) {
					tree[i].addChild(tree[(int) child]);
				}
			}
		}
		return tree[0];
	}

	/** Leaves of a complete tree (heap shaped) of "nodes" nodes. */
	private static int leavesInCompleteTree(int nodes, int branching) {
		int intermediate = (nodes - 1 + branching - 1) / branching; // ceil((nodes-1)/branching)
		return nodes - intermediate;
	}

	/**
	 * Chain with the given number of leaves: each intermediate goal has a leaf and
	 * the next intermediate goal as children, and the last one has two leaves. The
	 * type of each intermediate goal is chosen randomly.
	 */
	protected static Goal buildChain(int leaves, Random rnd) throws FunctionOperationException {
		int levels = leaves - 1;
		Goal root = GoalFactory.CreateGoal(randomType(rnd));
		Goal current = root;
		for (int i = 0; i < levels; i++) {
			current.addChild(createLeaf(rnd));
			if (i < levels - 1) {
				Goal next = GoalFactory.CreateGoal(randomType(rnd));
				current.addChild(next);
				current = next;
			} else {
				current.addChild(createLeaf(rnd));
			}
		}
		return root;
	}

	protected static Goal buildWide(GoalType type, int leaves, Random rnd) throws FunctionOperationException {
		Goal root = GoalFactory.CreateGoal(type);
		for (int i = 0; i < leaves; i++) {
			root.addChild(createLeaf(rnd));
		}
		return root;
	}

	private static GoalType randomType(Random rnd) {
		return rnd.nextBoolean() ? GoalType.AND : GoalType.OR;
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

	/** Sets the scenario title (used in the csv) and prints it in the console */
	protected void printHeader(String title) {
		scenario = title;
		System.out.println();
		System.out.println("=== " + title + " (" + REPETITIONS + " repetitions) ===");
	}

	/** Numbers of leaves to test, from LEAVES_FROM to LEAVES_TO in steps of LEAVES_STEP */
	protected static int[] leafCounts() {
		int count = (LEAVES_TO - LEAVES_FROM) / LEAVES_STEP + 1;
		int[] counts = new int[count];
		for (int i = 0; i < count; i++) {
			counts[i] = LEAVES_FROM + i * LEAVES_STEP;
		}
		return counts;
	}

	protected static int[] parseInts(String csv) {
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
