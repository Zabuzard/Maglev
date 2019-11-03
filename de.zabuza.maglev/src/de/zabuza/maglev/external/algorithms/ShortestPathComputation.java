package de.zabuza.maglev.external.algorithms;

import de.zabuza.maglev.external.graph.Edge;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for algorithms that are able to compute shortest paths from a source to a destination.
 * <p>
 * Use {@link ShortestPathComputationBuilder} for convenient construction of instances.
 *
 * @param <N> Type of node
 * @param <E> Type of edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface ShortestPathComputation<N, E extends Edge<N>> {
	/**
	 * Computes and returns a collection of all nodes that were visited by the algorithm while computing the shortest
	 * path from the given sources to the given destination. This is known as <i>search space</i> and is primarily used
	 * for debugging and benchmarking.<br>
	 * <br>
	 * The shortest path from multiple sources is the minimal shortest path for all source nodes individually.
	 *
	 * @param sources     The sources to compute the shortest path from
	 * @param destination The destination to compute the shortest path to
	 *
	 * @return A collection of all visited nodes, the <i>search space</i>
	 */
	Collection<N> searchSpace(Collection<? extends N> sources, N destination);

	/**
	 * Computes and returns a collection of all nodes that were visited by the algorithm while computing the shortest
	 * path from the given source to the given destination. This is known as <i>search space</i> and is primarily used
	 * for debugging and benchmarking.
	 *
	 * @param source      The source to compute the shortest path from
	 * @param destination The destination to compute the shortest path to
	 *
	 * @return A collection of all visited nodes, the <i>search space</i>
	 */
	Collection<N> searchSpace(N source, N destination);

	/**
	 * Computes the shortest path from the given sources to the given destination.<br>
	 * <br>
	 * The shortest path from multiple sources is the minimal shortest path for all source nodes individually.
	 *
	 * @param sources     The sources to compute the shortest path from
	 * @param destination The destination to compute the shortest path to
	 *
	 * @return The shortest path if present, else empty
	 */
	Optional<Path<N, E>> shortestPath(Collection<? extends N> sources, N destination);

	/**
	 * Computes the shortest path from the given source to the given destination.
	 *
	 * @param source      The source to compute the shortest path from
	 * @param destination The destination to compute the shortest path to
	 *
	 * @return The shortest path if present, else empty
	 */
	Optional<Path<N, E>> shortestPath(N source, N destination);

	/**
	 * Computes the cost of the shortest path from the given sources to the given destination.<br>
	 * <br>
	 * The shortest path from multiple sources is the minimal shortest path for all source nodes individually.
	 *
	 * @param sources     The sources to compute the shortest path from
	 * @param destination The destination to compute the shortest path to
	 *
	 * @return The cost of the shortest path if present, else empty
	 */
	Optional<Double> shortestPathCost(Collection<? extends N> sources, N destination);

	/**
	 * Computes the cost of the shortest path from the given source to the given destination.
	 *
	 * @param source      The source to compute the shortest path from
	 * @param destination The destination to compute the shortest path to
	 *
	 * @return The cost of the shortest path if present, else empty
	 */
	Optional<Double> shortestPathCost(N source, N destination);

	/**
	 * Computes the costs of all shortest paths from the given sources to all other nodes.<br>
	 * <br>
	 * The shortest path from multiple sources is the minimal shortest path for all source nodes individually.
	 *
	 * @param sources The sources to compute the shortest path from
	 *
	 * @return A map which connects destination nodes to the costs of their shortest path
	 */
	Map<N, ? extends HasPathCost> shortestPathCostsReachable(Collection<? extends N> sources);

	/**
	 * Computes the costs of all shortest paths from the given source to all other nodes.
	 *
	 * @param source The source to compute the shortest path from
	 *
	 * @return A map which connects destination nodes to the costs of their shortest path
	 */
	Map<N, ? extends HasPathCost> shortestPathCostsReachable(N source);
}