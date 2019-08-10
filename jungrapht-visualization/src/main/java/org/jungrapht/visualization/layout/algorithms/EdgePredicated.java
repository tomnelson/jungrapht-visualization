package org.jungrapht.visualization.layout.algorithms;

import java.util.function.Predicate;

public interface EdgePredicated<E> {

  void setEdgePredicate(Predicate<E> edgePredicate);
}
