package de.zabuza.maglev.internal.collections;

import java.util.Objects;

/**
 * Object for generic triples which hold three objects of given types.
 *
 * @param <E1> Type of the first element
 * @param <E2> Type of the second element
 * @param <E3> Type of the third element
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
final class Triple<E1, E2, E3> {
	/**
	 * First element of the triple.
	 */
	private final E1 first;
	/**
	 * Second element of the triple.
	 */
	private final E2 second;
	/**
	 * Third element of the triple.
	 */
	private final E3 third;

	/**
	 * Creates a new triple holding the three given objects.
	 *
	 * @param first  First object of the triple
	 * @param second Second object of the triple
	 * @param third  Third object of the triple
	 */
	Triple(final E1 first, final E2 second, final E3 third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	/**
	 * Gets the first element of the triple.
	 *
	 * @return The first element of the triple
	 */
	public E1 getFirst() {
		return first;
	}

	/**
	 * Gets the second element of the triple.
	 *
	 * @return The second element of the triple
	 */
	public E2 getSecond() {
		return second;
	}

	/**
	 * Gets the third element of the triple.
	 *
	 * @return The third element of the triple
	 */
	public E3 getThird() {
		return third;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
		return Objects.equals(first, triple.first) && Objects.equals(second, triple.second) && Objects.equals(third,
				triple.third);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second, third);
	}

	@Override
	public String toString() {
		return "[" + first + ", " + second + ", " + third + "]";
	}
}
