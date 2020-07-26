package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.jungrapht.visualization.layout.util.synthetics.SE;

/**
 * Interface for an edge type used for the application of a layered graph layout algorithm<br>
 * Instances of LE&lt;V,E&gt; replace instances of E during layout
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public interface LE<V, E> extends SE<E> {

  static <V, E> LE<V, E> of(E edge, LV<V> source, LV<V> target) {
    return new LEI(edge, source, target);
  }

  LE<V, E> swapped();

  LV<V> getSource();

  LV<V> getTarget();
}
