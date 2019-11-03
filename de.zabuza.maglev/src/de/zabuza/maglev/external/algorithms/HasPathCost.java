package de.zabuza.maglev.external.algorithms;

/**
 * Interface for classes that provide path costs.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@FunctionalInterface
public interface HasPathCost {
	/**
	 * Gets the path cost.
	 *
	 * @return The cost of the path, not negative
	 */
	double getPathCost();
}