package org.jungrapht.visualization.subLayout;

import java.util.Objects;

/**
 * container for a type that cam be made Collapsable. Used for vertex collapse to make a
 *
 * @code {Graph<Collapsable<V>, E>} instead of a {@code Graph<V,E>}
 * @param <T> the type that may be collapsed
 */
public class Collapsable<T> {

  private T item;

  public static <T> Collapsable<T> of(T item) {
    return new Collapsable(item);
  }

  private Collapsable(T item) {
    this.item = item;
  }

  public T get() {
    return item;
  }

  public String toString() {
    return Objects.toString(this.item, "null");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Collapsable<?> that = (Collapsable<?>) o;
    return Objects.equals(item, that.item);
  }

  @Override
  public int hashCode() {
    return Objects.hash(item);
  }
}
