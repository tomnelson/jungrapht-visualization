package org.jungrapht.visualization.layout.algorithms.util;

import java.util.function.Consumer;
import java.util.function.Function;
import org.jungrapht.visualization.layout.model.Rectangle;

public interface VertexBoundsFunctionConsumer<V> extends Consumer<Function<V, Rectangle>> {

  void setVertexBoundsFunction(Function<V, Rectangle> vertexBoundsFunction);

  default void accept(Function<V, Rectangle> vertexBoundsFunction) {
    setVertexBoundsFunction(vertexBoundsFunction);
  }
}
