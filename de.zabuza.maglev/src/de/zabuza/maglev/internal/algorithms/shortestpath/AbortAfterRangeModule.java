package de.zabuza.maglev.internal.algorithms.shortestpath;

import de.zabuza.maglev.external.graph.Edge;

/**
 * Module for a {@link ModuleDijkstra} that aborts computation of the shortest path after exploring to a given
 * range.<br>
 * <br>
 * The factory method {@link #of(double)} can be used for convenient instance creation.
 *
 * @param <N> Type of the nodes
 * @param <E> Type of the edges
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class AbortAfterRangeModule<N, E extends Edge<N>> extends AbortBeforeIfModule<N, E> {

	/**
	 * Creates an module which aborts computation after exploring to the given range.
	 *
	 * @param <N>   Type of the nodes
	 * @param <E>   Type of the edges
	 * @param range The range after which to abort, in travel time measured in {@code seconds}
	 *
	 * @return The created module
	 */
	public static <N, E extends Edge<N>> AbortAfterRangeModule<N, E> of(final double range) {
		return new AbortAfterRangeModule<>(range);
	}

	/**
	 * Creates an module which aborts computation after exploring to the given range.
	 *
	 * @param range The range after which to abort, in travel time measured in
	 *              <tt>seconds</tt>
	 */
	private AbortAfterRangeModule(final double range) {
		super(tentativeDistance -> tentativeDistance.getTentativeDistance() > range);
	}

}