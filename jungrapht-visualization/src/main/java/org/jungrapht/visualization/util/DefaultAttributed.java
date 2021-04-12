package org.jungrapht.visualization.util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DefaultAttributed<T> implements Attributed<T> {

  protected T value;

  protected String html;

  protected Map<String, String> delegate = new HashMap<>();

  public DefaultAttributed(T value) {
    this.value = value;
  }

  @Override
  public T getValue() {
    return value;
  }

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
    this.html = null;
    return delegate.get(key);
  }

  @Override
  public String put(String key, String value) {
    this.html = null;
    return delegate.put(key, value);
  }

  @Override
  public String remove(Object key) {
    this.html = null;
    return delegate.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> m) {
    this.html = null;
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
    this.html = null;
    delegate.replaceAll(function);
  }

  @Override
  public String putIfAbsent(String key, String value) {
    this.html = null;
    return delegate.putIfAbsent(key, value);
  }

  @Override
  public boolean remove(Object key, Object value) {
    this.html = null;
    return delegate.remove(key, value);
  }

  @Override
  public boolean replace(String key, String oldValue, String newValue) {
    this.html = null;
    return delegate.replace(key, oldValue, newValue);
  }

  @Override
  public String replace(String key, String value) {
    this.html = null;
    return delegate.replace(key, value);
  }

  @Override
  public String computeIfAbsent(
      String key, Function<? super String, ? extends String> mappingFunction) {
    this.html = null;
    return delegate.computeIfAbsent(key, mappingFunction);
  }

  @Override
  public String computeIfPresent(
      String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
    this.html = null;
    return delegate.computeIfPresent(key, remappingFunction);
  }

  @Override
  public String compute(
      String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
    this.html = null;
    return delegate.compute(key, remappingFunction);
  }

  @Override
  public String merge(
      String key,
      String value,
      BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
    this.html = null;
    return delegate.merge(key, value, remappingFunction);
  }

  public String toString() {
    return "value:" + value + "," + delegate.toString();
  }

  public String toHtml() {
    if (html == null) {
      html = "<html>" + "value:" + value + "<br>";
      delegate
          .entrySet()
          .forEach(
              entry -> {
                String value = entry.getValue();
                if (value.length() > 20) {
                  value = value.substring(0, 20) + "...";
                }
                html += entry.getKey() + ":" + value + "<br>";
              });
    }
    return html;
  }

  @Override
  public void set(String key, String value) {
    this.delegate.put(key, value);
    this.html = null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultAttributed<?> that = (DefaultAttributed<?>) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
