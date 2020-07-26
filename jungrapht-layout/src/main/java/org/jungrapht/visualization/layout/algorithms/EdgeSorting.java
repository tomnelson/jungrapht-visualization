package org.jungrapht.visualization.layout.algorithms;

import java.util.Comparator;

/**
 * an interface for {@code LayoutAlgorithm}s with a settable Comparator for edges
 *
 * @param <E>
 */
public interface EdgeSorting<E> {

  void setEdgeComparator(Comparator<E> comparator);
}
