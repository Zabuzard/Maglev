package io.github.zabuzard.maglev.external.algorithms;

import io.github.zabuzard.maglev.external.graph.Edge;

import java.util.StringJoiner;

/**
 * Tentative distance container for a given node. The container consists of a node, the parent edge that lead to this
 * node, a tentative distance and optionally an estimate for the remaining distance.<br>
 * <br>
 * The parent edge can be used for backtracking, for example to construct a shortest path. The tentative distance is the
 * distance from a source to that node, i.e. the sum of the edge costs when backtracking the parent edges to the source.
 * The estimated distance is from this node to the desired destination, if present.<br>
 * <br>
 * The natural ordering of this container sums up the tentative distance and the distance estimate and then compares
 * ascending, i.e. smaller values first.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @param <N> Type of the node
 * @param <E> Type of the edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class TentativeDistance<N, E extends Edge<N>> implements HasPathCost, Comparable<TentativeDistance<N, E>> {
	/**
	 * The estimated distance from this node to the desired destination or
	 * <tt>0.0</tt> if not present.
	 */
	private final double estimatedDistance;
	/**
	 * The node this container wraps around.
	 */
	private final N node;

	/**
	 * The parent edge that lead to this node.
	 */
	private final E parentEdge;
	/**
	 * The tentative distance from a source to this node.
	 */
	private final double tentativeDistance;

	/**
	 * Creates a new tentative distance container for the given node. This constructor sets the estimated distance to
	 * {@code 0.0} which should be used whenever there is no desired destination.
	 *
	 * @param node              The node to wrap around
	 * @param parentEdge        The parent edge that lead to this node
	 * @param tentativeDistance The tentative distance from a source to this node, i.e. the sum of the edge costs when
	 *                          backtracking the parent edges to the source
	 */
	public TentativeDistance(final N node, final E parentEdge, final double tentativeDistance) {
		this(node, parentEdge, tentativeDistance, 0.0);
	}

	/**
	 * Creates a new tentative distance container for the given node.
	 *
	 * @param node              The node to wrap around
	 * @param parentEdge        The parent edge that lead to this node
	 * @param tentativeDistance The tentative distance from a source to this node, i.e. the sum of the edge costs when
	 *                          backtracking the parent edges to the source
	 * @param estimatedDistance An estimate about the distance from this node to the desired destination, the guess must
	 *                          be
	 *                          <i>monotone</i> and <i>admissible</i>
	 */
	public TentativeDistance(final N node, final E parentEdge, final double tentativeDistance,
			final double estimatedDistance) {
		this.node = node;
		this.parentEdge = parentEdge;
		this.tentativeDistance = tentativeDistance;
		this.estimatedDistance = estimatedDistance;
	}

	/**
	 * The natural ordering of this container sums up the tentative distance and the distance estimate and then compares
	 * ascending, i.e. smaller values first.
	 */
	@Override
	public int compareTo(final TentativeDistance<N, E> other) {
		return Double.compare(tentativeDistance + estimatedDistance, other.tentativeDistance + other.estimatedDistance);
	}

	/**
	 * Gets an estimate about the distance from this node to the desired destination, the guess is <i>monotone</i> and
	 * <i>admissible</i>.
	 *
	 * @return The estimated distance
	 */
	public double getEstimatedDistance() {
		return estimatedDistance;
	}

	/**
	 * Gets the node this container wraps around.
	 *
	 * @return The node to get
	 */
	public N getNode() {
		return node;
	}

	/**
	 * Gets the parent edge that lead to this node. Can be used for backtracking, for example for shortest path
	 * construction.
	 *
	 * @return The parent edge that lead to this node
	 */
	public E getParentEdge() {
		return parentEdge;
	}

	/**
	 * Gets the tentative distance from a source to this node, i.e. the sum of the edge costs when backtracking the
	 * parent edges to the source.
	 *
	 * @return The tentative distance
	 */
	@SuppressWarnings("SuspiciousGetterSetter")
	@Override
	public double getPathCost() {
		return tentativeDistance;
	}

	/**
	 * Gets the tentative distance from a source to this node, i.e. the sum of the edge costs when backtracking the
	 * parent edges to the source.
	 *
	 * @return The tentative distance
	 */
	public double getTentativeDistance() {
		return tentativeDistance;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", TentativeDistance.class.getSimpleName() + "[", "]").add("node=" + node)
				.add("parentEdge=" + parentEdge)
				.add("tentativeDistance=" + tentativeDistance)
				.add("estimatedDistance=" + estimatedDistance)
				.toString();
	}
}