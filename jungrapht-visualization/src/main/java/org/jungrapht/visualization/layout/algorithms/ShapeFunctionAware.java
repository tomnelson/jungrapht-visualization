package org.jungrapht.visualization.layout.algorithms;

import java.awt.Shape;
import java.util.function.Function;

/**
 * an interface for {@code LayoutAlgorithm} with a settable Shape Function for vertices
 *
 * @param <V>
 */
public interface ShapeFunctionAware<V> {

  void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction);
}
