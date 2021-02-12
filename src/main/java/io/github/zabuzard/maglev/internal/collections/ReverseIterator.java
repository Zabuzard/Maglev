package io.github.zabuzard.maglev.internal.collections;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Iterator which iterates a given list reversely.
 *
 * @param <E> Type of the element
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class ReverseIterator<E> implements Iterator<E> {
	/**
	 * The list iterator of the list to iterate over.
	 */
	private final ListIterator<E> listIterator;

	/**
	 * Creates a new reverse iterator which is able to reversely iterate the given list.
	 *
	 * @param list The list to iterate over
	 */
	public ReverseIterator(final List<E> list) {
		listIterator = list.listIterator(list.size());
	}

	@Override
	public boolean hasNext() {
		return listIterator.hasPrevious();
	}

	@Override
	public E next() {
		return listIterator.previous();
	}

}