package org.jungrapht.visualization.layout.util.synthetics;

import java.util.Objects;

/**
 * A delegate class for a generic type V
 *
 * @param <V> vertex type
 */
public class SV<V> {

  public final V vertex;

  public static <V> SV<V> of(V vertex) {
    return new SV(vertex);
  }

  protected SV() {
    this.vertex = null;
  }

  protected SV(V vertex) {
    this.vertex = vertex;
  }

  public SV(SV<V> other) {
    this(other.vertex);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SV<?> se = (SV<?>) o;
    if (o instanceof SyntheticVertex) {
      return false;
    }
    return Objects.equals(vertex, se.vertex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vertex);
  }

  @Override
  public String toString() {
    return "SV{" + "vertex=" + vertex + '}';
  }
}
