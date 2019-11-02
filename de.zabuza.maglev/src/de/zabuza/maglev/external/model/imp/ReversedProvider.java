package de.zabuza.maglev.external.model.imp;

/**
 * Interface for objects that provide a reversed state flag.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@FunctionalInterface
public interface ReversedProvider {
	/**
	 * Whether or not the object is reversed.
	 *
	 * @return {@code True} if the object is reversed, {@code false} otherwise
	 */
	boolean isReversed();
}
