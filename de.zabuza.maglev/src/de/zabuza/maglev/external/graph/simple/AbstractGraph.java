package de.zabuza.maglev.external.graph.simple;

import de.zabuza.maglev.external.graph.Edge;
import de.zabuza.maglev.external.graph.Graph;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * Abstract implementation of the {@link Graph} model. Implements some utility methods and everything related to edges.
 * <p>
 * The core methods that deal with nodes, like {@link #addNode(Object)}, {@link #removeNode(Object)} and {@link
 * #getNodes()}, as well as {@link #reverse()} are not implemented.
 *
 * @param <N> The type of nodes
 * @param <E> The type of edges
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@SuppressWarnings("DesignForExtension")
public abstract class AbstractGraph<N, E extends Edge<N>> implements Graph<N, E> {
	/**
	 * The amount of edges in this graph.
	 */
	private int amountOfEdges;

	/**
	 * Creates a new, initially empty, graph.
	 */
	@SuppressWarnings("WeakerAccess")
	protected AbstractGraph() {
		amountOfEdges = 0;
	}

	@Override
	public boolean addEdge(final E edge) {
		boolean wasAdded;

		// Add edge to outgoing edges of its source
		Set<E> outgoingEdges = getNodeToOutgoingEdges().get(edge.getSource());
		if (outgoingEdges == null) {
			outgoingEdges = constructEdgeSetWith(edge);
			getNodeToOutgoingEdges().put(edge.getSource(), outgoingEdges);
			wasAdded = true;
		} else {
			wasAdded = outgoingEdges.add(edge);
		}

		// Add edge to incoming edges of its destination
		Set<E> incomingEdges = getNodeToIncomingEdges().get(edge.getDestination());
		if (incomingEdges == null) {
			incomingEdges = constructEdgeSetWith(edge);
			getNodeToIncomingEdges().put(edge.getDestination(), incomingEdges);
			wasAdded = true;
		} else {
			wasAdded |= incomingEdges.add(edge);
		}

		if (wasAdded) {
			amountOfEdges++;
		}
		return wasAdded;
	}

	@Override
	public boolean containsEdge(final E edge) {
		// We don't check the other direction, unit tests should cover this
		final Set<E> outgoingEdges = getNodeToOutgoingEdges().get(edge.getSource());
		return outgoingEdges != null && outgoingEdges.contains(edge);
	}

	@Override
	public int getAmountOfEdges() {
		return amountOfEdges;
	}

	@Override
	public Stream<E> getEdges() {
		return getNodeToOutgoingEdges().values()
				.stream()
				.flatMap(Collection::stream);
	}

	@Override
	public Stream<E> getIncomingEdges(final N destination) {
		final Set<E> edges = getNodeToIncomingEdges().get(destination);
		if (edges == null) {
			return Stream.empty();
		}
		return edges.stream();
	}

	@Override
	public Stream<E> getOutgoingEdges(final N source) {
		final Set<E> edges = getNodeToOutgoingEdges().get(source);
		if (edges == null) {
			return Stream.empty();
		}
		return edges.stream();
	}

	@Override
	public boolean removeEdge(final E edge) {
		boolean wasRemoved = removeEdgeFromMap(edge, edge.getSource(), getNodeToOutgoingEdges());
		wasRemoved |= removeEdgeFromMap(edge, edge.getDestination(), getNodeToIncomingEdges());
		if (wasRemoved) {
			amountOfEdges--;
		}
		return wasRemoved;
	}

	@Override
	public int size() {
		return getNodes().size();
	}

	@Override
	public String toString() {
		final StringJoiner sj = new StringJoiner(", ", getClass().getSimpleName() + "[", "]");
		sj.add("nodes=" + size());
		sj.add("edges=" + getAmountOfEdges());
		return sj.toString();
	}

	/**
	 * Constructs a set which is used to hold edges. The set must initially contain the given edge.
	 *
	 * @param edge The edge to add to the set
	 *
	 * @return The constructed set which contains the given edge
	 */
	protected abstract Set<E> constructEdgeSetWith(E edge);

	/**
	 * Gets a map that connects nodes to their incoming edges. The map is backed by the graph, changes will be reflected
	 * in the graph.<br>
	 * <br>
	 * Do only change the map directly if you know the consequences. Else the graph can easily get into a corrupted
	 * state. In many situations it is best to use the given methods like {@link #addEdge(Edge)} instead.
	 *
	 * @return A map connecting nodes to their incoming edges
	 */
	protected abstract Map<N, Set<E>> getNodeToIncomingEdges();

	/**
	 * Gets a map that connects nodes to their outgoing edges. The map is backed by the graph, changes will be reflected
	 * in the graph.<br>
	 * <br>
	 * Do only change the map directly if you know the consequences. Else the graph can easily get into a corrupted
	 * state. In many situations it is best to use the given methods like {@link #addEdge(Edge)} instead.
	 *
	 * @return A map connecting nodes to their outgoing edges
	 */
	protected abstract Map<N, Set<E>> getNodeToOutgoingEdges();

	/**
	 * Removes the given edge from the given map by using the given key.<br>
	 * <br>
	 * If the edge set is empty after removal, the key is removed from the map too.
	 *
	 * @param edge        The edge to remove
	 * @param keyNode     The key of the set where the edge is to be removed from
	 * @param nodeToEdges The map that connects nodes to a set of edges
	 *
	 * @return <tt>True</tt> if the edge was found and thus removed,
	 * <tt>false</tt> otherwise
	 */
	private boolean removeEdgeFromMap(final E edge, final N keyNode, final Map<N, ? extends Set<E>> nodeToEdges) {
		final Set<E> edges = nodeToEdges.get(keyNode);
		if (edges != null) {
			final boolean wasRemoved = edges.remove(edge);
			if (edges.isEmpty()) {
				nodeToEdges.remove(keyNode);
			}
			return wasRemoved;
		}
		return false;
	}
}