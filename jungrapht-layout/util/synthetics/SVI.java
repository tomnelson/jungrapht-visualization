package org.jungrapht.visualization.layout.util.synthetics;

import java.util.Objects;

/**
 * Implementation of a delegate class for a generic type V
 *
 * @param <V> vertex type
 */
public class SVI<V> implements SV<V> {

  public final V vertex;

  protected SVI() {
    this.vertex = null;
  }

  protected SVI(V vertex) {
    this.vertex = vertex;
  }

  public SVI(SVI<V> other) {
    this(other.vertex);
  }

  @Override
  public V getVertex() {
    return vertex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SVI<?> se = (SVI<?>) o;
    if (o instanceof SyntheticSV) {
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
