package io.github.zabuzard.maglev.internal.algorithms.metrics;

import io.github.zabuzard.maglev.external.algorithms.HasPathCost;
import io.github.zabuzard.maglev.external.algorithms.LandmarkProvider;
import io.github.zabuzard.maglev.external.algorithms.Metric;
import io.github.zabuzard.maglev.external.algorithms.ShortestPathComputation;
import io.github.zabuzard.maglev.external.graph.Edge;
import io.github.zabuzard.maglev.external.graph.Graph;
import io.github.zabuzard.maglev.internal.algorithms.shortestpath.Dijkstra;
import io.github.zabuzard.maglev.internal.collections.NestedMap;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Implements a metric for nodes by using landmarks.<br>
 * <br>
 * Given two objects it approximates the distance by comparing shortest paths from the objects to the landmarks. The
 * distance depends on the underlying distance model of the graph, i.e. the format used by the edge cost.
 *
 * @param <N> The type of the nodes and landmarks
 * @param <E> The type of the edges
 * @param <G> The type of the graph
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class LandmarkMetric<N, E extends Edge<N>, G extends Graph<N, E>> implements Metric<N> {
	/**
	 * Nested map that connects landmarks to all other nodes and the corresponding shortest path distance.
	 */
	private final NestedMap<N, N, Double> landmarkToNodeDistance;
	/**
	 * Nested map that connects all nodes to the landmarks and the corresponding shortest path distance.
	 */
	private final NestedMap<N, N, Double> nodeToLandmarkDistance;
	/**
	 * Landmarks to use for computing the metric.
	 */
	private Collection<N> landmarks;

	/**
	 * Creates a new landmark metric that uses the given amount of landmarks produced by the given provider.<br>
	 * <br>
	 * Given two objects it approximates the distance by comparing shortest paths from the objects to the landmarks. The
	 * distance depends on the underlying distance model of the graph, i.e. the format used by the edge cost.<br>
	 * <br>
	 * Due to the computation of landmarks and shortest paths, the creation of this metric might take a while.
	 *
	 * @param amount           The amount of landmarks to use
	 * @param graph            The graph to define the metric on
	 * @param landmarkProvider The provider to use for generation of the landmarks
	 */
	public LandmarkMetric(final int amount, final G graph, final LandmarkProvider<N> landmarkProvider) {
		// TODO Use a primitive collection to avoid the overhead of the wrapper
		landmarkToNodeDistance = new NestedMap<>(amount);
		nodeToLandmarkDistance = new NestedMap<>(graph.size());
		nodeToLandmarkDistance.setNestedInitialCapacity(amount);

		initialize(amount, graph, landmarkProvider, new Dijkstra<>(graph));
	}

	/**
	 * Approximates the distance between the given two nodes by comparing shortest paths from the nodes to the
	 * landmarks. The distance depends on the underlying distance model of the graph, i.e. the format used by the edge
	 * cost.
	 */
	@Override
	public double distance(final N first, final N second) {
		double greatestDistance = 0.0;
		for (final N landmark : landmarks) {
			// Ignore the landmark if no one can reach it
			if (!nodeToLandmarkDistance.contains(first, landmark) || !nodeToLandmarkDistance.contains(second, landmark)
					|| !landmarkToNodeDistance.contains(landmark, second) || !landmarkToNodeDistance.contains(landmark,
					first)) {
				continue;
			}

			final double firstToLandmark = Objects.requireNonNull(nodeToLandmarkDistance.get(first, landmark));
			final double secondToLandmark = Objects.requireNonNull(nodeToLandmarkDistance.get(second, landmark));
			final double landmarkToSecond = Objects.requireNonNull(landmarkToNodeDistance.get(landmark, second));
			final double landmarkToFirst = Objects.requireNonNull(landmarkToNodeDistance.get(landmark, first));

			final double landmarkBehindDestination = firstToLandmark - secondToLandmark;
			final double landmarkBeforeSource = landmarkToSecond - landmarkToFirst;
			final double distance = Math.max(landmarkBehindDestination, landmarkBeforeSource);
			if (distance > greatestDistance) {
				greatestDistance = distance;
			}
		}

		return greatestDistance;
	}

	/**
	 * Initializes this metric. It generates landmarks using the given provider and computes shortest path distances
	 * from the landmarks to all nodes and vice versa.<br>
	 * <br>
	 * Depending on the size of the graph and the amount of landmarks this method may take a while.
	 *
	 * @param amount           The amount of landmarks to generate
	 * @param graph            The graph to operate on
	 * @param landmarkProvider The provider to use to generate landmarks
	 * @param computation      The algorithm to use for computing shortest paths
	 */
	private void initialize(final int amount, final G graph, final LandmarkProvider<N> landmarkProvider,
			final ShortestPathComputation<N, E> computation) {
		landmarks = landmarkProvider.getLandmarks(amount);

		// Compute distances from landmarks to all other nodes
		for (final N landmark : landmarks) {
			final Map<N, ? extends HasPathCost> nodeToDistance = computation.shortestPathCostsReachable(landmark);
			landmarkToNodeDistance.setNestedInitialCapacity(nodeToDistance.size());
			for (final Map.Entry<N, ? extends HasPathCost> entry : nodeToDistance.entrySet()) {
				landmarkToNodeDistance.put(landmark, entry.getKey(), entry.getValue()
						.getPathCost());
			}
		}

		// Compute distances from all nodes to landmarks
		graph.reverse();
		for (final N landmark : landmarks) {
			final Map<N, ? extends HasPathCost> nodeToDistance = computation.shortestPathCostsReachable(landmark);
			for (final Map.Entry<N, ? extends HasPathCost> entry : nodeToDistance.entrySet()) {
				nodeToLandmarkDistance.put(entry.getKey(), landmark, entry.getValue()
						.getPathCost());
			}
		}
		graph.reverse();
	}
}