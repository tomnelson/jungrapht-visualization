package org.jungrapht.visualization.layout.algorithms;

import java.util.function.Predicate;

public interface VertexPredicated<V> {

  void setVertexPredicate(Predicate<V> vertexPredicate);
}
