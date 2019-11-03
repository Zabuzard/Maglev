package de.zabuza.maglev.external.graph.simple;

/**
 * Interface for consumer of {@link ReversedProvider}.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@SuppressWarnings("WeakerAccess")
@FunctionalInterface
public interface ReversedConsumer {
	/**
	 * Sets the {@link ReversedProvider} to be consumed.
	 *
	 * @param provider The provider to consume
	 */
	void setReversedProvider(ReversedProvider provider);
}