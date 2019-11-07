package de.zabuza.maglev.external.algorithms;

import de.zabuza.maglev.external.graph.Edge;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface for a shortest path tree. That is the result of a shortest path exploration from given sources to possibly
 * multiple destinations.
 * <p>
 * It provides methods to construct the actual shortest path to a given destination and also to retrieve the leaves,
 * i.e. the boundary of the exploration.
 *
 * @param <N> The type of nodes
 * @param <E> The type of edges
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface PathTree<N, E extends Edge<N>> {
	/**
	 * The sources of the shortest path exploration, i.e. the roots of the tree. This can be a single but also multiple
	 * nodes, depending on the type of exploration.
	 *
	 * @return The sources of the shortest path exploration, can also only contain a single node. The collection is not
	 * modifiable.
	 */
	Collection<N> getSources();

	/**
	 * Gets a stream over all nodes that are reachable from the given sources, i.e. all nodes in the tree. This includes
	 * the sources themselves.
	 * <p>
	 * Implementations must construct the stream in {@code O(1)} time.
	 *
	 * @return All reachable nodes, including the sources
	 */
	Stream<N> getReachableNodes();

	/**
	 * Constructs the shortest path from the closest of the given sources to the given destination, i.e. the path from
	 * the roots down to the given destination.
	 *
	 * @param destination The destination to construct the path to
	 *
	 * @return The shortest path from one of the sources to the given destination. Or empty if the destination is not
	 * reachable.
	 */
	Optional<Path<N, E>> getPathTo(N destination);

	/**
	 * Gets the boundary nodes of the shortest path exploration, i.e. the leaves of the tree. Such nodes are farthest
	 * away from the given set of sources in their respective direction.
	 *
	 * @return The boundary nodes of the exploration, can include source nodes
	 */
	Collection<N> getLeaves();
}
