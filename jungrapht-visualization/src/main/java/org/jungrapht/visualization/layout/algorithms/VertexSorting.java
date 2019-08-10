package org.jungrapht.visualization.layout.algorithms;

import java.util.Comparator;

public interface VertexSorting<V> {

  void setVertexComparator(Comparator<V> comparator);
}
