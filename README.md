# Library for the assessment of goal fulfillment in uncertain scenarios using fuzzy theory

This code includes the developments of the library and experiments published in *Embracing Uncertainty: A Fuzzy Theoretical Model for Goal Fulfillment Assessment*

Please, refer to that paper if you wish to cite this library


## Stress tests

The stress tests measure the execution time and the memory usage of the assessment of large goal trees. They are in `src/test/java/se/lnu/eres/fuzzy/stress/` and they are excluded from the normal build because they are slow, so `mvn test` and `mvn clean install` do not run them. They are intended for release candidates, like integration tests. Java 21 and Maven are needed.

There is one class for each tree shape, with trees of 100 to 1000 leaves in steps of 100:

| Class | Trees |
|---|---|
| `GoalTreeBalancedStressTest` | Complete trees with 2, 4, 6, 8 and 10 children per intermediate goal, and random AND/OR goals |
| `GoalTreeDeepChainStressTest` | Deep trees, where each intermediate goal has a leaf and the next intermediate goal as children (up to 999 levels), and random AND/OR goals |
| `GoalTreeWideStressTest` | One AND goal, and one OR goal, with all the leaves as direct children |

### Running the stress tests

The `stress` profile enables them.

Run the whole battery (stress tests together with the regular unit tests):

```
mvn clean test -Pstress
```

or as part of a full build:

```
mvn clean install -Pstress
```

Run only the stress tests:

```
mvn test -Pstress -Dtest='GoalTree*StressTest'
```

Run the tests of one tree shape:

```
mvn test -Pstress -Dtest=GoalTreeBalancedStressTest
mvn test -Pstress -Dtest=GoalTreeDeepChainStressTest
mvn test -Pstress -Dtest=GoalTreeWideStressTest
```

Note that any use of `-Dtest=...` overrides the exclusion of the stress tests, so with `-Dtest` the tests are executed even without `-Pstress`. Always list the classes to run explicitly, because patterns that only exclude classes (such as `-Dtest='!SomeTest'`) also run the stress tests.

### Options

The experiments can be configured with system properties in the command line:

| Property | Default | Meaning |
|---|---|---|
| `perf.repetitions` | 5 | Repetitions of each configuration (the median, minimum and maximum are reported) |
| `perf.leaves.from` | 100 | Smallest number of leaves |
| `perf.leaves.to` | 1000 | Largest number of leaves |
| `perf.leaves.step` | 100 | Step between numbers of leaves |
| `perf.balanced.branching` | `2,4,6,8,10` | Children per intermediate goal in the balanced trees (comma separated) |
| `perf.timeout.seconds` | 900 | Maximum time of a single assessment |

For example, a quick check with smaller trees:

```
mvn test -Pstress -Dtest='GoalTree*StressTest' -Dperf.leaves.to=300 -Dperf.repetitions=2
```

### Results

Each configuration prints a line in the console with the execution time (median, minimum and maximum), the memory allocated during the assessment, the memory retained after it, and the number of points of the result. The same results are written, one file per class, to `target/stress-results/`, as CSV files with the columns `scenario, children_per_node, leaves, depth, repetitions, time_median_ms, time_min_ms, time_max_ms, allocated_median_mb, retained_median_mb, result_points`.

The tests also check that the result of each assessment is a fuzzy boolean. If it is not, the test fails after printing the points of the result and saving a plot in `target/images/test-errors/` (a desktop environment is needed to create the plot).
