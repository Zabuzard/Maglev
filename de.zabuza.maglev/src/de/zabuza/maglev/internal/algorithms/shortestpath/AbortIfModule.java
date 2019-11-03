package de.zabuza.maglev.internal.algorithms.shortestpath;

import de.zabuza.maglev.external.algorithms.DijkstraModule;
import de.zabuza.maglev.external.algorithms.TentativeDistance;
import de.zabuza.maglev.external.graph.Edge;

import java.util.function.Predicate;

/**
 * Module for a {@link ModuleDijkstra} that aborts computation of the shortest path after exploring to a node which
 * matches the given predicate.<br>
 * <br>
 * The factory method {@link #of(Predicate)} can be used for convenient instance creation.
 *
 * @param <N> Type of the nodes
 * @param <E> Type of the edges
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public class AbortIfModule<N, E extends Edge<N>> implements DijkstraModule<N, E> {

	/**
	 * Creates an module which aborts computation after exploring to a node which matches the given predicate.
	 *
	 * @param <N>       Type of the nodes
	 * @param <E>       Type of the edges
	 * @param predicate The predicate to test the node against.
	 *
	 * @return The created module
	 */
	public static <N, E extends Edge<N>> AbortIfModule<N, E> of(
			final Predicate<? super TentativeDistance<N, E>> predicate) {
		return new AbortIfModule<>(predicate);
	}

	/**
	 * The predicate to test the node against.
	 */
	private final Predicate<? super TentativeDistance<N, E>> predicate;

	/**
	 * Creates an module which aborts computation after exploring to a node which matches the given predicate.
	 *
	 * @param predicate The predicate to test the node against
	 */
	AbortIfModule(final Predicate<? super TentativeDistance<N, E>> predicate) {
		this.predicate = predicate;
	}

	@Override
	public final boolean shouldAbort(final TentativeDistance<N, E> tentativeDistance) {
		return predicate.test(tentativeDistance);
	}

}