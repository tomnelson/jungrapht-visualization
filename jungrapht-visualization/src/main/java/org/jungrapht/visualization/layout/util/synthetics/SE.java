package org.jungrapht.visualization.layout.util.synthetics;

/**
 * A delegate class for a generic edge of type E
 *
 * @param <E> edge type
 */
public interface SE<E> {

  static <E> SE<E> of(E edge) {
    return new SEI(edge);
  }

  public E getEdge();
}
