package de.siphalor.amecs.impl.util;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Similar to Collections.EmptyMap
 *
 * @param <K> Key
 * @param <V> Value
 *
 * @serial include
 */
@Environment(EnvType.CLIENT)
public class NOPMap<K, V> implements Map<K, V>, Serializable {
	private static final long serialVersionUID = -5357463983776463053L;

	@SuppressWarnings("rawtypes")
	public static final Map NOP_MAP = new NOPMap<>();

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> nopMap() {
		return NOP_MAP;
	}

	private NOPMap() {

	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean containsKey(Object key) {
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public V get(Object key) {
		return null;
	}

	@Override
	public Set<K> keySet() {
		return Collections.emptySet();
	}

	@Override
	public Collection<V> values() {
		return Collections.emptySet();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return Collections.emptySet();
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Map) && ((Map<?, ?>) o).isEmpty();
	}

	@Override
	public int hashCode() {
		return 0;
	}

	// Override default methods in Map
	@Override
	public V getOrDefault(Object k, V defaultValue) {
		return defaultValue;
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		Objects.requireNonNull(action);
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		Objects.requireNonNull(function);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		// nop
		return null;
	}

	@Override
	public boolean remove(Object key, Object value) {
		// nop
		return false;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		// nop
		return false;
	}

	@Override
	public V replace(K key, V value) {
		// nop
		return null;
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		// nop
		return null;
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		// nop
		return null;
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		// nop
		return null;
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		// nop
		return null;
	}

	// Preserves singleton property
	private Object readResolve() {
		return NOP_MAP;
	}

	@Override
	public V put(K key, V value) {
		// nop
		return null;
	}

	@Override
	public V remove(Object key) {
		// nop
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		// nop
	}

	@Override
	public void clear() {
		// nop
	}
}
