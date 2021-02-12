package org.jungrapht.visualization.layout.algorithms;

import java.util.function.Predicate;

public interface NormalizesFavoredEdge<E> {

  void setFavoredEdgePredicate(Predicate<E> favoredEdgePredicate);
}
