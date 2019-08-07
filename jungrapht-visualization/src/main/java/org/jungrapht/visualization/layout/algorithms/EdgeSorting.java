package org.jungrapht.visualization.layout.algorithms;

import java.util.Comparator;

public interface EdgeSorting<E> {

  void setEdgeComparator(Comparator<E> comparator);
}
