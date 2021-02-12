package io.github.zabuzard.maglev.internal.algorithms.shortestpath;

import io.github.zabuzard.maglev.external.algorithms.Path;
import io.github.zabuzard.maglev.external.algorithms.PathTree;
import io.github.zabuzard.maglev.external.algorithms.TentativeDistance;
import io.github.zabuzard.maglev.external.graph.Edge;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class OnDemandPathTree<N, E extends Edge<N>> implements PathTree<N, E> {

	private final Collection<N> sources;
	private final Map<N, TentativeDistance<N, E>> nodeToDistance;

	public OnDemandPathTree(final Collection<N> sources, final Map<N, TentativeDistance<N, E>> nodeToDistance) {
		this.sources = Collections.unmodifiableCollection(sources);
		this.nodeToDistance = Collections.unmodifiableMap(nodeToDistance);
	}

	@Override
	public Collection<N> getSources() {
		return sources;
	}

	@Override
	public Stream<N> getReachableNodes() {
		return nodeToDistance.values()
				.stream()
				.map(TentativeDistance::getNode);
	}

	@Override
	public Optional<Path<N, E>> getPathTo(final N destination) {
		final TentativeDistance<N, E> destinationDistance = nodeToDistance.get(destination);

		// Destination is not reachable from the given sources
		if (destinationDistance == null) {
			return Optional.empty();
		}

		final E parentEdge = destinationDistance.getParentEdge();
		// Destination is already a source node
		if (parentEdge == null) {
			return Optional.of(new EmptyPath<>(destination));
		}

		// Build the path reversely by following the pointers from the destination
		// to one of the sources
		final EdgePath<N, E> path = new EdgePath<>(EdgePath.ConstructionDirection.BACKWARD);
		TentativeDistance<N, E> currentDistanceContainer = destinationDistance;
		E currentEdge = parentEdge;
		while (currentEdge != null) {
			// Add the edge
			final double distance = currentDistanceContainer.getTentativeDistance();
			final N parent = currentEdge.getSource();
			final TentativeDistance<N, E> parentDistanceContainer = nodeToDistance.get(parent);
			final double parentDistance = parentDistanceContainer.getTentativeDistance();

			path.addEdge(currentEdge, distance - parentDistance);

			// Prepare next round
			currentEdge = parentDistanceContainer.getParentEdge();
			currentDistanceContainer = parentDistanceContainer;
		}
		return Optional.of(path);
	}

	@Override
	public Collection<N> getLeaves() {
		final Set<N> reachableNodes = getReachableNodes().collect(Collectors.toSet());
		// Remove all nodes that appear as source of a parent-pointer
		nodeToDistance.values()
				.stream()
				.map(TentativeDistance::getParentEdge)
				.filter(Predicate.not(Objects::isNull))
				.map(E::getSource)
				.forEach(reachableNodes::remove);
		return reachableNodes;
	}
}
