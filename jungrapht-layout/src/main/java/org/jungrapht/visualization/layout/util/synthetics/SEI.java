package org.jungrapht.visualization.layout.util.synthetics;

import java.util.Objects;

/**
 * Implementation of a delegate class for a generic edge of type E
 *
 * @param <E> edge type
 */
public class SEI<E> implements SE<E> {

  public final E edge;

  public SEI(E edge) {
    this.edge = edge;
  }

  @Override
  public E getEdge() {
    return edge;
  }

  @Override
  public String toString() {
    return "SE{" + "edge=" + edge + "hash:" + hashCode() + "'}'";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SEI<?> se = (SEI<?>) o;
    return Objects.equals(edge, se.edge);
  }

  @Override
  public int hashCode() {
    return Objects.hash(edge);
  }
}
