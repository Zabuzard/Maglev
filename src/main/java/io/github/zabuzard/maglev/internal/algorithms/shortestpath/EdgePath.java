package io.github.zabuzard.maglev.internal.algorithms.shortestpath;

import io.github.zabuzard.maglev.external.algorithms.EdgeCost;
import io.github.zabuzard.maglev.external.algorithms.Path;
import io.github.zabuzard.maglev.external.graph.Edge;
import io.github.zabuzard.maglev.internal.collections.ReverseIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringJoiner;

/**
 * Implementation of a {@link Path} which connects edges.<br>
 * <br>
 * Does not support empty paths, i.e. paths without any edges, use {@link EmptyPath} instead. It can be build reversely
 * without additional overhead when iterating.
 *
 * @param <N> Type of the node
 * @param <E> Type of the edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class EdgePath<N, E extends Edge<N>> implements Path<N, E> {
	/**
	 * Direction the path is built in.
	 */
	private final ConstructionDirection constructionDirection;
	/**
	 * The edges this path consists of.
	 */
	private final ArrayList<EdgeCost<N, E>> edges;
	/**
	 * The total cost of the path, i.e. the sum of all edges cost.
	 */
	private double totalCost;

	/**
	 * Creates a new initially empty edge path that is not build reversely.
	 */
	@SuppressWarnings("WeakerAccess")
	EdgePath() {
		this(ConstructionDirection.FORWARD);
	}

	/**
	 * Creates a new initially empty edge path that can be build reversely
	 *
	 * @param constructionDirection Direction to construct the path in. If {@link ConstructionDirection#BACKWARD}, calls
	 *                              to {@link #addEdge(Edge, double)} are interpreted to begin from the end of the path.
	 *                              So the destination of the first added edge is the destination of the path and the
	 *                              source of the last added edge is the source of the path.
	 */
	EdgePath(final ConstructionDirection constructionDirection) {
		this.constructionDirection = constructionDirection;
		edges = new ArrayList<>();
	}

	@Override
	public String toString() {
		final StringJoiner pathJoiner = new StringJoiner(", ", "[", "]");
		for (final EdgeCost<N, E> edgeCost : this) {
			final Edge<N> edge = edgeCost.getEdge();
			pathJoiner.add(edge.getSource() + " -(" + edgeCost.getCost() + ")-> " + edge.getDestination());
		}

		return new StringJoiner(", ", EdgePath.class.getSimpleName() + "[", "]").add("totalCost=" + totalCost)
				.add("path=" + pathJoiner)
				.toString();
	}

	/**
	 * Adds the given edge to this path.<br>
	 * <br>
	 * If the path is to be build reversely the edges are interpreted to start from the end of the path. So the
	 * destination of the first added edge is the destination of the path and the source of the last added edge is the
	 * source of the path.
	 *
	 * @param edge The edge to add
	 * @param cost The cost of the edge
	 */
	public void addEdge(final E edge, final double cost) {
		edges.add(new EdgeCost<>(edge, cost));
		totalCost += cost;
	}

	@Override
	public N getDestination() {
		final int destinationIndex;
		switch (constructionDirection) {
			case BACKWARD:
				// Destination is the first entry
				destinationIndex = 0;
				break;
			case FORWARD:
				// Destination is the last entry
				destinationIndex = edges.size() - 1;
				break;
			default:
				throw new AssertionError();
		}
		return edges.get(destinationIndex)
				.getEdge()
				.getDestination();
	}

	@Override
	public N getSource() {
		final int sourceIndex;
		switch (constructionDirection) {
			case BACKWARD:
				// Source is the last entry
				sourceIndex = edges.size() - 1;
				break;
			case FORWARD:
				// Source is the first entry
				sourceIndex = 0;
				break;
			default:
				throw new AssertionError();
		}
		return edges.get(sourceIndex)
				.getEdge()
				.getSource();
	}

	@Override
	public double getTotalCost() {
		return totalCost;
	}

	@Override
	public Iterator<EdgeCost<N, E>> iterator() {
		if (constructionDirection == ConstructionDirection.BACKWARD) {
			return new ReverseIterator<>(edges);
		}
		return edges.iterator();
	}

	@Override
	public int length() {
		return edges.size();
	}

	@Override
	public Iterator<EdgeCost<N, E>> reverseIterator() {
		if (constructionDirection == ConstructionDirection.BACKWARD) {
			return edges.iterator();
		}
		return new ReverseIterator<>(edges);
	}

	/**
	 * Direction to construct an edge in.
	 */
	enum ConstructionDirection {
		/**
		 * Edge is constructed forwards.
		 */
		FORWARD,
		/**
		 * Edge is constructed backwards.
		 */
		BACKWARD
	}
}