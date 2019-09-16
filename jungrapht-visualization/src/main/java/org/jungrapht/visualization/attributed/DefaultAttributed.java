package org.jungrapht.visualization.attributed;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DefaultAttributed<K, V> implements Attributed<K, V> {

  protected Map<K, V> delegate = new HashMap<>();

  @Override
  public Map<K, V> getAttributeMap() {
    return delegate;
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return delegate.get(key);
  }

  @Override
  public V put(K key, V value) {
    return delegate.put(key, value);
  }

  @Override
  public V remove(Object key) {
    return delegate.remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    delegate.putAll(m);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public Set<K> keySet() {
    return delegate.keySet();
  }

  @Override
  public Collection<V> values() {
    return delegate.values();
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return delegate.entrySet();
  }

  @Override
  public V getOrDefault(Object key, V defaultValue) {
    return delegate.getOrDefault(key, defaultValue);
  }

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    delegate.forEach(action);
  }

  @Override
  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    delegate.replaceAll(function);
  }

  @Override
  public V putIfAbsent(K key, V value) {
    return delegate.putIfAbsent(key, value);
  }

  @Override
  public boolean remove(Object key, Object value) {
    return delegate.remove(key, value);
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    return delegate.replace(key, oldValue, newValue);
  }

  @Override
  public V replace(K key, V value) {
    return delegate.replace(key, value);
  }

  @Override
  public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    return delegate.computeIfAbsent(key, mappingFunction);
  }

  @Override
  public V computeIfPresent(
      K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    return delegate.computeIfPresent(key, remappingFunction);
  }

  @Override
  public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    return delegate.compute(key, remappingFunction);
  }

  @Override
  public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    return delegate.merge(key, value, remappingFunction);
  }

  public static <K1, V1> Map<K1, V1> of() {
    return Map.of();
  }

  public static <K1, V1> Map<K1, V1> of(K1 k1, V1 v1) {
    return Map.of(k1, v1);
  }

  public static <K1, V1> Map<K1, V1> of(K1 k1, V1 v1, K1 k2, V1 v2) {
    return Map.of(k1, v1, k2, v2);
  }

  public static <K1, V1> Map<K1, V1> of(K1 k1, V1 v1, K1 k2, V1 v2, K1 k3, V1 v3) {
    return Map.of(k1, v1, k2, v2, k3, v3);
  }

  public static <K1, V1> Map<K1, V1> of(K1 k1, V1 v1, K1 k2, V1 v2, K1 k3, V1 v3, K1 k4, V1 v4) {
    return Map.of(k1, v1, k2, v2, k3, v3, k4, v4);
  }

  public static <K1, V1> Map<K1, V1> of(
      K1 k1, V1 v1, K1 k2, V1 v2, K1 k3, V1 v3, K1 k4, V1 v4, K1 k5, V1 v5) {
    return Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
  }

  public static <K1, V1> Map<K1, V1> of(
      K1 k1, V1 v1, K1 k2, V1 v2, K1 k3, V1 v3, K1 k4, V1 v4, K1 k5, V1 v5, K1 k6, V1 v6) {
    return Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
  }

  public static <K1, V1> Map<K1, V1> of(
      K1 k1,
      V1 v1,
      K1 k2,
      V1 v2,
      K1 k3,
      V1 v3,
      K1 k4,
      V1 v4,
      K1 k5,
      V1 v5,
      K1 k6,
      V1 v6,
      K1 k7,
      V1 v7) {
    return Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
  }

  public static <K1, V1> Map<K1, V1> of(
      K1 k1,
      V1 v1,
      K1 k2,
      V1 v2,
      K1 k3,
      V1 v3,
      K1 k4,
      V1 v4,
      K1 k5,
      V1 v5,
      K1 k6,
      V1 v6,
      K1 k7,
      V1 v7,
      K1 k8,
      V1 v8) {
    return Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
  }

  public static <K1, V1> Map<K1, V1> of(
      K1 k1,
      V1 v1,
      K1 k2,
      V1 v2,
      K1 k3,
      V1 v3,
      K1 k4,
      V1 v4,
      K1 k5,
      V1 v5,
      K1 k6,
      V1 v6,
      K1 k7,
      V1 v7,
      K1 k8,
      V1 v8,
      K1 k9,
      V1 v9) {
    return Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
  }

  public static <K1, V1> Map<K1, V1> of(
      K1 k1,
      V1 v1,
      K1 k2,
      V1 v2,
      K1 k3,
      V1 v3,
      K1 k4,
      V1 v4,
      K1 k5,
      V1 v5,
      K1 k6,
      V1 v6,
      K1 k7,
      V1 v7,
      K1 k8,
      V1 v8,
      K1 k9,
      V1 v9,
      K1 k10,
      V1 v10) {
    return Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
  }

  @SafeVarargs
  public static <K1, V1> Map<K1, V1> ofEntries(Map.Entry<? extends K1, ? extends V1>... entries) {
    return Map.ofEntries(entries);
  }

  public static <K1, V1> Map.Entry<K1, V1> entry(K1 k1, V1 v1) {
    return Map.entry(k1, v1);
  }

  public static <K1, V1> Map<K1, V1> copyOf(Map<? extends K1, ? extends V1> map) {
    return Map.copyOf(map);
  }
}
