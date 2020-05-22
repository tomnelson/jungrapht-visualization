package org.jungrapht.visualization.layout.algorithms.util;

import java.awt.Shape;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.util.Context;

public interface EdgeShapeFunctionSupplier<V, E> {

  void setEdgeShapeFunctionConsumer(
      Consumer<Function<Context<Graph<V, E>, E>, Shape>> edgeShapeConsumer);
}
