package io.github.zabuzard.maglev.internal.collections;

import java.util.*;

/**
 * Nested hash map which uses two keys in a nested structure for storing values.
 *
 * @param <K1> Type of the first key
 * @param <K2> Type of the second key
 * @param <V>  Type of the value
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class NestedMap<K1, K2, V> {
	/**
	 * Initial capacity of a nested map, if not stated otherwise.
	 */
	private static final int INITIAL_CAPACITY = 50;
	/**
	 * Internal map which stores maps of second keys and values for the first keys.
	 */
	private final Map<K1, Map<K2, V>> k1ToK2ToV;
	/**
	 * The initial capacity to use when creating maps which connect the second key to the value or {@code -1} if a
	 * default value should be used.
	 */
	private int nestedInitialCapacity;

	/**
	 * Creates a new empty nested map.
	 */
	public NestedMap() {
		this(new HashMap<>(NestedMap.INITIAL_CAPACITY));
	}

	/**
	 * Creates a new empty nested map with an initial capacity.
	 *
	 * @param initialCapacity The initial capacity of the map
	 */
	public NestedMap(final int initialCapacity) {
		this(new HashMap<>(initialCapacity));
	}

	/**
	 * Creates a new nested map which uses the given map.
	 *
	 * @param nestedMap The nested map to use
	 */
	private NestedMap(final Map<K1, Map<K2, V>> nestedMap) {
		k1ToK2ToV = nestedMap;
		nestedInitialCapacity = -1;
	}

	/**
	 * Adds all entries from the given map to this map.
	 *
	 * @param nestedMap The map to add entries from
	 */
	public void addAll(final NestedMap<K1, K2, V> nestedMap) {
		for (final Triple<K1, K2, V> triple : nestedMap.entrySet()) {
			put(triple.getFirst(), triple.getSecond(), triple.getThird());
		}
	}

	/**
	 * Removes all of the mappings from this map. The map will be empty after this call returns.
	 */
	public void clear() {
		k1ToK2ToV.clear();
	}

	/**
	 * Whether the map contains an entry for the given keys.
	 *
	 * @param key1 The first key
	 * @param key2 The second key
	 *
	 * @return {@code True} if the map contains an entry for the keys, {@code false} otherwise
	 */
	public boolean contains(final K1 key1, final K2 key2) {
		final Map<K2, V> k2ToV = k1ToK2ToV.get(key1);
		if (k2ToV == null) {
			return false;
		}
		return k2ToV.containsKey(key2);
	}

	/**
	 * Returns an iterable object which contains all entries of this map. The result will be constructed on call.
	 *
	 * @return An iterable object which contains all entries of this map
	 */
	@SuppressWarnings("WeakerAccess")
	public Iterable<Triple<K1, K2, V>> entrySet() {
		final Collection<Triple<K1, K2, V>> result = new ArrayList<>();
		for (final Map.Entry<K1, Map<K2, V>> entryOuter : k1ToK2ToV.entrySet()) {
			for (final Map.Entry<K2, V> entryInner : entryOuter.getValue()
					.entrySet()) {
				result.add(new Triple<>(entryOuter.getKey(), entryInner.getKey(), entryInner.getValue()));
			}
		}
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof NestedMap)) {
			return false;
		}
		final NestedMap<?, ?, ?> other = (NestedMap<?, ?, ?>) obj;
		return Objects.equals(k1ToK2ToV, other.k1ToK2ToV);
	}

	/**
	 * Returns the map to which the specified first key is mapped, or {@code null} if this map contains no mapping for
	 * the first key.
	 *
	 * @param key1 The first key
	 *
	 * @return The map to which the specified first key is mapped, or {@code null} if this map contains no mapping for
	 * the first key.
	 */
	public Map<K2, V> get(final K1 key1) {
		return k1ToK2ToV.get(key1);
	}

	/**
	 * Returns the value to which the specified keys are mapped, or {@code null} if this map contains no mapping for the
	 * keys.
	 *
	 * @param key1 The first key
	 * @param key2 The second key
	 *
	 * @return The value to which the specified keys are mapped, or {@code null} if this map contains no mapping for the
	 * keys.
	 */
	public V get(final K1 key1, final K2 key2) {
		final Map<K2, V> k2toV = k1ToK2ToV.get(key1);
		if (k2toV == null) {
			//noinspection ReturnOfNull
			return null;
		}
		return k2toV.get(key2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((k1ToK2ToV == null) ? 0 : k1ToK2ToV.hashCode());
		return result;
	}

	/**
	 * Returns a set view of the first keys contained in this map.
	 *
	 * @return A set view of the first keys contained in this map.
	 */
	public Set<K1> keySet() {
		return k1ToK2ToV.keySet();
	}

	/**
	 * Associates the specified value with the two specified keys in this map.
	 *
	 * @param key1  First key
	 * @param key2  Second key
	 * @param value Value to associate
	 *
	 * @return The previous value associated with the two keys, or {@code null} if there was no mapping for the keys.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public V put(final K1 key1, final K2 key2, final V value) {
		return k1ToK2ToV.computeIfAbsent(key1, key -> buildMap())
				.put(key2, value);
	}

	/**
	 * Associates the specified value with the given map.
	 *
	 * @param key1        First key
	 * @param key2ToValue Map to associate
	 *
	 * @return The previous map associated with the given key, or {@code null} if there was no mapping for the key.
	 */
	public Map<K2, V> put(final K1 key1, final Map<K2, V> key2ToValue) {
		return k1ToK2ToV.put(key1, key2ToValue);
	}

	/**
	 * Removes the mapping for the first key from this map if it is present.
	 *
	 * @param k1 The first key
	 *
	 * @return The previous value associated with the first key, or {@code null} if there was no mapping for it.
	 */
	public Map<K2, V> remove(final K1 k1) {
		return k1ToK2ToV.remove(k1);
	}

	/**
	 * Removes the mapping for the two keys from this map if it is present.
	 *
	 * @param k1 The first key
	 * @param k2 The second key
	 *
	 * @return The previous value associated with the two keys, or {@code null} if there was no mapping for it.
	 */
	@SuppressWarnings("ReturnOfNull")
	public V remove(final K1 k1, final K2 k2) {
		final Map<K2, V> k2ToV = k1ToK2ToV.get(k1);
		if (k2ToV == null) {
			return null;
		}
		final V value = k2ToV.remove(k2);
		if (k2ToV.isEmpty()) {
			k1ToK2ToV.remove(k1);
		}
		return value;
	}

	/**
	 * The initial capacity to use when creating maps which connect the second key to the value or {@code -1} if a
	 * default value should be used.
	 *
	 * @param nestedInitialCapacity The initial capacity to use or {@code -1} for a default value
	 */
	public void setNestedInitialCapacity(final int nestedInitialCapacity) {
		this.nestedInitialCapacity = nestedInitialCapacity;
	}

	@Override
	public String toString() {
		return k1ToK2ToV.toString();
	}

	/**
	 * Builds a new map which connects the second key to values.
	 *
	 * @return The constructed map
	 */
	private Map<K2, V> buildMap() {
		if (nestedInitialCapacity == -1) {
			return new HashMap<>();
		}
		return new HashMap<>(nestedInitialCapacity);
	}
}