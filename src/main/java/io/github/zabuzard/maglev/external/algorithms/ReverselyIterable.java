package io.github.zabuzard.maglev.external.algorithms;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Extension of {@link Iterable} for classes offering {@link java.util.Iterator}s that iterate the given data source
 * reversely, in comparison to the order defined by the iterator returned by the {@link Iterable} implementation.
 *
 * @param <T> The type of the elements contained in the data source
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface ReverselyIterable<T> extends Iterable<T> {
	/**
	 * Returns an iterator over elements of type {@code T} that iterates the data source reversely.
	 *
	 * @return A reverse iterator
	 */
	Iterator<T> reverseIterator();

	/**
	 * Performs the given action for each element of the {@code ReverselyIterable} until all elements have been
	 * processed or the action throws an exception.  Actions are performed in the order of iteration, if that order is
	 * specified. Exceptions thrown by the action are relayed to the caller.
	 * <p>
	 * The behavior of this method is unspecified if the action performs side-effects that modify the underlying source
	 * of elements, unless an overriding class has specified a concurrent modification policy.
	 *
	 * @param action The action to be performed for each element
	 *
	 * @throws NullPointerException if the specified action is null
	 */
	default void forEachReversed(final Consumer<? super T> action) {
		Objects.requireNonNull(action);
		final Iterator<T> reverseIter = reverseIterator();
		while (reverseIter.hasNext()) {
			action.accept(reverseIter.next());
		}
	}

	/**
	 * Creates a {@link Spliterator} over the elements described by this {@code ReverselyIterable}.
	 *
	 * @return a {@code Spliterator} over the elements described by this {@code ReverselyIterable}.
	 */
	default Spliterator<T> spliteratorReversed() {
		return Spliterators.spliteratorUnknownSize(reverseIterator(), 0);
	}
}
