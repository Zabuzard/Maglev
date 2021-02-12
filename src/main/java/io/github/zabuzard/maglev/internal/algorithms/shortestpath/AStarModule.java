package io.github.zabuzard.maglev.internal.algorithms.shortestpath;

import io.github.zabuzard.maglev.external.algorithms.DijkstraModule;
import io.github.zabuzard.maglev.external.algorithms.Metric;
import io.github.zabuzard.maglev.external.graph.Edge;

import java.util.OptionalDouble;

/**
 * Implementation of the A-Star algorithm as {@link Module} for a {@link ModuleDijkstra} that speedups shortest path
 * communication on graphs by estimating the distance between nodes using a heuristic metric.<br>
 * <br>
 * The heuristic metric must be <i>monotone</i> and <i>admissible</i>.<br>
 * <br>
 * The factory method {@link #of(Metric)} can be used for convenient instance creation.
 *
 * @param <N> Type of node
 * @param <E> Type of edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class AStarModule<N, E extends Edge<N>> implements DijkstraModule<N, E> {
	/**
	 * Creates an AStar algorithm instance using the given metric.
	 *
	 * @param <N>    Type of the node
	 * @param <E>    Type of the edge
	 * @param metric The metric to use
	 *
	 * @return The created AStar algorithm instance
	 */
	public static <N, E extends Edge<N>> AStarModule<N, E> of(final Metric<? super N> metric) {
		return new AStarModule<>(metric);
	}

	/**
	 * The heuristic metric to use.
	 */
	private final Metric<? super N> metric;

	/**
	 * Creates a new A-Star algorithm module which uses the given heuristic metric.
	 *
	 * @param metric The heuristic metric which must be <i>monotone</i> and
	 *               <i>admissible</i>
	 */
	private AStarModule(final Metric<? super N> metric) {
		this.metric = metric;
	}

	/**
	 * Gets an estimate about the shortest path distance from the given node to the destination of the shortest path
	 * computation.<br>
	 * <br>
	 * Therefore, it estimates the distance by using the given metric.
	 */
	@Override
	public OptionalDouble getEstimatedDistance(final N node, final N pathDestination) {
		return OptionalDouble.of(metric.distance(node, pathDestination));
	}

}