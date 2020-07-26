package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Comparator;

public class Comparators {

  public static <V, E> Comparator<LE<V, E>> sourceIndexComparator() {
    return Comparator.comparingInt(e -> e.getSource().getIndex());
  }

  public static <V, E> Comparator<LE<V, E>> targetIndexComparator() {
    return Comparator.comparingInt(e -> e.getTarget().getIndex());
  }

  public static <V, E> Comparator<LE<V, E>> biLevelEdgeComparator() {
    return Comparator.<LE<V, E>>comparingInt(e -> e.getSource().getIndex())
        .thenComparingInt(e -> e.getTarget().getIndex());
  }

  public static <V, E> Comparator<LE<V, E>> biLevelEdgeComparatorReverse() {
    return Comparator.<LE<V, E>>comparingInt(e -> e.getTarget().getIndex())
        .thenComparingInt(e -> e.getSource().getIndex());
  }
}
