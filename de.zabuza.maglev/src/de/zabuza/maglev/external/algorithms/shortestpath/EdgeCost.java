package de.zabuza.maglev.external.algorithms.shortestpath;

import de.zabuza.maglev.external.model.Edge;

import java.util.Objects;

/**
 * POJO class that wraps an edge and cost. Can be used to group a different cost
 * than the default cost provided by the edge with the edge.
 *
 * @param <N> The type of the node
 * @param <E> The type of the edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class EdgeCost<N, E extends Edge<N>> {
	/**
	 * The cost of this element.
	 */
	private final double cost;
	/**
	 * The edge of this element.
	 */
	private final E edge;

	/**
	 * Creates a new edge cost element.
	 *
	 * @param edge The edge of this element, not null
	 * @param cost The cost of this element, not negative
	 */
	public EdgeCost(final E edge, final double cost) {
		if (cost < 0) {
			throw new IllegalArgumentException("Cost must not be negative");
		}
		this.edge = Objects.requireNonNull(edge);
		this.cost = cost;
	}

	/**
	 * Gets the cost.
	 *
	 * @return The cost to get, not negative
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Gets the edge.
	 *
	 * @return The edge to get, not null
	 */
	public E getEdge() {
		return edge;
	}
}