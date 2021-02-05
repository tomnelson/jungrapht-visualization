package org.jungrapht.visualization.layout.algorithms;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;

public interface Layered<V, E> {

  void setLayering(Layering layering);

  void setMaxLevelCrossFunction(Function<Graph<V, E>, Integer> maxLevelCrossFunction);

  Comparator noopComparator = (v1, v2) -> 0;

  Predicate truePredicate = t -> true;

  Predicate falsePredicate = t -> false;
}
