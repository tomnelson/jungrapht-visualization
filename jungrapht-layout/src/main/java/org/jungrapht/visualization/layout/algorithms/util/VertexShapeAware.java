package org.jungrapht.visualization.layout.algorithms.util;

import java.util.function.Function;
import org.jungrapht.visualization.layout.model.Rectangle;

public interface VertexShapeAware<V> {

  void setVertexShapeFunction(Function<V, Rectangle> vertexShapeFunction);
}
