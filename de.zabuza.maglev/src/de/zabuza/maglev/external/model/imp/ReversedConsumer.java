package de.zabuza.maglev.external.model.imp;

/**
 * Interface for consumer of {@link ReversedProvider}.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@FunctionalInterface
public interface ReversedConsumer {
	/**
	 * Sets the {@link ReversedProvider} to be consumed.
	 *
	 * @param provider The provider to consume
	 */
	void setReversedProvider(ReversedProvider provider);
}