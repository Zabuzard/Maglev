package de.zabuza.maglev.internal.algorithms.shortestpath;

import de.zabuza.maglev.external.algorithms.EdgeCost;
import de.zabuza.maglev.external.algorithms.Path;
import de.zabuza.maglev.external.graph.Edge;

import java.util.Collections;
import java.util.Iterator;

/**
 * Implementation of {@link Path} which represent an empty path. That is a path with no edges and only one node.
 *
 * @param <N> Type of the node
 * @param <E> Type of the edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class EmptyPath<N, E extends Edge<N>> implements Path<N, E> {
	/**
	 * The node this path consists of.
	 */
	private final N node;

	/**
	 * Creates a new empty path which consists only of the given node.
	 *
	 * @param node The node to add
	 */
	public EmptyPath(final N node) {
		this.node = node;
	}

	@SuppressWarnings("SuspiciousGetterSetter")
	@Override
	public N getDestination() {
		return node;
	}

	@SuppressWarnings("SuspiciousGetterSetter")
	@Override
	public N getSource() {
		return node;
	}

	@Override
	public double getTotalCost() {
		return 0.0;
	}

	@Override
	public Iterator<EdgeCost<N, E>> iterator() {
		return Collections.emptyListIterator();
	}

	@Override
	public int length() {
		return 0;
	}

	@Override
	public Iterator<EdgeCost<N, E>> reverseIterator() {
		return Collections.emptyListIterator();
	}
}