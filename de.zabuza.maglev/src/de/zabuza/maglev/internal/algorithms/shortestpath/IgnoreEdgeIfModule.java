package de.zabuza.maglev.internal.algorithms.shortestpath;

import de.zabuza.maglev.external.algorithms.DijkstraModule;
import de.zabuza.maglev.external.algorithms.TentativeDistance;
import de.zabuza.maglev.external.graph.Edge;

import java.util.function.Predicate;

/**
 * Module for a {@link ModuleDijkstra} that ignores exploring an edge if it matches against a given predicate.<br>
 * <br>
 * The factory method {@link #of(Predicate)} can be used for convenient instance creation.
 *
 * @param <N> Type of the nodes
 * @param <E> Type of the edges
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class IgnoreEdgeIfModule<N, E extends Edge<N>> implements DijkstraModule<N, E> {

	/**
	 * Creates an module which ignores exploring an edge if it matches against the given predicate.
	 *
	 * @param <N>       Type of the nodes
	 * @param <E>       Type of the edges
	 * @param predicate The predicate to test the edge against.
	 *
	 * @return The created module
	 */
	public static <N, E extends Edge<N>> IgnoreEdgeIfModule<N, E> of(final Predicate<? super E> predicate) {
		return new IgnoreEdgeIfModule<>(predicate);
	}

	/**
	 * The predicate to test the edge against.
	 */
	private final Predicate<? super E> considerEdgePredicate;

	/**
	 * Creates an module which ignores exploring an edge if it matches against the given predicate.
	 *
	 * @param predicate The predicate to test the edge against
	 */
	private IgnoreEdgeIfModule(final Predicate<? super E> predicate) {
		considerEdgePredicate = predicate.negate();
	}

	@Override
	public boolean doConsiderEdgeForRelaxation(final E edge, final N pathDestination,
			final TentativeDistance<? extends N, E> tentativeDistance) {
		return considerEdgePredicate.test(edge);
	}

}