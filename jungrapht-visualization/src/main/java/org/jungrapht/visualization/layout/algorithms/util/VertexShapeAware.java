package org.jungrapht.visualization.layout.algorithms.util;

import java.awt.*;
import java.util.function.Function;

public interface VertexShapeAware<V> {

  void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction);
}
