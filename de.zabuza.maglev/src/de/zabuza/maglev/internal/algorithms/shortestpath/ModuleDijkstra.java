package de.zabuza.maglev.internal.algorithms.shortestpath;

import de.zabuza.maglev.external.algorithms.DijkstraModule;
import de.zabuza.maglev.external.algorithms.TentativeDistance;
import de.zabuza.maglev.external.graph.Edge;
import de.zabuza.maglev.external.graph.Graph;

import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * A Dijkstra algorithm for shortest path computation that can be modified by using modules.<br>
 * <br>
 * Use {@link #addModule(DijkstraModule)} and {@link #removeModule(DijkstraModule)} to register and unregister modules.
 * Alternatively use the factory method {@link #of(Graph, DijkstraModule...)} for convenient instance creation.
 *
 * @param <N> Type of the nodes
 * @param <E> Type of the edges
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class ModuleDijkstra<N, E extends Edge<N>> extends Dijkstra<N, E> {
	/**
	 * Creates a new module Dijkstra instance routing on the given graph and using the given modules.
	 *
	 * @param <N>     Type of the nodes
	 * @param <E>     Type of the edges
	 * @param graph   The graph to route on
	 * @param modules The modules to use
	 *
	 * @return The created module Dijkstra instance
	 */
	@SafeVarargs
	public static <N, E extends Edge<N>> ModuleDijkstra<N, E> of(final Graph<N, E> graph,
			final DijkstraModule<N, E>... modules) {
		final ModuleDijkstra<N, E> moduleDijkstra = new ModuleDijkstra<>(graph);
		if (modules != null) {
			for (final DijkstraModule<N, E> module : modules) {
				moduleDijkstra.addModule(module);
			}
		}
		return moduleDijkstra;
	}

	/**
	 * The modules to use.
	 */
	private final Set<DijkstraModule<N, E>> modules;

	/**
	 * Creates a new module Dijkstra instance routing on the given graph.
	 *
	 * @param graph The graph to route on
	 */
	public ModuleDijkstra(final Graph<N, E> graph) {
		super(graph);
		modules = new HashSet<>();
	}

	/**
	 * Adds the given module.
	 *
	 * @param module The module to add
	 */
	public void addModule(final DijkstraModule<N, E> module) {
		modules.add(module);
	}

	/**
	 * Removes the given module.
	 *
	 * @param module The module to remove
	 */
	public void removeModule(final DijkstraModule<N, E> module) {
		modules.remove(module);
	}

	/**
	 * Whether or not the given edge should be considered for relaxation. The algorithm will ignore the edge and not
	 * follow it if this method returns {@code false}.<br>
	 * <br>
	 * This will be the case if any modules {@link DijkstraModule#doConsiderEdgeForRelaxation(Edge, Object,
	 * TentativeDistance)} method returns {@code false}.
	 */
	@Override
	protected boolean doConsiderEdgeForRelaxation(final E edge, final N pathDestination,
			final TentativeDistance<? extends N, E> tentativeDistance) {
		// Ignore the base, it always considers all edges
		// Ask all modules and accumulate with logical and
		for (final DijkstraModule<N, E> module : modules) {
			final boolean doNotConsider = !module.doConsiderEdgeForRelaxation(edge, pathDestination, tentativeDistance);
			if (doNotConsider) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Gets an estimate about the shortest path distance from the given node to the destination of the shortest path
	 * computation.<br>
	 * <br>
	 * Therefore, {@link DijkstraModule#getEstimatedDistance(Object, Object)} is called on all modules and the greatest
	 * estimate is chosen. If there is no module estimate the method falls back to the base implementation.
	 */
	@Override
	protected double getEstimatedDistance(final N node, final N pathDestination) {
		// Choose greatest estimate
		final OptionalDouble maxEstimate = modules.stream()
				.map(module -> module.getEstimatedDistance(node, pathDestination))
				.filter(OptionalDouble::isPresent)
				.mapToDouble(OptionalDouble::getAsDouble)
				.max();
		if (maxEstimate.isPresent()) {
			return maxEstimate.getAsDouble();
		}

		// Fallback to base implementation
		return super.getEstimatedDistance(node, pathDestination);
	}

	/**
	 * Provides the cost of a given edge.<br>
	 * <br>
	 * Therefore, {@link DijkstraModule#provideEdgeCost(Edge, double)} is called on all modules and the greatest cost is
	 * chosen. If no module provides a cost the method falls back to the base implementation.
	 */
	@Override
	protected double provideEdgeCost(final E edge, final double tentativeDistance) {
		// Choose greatest cost
		final OptionalDouble maxEdgeCost = modules.stream()
				.map(module -> module.provideEdgeCost(edge, tentativeDistance))
				.filter(OptionalDouble::isPresent)
				.mapToDouble(OptionalDouble::getAsDouble)
				.max();
		if (maxEdgeCost.isPresent()) {
			return maxEdgeCost.getAsDouble();
		}

		// Fallback to base implementation
		return super.provideEdgeCost(edge, tentativeDistance);
	}

	/**
	 * Whether or not the algorithm should abort computation of the shortest path. The method is called right before the
	 * given node will be settled.<br>
	 * <br>
	 * This will be the case if any modules {@link DijkstraModule#shouldAbortBefore(TentativeDistance)} method returns
	 * {@code true}.
	 *
	 * @param tentativeDistance The tentative distance wrapper of the node that will be settled next
	 *
	 * @return {@code True} if the computation should be aborted, {@code false} if not
	 */
	@Override
	protected boolean shouldAbortBefore(final TentativeDistance<N, E> tentativeDistance) {
		// Ignore the base, it never aborts computation
		// Ask all modules and accumulate with logical or
		for (final DijkstraModule<N, E> module : modules) {
			final boolean abort = module.shouldAbortBefore(tentativeDistance);
			if (abort) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Whether or not the algorithm should abort computation of the shortest path. The method is called right after the
	 * given node has been settled.<br>
	 * <br>
	 * This will be the case if any modules {@link DijkstraModule#shouldAbortAfter(TentativeDistance)} method returns
	 * {@code true}.
	 *
	 * @param tentativeDistance The tentative distance wrapper of the node that was settled
	 *
	 * @return {@code True} if the computation should be aborted, {@code false} if not
	 */
	@Override
	protected boolean shouldAbortAfter(final TentativeDistance<N, E> tentativeDistance) {
		// Ignore the base, it never aborts computation
		// Ask all modules and accumulate with logical or
		for (final DijkstraModule<N, E> module : modules) {
			final boolean abort = module.shouldAbortAfter(tentativeDistance);
			if (abort) {
				return true;
			}
		}

		return false;
	}

}