package org.jungrapht.visualization.layout.algorithms;

import java.util.function.Predicate;

/**
 * an interface for {@code LayoutAlgorithm} that can set a {@code Predicate} to filter edges
 *
 * @param <E>
 */
public interface EdgePredicated<E> {

  void setEdgePredicate(Predicate<E> edgePredicate);
}
