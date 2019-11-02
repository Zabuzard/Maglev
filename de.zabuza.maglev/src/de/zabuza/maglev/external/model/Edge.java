package de.zabuza.maglev.external.model;

/**
 * Interface for a weighted directed edge that connects two nodes.
 *
 * @param <N> Type of the node
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface Edge<N> {
	/**
	 * The cost of the edge, i.e. its weight.
	 *
	 * @return The cost of the edge, not negative
	 */
	double getCost();

	/**
	 * The destination node of the edge.
	 *
	 * @return The destination node of the edge, not null
	 */
	N getDestination();

	/**
	 * The source node of the edge.
	 *
	 * @return The source node of the edge, not null
	 */
	N getSource();
}