package io.github.zabuzard.maglev.internal.algorithms.metrics;

import io.github.zabuzard.maglev.external.algorithms.LandmarkProvider;
import io.github.zabuzard.maglev.external.graph.Edge;
import io.github.zabuzard.maglev.external.graph.Graph;

import java.util.*;

/**
 * Implementation of a landmark provider that selects landmarks randomly.<br>
 * <br>
 * If the nodes of the graph do not provide {@link RandomAccess}, the selection might need to iterate all nodes in order
 * to select the random nodes. In this case the landmark generation runs in {@code O(n)}, else only in the amount of
 * landmarks.
 *
 * @param <N> Type of the nodes and landmarks
 * @param <G> Type of the graph
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class RandomLandmarkProvider<N, G extends Graph<N, ? extends Edge<N>>> implements LandmarkProvider<N> {
	/**
	 * The graph to select landmarks from.
	 */
	private final G graph;
	/**
	 * The random number generator to use for generating the indices of the nodes to select as landmarks.
	 */
	private final Random random;

	/**
	 * Creates a new landmark provider that selects landmarks randomly.
	 *
	 * @param graph The graph to select landmarks from
	 */
	public RandomLandmarkProvider(final G graph) {
		this.graph = graph;
		random = new Random();
	}

	/**
	 * Selects landmarks randomly from the set of nodes.<br>
	 * <br>
	 * If the nodes of the graph do not provide {@link RandomAccess}, the selection might need to iterate all nodes in
	 * order to select the random nodes. In this case the landmark generation runs in {@code O(n)}, else only in the
	 * amount of landmarks.
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
		final int amountOfNodes = nodes.size();

		// Designate random indices
		final Collection<Integer> indicesSet = new HashSet<>(amountToUse);
		while (indicesSet.size() < amountToUse) {
			indicesSet.add(random.nextInt(amountOfNodes));
		}

		// If the nodes support RandomAccess, fetch them directly
		if (nodes instanceof RandomAccess && nodes instanceof List) {
			final List<N> nodesAsList = (List<N>) nodes;
			for (final int index : indicesSet) {
				landmarks.add(nodesAsList.get(index));
			}
			return landmarks;
		}

		// Sort the indices
		final int[] indices = indicesSet.stream()
				.mapToInt(Integer::intValue)
				.toArray();
		Arrays.sort(indices);

		// Iterate to each index and collect the node
		// This loop is optimized and faster than a straightforward approach because
		// it
		// has no conditional checks
		final Iterator<N> nodeIter = nodes.iterator();
		int indexBefore = 0;
		for (final int index : indices) {
			// Throw away the values
			for (int j = indexBefore; j < index; j++) {
				nodeIter.next();
			}

			// Collect the value, it is at the desired index
			landmarks.add(nodeIter.next());
			// Prepare the next round
			indexBefore = index + 1;
		}

		return landmarks;
	}

}