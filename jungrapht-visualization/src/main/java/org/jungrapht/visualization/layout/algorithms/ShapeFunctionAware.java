package org.jungrapht.visualization.layout.algorithms;

import java.util.function.Function;
import org.jungrapht.visualization.layout.model.Rectangle;

/**
 * an interface for {@code LayoutAlgorithm} with a settable Shape Function for vertices
 *
 * @param <V> vertex type
 */
public interface ShapeFunctionAware<V> {

  void setVertexShapeFunction(Function<V, Rectangle> vertexShapeFunction);
}
