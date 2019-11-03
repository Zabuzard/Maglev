package de.zabuza.maglev.internal.algorithms.metrics;

import de.zabuza.maglev.external.algorithms.HasPathCost;
import de.zabuza.maglev.external.algorithms.LandmarkProvider;
import de.zabuza.maglev.external.algorithms.ShortestPathComputation;
import de.zabuza.maglev.external.graph.Edge;
import de.zabuza.maglev.external.graph.Graph;
import de.zabuza.maglev.internal.algorithms.shortestpath.Dijkstra;

import java.util.*;

/**
 * Implementation of a landmark provider that greedily selects landmarks that are farthest away from each other.<br>
 * <br>
 * The resulting set of landmarks is thus distributed well along the graph. Distances are computed by using a {@link
 * ShortestPathComputation} on the whole graph for every landmark. Thus, depending on the graph size and the amount of
 * landmarks, the landmark selection might take a while.
 *
 * @param <N> Type of the nodes and landmarks
 * @param <E> Type of the edges
 * @param <G> Type of the graph
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class GreedyFarthestLandmarkProvider<N, E extends Edge<N>, G extends Graph<N, E>>
		implements LandmarkProvider<N> {
	/**
	 * Algorithm to use for computing distances between nodes.
	 */
	private final ShortestPathComputation<N, E> computation;
	/**
	 * The graph to operate on.
	 */
	private final G graph;
	/**
	 * The random number generator to use for selection of the first landmark.
	 */
	private final Random random;

	/**
	 * Creates a new landmark provider which generates landmarks on the given graph.
	 *
	 * @param graph The graph to select landmarks from
	 */
	public GreedyFarthestLandmarkProvider(final G graph) {
		this.graph = graph;
		random = new Random();
		computation = new Dijkstra<>(graph);
	}

	/**
	 * Greedily selects nodes from the graph as landmarks that are farthest away from each other.<br>
	 * <br>
	 * The resulting collection of landmarks is thus distributed well along the graph. Distances are computed by using a
	 * {@link ShortestPathComputation} on the whole graph for every landmark. Thus, depending on the graph size and the
	 * amount of landmarks, the landmark selection might take a while.
	 */
	@Override
	public Collection<N> getLandmarks(final int amount) {
		if (amount <= 0) {
			return Collections.emptyList();
		}

		int amountToUse = amount;
		if (amount > graph.size()) {
			amountToUse = graph.size();
		}

		final Collection<N> landmarks = new ArrayList<>(amountToUse);
		final Collection<N> nodes = graph.getNodes();

		// Choose the first landmark randomly
		final int index = random.nextInt(nodes.size());
		// If the nodes support RandomAccess, fetch it directly
		if (nodes instanceof RandomAccess && nodes instanceof List) {
			landmarks.add(((List<N>) nodes).get(index));
		} else {
			// Iterate to the node and skip previous values
			final Iterator<N> nodeIter = nodes.iterator();
			for (int i = 0; i < index; i++) {
				nodeIter.next();
			}
			landmarks.add(nodeIter.next());
		}

		// Iteratively select the node which is farthest away from the current
		// landmarks
		// Start by one since we already have the first landmark
		for (int i = 1; i < amountToUse; i++) {
			// Compute shortest path distances to all nodes
			final Map<N, ? extends HasPathCost> nodeToDistance = computation.shortestPathCostsReachable(landmarks);

			// Search the node with highest distance
			double highestDistance = -1;
			N farthestNode = null;
			for (final Map.Entry<N, ? extends HasPathCost> entry : nodeToDistance.entrySet()) {
				final double distance = entry.getValue()
						.getPathCost();
				if (distance > highestDistance) {
					// Node is farther, update
					highestDistance = distance;
					farthestNode = entry.getKey();
				}
			}
			// Add the farthest node
			landmarks.add(farthestNode);
		}

		return landmarks;
	}

}