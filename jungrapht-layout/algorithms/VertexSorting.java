package org.jungrapht.visualization.layout.algorithms;

import java.util.Comparator;

/**
 * an interface for {@code LayoutAlgorithm} with a settable Comparator for vertices
 *
 * @param <V>
 */
public interface VertexSorting<V> {

  void setVertexComparator(Comparator<V> comparator);
}
