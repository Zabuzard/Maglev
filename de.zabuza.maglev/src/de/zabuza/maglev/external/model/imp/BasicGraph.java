package de.zabuza.maglev.external.model.imp;

import de.zabuza.maglev.external.model.Edge;

import java.util.*;

/**
 * Basic implementation of a graph which operates on given nodes and
 * {@link BasicEdge}s. It is capable of implicitly
 * reversing nodes in constant time.
 *
 * @param <N> The type of nodes
 * @param <E> The type of edges
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class BasicGraph<N, E extends Edge<N> & ReversedConsumer> extends AbstractGraph<N, E>
		implements ReversedProvider {
	/**
	 * A set with all contained nodes.
	 */
	private final Set<N> nodes;
	/**
	 * A map that connects nodes to their incoming edges.
	 */
	private final Map<N, Set<E>> nodeToIncomingEdges;
	/**
	 * A map that connects nodes to their outgoing edges.
	 */
	private final Map<N, Set<E>> nodeToOutgoingEdges;
	/**
	 * Whether or not the graph is currently reversed.
	 */
	private boolean isReversed;

	/**
	 * Creates a new initially empty graph.
	 */
	public BasicGraph() {
		nodes = new HashSet<>();
		nodeToIncomingEdges = new HashMap<>();
		nodeToOutgoingEdges = new HashMap<>();
	}

	@Override
	public boolean addEdge(final E edge) {
		edge.setReversedProvider(this);
		return super.addEdge(edge);
	}

	@Override
	public boolean addNode(final N node) {
		return nodes.add(node);
	}

	@Override
	public boolean isReversed() {
		return isReversed;
	}

	/**
	 * Gets a collection of all nodes that the graph contains.<br>
	 * <br>
	 * The collection is backed by the graph, changes will be reflected in the
	 * graph. Do only change the collection directly if you know the consequences.
	 * Else the graph can easily get into a corrupted state. In many situations it
	 * is best to use the given methods like {@link #addNode(N)} instead.
	 */
	@Override
	public Collection<N> getNodes() {
		return nodes;
	}

	@Override
	public boolean removeNode(final N node) {
		return nodes.remove(node);
	}

	/**
	 * Reverses the graph. That is, all directed edges switch source with
	 * destination.<br>
	 * <br>
	 * The implementation runs in constant time, edge reversal is only made
	 * implicit.
	 */
	@Override
	public void reverse() {
		isReversed = !isReversed;
	}

	@Override
	protected Set<E> constructEdgeSetWith(final E edge) {
		return new HashSet<>(Collections.singletonList(edge));
	}

	@Override
	protected Map<N, Set<E>> getNodeToIncomingEdges() {
		if (isReversed) {
			return nodeToOutgoingEdges;
		}
		return nodeToIncomingEdges;
	}

	@Override
	protected Map<N, Set<E>> getNodeToOutgoingEdges() {
		if (isReversed) {
			return nodeToIncomingEdges;
		}
		return nodeToOutgoingEdges;
	}

}