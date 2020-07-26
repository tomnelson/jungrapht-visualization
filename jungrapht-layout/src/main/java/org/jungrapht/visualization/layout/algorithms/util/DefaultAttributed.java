package org.jungrapht.visualization.layout.algorithms.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DefaultAttributed<T> implements Attributed<T> {

  protected T value;

  public DefaultAttributed(T value) {
    this.value = value;
  }

  @Override
  public T getValue() {
    return value;
  }

  protected Map<String, String> delegate = new HashMap<>();

  @Override
  public Map<String, String> getAttributeMap() {
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
  public String get(Object key) {
    return delegate.get(key);
  }

  @Override
  public String put(String key, String value) {
    return delegate.put(key, value);
  }

  @Override
  public String remove(Object key) {
    return delegate.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> m) {
    delegate.putAll(m);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public Set<String> keySet() {
    return delegate.keySet();
  }

  @Override
  public Collection<String> values() {
    return delegate.values();
  }

  @Override
  public Set<Map.Entry<String, String>> entrySet() {
    return delegate.entrySet();
  }

  @Override
  public String getOrDefault(Object key, String defaultValue) {
    return delegate.getOrDefault(key, defaultValue);
  }

  @Override
  public void forEach(BiConsumer<? super String, ? super String> action) {
    delegate.forEach(action);
  }

  @Override
  public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
    delegate.replaceAll(function);
  }

  @Override
  public String putIfAbsent(String key, String value) {
    return delegate.putIfAbsent(key, value);
  }

  @Override
  public boolean remove(Object key, Object value) {
    return delegate.remove(key, value);
  }

  @Override
  public boolean replace(String key, String oldValue, String newValue) {
    return delegate.replace(key, oldValue, newValue);
  }

  @Override
  public String replace(String key, String value) {
    return delegate.replace(key, value);
  }

  @Override
  public String computeIfAbsent(
      String key, Function<? super String, ? extends String> mappingFunction) {
    return delegate.computeIfAbsent(key, mappingFunction);
  }

  @Override
  public String computeIfPresent(
      String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
    return delegate.computeIfPresent(key, remappingFunction);
  }

  @Override
  public String compute(
      String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
    return delegate.compute(key, remappingFunction);
  }

  @Override
  public String merge(
      String key,
      String value,
      BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
    return delegate.merge(key, value, remappingFunction);
  }

  public String toString() {
    return value + ":" + delegate.toString();
  }

  @Override
  public void set(String key, String value) {
    this.delegate.put(key, value);
  }
}
