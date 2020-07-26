package org.jungrapht.visualization.layout.algorithms;

import java.util.function.Predicate;

/**
 * an interface for {@code LayoutAlgorithm} with a settable {@link Predicate} to filter vertices
 *
 * @param <V>
 */
public interface VertexPredicated<V> {

  void setVertexPredicate(Predicate<V> vertexPredicate);
}
