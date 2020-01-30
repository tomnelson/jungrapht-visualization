package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.Comparator;

class Comparators {

  static <V, E> Comparator<LE<V, E>> sourceIndexComparator() {
    return Comparator.comparingInt(e -> e.getSource().getIndex());
  }

  static <V, E> Comparator<LE<V, E>> targetIndexComparator() {
    return Comparator.comparingInt(e -> e.getTarget().getIndex());
  }

  public static <V, E> Comparator<LE<V, E>> biLevelEdgeComparator() {
    return Comparator.<LE<V, E>>comparingInt(e -> e.getSource().getIndex())
        .thenComparingInt(e -> e.getTarget().getIndex());
  }
}
