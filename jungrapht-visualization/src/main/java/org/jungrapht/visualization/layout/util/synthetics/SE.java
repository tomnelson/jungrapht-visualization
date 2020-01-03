package org.jungrapht.visualization.layout.util.synthetics;

import java.util.Objects;

/**
 * A delegate class for a generic edge of type E
 *
 * @param <E> edge type
 */
public class SE<E> {

  public final E edge;

  public static <E> SE<E> of(E edge) {
    return new SE(edge);
  }

  public SE(E edge) {
    this.edge = edge;
  }

  @Override
  public String toString() {
    return "SE{" + "edge=" + edge + "hash:" + hashCode() + "'}'";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SE<?> se = (SE<?>) o;
    return Objects.equals(edge, se.edge);
  }

  @Override
  public int hashCode() {
    return Objects.hash(edge);
  }
}
