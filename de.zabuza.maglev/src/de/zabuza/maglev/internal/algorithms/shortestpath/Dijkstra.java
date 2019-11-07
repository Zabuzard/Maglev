package de.zabuza.maglev.internal.algorithms.shortestpath;

import de.zabuza.maglev.external.algorithms.HasPathCost;
import de.zabuza.maglev.external.algorithms.Path;
import de.zabuza.maglev.external.algorithms.PathTree;
import de.zabuza.maglev.external.algorithms.TentativeDistance;
import de.zabuza.maglev.external.graph.Edge;
import de.zabuza.maglev.external.graph.Graph;

import java.util.*;
import java.util.stream.Stream;

/**
 * Implementation of Dijkstras algorithm that is able to compute shortest paths on a given graph.<br>
 * <br>
 * Dijkstra has no sense of goal direction. When finishing the computation of the shortest path it has also computed all
 * shortest paths to nodes less far away, so the <i>search space</i> is rather big.<br>
 * <br>
 * Subclasses can override {@link #doConsiderEdgeForRelaxation(Edge, Object, TentativeDistance)} and {@link
 * #getEstimatedDistance(Object, Object)} to speedup the algorithm by giving it a sense of goal direction or exploiting
 * precomputed knowledge.
 *
 * @param <N> Type of the node
 * @param <E> Type of the edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@SuppressWarnings("DesignForExtension")
public class Dijkstra<N, E extends Edge<N>> extends AbstractShortestPathComputation<N, E> {
	/**
	 * The graph to operate on.
	 */
	private final Graph<? super N, E> graph;

	/**
	 * Creates a new Dijkstra instance which operates on the given graph.
	 *
	 * @param graph The graph to operate on
	 */
	public Dijkstra(final Graph<? super N, E> graph) {
		this.graph = graph;
	}

	@Override
	public Collection<N> searchSpace(final Collection<? extends N> sources, final N destination) {
		return computeShortestPathCostHelper(sources, null).keySet();
	}

	@Override
	public Optional<Path<N, E>> shortestPath(final Collection<? extends N> sources, final N destination) {
		final Map<N, TentativeDistance<N, E>> nodeToDistance = computeShortestPathCostHelper(sources, destination);
		final TentativeDistance<N, E> destinationDistance = nodeToDistance.get(destination);

		// Destination is not reachable from the given sources
		if (destinationDistance == null) {
			return Optional.empty();
		}

		final E parentEdge = destinationDistance.getParentEdge();
		// Destination is already a source node
		if (parentEdge == null) {
			return Optional.of(new EmptyPath<>(destination));
		}

		// Build the path reversely by following the pointers from the destination
		// to one of the sources
		final EdgePath<N, E> path = new EdgePath<>(EdgePath.ConstructionDirection.BACKWARD);
		TentativeDistance<N, E> currentDistanceContainer = destinationDistance;
		E currentEdge = parentEdge;
		while (currentEdge != null) {
			// Add the edge
			final double distance = currentDistanceContainer.getTentativeDistance();
			final N parent = currentEdge.getSource();
			final TentativeDistance<N, E> parentDistanceContainer = nodeToDistance.get(parent);
			final double parentDistance = parentDistanceContainer.getTentativeDistance();

			path.addEdge(currentEdge, distance - parentDistance);

			// Prepare next round
			currentEdge = parentDistanceContainer.getParentEdge();
			currentDistanceContainer = parentDistanceContainer;
		}
		return Optional.of(path);
	}

	@Override
	public Optional<Double> shortestPathCost(final Collection<? extends N> sources, final N destination) {
		final Map<N, TentativeDistance<N, E>> nodeToDistance = computeShortestPathCostHelper(sources, destination);
		return Optional.ofNullable(nodeToDistance.get(destination))
				.map(TentativeDistance::getTentativeDistance);
	}

	@Override
	public Map<N, ? extends HasPathCost> shortestPathCostsReachable(final Collection<? extends N> sources) {
		return computeShortestPathCostHelper(sources, null);
	}

	@Override
	public PathTree<N, E> shortestPathReachable(final Collection<N> sources) {
		return new OnDemandPathTree<>(sources, computeShortestPathCostHelper(sources, null));
	}

	/**
	 * Computes the shortest path from the given sources to the given destination and to all other nodes that were
	 * visited in the mean time.<br>
	 * <br>
	 * The shortest path from multiple sources is the minimal shortest path for all source nodes individually. If the
	 * destination is {@code null} the shortest paths to all nodes in the graph are computed.
	 *
	 * @param sources         The sources to compute the shortest path from
	 * @param pathDestination The destination to compute the shortest path to or {@code null} if not present
	 *
	 * @return A map connecting all visited nodes to their tentative distance container. The container represent the
	 * shortest path from the sources to that given node as destination.
	 */
	@SuppressWarnings("WeakerAccess")
	protected Map<N, TentativeDistance<N, E>> computeShortestPathCostHelper(final Collection<? extends N> sources,
			final N pathDestination) {
		final Map<N, TentativeDistance<N, E>> nodeToDistance = new HashMap<>(sources.size());
		final Map<N, TentativeDistance<N, E>> nodeToSettledDistance = new HashMap<>(sources.size());
		final PriorityQueue<TentativeDistance<N, E>> activeNodes = new PriorityQueue<>(sources.size());

		// Sources are initial active nodes
		for (final N source : sources) {
			// Create a container for the node
			final TentativeDistance<N, E> distance = createDistance(source, null, 0.0, pathDestination);
			// Put the distance as active node
			nodeToDistance.put(source, distance);
			activeNodes.add(distance);
		}

		// Poll and settle all active nodes
		while (!activeNodes.isEmpty()) {
			final TentativeDistance<N, E> distance = activeNodes.poll();
			final N node = distance.getNode();
			final double tentativeDistance = distance.getTentativeDistance();

			// Skip the element if the node was already settled before. In that case
			// there was a better path to this node around and this path was
			// abandoned.
			if (nodeToSettledDistance.containsKey(node)) {
				continue;
			}

			// End the algorithm if a subclass implementation demands it
			if (shouldAbortBefore(distance)) {
				break;
			}

			// Settle the current node
			nodeToSettledDistance.put(node, distance);

			// End the algorithm if destination was settled or a subclass
			// implementation demands it
			//noinspection PointlessNullCheck
			if ((pathDestination != null && node.equals(pathDestination)) || shouldAbortAfter(distance)) {
				break;
			}

			// Relax all outgoing edges
			provideEdgesToRelax(distance).forEach(edge -> {
				// Skip the edge if it should not be considered
				if (!doConsiderEdgeForRelaxation(edge, pathDestination, distance)) {
					return;
				}

				final N destination = edge.getDestination();
				final double tentativeEdgeDistance = tentativeDistance + provideEdgeCost(edge, tentativeDistance);

				// Check if the destination is visited for the first time
				TentativeDistance<N, E> destinationDistance = nodeToDistance.get(destination);
				if (destinationDistance == null) {
					// Create a container for the destination
					destinationDistance = createDistance(destination, edge, tentativeEdgeDistance, pathDestination);

					// Put the distance as active node
					nodeToDistance.put(destination, destinationDistance);
					activeNodes.add(destinationDistance);

					// Relaxation has finished
					return;
				}

				// Destination is not visited for the first time
				// Don't relax if the node was already settled
				if (nodeToSettledDistance.containsKey(destination)) {
					return;
				}

				// Don't relax if the edge does not improve the distance to this
				// destination
				if (tentativeEdgeDistance >= destinationDistance.getTentativeDistance()) {
					return;
				}

				// Improve the distance by replacing the old container with a new one
				// representing the path taken by this edge
				destinationDistance = createDistance(destination, edge, tentativeEdgeDistance, pathDestination);
				// Replace the old distance by the new one and set as active
				nodeToDistance.put(destination, destinationDistance);
				activeNodes.add(destinationDistance);
			});
		}

		return nodeToSettledDistance;
	}

	/**
	 * Whether or not the given edge should be considered for relaxation. The algorithm will ignore the edge and not
	 * follow it if this method returns {@code false}.
	 *
	 * @param edge              The edge in question
	 * @param pathDestination   The destination of the shortest path computation or {@code null} if not present
	 * @param tentativeDistance The current tentative distance when relaxing this edge
	 *
	 * @return {@code True} if the edge should be considered, {@code false} otherwise
	 */
	@SuppressWarnings({ "unused", "WeakerAccess", "BooleanMethodNameMustStartWithQuestion" })
	protected boolean doConsiderEdgeForRelaxation(final E edge, final N pathDestination,
			final TentativeDistance<? extends N, E> tentativeDistance) {
		// Dijkstras algorithm considers every outgoing edge.
		// This method may be used by extending classes to improve performance.
		return true;
	}

	/**
	 * Gets an estimate about the shortest path distance from the given node to the destination of the shortest path
	 * computation.<br>
	 * <br>
	 * The estimate must be <i>monotone</i> and <i>admissible</i>.
	 *
	 * @param node            The node to estimate the distance from
	 * @param pathDestination The destination to estimate the distance to
	 *
	 * @return An estimate about the shortest path distance
	 */
	@SuppressWarnings({ "unused", "WeakerAccess" })
	protected double getEstimatedDistance(final N node, final N pathDestination) {
		// Dijkstras algorithm does not use estimations. It makes the worst possible
		// guess of 0 for every node.
		// This method may be used by extending classes to improve performance.
		return 0.0;
	}

	/**
	 * Provides the cost of a given edge.<br>
	 * <br>
	 * The base is the result of {@link Edge#getCost()}. Implementations are allowed to override this method in order to
	 * modify the cost.
	 *
	 * @param edge              The edge whose cost to provide
	 * @param tentativeDistance The current tentative distance when relaxing this edge
	 *
	 * @return Stream of edges to process for relaxation
	 */
	@SuppressWarnings("WeakerAccess")
	protected double provideEdgeCost(final E edge, @SuppressWarnings("unused") final double tentativeDistance) {
		return edge.getCost();
	}

	/**
	 * Generates a stream of edges to process for relaxation.<br>
	 * <br>
	 * The base are all outgoing edges of the given node. Implementations are allowed to override this method in order
	 * to further filter the stream. Additionally, the method {@link #doConsiderEdgeForRelaxation(Edge, Object,
	 * TentativeDistance)} will be called on each element of this stream.
	 *
	 * @param tentativeDistance The tentative distance wrapper of the node to relax edges of
	 *
	 * @return Stream of edges to process for relaxation
	 */
	@SuppressWarnings("WeakerAccess")
	protected Stream<E> provideEdgesToRelax(final TentativeDistance<? extends N, E> tentativeDistance) {
		return graph.getOutgoingEdges(tentativeDistance.getNode());
	}

	/**
	 * Whether or not the algorithm should abort computation of the shortest path. The method is called right before the
	 * given node will be settled.
	 *
	 * @param tentativeDistance The tentative distance wrapper of the node that will be settled next
	 *
	 * @return {@code True} if the computation should be aborted, {@code false} if not
	 */
	@SuppressWarnings("WeakerAccess")
	protected boolean shouldAbortBefore(@SuppressWarnings("unused") final TentativeDistance<N, E> tentativeDistance) {
		// Dijkstras algorithm relaxes the whole network, it only aborts if the
		// target was settled. However, the method can be used by subclasses to
		// abort computation earlier, for example after exploring to a fixed
		// distance.
		return false;
	}

	/**
	 * Whether or not the algorithm should abort computation of the shortest path. The method is called right after the
	 * given node has been settled.
	 *
	 * @param tentativeDistance The tentative distance wrapper of the node that was settled
	 *
	 * @return {@code True} if the computation should be aborted, {@code false} if not
	 */
	@SuppressWarnings("WeakerAccess")
	protected boolean shouldAbortAfter(@SuppressWarnings("unused") final TentativeDistance<N, E> tentativeDistance) {
		// Dijkstras algorithm relaxes the whole network, it only aborts if the
		// target was settled. However, the method can be used by subclasses to
		// abort computation earlier, for example after exploring to a fixed
		// distance.
		return false;
	}

	/**
	 * Creates a tentative distance container for the given node.<br>
	 * <br>
	 * If the {@code pathDestination} is not {@code null} the container will also include an estimated distance from the
	 * node to the destination. This is computed using {@link #getEstimatedDistance(N, N)}.
	 *
	 * @param node              The node to create the container for
	 * @param parentEdge        The parent edge that lead to that node, used for shortest path construction by
	 *                          backtracking
	 * @param tentativeDistance The tentative distance from the source to that node, i.e. the total cost of backtracking
	 *                          the given parent edges to the source
	 * @param pathDestination   The destination of the shortest path computation or {@code null} if not present
	 *
	 * @return A tentative distance container for the given node
	 */
	private TentativeDistance<N, E> createDistance(final N node, final E parentEdge, final double tentativeDistance,
			final N pathDestination) {
		if (pathDestination == null) {
			return new TentativeDistance<>(node, parentEdge, tentativeDistance);
		}

		final double estimatedDistance = getEstimatedDistance(node, pathDestination);
		return new TentativeDistance<>(node, parentEdge, tentativeDistance, estimatedDistance);
	}

}