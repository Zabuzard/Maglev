package io.github.zabuzard.maglev.external.algorithms;

import io.github.zabuzard.maglev.external.graph.Edge;
import io.github.zabuzard.maglev.external.graph.Graph;
import io.github.zabuzard.maglev.internal.algorithms.metrics.GreedyFarthestLandmarkProvider;
import io.github.zabuzard.maglev.internal.algorithms.metrics.LandmarkMetric;
import io.github.zabuzard.maglev.internal.algorithms.metrics.RandomLandmarkProvider;
import io.github.zabuzard.maglev.internal.algorithms.shortestpath.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Builder for convenient construction of {@link ShortestPathComputation} instances.
 * <p>
 * The builder offers highly customizable algorithms based on Dijkstra, called Module-Dijkstra. That is a regular
 * Dijkstra algorithm which can be extended using extension modules that modify its behavior. Offered modules are:
 * <ul>
 *     <li>{@code AbortAfterIfModule} - Aborts further computation as soon as a node that matches a given predicate has been settled</li>
 *     <li>{@code AbortBeforeIfModule} - Aborts further computation as soon as a node that matches a given predicate would be settled</li>
 *     <li>{@code AbortAfterRangeModule} - Only explores shortest paths up to the given range</li>
 *     <li>{@code IgnoreEdgeIfModule} - Ignores exploring edges that match the given predicate</li>
 *     <li>{@code AStarModule} - Optimization of the algorithm by utilizing a given heuristic metric</li>
 * </ul>
 * It is also possible to add custom modules by simply implementing {@link DijkstraModule}.
 * Modules can be added by using {@link #addModule(DijkstraModule)} and the other {@code addModuleXXX} methods.
 * <p>
 * If the {@code AStarModule} was chosen, a heuristic metric must be given. Offered metrics are:
 * <ul>
 *     <li>{@code LandmarkMetric} - Dynamic heuristic computed based on the underlying graph model (also known as <i>ALT</i> algorithm)</li>
 * </ul>
 * It is also possible to use a custom metric by simply implementing {@link Metric}, for example a metric based on the <i>Euclidean distance</i>.
 * A metric can be set by using {@link #setMetric(Metric)} and the other {@code setMetricXXX} methods.
 * <p>
 * If the {@code LandmarkMetric} was chosen, the amount of landmarks must be set, which can be done using {@link #setAmountOfLandmarks(int)}.
 * Additionally, a landmark provider must be given. Offered landmark providers are:
 * <ul>
 *     <li>{@code GreedyFarthestLandmarkProvider} - Chooses landmarks that are optimally spread across the graph by dynamically utilizing its structure</li>
 *     <li>{@code RandomLandmarkProvider} - Randomly selects nodes as landmarks</li>
 * </ul>
 * It is also possible to use a custom landmark provider by simply implementing {@link LandmarkProvider}.
 * A landmark provider can be set by using {@link #setLandmarkProvider(LandmarkProvider)} and the other {@code setLandmarkProviderXXX} methods.
 * <p>
 * Finally, an algorithm using the selected properties can be created using {@link #build()}.
 * The initial construction might take a while, depending on the graph size. Results are cached and further constructions
 * will try to utilize the cache whenever possible.
 * <p>
 * The <b>default configuration</b> of the builder is:
 * <ul>
 *     <li>{@code AStarModule}</li>
 *     <li>{@code LandmarkMetric}</li>
 *     <li>20 landmarks</li>
 *     <li>{@code RandomLandmarkProvider}</li>
 * </ul>
 * The method {@link #resetDefault()} can be used to restore the default settings.
 * Likewise {@link #resetOrdinaryDijkstra()} can be used to get a configuration that just uses the
 * ordinary Dijkstra algorithm without any modules.
 *
 * @param <N> Type of node
 * @param <E> Type of edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class ShortestPathComputationBuilder<N, E extends Edge<N>> {
	/**
	 * The default amount of landmarks to use.
	 */
	private static final int DEFAULT_AMOUNT_OF_LANDMARKS = 20;
	/**
	 * List of modules to use for {@link ModuleDijkstra}. Will use ordinary {@link Dijkstra} if empty.
	 */
	private final List<DijkstraModule<N, E>> modules;
	/**
	 * The graph to operate on.
	 */
	private Graph<N, E> graph;
	/**
	 * Whether to use an {@link AStarModule}. Will not be added to {@link #modules} but implicitly added during {@link
	 * #build()}.
	 */
	private boolean useAStarModule;
	/**
	 * The metric to use, if {@link #useAStarModule} is set. Building a metric may be expensive, the result is cached as
	 * long as possible and will be reset to {@code null} whenever the cache needs to be invalidated.
	 */
	private Metric<N> metric;
	/**
	 * Whether to use a {@link LandmarkMetric}. Only interpreted if {@link #useAStarModule} is set. Will not directly be
	 * set to {@link #metric}, as computation might be expensive. The result is set later during {@link #build()}.
	 */
	private boolean useLandmarkMetric;
	/**
	 * The landmark provider to use, if {@link #useLandmarkMetric} is set.
	 */
	private LandmarkProvider<N> landmarkProvider;
	/**
	 * The amount of landmarks to use, if {@link #useLandmarkMetric} is set.
	 */
	private int amountOfLandmarks;

	/**
	 * Creates a new shortest path computation builder operating on the given graph with default settings:
	 * <ul>
	 *     <li>{@code AStarModule}</li>
	 *     <li>{@code LandmarkMetric}</li>
	 *     <li>20 landmarks</li>
	 *     <li>{@code RandomLandmarkProvider}</li>
	 * </ul>
	 *
	 * @param graph The graph to operate on, not null
	 */
	public ShortestPathComputationBuilder(final Graph<N, E> graph) {
		this.graph = Objects.requireNonNull(graph);
		modules = new ArrayList<>();

		resetDefault();
	}

	/**
	 * Sets the graph to operate on.
	 *
	 * @param graph The graph to operate on, not null
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> setGraph(final Graph<N, E> graph) {
		this.graph = Objects.requireNonNull(graph);
		invalidateMetricCache();
		return this;
	}

	/**
	 * Resets the current configuration to the default settings:
	 * <ul>
	 *     <li>{@code AStarModule}</li>
	 *     <li>{@code LandmarkMetric}</li>
	 *     <li>20 landmarks</li>
	 *     <li>{@code RandomLandmarkProvider}</li>
	 * </ul>
	 *
	 * @return This builder instance
	 */
	@SuppressWarnings({ "WeakerAccess", "UnusedReturnValue" })
	public ShortestPathComputationBuilder<N, E> resetDefault() {
		modules.clear();
		useAStarModule = true;
		metric = null;
		useLandmarkMetric = true;
		landmarkProvider = new RandomLandmarkProvider<>(graph);
		amountOfLandmarks = ShortestPathComputationBuilder.DEFAULT_AMOUNT_OF_LANDMARKS;
		return this;
	}

	/**
	 * Resets the current configuration to a setting that uses the ordinary Dijkstra algorithm, without any modules.
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> resetOrdinaryDijkstra() {
		modules.clear();
		useAStarModule = false;
		metric = null;
		useLandmarkMetric = false;
		landmarkProvider = new RandomLandmarkProvider<>(graph);
		amountOfLandmarks = ShortestPathComputationBuilder.DEFAULT_AMOUNT_OF_LANDMARKS;
		return this;
	}

	/**
	 * Adds the given module to be used by {@code Module-Dijkstra}.
	 *
	 * @param module The module to add, not null
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> addModule(final DijkstraModule<N, E> module) {
		modules.add(Objects.requireNonNull(module));
		return this;
	}

	/**
	 * Adds a module to be used by {@code Module-Dijkstra} which aborts computation right before a node has been settled
	 * that matches the given predicate.
	 *
	 * @param predicate The predicate to test the node against, not null
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> addModuleAbortBeforeIf(
			final Predicate<? super TentativeDistance<N, E>> predicate) {
		modules.add(AbortBeforeIfModule.of(Objects.requireNonNull(predicate)));
		return this;
	}

	/**
	 * Adds a module to be used by {@code Module-Dijkstra} which aborts computation as soon as a node has been settled
	 * that matches the given predicate.
	 *
	 * @param predicate The predicate to test the node against, not null
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> addModuleAbortAfterIf(
			final Predicate<? super TentativeDistance<N, E>> predicate) {
		modules.add(AbortAfterIfModule.of(Objects.requireNonNull(predicate)));
		return this;
	}

	/**
	 * Adds a module to be used by {@code Module-Dijkstra} which ignores exploring edges if they match the given
	 * predicate.
	 *
	 * @param predicate The predicate to test the edge against, not null
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> addModuleIgnoreEdgeIf(final Predicate<? super E> predicate) {
		modules.add(IgnoreEdgeIfModule.of(Objects.requireNonNull(predicate)));
		return this;
	}

	/**
	 * Adds a module to be used by {@code Module-Dijkstra} which only explores the graph up until the given range.
	 *
	 * @param range The range to explore to, not negative
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> addModuleAbortAfterRange(final double range) {
		if (range < 0) {
			throw new IllegalArgumentException("Range must not be negative");
		}
		modules.add(AbortAfterRangeModule.of(range));
		return this;
	}

	/**
	 * Adds a module to be used by {@code Module-Dijkstra} which uses the A-Star algorithm with a given heuristic metric
	 * for optimization.
	 * <p>
	 * Use {@link #setMetric(Metric)} or similar methods to set a metric.
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> addModuleAStar() {
		useAStarModule = true;
		return this;
	}

	/**
	 * Removes a previously added A-Star module. Has no effect if such a module was not added before.
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> removeModuleAStar() {
		useAStarModule = false;
		return this;
	}

	/**
	 * Clears all previously added modules to be used by {@code Module-Dijkstra}.
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> clearModules() {
		modules.clear();
		useAStarModule = false;
		return this;
	}

	/**
	 * Sets the metric to be used by the A-Star module. Use {@link #addModuleAStar()} to add such a module.
	 * <p>
	 * Has no effect if no such module has been added.
	 *
	 * @param metric The heuristic metric to use, not null
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> setMetric(final Metric<N> metric) {
		this.metric = Objects.requireNonNull(metric);
		useLandmarkMetric = false;
		return this;
	}

	/**
	 * Sets the metric to be used by the A-Star module to a landmark heuristic. Use {@link #addModuleAStar()} to add
	 * such a module.
	 * <p>
	 * Has no effect if no such module has been added.
	 * <p>
	 * The landmark heuristic needs a landmark provider, use {@link #setLandmarkProvider(LandmarkProvider)} and similar
	 * methods to set one. Additionally, it needs the amount of landmarks to use, use {@link #setAmountOfLandmarks(int)}
	 * to set the amount.
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> setMetricLandmark() {
		useLandmarkMetric = true;
		invalidateMetricCache();
		return this;
	}

	/**
	 * Sets the landmark provider to be used by the landmark heuristic. Use {@link #setMetricLandmark()} to use such a
	 * metric.
	 * <p>
	 * Has no effect if the chosen metric is not landmarks.
	 *
	 * @param landmarkProvider The landmark provider to use, not null
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> setLandmarkProvider(final LandmarkProvider<N> landmarkProvider) {
		this.landmarkProvider = Objects.requireNonNull(landmarkProvider);
		invalidateMetricCache();
		return this;
	}

	/**
	 * Sets the landmark provider to be used by the landmark heuristic to a greedy farthest strategy. Use {@link
	 * #setMetricLandmark()} to use such a metric.
	 * <p>
	 * Has no effect if the chosen metric is not landmarks.
	 * <p>
	 * This strategy is more expensive than {@link #setLandmarkProviderRandom()} and consumes more space, but yields
	 * better results.
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> setLandmarkProviderGreedyFarthest() {
		landmarkProvider = new GreedyFarthestLandmarkProvider<>(graph);
		invalidateMetricCache();
		return this;
	}

	/**
	 * Sets the landmark provider to be used by the landmark heuristic to a random selection strategy. Use {@link
	 * #setMetricLandmark()} to use such a metric.
	 * <p>
	 * Has no effect if the chosen metric is not landmarks.
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> setLandmarkProviderRandom() {
		landmarkProvider = new RandomLandmarkProvider<>(graph);
		invalidateMetricCache();
		return this;
	}

	/**
	 * Sets the amount of landmarks to be used by the landmark heuristic. Use {@link #setMetricLandmark()} to use such a
	 * metric.
	 * <p>
	 * Has no effect if the chosen metric is not landmarks.
	 * <p>
	 * The more landmarks, the better the heuristic, but the longer the memory consumption and pre-computation takes.
	 *
	 * @param amountOfLandmarks The amount of landmarks to use, not negative.
	 *
	 * @return This builder instance
	 */
	public ShortestPathComputationBuilder<N, E> setAmountOfLandmarks(final int amountOfLandmarks) {
		if (amountOfLandmarks < 0) {
			throw new IllegalArgumentException("Amount of landmarks must not be negative");
		}
		this.amountOfLandmarks = amountOfLandmarks;
		return this;
	}

	/**
	 * Builds a shortest path computation algorithm using the set properties.
	 * <p>
	 * Depending on the graph size and the current settings, the initial call to this method might take some time.
	 * However, results are cached and further calls try to utilize the cache as much as possible.
	 *
	 * @return An algorithm using the set properties
	 *
	 * @throws IllegalStateException If the builder has an invalid configuration, such as specifying A-Star module but
	 *                               no metric.
	 */
	public ShortestPathComputation<N, E> build() {
		if (modules.isEmpty() && !useAStarModule) {
			return new Dijkstra<>(graph);
		}

		final ModuleDijkstra<N, E> moduleDijkstra = new ModuleDijkstra<>(graph);

		if (useAStarModule) {
			if (useLandmarkMetric && metric == null) {
				// Construction takes time, result is cached in field
				metric = new LandmarkMetric<>(amountOfLandmarks, graph, landmarkProvider);
			}
			if (metric == null) {
				throw new IllegalStateException("Invalid builder configuration, no metric is set");
			}
			moduleDijkstra.addModule(AStarModule.of(metric));
		}

		modules.forEach(moduleDijkstra::addModule);

		return moduleDijkstra;
	}

	/**
	 * Invalidates previously cached metrics if the current settings would utilize the metric if {@link #build()} would
	 * be called now.
	 */
	private void invalidateMetricCache() {
		if (!useLandmarkMetric || metric == null) {
			return;
		}
		metric = null;
	}

}
