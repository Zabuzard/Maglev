package de.zabuza.maglev.external.model;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Interface for a graph model. A graph consists of nodes and edges connecting
 * the nodes.
 *
 * @param <N> Type of the node
 * @param <E> Type of the edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface Graph<N, E extends Edge<N>> {
	/**
	 * Adds the given edge to the graph if not already contained.
	 *
	 * @param edge The edge to add, not null
	 *
	 * @return {@code True} if the edge was not already contained and thus added,
	 * {@code false} otherwise
	 */
	boolean addEdge(E edge);

	/**
	 * Adds the given node to the graph if not already contained.
	 *
	 * @param node The node to add, not null
	 *
	 * @return {@code True} if the node was not already contained and thus added,
	 * {@code false} otherwise
	 */
	boolean addNode(N node);

	/**
	 * Whether or not the given edge is contained in the graph.
	 *
	 * @param edge The edge in question, not null
	 *
	 * @return {@code True} if the edge is contained in the graph, {@code false}
	 * otherwise
	 */
	boolean containsEdge(E edge);

	/**
	 * Gets the amount of edges contained in the graph. This operation runs
	 * in {@code O(1)}.
	 *
	 * @return The amount of edges contained in the graph
	 */
	int getAmountOfEdges();

	/**
	 * A stream over all edges this graph contains. The construction of the stream
	 * runs in {@code O(1)}.
	 *
	 * @return A stream over all edges this graph contains
	 */
	Stream<E> getEdges();

	/**
	 * Gets a stream of all edges that have the given node as destination.
	 *
	 * @param destination The destination to get incoming edges for, not null
	 *
	 * @return A stream of all incoming edges
	 */
	Stream<E> getIncomingEdges(N destination);

	/**
	 * Gets a collection of all nodes that the graph contains.<br>
	 * <br>
	 * There are no guarantees made on if the collection is backed by the graph or
	 * not.
	 *
	 * @return A collection of all contained nodes
	 */
	Collection<N> getNodes();

	/**
	 * Gets a stream of all edges that have the given node as source.
	 *
	 * @param source The source to get outgoing edges for, not null
	 *
	 * @return A stream of all outgoing edges
	 */
	Stream<E> getOutgoingEdges(N source);

	/**
	 * Removes the given edge if it is contained in the graph.
	 *
	 * @param edge The edge to remove, not null
	 *
	 * @return {@code True} if the edge was contained and thus removed,
	 * {@code false} otherwise
	 */
	boolean removeEdge(E edge);

	/**
	 * Removes the given node if it is contained in the graph.
	 *
	 * @param node The node to remove, not null
	 *
	 * @return {@code True} if the node was contained and thus removed,
	 * {@code false} otherwise
	 */
	boolean removeNode(N node);

	/**
	 * Reverses the graph. That is, all directed edges switch source with
	 * destination.
	 * <p>
	 * There are no requirements made on the time complexity. It is up to the
	 * implementing class if this method runs fast or if it explicitly reverses
	 * each edge. Algorithms will use this method, so its complexity has a direct
	 * impact on algorithm performance.
	 * <p>
	 * Edges retrieved before the reversal must
	 * remain equal to the edges after the reversal, according to their
	 * {@code equals} method.
	 */
	void reverse();

	/**
	 * Gets the amount of nodes contained in the graph. This operation runs
	 * in {@code O(1)}.
	 *
	 * @return The amount of nodes contained in the graph
	 */
	int size();
}