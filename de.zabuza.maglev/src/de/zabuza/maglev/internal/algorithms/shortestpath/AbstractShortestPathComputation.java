package de.zabuza.maglev.internal.algorithms.shortestpath;

import de.zabuza.maglev.external.algorithms.shortestpath.HasPathCost;
import de.zabuza.maglev.external.algorithms.shortestpath.Path;
import de.zabuza.maglev.external.algorithms.shortestpath.ShortestPathComputation;
import de.zabuza.maglev.external.model.Edge;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract class for implementations of {@link ShortestPathComputation}.
 * Implements some of the overloaded methods by using the core variant of the
 * corresponding method.
 *
 * @param <N> Type of the node
 * @param <E> Type of the edge
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public abstract class AbstractShortestPathComputation<N, E extends Edge<N>> implements ShortestPathComputation<N, E> {

	@Override
	public Collection<N> computeSearchSpace(final N source, final N destination) {
		return computeSearchSpace(Collections.singletonList(source), destination);
	}

	@Override
	public Optional<Path<N, E>> computeShortestPath(final N source, final N destination) {
		return computeShortestPath(Collections.singletonList(source), destination);
	}

	@Override
	public Optional<Double> computeShortestPathCost(final N source, final N destination) {
		return computeShortestPathCost(Collections.singletonList(source), destination);
	}

	@Override
	public Map<N, ? extends HasPathCost> computeShortestPathCostsReachable(final N source) {
		return computeShortestPathCostsReachable(Collections.singletonList(source));
	}

}