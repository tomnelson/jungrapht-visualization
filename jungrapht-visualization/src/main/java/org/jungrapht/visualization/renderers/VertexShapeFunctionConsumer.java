package org.jungrapht.visualization.renderers;

import java.awt.Shape;
import java.util.function.Consumer;
import java.util.function.Function;

public interface VertexShapeFunctionConsumer<V> extends Consumer<Function<V, Shape>> {

  void setVertexShapeFunction(Function<V, Shape> vertexBoundsFunction);

  default void accept(Function<V, Shape> vertexShapeFunction) {
    setVertexShapeFunction(vertexShapeFunction);
  }
}
