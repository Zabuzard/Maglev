package de.zabuza.maglev.external.graph.simple;

import de.zabuza.maglev.external.graph.Edge;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Basic implementation of an {@link Edge} that can be reversed implicitly in O(1).
 *
 * @param <N> Type of node
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class SimpleEdge<N> implements Edge<N>, ReversedConsumer {
	/**
	 * The cost of the edge, i.e. its weight.
	 */
	private final double cost;
	/**
	 * The destination node of the edge.
	 */
	private final N destination;
	/**
	 * The source node of the edge.
	 */
	private final N source;
	/**
	 * An object that provides a reversed flag or <tt>null</tt> if not present. Can be used to determine if the edge
	 * should be interpreted as reversed to implement implicit edge reversal at constant time.
	 */
	private ReversedProvider reversedProvider;

	/**
	 * Creates a new simple edge.
	 *
	 * @param source      The source node of the edge, not null
	 * @param destination The destination node of the edge, not null
	 * @param cost        The cost of the edge, i.e. its weight, not negative
	 */
	@SuppressWarnings("WeakerAccess")
	public SimpleEdge(final N source, final N destination, final double cost) {
		if (cost < 0) {
			throw new IllegalArgumentException("Cost must not be negative");
		}
		this.source = Objects.requireNonNull(source);
		this.destination = Objects.requireNonNull(destination);
		this.cost = cost;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", SimpleEdge.class.getSimpleName() + "[", "]").add(
				getSource() + " -(" + cost + ")-> " + getDestination())
				.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final SimpleEdge<?> simpleEdge = (SimpleEdge<?>) o;
		return Double.compare(simpleEdge.cost, cost) == 0 && destination.equals(simpleEdge.destination)
				&& source.equals(simpleEdge.source);
	}

	@Override
	public int hashCode() {
		return Objects.hash(cost, destination, source);
	}

	@Override
	public double getCost() {
		return cost;
	}

	@Override
	public N getDestination() {
		if (reversedProvider != null && reversedProvider.isReversed()) {
			return source;
		}
		return destination;
	}

	@Override
	public N getSource() {
		if (reversedProvider != null && reversedProvider.isReversed()) {
			return destination;
		}
		return source;
	}

	@Override
	public void setReversedProvider(final ReversedProvider provider) {
		reversedProvider = Objects.requireNonNull(provider);
	}
}