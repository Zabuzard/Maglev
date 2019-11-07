package de.zabuza.maglev.internal.algorithms.shortestpath;

import de.zabuza.maglev.external.algorithms.HasPathCost;
import de.zabuza.maglev.external.algorithms.Path;
import de.zabuza.maglev.external.algorithms.PathTree;
import de.zabuza.maglev.external.algorithms.ShortestPathComputation;
import de.zabuza.maglev.external.graph.Edge;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract class for implementations of {@link ShortestPathComputation}. Implements some of the overloaded methods by
 * using the core variant of the corresponding method.
 *
 * @param <N> Type of the node
 * @param <E> Type of the edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@SuppressWarnings("DesignForExtension")
public abstract class AbstractShortestPathComputation<N, E extends Edge<N>> implements ShortestPathComputation<N, E> {

	@Override
	public Collection<N> searchSpace(final N source, final N destination) {
		return searchSpace(Collections.singletonList(source), destination);
	}

	@Override
	public Optional<Path<N, E>> shortestPath(final N source, final N destination) {
		return shortestPath(Collections.singletonList(source), destination);
	}

	@Override
	public Optional<Double> shortestPathCost(final N source, final N destination) {
		return shortestPathCost(Collections.singletonList(source), destination);
	}

	@Override
	public Map<N, ? extends HasPathCost> shortestPathCostsReachable(final N source) {
		return shortestPathCostsReachable(Collections.singletonList(source));
	}

	@Override
	public PathTree<N, E> shortestPathReachable(final N source) {
		return shortestPathReachable(Collections.singletonList(source));
	}

}