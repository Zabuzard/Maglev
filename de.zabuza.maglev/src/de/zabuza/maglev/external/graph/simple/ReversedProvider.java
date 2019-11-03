package de.zabuza.maglev.external.graph.simple;

/**
 * Interface for objects that provide a reversed state flag.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@SuppressWarnings("WeakerAccess")
@FunctionalInterface
public interface ReversedProvider {
	/**
	 * Whether or not the object is reversed.
	 *
	 * @return {@code True} if the object is reversed, {@code false} otherwise
	 */
	boolean isReversed();
}
